/*
 * Copyright (c) 2014-2026 Hard-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.hardclient.discord;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * A minimal implementation of Discord's local RPC protocol.
 *
 * <p>
 * Discord exposes this protocol through a named pipe on Windows and through a
 * Unix domain socket everywhere else, which means that no external library is
 * needed to talk to it.
 *
 * <p>
 * Frames consist of a 32-bit little-endian opcode, a 32-bit little-endian
 * payload length, and a UTF-8 JSON payload.
 */
final class DiscordIpc implements Closeable
{
	private static final int OP_HANDSHAKE = 0;
	private static final int OP_FRAME = 1;
	private static final int OP_CLOSE = 2;
	
	private static final int MAX_IPC_INDEX = 9;
	private static final int MAX_PAYLOAD_LENGTH = 1 << 20;
	
	private static final boolean WINDOWS =
		System.getProperty("os.name").toLowerCase().contains("win");
	
	private final Transport transport;
	private boolean closed;
	
	private DiscordIpc(Transport transport)
	{
		this.transport = transport;
	}
	
	/**
	 * Connects to the first Discord client that is found and performs the
	 * handshake for the given application ID.
	 */
	static DiscordIpc connect(String appId) throws IOException
	{
		IOException lastError = null;
		
		for(String address : getAddresses())
		{
			Transport transport;
			try
			{
				transport = open(address);
				
			}catch(IOException e)
			{
				lastError = e;
				continue;
			}
			
			DiscordIpc ipc = new DiscordIpc(transport);
			try
			{
				ipc.handshake(appId);
				return ipc;
				
			}catch(IOException e)
			{
				// The pipe exists but Discord refused the connection, so
				// there is no point in trying the other sockets.
				ipc.close();
				throw e;
			}
		}
		
		throw lastError != null ? lastError
			: new IOException("Discord is not running.");
	}
	
	private static List<String> getAddresses()
	{
		List<String> addresses = new ArrayList<>();
		
		if(WINDOWS)
		{
			for(int i = 0; i <= MAX_IPC_INDEX; i++)
				addresses.add("\\\\.\\pipe\\discord-ipc-" + i);
			
			return addresses;
		}
		
		for(String dir : getSocketDirectories())
			for(int i = 0; i <= MAX_IPC_INDEX; i++)
			{
				addresses.add(Path.of(dir, "discord-ipc-" + i).toString());
				
				// Flatpak and Snap use their own runtime directories.
				addresses.add(Path.of(dir, "app", "com.discordapp.Discord",
					"discord-ipc-" + i).toString());
				addresses.add(Path.of(dir, "snap.discord", "discord-ipc-" + i)
					.toString());
			}
		
		return addresses;
	}
	
	private static List<String> getSocketDirectories()
	{
		List<String> dirs = new ArrayList<>();
		
		for(String name : new String[]{"XDG_RUNTIME_DIR", "TMPDIR", "TMP",
			"TEMP"})
		{
			String value = System.getenv(name);
			if(value != null && !value.isEmpty() && !dirs.contains(value))
				dirs.add(value);
		}
		
		dirs.add("/tmp");
		return dirs;
	}
	
	private static Transport open(String address) throws IOException
	{
		if(WINDOWS)
			return new PipeTransport(new RandomAccessFile(address, "rw"));
		
		SocketChannel channel = SocketChannel.open(StandardProtocolFamily.UNIX);
		try
		{
			channel.connect(UnixDomainSocketAddress.of(address));
			return new SocketTransport(channel);
			
		}catch(IOException e)
		{
			channel.close();
			throw e;
		}
	}
	
	private void handshake(String appId) throws IOException
	{
		JsonObject payload = new JsonObject();
		payload.addProperty("v", 1);
		payload.addProperty("client_id", appId);
		
		write(OP_HANDSHAKE, payload);
		
		for(int i = 0; i < 5; i++)
		{
			JsonObject frame = read();
			String event = getString(frame, "evt");
			
			if("READY".equals(event))
				return;
			
			if("ERROR".equals(event))
				throw new IOException(frame.has("data")
					? frame.get("data").toString() : frame.toString());
		}
		
		throw new IOException("Discord did not answer the handshake.");
	}
	
