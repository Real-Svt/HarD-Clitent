/*
 * Copyright (c) 2014-2026 Hard-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.hardclient.hacks;

import net.hardclient.Category;
import net.hardclient.SearchTags;
import net.hardclient.events.UpdateListener;
import net.hardclient.hack.Hack;

@SearchTags({"jet pack", "AirJump", "air jump"})
public final class JetpackHack extends Hack implements UpdateListener
{
	public JetpackHack()
	{
		super("Jetpack");
		
		setCategory(Category.MOVEMENT);
	}
	
	@Override
	protected void onEnable()
	{
		HARD.getHax().creativeFlightHack.setEnabled(false);
		HARD.getHax().flightHack.setEnabled(false);
		
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
		if(MC.options.jumpKey.isPressed())
			MC.player.jump();
	}
}
