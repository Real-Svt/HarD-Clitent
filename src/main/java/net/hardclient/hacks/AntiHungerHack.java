/*
 * Copyright (c) 2014-2026 Hard-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.hardclient.hacks;

import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.hardclient.Category;
import net.hardclient.SearchTags;
import net.hardclient.events.PacketOutputListener;
import net.hardclient.hack.DontSaveState;
import net.hardclient.hack.Hack;
import net.hardclient.util.PacketUtils;

@DontSaveState
@SearchTags({"anti hunger"})
public final class AntiHungerHack extends Hack implements PacketOutputListener
{
	public AntiHungerHack()
	{
		super("AntiHunger");
		setCategory(Category.MOVEMENT);
	}
	
	@Override
	protected void onEnable()
	{
		HARD.getHax().noFallHack.setEnabled(false);
		EVENTS.add(PacketOutputListener.class, this);
	}
	
	@Override
	protected void onDisable()
	{
		EVENTS.remove(PacketOutputListener.class, this);
	}
	
	@Override
	public void onSentPacket(PacketOutputEvent event)
	{
		if(!(event.getPacket() instanceof PlayerMoveC2SPacket packet))
			return;
		
		if(!MC.player.isOnGround() || MC.player.fallDistance > 0.5)
			return;
		
		if(MC.interactionManager.isBreakingBlock())
			return;
		
		event.setPacket(PacketUtils.modifyOnGround(packet, false));
	}
}
