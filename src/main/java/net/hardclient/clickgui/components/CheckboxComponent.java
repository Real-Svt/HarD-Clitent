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
import net.hardclient.clickgui.ClickGuiIcons;
import net.hardclient.clickgui.Component;
import net.hardclient.settings.CheckboxSetting;
import net.hardclient.util.RenderUtils;

public final class CheckboxComponent extends Component
{
	private static final ClickGui GUI = HARD.getGui();
	private static final TextRenderer TR = MC.textRenderer;
	private static final int BOX_SIZE = 11;
	
	private final CheckboxSetting setting;
	
	public CheckboxComponent(CheckboxSetting setting)
	{
		this.setting = setting;
		setWidth(getDefaultWidth());
		setHeight(getDefaultHeight());
	}
	
	@Override
	public void handleMouseClick(double mouseX, double mouseY, int mouseButton)
	{
		switch(mouseButton)
		{
			case GLFW.GLFW_MOUSE_BUTTON_LEFT:
			setting.setChecked(!setting.isChecked());
			break;
			
			case GLFW.GLFW_MOUSE_BUTTON_RIGHT:
			setting.setChecked(setting.isCheckedByDefault());
			break;
		}
	}
	
	@Override
	public void render(DrawContext context, int mouseX, int mouseY,
		float partialTicks)
	{
		int x1 = getX();
		int x2 = x1 + getWidth();
		int x3 = x1 + BOX_SIZE;
		int y1 = getY();
		int y2 = y1 + getHeight();
		
		boolean hovering = isHovering(mouseX, mouseY);
		boolean hText = hovering && mouseX >= x3;
		
		if(hText)
			GUI.setTooltip(getTooltip());
		
		if(setting.isLocked())
			hovering = false;
		
		boolean checked = setting.isChecked();
		
		// background
		RenderUtils.fillRoundedRect(context, x3, y1, x2, y2, 2,
			GUI.getSurface(hovering ? 0.14F : 0.06F, GUI.getOpacity()));
		
		// box
		int boxColor = checked
			? RenderUtils.toIntColor(GUI.getAccent(), hovering ? 1F : 0.9F)
			: GUI.getSurface(hovering ? 0.22F : 0.10F, GUI.getOpacity());
		RenderUtils.fillRoundedRect(context, x1 + 1, y1 + 1, x3 - 1, y2 - 1, 3,
			boxColor);
		RenderUtils.drawRoundedBorder2D(context, x1 + 1, y1 + 1, x3 - 1, y2 - 1,
			3, RenderUtils.toIntColor(GUI.getAccent(), checked ? 0.9F : 0.3F));
		
		// check
		if(checked)
			ClickGuiIcons.drawCheck(context, x1 + 1, y1 + 1, x3 - 1, y2 - 1,
				hovering, setting.isLocked());
		
		// text
		String name = setting.getName();
		context.drawText(TR, name, x3 + 2, y1 + 2, GUI.getTxtColor(), false);
	}
	
	private String getTooltip()
	{
		String tooltip = setting.getWrappedDescription(200);
		if(setting.isLocked())
		{
			tooltip += "\n\nThis checkbox is locked to ";
			tooltip += setting.isChecked() + ".";
		}
		
		return tooltip;
	}
	
	@Override
	public int getDefaultWidth()
	{
		return BOX_SIZE + TR.getWidth(setting.getName()) + 2;
	}
	
	@Override
	public int getDefaultHeight()
	{
		return BOX_SIZE;
	}
}
