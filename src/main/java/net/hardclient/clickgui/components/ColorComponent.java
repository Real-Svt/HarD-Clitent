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
import net.hardclient.clickgui.screens.EditColorScreen;
import net.hardclient.settings.ColorSetting;
import net.hardclient.util.ColorUtils;
import net.hardclient.util.RenderUtils;

public final class ColorComponent extends Component
{
	private static final ClickGui GUI = HARD.getGui();
	private static final TextRenderer TR = MC.textRenderer;
	private static final int TEXT_HEIGHT = 11;
	
	private final ColorSetting setting;
	
	public ColorComponent(ColorSetting setting)
	{
		this.setting = setting;
		setWidth(getDefaultWidth());
		setHeight(getDefaultHeight());
	}
	
	@Override
	public void handleMouseClick(double mouseX, double mouseY, int mouseButton)
	{
		if(mouseY < getY() + TEXT_HEIGHT)
			return;
		
		switch(mouseButton)
		{
			case GLFW.GLFW_MOUSE_BUTTON_LEFT:
			MC.setScreen(new EditColorScreen(MC.currentScreen, setting));
			break;
			
			case GLFW.GLFW_MOUSE_BUTTON_RIGHT:
			setting.setColor(setting.getDefaultColor());
			break;
		}
	}
	
	@Override
	public void render(DrawContext context, int mouseX, int mouseY,
		float partialTicks)
	{
		int x1 = getX();
		int x2 = x1 + getWidth();
		int y1 = getY();
		int y2 = y1 + getHeight();
		int y3 = y1 + TEXT_HEIGHT;
		
		boolean hovering = isHovering(mouseX, mouseY);
		boolean hText = hovering && mouseY < y3;
		boolean hColor = hovering && mouseY >= y3;
		
		if(hText)
			GUI.setTooltip(setting.getWrappedDescription(200));
		else if(hColor)
			GUI.setTooltip(getColorTooltip());
		
		// background
		float opacity = GUI.getOpacity();
		RenderUtils.fillRoundedRect(context, x1, y1, x2, y3, 3,
			GUI.getSurface(0.06F, opacity));
		
		// box
		RenderUtils.fillRoundedRect(context, x1 + 1, y3 + 1, x2 - 1, y2 - 1, 3,
			setting.getColorI(hovering ? 1F : Math.max(0.5F, opacity)));
		RenderUtils.drawRoundedBorder2D(context, x1 + 1, y3 + 1, x2 - 1, y2 - 1,
			3, RenderUtils.toIntColor(GUI.getAccent(), 0.45F));
		
		// text
		String name = setting.getName();
		String value = ColorUtils.toHex(setting.getColor());
		int valueWidth = TR.getWidth(value);
		int txtColor = GUI.getTxtColor();
		context.drawText(TR, name, x1, y1 + 2, txtColor, false);
		context.drawText(TR, value, x2 - valueWidth, y1 + 2, txtColor, false);
	}
	
	private String getColorTooltip()
	{
		String tooltip = "\u00a7cR:\u00a7r" + setting.getRed();
		tooltip += " \u00a7aG:\u00a7r" + setting.getGreen();
		tooltip += " \u00a79B:\u00a7r" + setting.getBlue();
		tooltip += "\n\n\u00a7e[left-click]\u00a7r to edit";
		tooltip += "\n\u00a7e[right-click]\u00a7r to reset";
		return tooltip;
	}
	
	@Override
	public int getDefaultWidth()
	{
		return TR.getWidth(setting.getName() + "#FFFFFF") + 6;
	}
	
	@Override
	public int getDefaultHeight()
	{
		return TEXT_HEIGHT * 2;
	}
}
