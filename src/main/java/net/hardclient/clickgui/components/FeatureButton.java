/*
 * Copyright (c) 2014-2026 Hard-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.hardclient.clickgui.components;

import java.util.Objects;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.hardclient.Feature;
import net.hardclient.clickgui.ClickGui;
import net.hardclient.clickgui.ClickGuiIcons;
import net.hardclient.clickgui.Component;
import net.hardclient.clickgui.SettingsWindow;
import net.hardclient.clickgui.Window;
import net.hardclient.hacks.TooManyHaxHack;
import net.hardclient.util.ChatUtils;
import net.hardclient.util.RenderUtils;

public final class FeatureButton extends Component
{
	private static final ClickGui GUI = HARD.getGui();
	private static final TextRenderer TR = MC.textRenderer;
	
	private final Feature feature;
	private final boolean hasSettings;
	
	private Window settingsWindow;
	
	public FeatureButton(Feature feature)
	{
		this.feature = Objects.requireNonNull(feature);
		setWidth(getDefaultWidth());
		setHeight(getDefaultHeight());
		hasSettings = !feature.getSettings().isEmpty();
	}
	
	@Override
	public void handleMouseClick(double mouseX, double mouseY, int mouseButton)
	{
		if(mouseButton != 0)
			return;
		
		if(hasSettings && (mouseX > getX() + getWidth() - 12
			|| feature.getPrimaryAction().isEmpty()))
		{
			toggleSettingsWindow();
			return;
		}
		
		TooManyHaxHack tooManyHax = HARD.getHax().tooManyHaxHack;
		if(tooManyHax.isEnabled() && tooManyHax.isBlocked(feature))
		{
			ChatUtils.error(feature.getName() + " is blocked by TooManyHax.");
			return;
		}
		
		feature.doPrimaryAction();
	}
	
	private boolean isSettingsWindowOpen()
	{
		return settingsWindow != null && !settingsWindow.isClosing();
	}
	
	private void toggleSettingsWindow()
	{
		if(!isSettingsWindowOpen())
		{
			settingsWindow = new SettingsWindow(feature, getParent(), getY());
			GUI.addWindow(settingsWindow);
			
		}else
		{
			settingsWindow.close();
			settingsWindow = null;
		}
	}
	
	@Override
	public void render(DrawContext context, int mouseX, int mouseY,
		float partialTicks)
	{
		int x1 = getX();
		int x2 = x1 + getWidth();
		int x3 = hasSettings ? x2 - 11 : x2;
		int y1 = getY();
		int y2 = y1 + getHeight();
		
		boolean hovering = isHovering(mouseX, mouseY);
		boolean hFeature = hovering && mouseX < x3;
		boolean hSettings = hovering && mouseX >= x3;
		
		if(hFeature)
			GUI.setTooltip(feature.getWrappedDescription(200));
		
		boolean enabled = feature.isEnabled();
		
		// row background
		int rowColor = enabled
			? RenderUtils.toIntColor(GUI.getAccent(), hFeature ? 0.55F : 0.42F)
			: GUI.getSurface(hFeature ? 0.16F : 0.08F, GUI.getOpacity());
		RenderUtils.fillRoundedRect(context, x1, y1, x2, y2, 3, rowColor);
		
		// enabled indicator
		if(enabled)
			RenderUtils.fillRoundedRect(context, x1 + 1, y1 + 2, x1 + 3, y2 - 2,
				1, RenderUtils.toIntColor(GUI.getAccent(), 1));
		
		// settings button
		if(hasSettings)
		{
			RenderUtils.fillRoundedRect(context, x3 + 1, y1 + 1, x2 - 1, y2 - 1,
				2, GUI.getSurface(hSettings ? 0.24F : 0.12F, GUI.getOpacity()));
			ClickGuiIcons.drawMinimizeArrow(context, x3, y1 + 0.5F, x2,
				y2 - 0.5F, hSettings, !isSettingsWindowOpen());
		}
		
		// outline
		RenderUtils.drawRoundedBorder2D(context, x1, y1, x2, y2, 3,
			RenderUtils.toIntColor(GUI.getAccent(), enabled ? 0.55F : 0.22F));
		
		// text
		String name = feature.getName();
		int tx = x1 + (x3 - x1 - TR.getWidth(name)) / 2 + (enabled ? 1 : 0);
		int ty = y1 + 2;
		context.drawText(TR, name, tx, ty,
			enabled ? 0xFFFFFFFF : GUI.getTxtColor(), false);
	}
	
	@Override
	public int getDefaultWidth()
	{
		int width = TR.getWidth(feature.getName());
		width += hasSettings ? 15 : 4;
		return width;
	}
	
	@Override
	public int getDefaultHeight()
	{
		return 11;
	}
}
