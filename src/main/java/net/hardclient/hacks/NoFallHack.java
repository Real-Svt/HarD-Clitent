/*
 * Copyright (c) 2014-2026 Hard-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.hardclient.hacks;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.OnGroundOnly;
import net.hardclient.Category;
import net.hardclient.SearchTags;
import net.hardclient.events.UpdateListener;
import net.hardclient.hack.Hack;
import net.hardclient.settings.CheckboxSetting;

@SearchTags({"no fall"})
public final class NoFallHack extends Hack implements UpdateListener
{
	private final CheckboxSetting allowElytra = new CheckboxSetting(
		"Allow elytra", "description.hard.setting.nofall.allow_elytra", false);
	
	public NoFallHack()
	{
		super("NoFall");
		setCategory(Category.MOVEMENT);
		addSetting(allowElytra);
	}
	
	@Override
	public String getRenderName()
	{
		ClientPlayerEntity player = MC.player;
		if(player == null)
			return getName();
		
		if(player.isFallFlying() && !allowElytra.isChecked())
			return getName() + " (paused)";
		
		if(player.isCreative())
			return getName() + " (paused)";
		
		return getName();
	}
	
	@Override
	protected void onEnable()
	{
		HARD.getHax().antiHungerHack.setEnabled(false);
		EVENTS.add(UpdateListener.class, this);
	}
	
	@Override
	protected void onDisable()
	{
		EVENTS.remove(UpdateListener.class, this);
	}
	
	@Override
	public void onUpdate()
	{
		// do nothing in creative mode, since there is no fall damage anyway
		ClientPlayerEntity player = MC.player;
		if(player.isCreative())
			return;
		
		// pause when flying with elytra, unless allowed
		boolean fallFlying = player.isFallFlying();
		if(fallFlying && !allowElytra.isChecked())
			return;
		
		// attempt to fix elytra weirdness, if allowed
		if(fallFlying && player.isSneaking()
			&& !isFallingFastEnoughToCauseDamage(player))
			return;
		
		// send packet to stop fall damage
		player.networkHandler.sendPacket(new OnGroundOnly(true));
	}
	
	private boolean isFallingFastEnoughToCauseDamage(ClientPlayerEntity player)
	{
		return player.getVelocity().y < -0.5;
	}
}
