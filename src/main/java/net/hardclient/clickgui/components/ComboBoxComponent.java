/*
 * Copyright (c) 2014-2026 Hard-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.hardclient.clickgui.components;

import java.util.Arrays;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.hardclient.clickgui.ClickGui;
import net.hardclient.clickgui.ClickGuiIcons;
import net.hardclient.clickgui.ComboBoxPopup;
import net.hardclient.clickgui.Component;
import net.hardclient.settings.EnumSetting;
import net.hardclient.util.RenderUtils;

public final class ComboBoxComponent<T extends Enum<T>> extends Component
{
	private static final ClickGui GUI = HARD.getGui();
	private static final TextRenderer TR = MC.textRenderer;
	private static final int ARROW_SIZE = 11;
	
	private final EnumSetting<T> setting;
	private final int popupWidth;
	
	private ComboBoxPopup<T> popup;
	
	public ComboBoxComponent(EnumSetting<T> setting)
	{
		this.setting = setting;
		popupWidth = Arrays.stream(setting.getValues()).map(T::toString)
			.mapToInt(s -> TR.getWidth(s)).max().getAsInt();
		
		setWidth(getDefaultWidth());
		setHeight(getDefaultHeight());
	}
	
	@Override
	public void handleMouseClick(double mouseX, double mouseY, int mouseButton)
	{
		if(mouseX < getX() + getWidth() - popupWidth - ARROW_SIZE - 4)
			return;
		
		switch(mouseButton)
		{
			case GLFW.GLFW_MOUSE_BUTTON_LEFT:
			handleLeftClick();
			break;
			
			case GLFW.GLFW_MOUSE_BUTTON_RIGHT:
			handleRightClick();
			break;
		}
	}
	
	private void handleLeftClick()
	{
		if(isPopupOpen())
		{
			popup.close();
			popup = null;
			return;
		}
		
		popup = new ComboBoxPopup<>(this, setting, popupWidth);
		GUI.addPopup(popup);
	}
	
	private void handleRightClick()
	{
		if(isPopupOpen())
			return;
		
		setting.setSelected(setting.getDefaultSelected());
	}
	
	private boolean isPopupOpen()
	{
		return popup != null && !popup.isClosing();
	}
	
	@Override
	public void render(DrawContext context, int mouseX, int mouseY,
		float partialTicks)
	{
		int x1 = getX();
		int x2 = x1 + getWidth();
		int x3 = x2 - ARROW_SIZE;
		int x4 = x3 - popupWidth - 4;
		int y1 = getY();
		int y2 = y1 + getHeight();
		
		boolean hovering = isHovering(mouseX, mouseY);
		boolean hText = hovering && mouseX < x4;
		boolean hBox = hovering && mouseX >= x4;
		
		// tooltip
		if(hText)
			GUI.setTooltip(setting.getWrappedDescription(200));
		
		// background
		RenderUtils.fillRoundedRect(context, x1, y1, x4, y2, 3,
			GUI.getSurface(0.06F, GUI.getOpacity()));
		
		// box
		RenderUtils.fillRoundedRect(context, x4 + 1, y1 + 1, x2 - 1, y2 - 1, 2,
			GUI.getSurface(hBox ? 0.20F : 0.12F, GUI.getOpacity()));
		
		// outlines
		RenderUtils.drawRoundedBorder2D(context, x4 + 1, y1 + 1, x2 - 1, y2 - 1,
			2, RenderUtils.toIntColor(GUI.getAccent(), 0.45F));
		
		// arrow
		ClickGuiIcons.drawMinimizeArrow(context, x3, y1 + 0.5F, x2, y2 - 0.5F,
			hBox, !isPopupOpen());
		
		// text
		String name = setting.getName();
		String value = "" + setting.getSelected();
		int txtColor = GUI.getTxtColor();
		context.drawText(TR, name, x1, y1 + 2, txtColor, false);
		context.drawText(TR, value, x4 + 2, y1 + 2, txtColor, false);
	}
	
	@Override
	public int getDefaultWidth()
	{
		return TR.getWidth(setting.getName()) + popupWidth + ARROW_SIZE + 6;
	}
	
	@Override
	public int getDefaultHeight()
	{
		return ARROW_SIZE;
	}
}
