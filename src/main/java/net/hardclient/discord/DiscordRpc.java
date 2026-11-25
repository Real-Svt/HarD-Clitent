/*
 * Copyright (c) 2014-2026 Hard-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.hardclient.discord;

import java.io.IOException;

import com.google.gson.JsonObject;

/**
 * Keeps a Discord Rich Presence connection alive in the background.
 *
 * <p>
 * The Discord application ID is fixed in {@link #APP_ID}, so users cannot
 * change it. They can only enable or disable the feature.
 */
public final class DiscordRpc
{
	public static final DiscordRpc INSTANCE = new DiscordRpc();
	
	/**
	 * The Discord application ID that is used for the rich presence. This is
	 * fixed on purpose: it belongs to Hard Client's own Discord application
	 * and users are not supposed to change it.
	 */
	public static final String APP_ID = "1574471617818665030";
	
	/**
	 * Name of the rich presence asset that is displayed next to the status.
	 * Upload an image with this name to the Discord application to show a
	 * logo.
	 */
	private static final String LARGE_IMAGE = "hard";
	private static final String LARGE_TEXT = "Hard Client";
	
	private static final long TICK_INTERVAL_MS = 1000L;
	private static final long RETRY_INTERVAL_MS = 10000L;
	
	private static final int MAX_TEXT_LENGTH = 128;
	
	private final Object lock = new Object();
	
	private JsonObject requested;
	private boolean enabled;
	
	private Thread thread;
	private volatile DiscordIpc ipc;
	private volatile JsonObject sent;
	private volatile long nextConnectTime;
	private volatile boolean loggedError;
	
	private DiscordRpc()
	{
		
	}
	
	/**
	 * @return true if the given string looks like a Discord application ID.
	 */
	public static boolean isValidAppId(String appId)
	{
		if(appId == null || appId.length() < 15 || appId.length() > 25)
			return false;
		
		for(int i = 0; i < appId.length(); i++)
			if(!Character.isDigit(appId.charAt(i)))
				return false;
			
		return true;
	}
	
	/**
	 * Publishes the given status to Discord. Called repeatedly (once per
	 * second) so that the presence follows what the game is doing.
	 */
	public void update(String details, String state, long startTimestamp)
	{
		if(!isValidAppId(APP_ID))
			return;
		
		JsonObject activity = createActivity(details, state, startTimestamp);
		
		synchronized(lock)
		{
			requested = activity;
			enabled = true;
			
			if(thread == null)
			{
				thread = new Thread(this::runLoop, "Hard-Discord-RPC");
				thread.setDaemon(true);
				thread.start();
			}
		}
	}
	
	/**
	 * Removes the presence and stops trying to connect until the next call to
	 * {@link #update}.
	 */
	public void stop()
	{
		Thread thread;
		
		synchronized(lock)
		{
			enabled = false;
			requested = null;
			thread = this.thread;
			this.thread = null;
			closeConnection();
		}
		
		if(thread != null)
			thread.interrupt();
	}
	
	private void runLoop()
	{
		while(true)
		{
			JsonObject activity;
			
			synchronized(lock)
			{
				if(!enabled)
					return;
				
				activity = requested;
			}
			
			try
			{
				Thread.sleep(TICK_INTERVAL_MS);
				
				if(activity == null)
					continue;
				
				if(ipc == null)
				{
					if(System.currentTimeMillis() < nextConnectTime)
						continue;
					
					ipc = DiscordIpc.connect(APP_ID);
					sent = null;
					loggedError = false;
				}
				
				if(!activity.equals(sent))
				{
					ipc.setActivity(activity);
					sent = activity;
				}
				
			}catch(IOException e)
			{
				if(!loggedError)
				{
					loggedError = true;
					System.out.println("Discord RPC: " + e.getMessage());
				}
				
				closeConnection();
				nextConnectTime =
					System.currentTimeMillis() + RETRY_INTERVAL_MS;
				
			}catch(InterruptedException e)
			{
				Thread.currentThread().interrupt();
				return;
			}
		}
	}
	
	private void closeConnection()
	{
		DiscordIpc ipc = this.ipc;
		this.ipc = null;
		sent = null;
		
		if(ipc != null)
			ipc.close();
	}
	
	private static JsonObject createActivity(String details, String state,
		long startTimestamp)
	{
		JsonObject activity = new JsonObject();
		putText(activity, "details", details);
		putText(activity, "state", state);
		
		if(startTimestamp > 0)
		{
			JsonObject timestamps = new JsonObject();
			timestamps.addProperty("start", startTimestamp);
			activity.add("timestamps", timestamps);
		}
		
		JsonObject assets = new JsonObject();
		assets.addProperty("large_image", LARGE_IMAGE);
		assets.addProperty("large_text", LARGE_TEXT);
		activity.add("assets", assets);
		
		return activity;
	}
	
	private static void putText(JsonObject json, String key, String value)
	{
		if(value == null || value.isEmpty())
			return;
		
		json.addProperty(key, value.length() > MAX_TEXT_LENGTH
			? value.substring(0, MAX_TEXT_LENGTH) : value);
	}
}
