/*
 * Copyright (c) 2014-2026 Hard-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.hardclient.hud;

import net.minecraft.client.gui.DrawContext;
import net.hardclient.HardClient;
import net.hardclient.clickgui.ClickGui;
import net.hardclient.clickgui.screens.ClickGuiScreen;
import net.hardclient.events.GUIRenderListener;

public final class IngameHUD implements GUIRenderListener
{
	private final HardLogo hardLogo = new HardLogo();
	private final HackListHUD hackList = new HackListHUD();
	private TabGui tabGui;
	
	@Override
	public void onRenderGUI(DrawContext context, float partialTicks)
	{
		if(!HardClient.INSTANCE.isEnabled())
			return;
		
		if(tabGui == null)
			tabGui = new TabGui();
		
		ClickGui clickGui = HardClient.INSTANCE.getGui();
		
		clickGui.updateColors();
		
		hardLogo.render(context);
		hackList.render(context, partialTicks);
		tabGui.render(context, partialTicks);
		
		// pinned windows
		if(!(HardClient.MC.currentScreen instanceof ClickGuiScreen))
			clickGui.renderPinnedWindows(context, partialTicks);
	}
	
	public HackListHUD getHackList()
	{
		return hackList;
	}
}
