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
import net.hardclient.settings.Setting;
import net.hardclient.util.RenderUtils;

public abstract class AbstractListEditButton extends Component
{
	private static final ClickGui GUI = HARD.getGui();
	private static final TextRenderer TR = MC.textRenderer;
	
	private final String buttonText = "Edit...";
	private final int buttonWidth = TR.getWidth(buttonText);
	
	protected abstract void openScreen();
	
	protected abstract String getText();
	
	protected abstract Setting getSetting();
	
	@Override
	public void handleMouseClick(double mouseX, double mouseY, int mouseButton)
	{
		if(mouseButton != GLFW.GLFW_MOUSE_BUTTON_LEFT)
			return;
		
		if(mouseX < getX() + getWidth() - buttonWidth - 4)
			return;
		
		openScreen();
	}
	
	@Override
	public void render(DrawContext context, int mouseX, int mouseY,
		float partialTicks)
	{
		int x1 = getX();
		int x2 = x1 + getWidth();
		int x3 = x2 - buttonWidth - 4;
		int y1 = getY();
		int y2 = y1 + getHeight();
		
		boolean hovering = isHovering(mouseX, mouseY);
		boolean hText = hovering && mouseX < x3;
		boolean hBox = hovering && mouseX >= x3;
		
		if(hText)
			GUI.setTooltip(getSetting().getWrappedDescription(200));
		
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
		context.drawText(TR, getText(), x1, y1 + 2, txtColor, false);
		context.drawText(TR, buttonText, x3 + 2, y1 + 2, txtColor, false);
	}
	
	@Override
	public int getDefaultWidth()
	{
		return TR.getWidth(getText()) + buttonWidth + 6;
	}
	
	@Override
	public int getDefaultHeight()
	{
		return 11;
	}
}
