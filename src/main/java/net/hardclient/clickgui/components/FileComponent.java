/*
 * Copyright (c) 2014-2026 Hard-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.hardclient.clickgui.components;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.hardclient.clickgui.ClickGui;
import net.hardclient.clickgui.Component;
import net.hardclient.clickgui.screens.SelectFileScreen;
import net.hardclient.settings.FileSetting;
import net.hardclient.util.RenderUtils;

public final class FileComponent extends Component
{
	private static final ClickGui GUI = HARD.getGui();
	private static final TextRenderer TR = MC.textRenderer;
	
	private final FileSetting setting;
	
	public FileComponent(FileSetting setting)
	{
		this.setting = setting;
		setWidth(getDefaultWidth());
		setHeight(getDefaultHeight());
	}
	
	@Override
	public void handleMouseClick(double mouseX, double mouseY, int mouseButton)
	{
		if(mouseButton != GLFW.GLFW_MOUSE_BUTTON_LEFT)
			return;
		
		if(mouseX < getX() + getWidth() - getButtonWidth() - 4)
			return;
		
		MC.setScreen(new SelectFileScreen(MC.currentScreen, setting));
	}
	
	@Override
	public void render(DrawContext context, int mouseX, int mouseY,
		float partialTicks)
	{
		int x1 = getX();
		int x2 = x1 + getWidth();
		int x3 = x2 - getButtonWidth() - 4;
		int y1 = getY();
		int y2 = y1 + getHeight();
		
		boolean hovering = isHovering(mouseX, mouseY);
		boolean hText = hovering && mouseX < x3;
		boolean hBox = hovering && mouseX >= x3;
		
		// tooltip
		if(hText)
			GUI.setTooltip(setting.getWrappedDescription(200));
		else if(hBox)
			GUI.setTooltip("\u00a7e[left-click]\u00a7r to select file");
		
		// background
		RenderUtils.fillRoundedRect(context, x1, y1, x3, y2, 3,
			GUI.getSurface(0.06F, GUI.getOpacity()));
		
		// button
		RenderUtils.fillRoundedRect(context, x3 + 1, y1 + 1, x2 - 1, y2 - 1, 2,
			GUI.getSurface(hBox ? 0.24F : 0.12F, GUI.getOpacity()));
		RenderUtils.drawRoundedBorder2D(context, x3 + 1, y1 + 1, x2 - 1, y2 - 1,
			2, RenderUtils.toIntColor(GUI.getAccent(), 0.45F));
		
		// text
		int txtColor = GUI.getTxtColor();
		String labelText = setting.getName() + ":";
		String buttonText = setting.getSelectedFileName();
		context.drawText(TR, labelText, x1, y1 + 2, txtColor, false);
		context.drawText(TR, buttonText, x3 + 2, y1 + 2, txtColor, false);
	}
	
	private int getButtonWidth()
	{
		return TR.getWidth(setting.getSelectedFileName());
	}
	
	@Override
	public int getDefaultWidth()
	{
		String text = setting.getName() + ":";
		return TR.getWidth(text) + getButtonWidth() + 6;
	}
	
	@Override
	public int getDefaultHeight()
	{
		return 11;
	}
}
