/*
 * Copyright (c) 2014-2026 Hard-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.hardclient.hacks;

import net.hardclient.Category;
import net.hardclient.DontBlock;
import net.hardclient.SearchTags;
import net.hardclient.hack.Hack;
import net.hardclient.settings.SliderSetting;
import net.hardclient.settings.SliderSetting.ValueDisplay;

@SearchTags({"auto reconnect", "AutoRejoin", "auto rejoin"})
@DontBlock
public final class AutoReconnectHack extends Hack
{
	private final SliderSetting waitTime =
		new SliderSetting("Wait time", "Time before reconnecting in seconds.",
			5, 0, 60, 0.5, ValueDisplay.DECIMAL.withSuffix("s"));
	
	public AutoReconnectHack()
	{
		super("AutoReconnect");
		setCategory(Category.OTHER);
		addSetting(waitTime);
	}
	
	public int getWaitTicks()
	{
		return (int)(waitTime.getValue() * 20);
	}
	
	// See DisconnectedScreenMixin
}
