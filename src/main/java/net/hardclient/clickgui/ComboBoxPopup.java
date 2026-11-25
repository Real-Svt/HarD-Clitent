/*
 * Copyright (c) 2014-2026 Hard-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.hardclient.clickgui;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.hardclient.HardClient;
import net.hardclient.settings.EnumSetting;
import net.hardclient.util.RenderUtils;

public final class ComboBoxPopup<T extends Enum<T>> extends Popup
{
	private static final ClickGui GUI = HardClient.INSTANCE.getGui();
	private static final TextRenderer TR = HardClient.MC.textRenderer;
	
	private final EnumSetting<T> setting;
	private final int popupWidth;
	
	public ComboBoxPopup(Component owner, EnumSetting<T> setting,
		int popupWidth)
	{
		super(owner);
		this.setting = setting;
		this.popupWidth = popupWidth;
		
		setWidth(getDefaultWidth());
		setHeight(getDefaultHeight());
		
		setX(owner.getWidth() - getWidth());
		setY(owner.getHeight());
	}
	
	@Override
	public void handleMouseClick(int mouseX, int mouseY, int mouseButton)
	{
		if(mouseButton != GLFW.GLFW_MOUSE_BUTTON_LEFT)
			return;
		
		int yi1 = getY() - 11;
		for(T value : setting.getValues())
		{
			if(value == setting.getSelected())
				continue;
			
			yi1 += 11;
			int yi2 = yi1 + 11;
			
			if(mouseY < yi1 || mouseY >= yi2)
				continue;
			
			setting.setSelected(value);
			close();
			break;
		}
	}
	
	@Override
	public void render(DrawContext context, int mouseX, int mouseY)
	{
		int x1 = getX();
		int x2 = x1 + getWidth();
		int y1 = getY();
		int y2 = y1 + getHeight();
		
		boolean hovering = isHovering(mouseX, mouseY, x1, x2, y1, y2);
		
		if(hovering)
			GUI.setTooltip("");
		
		// shadow and panel
		RenderUtils.drawRoundedShadow2D(context, x1, y1, x2, y2, 4, 0x66000000);
		RenderUtils.fillRoundedRect(context, x1, y1, x2, y2, 4,
			RenderUtils.toIntColor(RenderUtils.lighten(GUI.getBgColor(), 0.04F),
				Math.min(1F, GUI.getOpacity() + 0.15F)));
		
		int yi1 = y1 - 11;
		for(T value : setting.getValues())
		{
			if(value == setting.getSelected())
				continue;
			
			yi1 += 11;
			int yi2 = yi1 + 11;
			
			boolean hValue = hovering && mouseY >= yi1 && mouseY < yi2;
			RenderUtils.fillRoundedRect(context, x1 + 1, yi1 + 1, x2 - 1,
				yi2 - 1, 3,
				GUI.getSurface(hValue ? 0.22F : 0.08F, GUI.getOpacity()));
			
			context.drawText(TR, value.toString(), x1 + 3, yi1 + 2,
				hValue ? 0xFFFFFFFF : GUI.getTxtColor(), false);
		}
		
		// outline
		RenderUtils.drawRoundedBorder2D(context, x1, y1, x2, y2, 4,
			GUI.getStroke(0.9F));
	}
	
	private boolean isHovering(int mouseX, int mouseY, int x1, int x2, int y1,
		int y2)
	{
		return mouseX >= x1 && mouseY >= y1 && mouseX < x2 && mouseY < y2;
	}
	
	@Override
	public int getDefaultWidth()
	{
		return popupWidth + 15;
	}
	
	@Override
	public int getDefaultHeight()
	{
		int numValues = setting.getValues().length;
		return (numValues - 1) * 11;
	}
}
