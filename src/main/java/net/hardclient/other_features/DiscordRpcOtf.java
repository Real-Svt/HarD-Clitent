/*
 * Copyright (c) 2014-2026 Hard-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.hardclient.other_features;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.hardclient.DontBlock;
import net.hardclient.HardClient;
import net.hardclient.SearchTags;
import net.hardclient.discord.DiscordRpc;
import net.hardclient.other_feature.OtherFeature;
import net.hardclient.settings.CheckboxSetting;

@DontBlock
@SearchTags({"discord", "discord rpc", "rich presence", "richpresence",
	"status", "game activity"})
public final class DiscordRpcOtf extends OtherFeature
{
	private static final int UPDATE_INTERVAL_TICKS = 20;
	private static final long START_TIME = System.currentTimeMillis();
	
	private final CheckboxSetting enabled = new CheckboxSetting("Enabled",
		"Shows what you are doing in Hard Client" + " as your Discord status.",
		true);
	
	private final CheckboxSetting showServer = new CheckboxSetting(
		"Show Server",
		"Shows the address of the server that"
			+ " you are playing on. Disable this if you don't want others to"
			+ " know where you play.",
		true);
	
	private final CheckboxSetting showVersion = new CheckboxSetting(
		"Show Version",
		"Shows the Minecraft version next to the Hard Client version.", true);
	
	private int ticks;
	
	public DiscordRpcOtf()
	{
		super("DiscordRPC",
			"Shows what you are doing in Hard Client as your Discord rich"
				+ " presence, for example which server you are playing on.\n\n"
				+ "Uses Hard Client's own Discord application, so there is"
				+ " nothing to configure. You can only enable or disable it.");
		addSetting(enabled);
		addSetting(showServer);
		addSetting(showVersion);
		
		ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
	}
	
	private void onTick(MinecraftClient client)
	{
		if(!enabled.isChecked())
		{
			DiscordRpc.INSTANCE.stop();
			return;
		}
		
		if(++ticks < UPDATE_INTERVAL_TICKS)
			return;
		
		ticks = 0;
		DiscordRpc.INSTANCE.update(getDetails(client), getState(), START_TIME);
	}
	
	private String getDetails(MinecraftClient client)
	{
		if(client.world == null)
			return "In the main menu";
		
		if(client.isInSingleplayer())
			return "Playing Singleplayer";
		
		ServerInfo server = client.getCurrentServerEntry();
		if(showServer.isChecked() && server != null
			&& !server.address.isEmpty())
			return "Playing on " + server.address;
		
		return "Playing on a server";
	}
	
	private String getState()
	{
		String state = "Hard Client " + HardClient.VERSION;
		
		if(showVersion.isChecked())
			state += " (MC " + HardClient.MC_VERSION + ")";
		
		return state;
	}
	
	@Override
	public boolean isEnabled()
	{
		return enabled.isChecked();
	}
	
	@Override
	public String getPrimaryAction()
	{
		return isEnabled() ? "Disable" : "Enable";
	}
	
	@Override
	public void doPrimaryAction()
	{
		enabled.setChecked(!enabled.isChecked());
	}
}
