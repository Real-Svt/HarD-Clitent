/*
 * Copyright (c) 2014-2026 Hard-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.hardclient.hacks;

import net.minecraft.util.math.Vec3d;
import net.hardclient.Category;
import net.hardclient.events.UpdateListener;
import net.hardclient.hack.Hack;

public final class NoWebHack extends Hack implements UpdateListener
{
	public NoWebHack()
	{
		super("NoWeb");
		setCategory(Category.MOVEMENT);
	}
	
	@Override
	protected void onEnable()
	{
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
		MC.player.movementMultiplier = Vec3d.ZERO;
	}
}
