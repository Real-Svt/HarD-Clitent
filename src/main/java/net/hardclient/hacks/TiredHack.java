/*
 * Copyright (c) 2014-2026 Hard-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.hardclient.hacks;

import net.hardclient.Category;
import net.hardclient.events.UpdateListener;
import net.hardclient.hack.Hack;
import net.hardclient.util.Rotation;

public final class TiredHack extends Hack implements UpdateListener
{
	public TiredHack()
	{
		super("Tired");
		setCategory(Category.FUN);
	}
	
	@Override
	protected void onEnable()
	{
		// disable incompatible derps
		HARD.getHax().derpHack.setEnabled(false);
		HARD.getHax().headRollHack.setEnabled(false);
		
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
		new Rotation(MC.player.getYaw(), MC.player.age % 100)
			.sendPlayerLookPacket();
	}
}