	void setActivity(JsonObject activity) throws IOException
	{
		JsonObject args = new JsonObject();
		args.addProperty("pid", ProcessHandle.current().pid());
		args.add("activity", activity);
		
		JsonObject payload = new JsonObject();
		payload.addProperty("cmd", "SET_ACTIVITY");
		payload.add("args", args);
		payload.addProperty("nonce", UUID.randomUUID().toString());
		
		write(OP_FRAME, payload);
	}
	
	private void write(int opcode, JsonObject payload) throws IOException
	{
		byte[] json = payload.toString().getBytes(StandardCharsets.UTF_8);
		
		ByteBuffer frame =
			ByteBuffer.allocate(8 + json.length).order(ByteOrder.LITTLE_ENDIAN);
		frame.putInt(opcode);
		frame.putInt(json.length);
		frame.put(json);
		
		transport.write(frame.array());
	}
	
	private JsonObject read() throws IOException
	{
		ByteBuffer header =
			ByteBuffer.wrap(new byte[8]).order(ByteOrder.LITTLE_ENDIAN);
		transport.readFully(header.array());
		
		int opcode = header.getInt(0);
		int length = header.getInt(4);
		
		if(length < 0 || length > MAX_PAYLOAD_LENGTH)
			throw new IOException("Invalid frame length: " + length);
		
		byte[] payload = new byte[length];
		transport.readFully(payload);
		
		if(opcode == OP_CLOSE)
			throw new IOException("Discord closed the connection: "
				+ new String(payload, StandardCharsets.UTF_8));
		
		JsonElement json =
			JsonParser.parseString(new String(payload, StandardCharsets.UTF_8));
		
		return json.isJsonObject() ? json.getAsJsonObject() : new JsonObject();
	}
	
	private static String getString(JsonObject json, String key)
	{
		JsonElement element = json.get(key);
		return element == null || element.isJsonNull() ? null
			: element.getAsString();
	}
	
	@Override
	public void close()
	{
		if(closed)
			return;
		
		closed = true;
		transport.close();
	}
	
	private interface Transport
	{
		void write(byte[] data) throws IOException;
		
		void readFully(byte[] buffer) throws IOException;
		
		void close();
	}
	
	private static final class PipeTransport implements Transport
	{
		private final RandomAccessFile pipe;
		
		PipeTransport(RandomAccessFile pipe)
		{
			this.pipe = pipe;
		}
		
		@Override
		public void write(byte[] data) throws IOException
		{
			pipe.write(data);
		}
		
		@Override
		public void readFully(byte[] buffer) throws IOException
		{
			pipe.readFully(buffer);
		}
		
		@Override
		public void close()
		{
			try
			{
				pipe.close();
				
			}catch(IOException e)
			{
				// nothing we can do about it
			}
		}
	}
	
	private static final class SocketTransport implements Transport
	{
		private final SocketChannel channel;
		
		SocketTransport(SocketChannel channel)
		{
			this.channel = channel;
		}
		
		@Override
		public void write(byte[] data) throws IOException
		{
			ByteBuffer buffer = ByteBuffer.wrap(data);
			
			while(buffer.hasRemaining())
				if(channel.write(buffer) < 0)
					throw new IOException("Discord closed the connection.");
		}
		
		@Override
		public void readFully(byte[] buffer) throws IOException
		{
			ByteBuffer bb = ByteBuffer.wrap(buffer);
			
			while(bb.hasRemaining())
				if(channel.read(bb) < 0)
					throw new EOFException("Discord closed the connection.");
		}
		
		@Override
		public void close()
		{
			try
			{
				channel.close();
				
			}catch(IOException e)
			{
				// nothing we can do about it
			}
		}
	}
}
