/*
 * Copyright (c) 2014-2026 Hard-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.hardclient.clickgui.components;

import java.util.Objects;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Style;
import net.hardclient.clickgui.ClickGui;
import net.hardclient.clickgui.Component;
import net.hardclient.clickgui.screens.EditTextFieldScreen;
import net.hardclient.settings.TextFieldSetting;
import net.hardclient.util.ChatUtils;
import net.hardclient.util.RenderUtils;

public final class TextFieldEditButton extends Component
{
	private static final ClickGui GUI = HARD.getGui();
	private static final TextRenderer TR = MC.textRenderer;
	private static final int TEXT_HEIGHT = 11;
	
	private final TextFieldSetting setting;
	
	public TextFieldEditButton(TextFieldSetting setting)
	{
		this.setting = Objects.requireNonNull(setting);
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
			MC.setScreen(new EditTextFieldScreen(MC.currentScreen, setting));
			break;
			
			case GLFW.GLFW_MOUSE_BUTTON_RIGHT:
			setting.resetToDefault();
			break;
		}
	}
	
	@Override
	public void render(DrawContext context, int mouseX, int mouseY,
		float partialTicks)
	{
		float[] bgColor = GUI.getBgColor();
		float opacity = GUI.getOpacity();
		
		int x1 = getX();
		int x2 = x1 + getWidth();
		int y1 = getY();
		int y2 = y1 + getHeight();
		int y3 = y1 + TEXT_HEIGHT;
		
		boolean hovering = isHovering(mouseX, mouseY);
		boolean hText = hovering && mouseY < y3;
		boolean hBox = hovering && mouseY >= y3;
		
		if(hText)
			GUI.setTooltip(ChatUtils.wrapText(setting.getDescription(), 200));
		else if(hBox)
			GUI.setTooltip(ChatUtils.wrapText(setting.getValue(), 200));
		
		// background
		RenderUtils.fillRoundedRect(context, x1, y1, x2, y3, 3,
			GUI.getSurface(0.06F, opacity));
		
		// box
		RenderUtils.fillRoundedRect(context, x1 + 1, y3 + 1, x2 - 1, y2 - 1, 3,
			GUI.getSurface(hBox ? 0.18F : 0.10F, opacity));
		RenderUtils.drawRoundedBorder2D(context, x1 + 1, y3 + 1, x2 - 1, y2 - 1,
			3, RenderUtils.toIntColor(GUI.getAccent(), 0.45F));
		
		// text
		int txtColor = GUI.getTxtColor();
		context.drawText(TR, setting.getName(), x1, y1 + 2, txtColor, false);
		String value = setting.getValue();
		int maxWidth = getWidth() - TR.getWidth("...") - 2;
		int maxLength = TR.getTextHandler().getLimitedStringLength(value,
			maxWidth, Style.EMPTY);
		if(maxLength < value.length())
			value = value.substring(0, maxLength) + "...";
		context.drawText(TR, value, x1 + 2, y3 + 2, txtColor, false);
	}
	
	@Override
	public int getDefaultWidth()
	{
		return TR.getWidth(setting.getName()) + 4;
	}
	
	@Override
	public int getDefaultHeight()
	{
		return TEXT_HEIGHT * 2;
	}
}
