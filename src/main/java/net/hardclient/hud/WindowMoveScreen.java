/*
 * Copyright (c) 2014-2026 Hard-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.hardclient.hud;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.hardclient.HardClient;
import net.hardclient.clickgui.ClickGui;
import net.hardclient.clickgui.Window;

/**
 * Lets the user drag a pinned window (for example the Radar) to a new position
 * while in game. The new position is saved to the windows file.
 */
public final class WindowMoveScreen extends Screen
{
	private final Window window;
	private final Screen parent;
	
	public WindowMoveScreen(Window window, Screen parent)
	{
		super(Text.literal("Move " + window.getTitle()));
		this.window = window;
		this.parent = parent;
	}
	
	@Override
	public boolean shouldPause()
	{
		return false;
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton)
	{
		if(mouseButton == GLFW.GLFW_MOUSE_BUTTON_LEFT && mouseX >= window.getX()
			&& mouseY >= window.getY()
			&& mouseX < window.getX() + window.getWidth()
			&& mouseY < window.getY() + window.getHeight())
		{
			window.startDragging((int)mouseX, (int)mouseY);
			return true;
		}
		
		return super.mouseClicked(mouseX, mouseY, mouseButton);
	}
	
	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int mouseButton,
		double deltaX, double deltaY)
	{
		if(window.isDragging() && mouseButton == GLFW.GLFW_MOUSE_BUTTON_LEFT)
		{
			window.dragTo((int)mouseX, (int)mouseY);
			return true;
		}
		
		return super.mouseDragged(mouseX, mouseY, mouseButton, deltaX, deltaY);
	}
	
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int mouseButton)
	{
		if(window.isDragging())
		{
			window.stopDragging();
			getGui().saveWindows();
			return true;
		}
		
		return super.mouseReleased(mouseX, mouseY, mouseButton);
	}
	
	@Override
	public void render(DrawContext context, int mouseX, int mouseY,
		float partialTicks)
	{
		super.render(context, mouseX, mouseY, partialTicks);
		
		// hint
		String hint =
			"Drag the " + window.getTitle() + " to move it (Esc to finish)";
		context.drawCenteredTextWithShadow(textRenderer, hint, width / 2, 20,
			0xFFFFFFFF);
		
		// window
		getGui().renderWindowPreview(context, window, mouseX, mouseY,
			partialTicks);
	}
	
	@Override
	public void close()
	{
		window.stopDragging();
		getGui().saveWindows();
		client.setScreen(parent);
	}
	
	private static ClickGui getGui()
	{
		return HardClient.INSTANCE.getGui();
	}
}
