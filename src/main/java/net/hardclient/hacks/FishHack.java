/*
 * Copyright (c) 2014-2026 Hard-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.hardclient.hacks;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.hardclient.Category;
import net.hardclient.SearchTags;
import net.hardclient.events.UpdateListener;
import net.hardclient.hack.Hack;

@SearchTags({"AutoSwim", "auto swim"})
public final class FishHack extends Hack implements UpdateListener
{
	public FishHack()
	{
		super("Fish");
		setCategory(Category.MOVEMENT);
	}
	
	@Override
	protected void onEnable()
	{
		EVENTS.add(UpdateListener.class, this);
		HARD.getHax().dolphinHack.setEnabled(false);
	}
	
	@Override
	protected void onDisable()
	{
		EVENTS.remove(UpdateListener.class, this);
	}
	
	@Override
	public void onUpdate()
	{
		ClientPlayerEntity player = MC.player;
		if(!player.isTouchingWater() || player.isSneaking())
			return;
		
		Vec3d velocity = player.getVelocity();
		player.setVelocity(velocity.x, velocity.y + 0.005, velocity.z);
	}
}
