/*
 * Copyright (c) 2014-2026 Hard-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.hardclient.test;

import static net.hardclient.test.HardClientTestHelper.*;

import net.minecraft.item.Items;

public enum GiveCmdTest
{
	;
	
	public static void testGiveCmd()
	{
		System.out.println("Testing .give command");
		runHardCommand("give diamond");
		waitForWorldTicks(1);
		assertOneItemInSlot(0, Items.DIAMOND);
		
		// Clean up
		runChatCommand("clear");
		clearChat();
	}
}
