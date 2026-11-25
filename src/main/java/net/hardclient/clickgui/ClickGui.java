/*
 * Copyright (c) 2014-2026 Hard-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.hardclient.clickgui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.stream.Stream;

import org.lwjgl.glfw.GLFW;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.hardclient.Category;
import net.hardclient.Feature;
import net.hardclient.HardClient;
import net.hardclient.clickgui.components.FeatureButton;
import net.hardclient.hacks.ClickGuiHack;
import net.hardclient.settings.Setting;
import net.hardclient.util.RenderUtils;
import net.hardclient.util.json.JsonUtils;

public final class ClickGui
{
	private static final HardClient HARD = HardClient.INSTANCE;
	private static final MinecraftClient MC = HardClient.MC;
	
	private static final int CORNER_RADIUS = 6;
	private static final int ORBIT_THICKNESS = 2;
	private static final long ORBIT_PERIOD_MS = 2600L;
	private static final int TITLE_HEIGHT = 13;
	
	private final ArrayList<Window> windows = new ArrayList<>();
	private final ArrayList<Popup> popups = new ArrayList<>();
	private final Path windowsFile;
	
	private float[] bgColor = new float[3];
	private float[] acColor = new float[3];
	private int txtColor;
	private float opacity;
	private float ttOpacity;
	private int maxHeight;
	private int maxSettingsHeight;
	
	private String tooltip = "";
	
	private boolean leftMouseButtonPressed;
	
	public ClickGui(Path windowsFile)
	{
		this.windowsFile = windowsFile;
	}
	
	public void init()
	{
		updateColors();
		
		LinkedHashMap<Category, Window> windowMap = new LinkedHashMap<>();
		for(Category category : Category.values())
			windowMap.put(category, new Window(category.getName()));
		
		ArrayList<Feature> features = new ArrayList<>();
		features.addAll(HARD.getHax().getAllHax());
		features.addAll(HARD.getCmds().getAllCmds());
		features.addAll(HARD.getOtfs().getAllOtfs());
		
		for(Feature f : features)
			if(f.getCategory() != null)
				windowMap.get(f.getCategory()).add(new FeatureButton(f));
			
		windows.addAll(windowMap.values());
		
		Window uiSettings = new Window("UI Settings");
		uiSettings.add(new FeatureButton(HARD.getOtfs().hardLogoOtf));
		uiSettings.add(new FeatureButton(HARD.getOtfs().hackListOtf));
		uiSettings.add(new FeatureButton(HARD.getOtfs().keybindManagerOtf));
		ClickGuiHack clickGuiHack = HARD.getHax().clickGuiHack;
		Stream<Setting> settings = clickGuiHack.getSettings().values().stream();
		settings.map(Setting::getComponent).forEach(c -> uiSettings.add(c));
		windows.add(uiSettings);
		
		for(Window window : windows)
			window.setMinimized(true);
		
		windows.add(HardClient.INSTANCE.getHax().radarHack.getWindow());
		
		int x = 5;
		int y = 5;
		int scaledWidth = MC.getWindow().getScaledWidth();
		for(Window window : windows)
		{
			window.pack();
			
			if(x + window.getWidth() + 5 > scaledWidth)
			{
				x = 5;
				y += 18;
			}
			
			window.setX(x);
			window.setY(y);
			x += window.getWidth() + 5;
		}
		
		JsonObject json;
		try(BufferedReader reader = Files.newBufferedReader(windowsFile))
		{
			json = JsonParser.parseReader(reader).getAsJsonObject();
			
		}catch(NoSuchFileException e)
		{
			saveWindows();
			return;
			
		}catch(Exception e)
		{
			System.out.println("Failed to load " + windowsFile.getFileName());
			e.printStackTrace();
			
			saveWindows();
			return;
		}
		
		for(Window window : windows)
		{
			JsonElement jsonWindow = json.get(window.getTitle());
			if(jsonWindow == null || !jsonWindow.isJsonObject())
				continue;
			
			JsonElement jsonX = jsonWindow.getAsJsonObject().get("x");
			if(jsonX.isJsonPrimitive() && jsonX.getAsJsonPrimitive().isNumber())
				window.setX(jsonX.getAsInt());
			
			JsonElement jsonY = jsonWindow.getAsJsonObject().get("y");
			if(jsonY.isJsonPrimitive() && jsonY.getAsJsonPrimitive().isNumber())
				window.setY(jsonY.getAsInt());
			
			JsonElement jsonMinimized =
				jsonWindow.getAsJsonObject().get("minimized");
			if(jsonMinimized.isJsonPrimitive()
				&& jsonMinimized.getAsJsonPrimitive().isBoolean())
				window.setMinimized(jsonMinimized.getAsBoolean());
			
			JsonElement jsonPinned = jsonWindow.getAsJsonObject().get("pinned");
			if(jsonPinned.isJsonPrimitive()
				&& jsonPinned.getAsJsonPrimitive().isBoolean())
				window.setPinned(jsonPinned.getAsBoolean());
		}
		
		saveWindows();
	}
	
	public void saveWindows()
	{
		JsonObject json = new JsonObject();
		
		for(Window window : windows)
		{
			if(window.isClosable())
				continue;
			
			JsonObject jsonWindow = new JsonObject();
			jsonWindow.addProperty("x", window.getActualX());
			jsonWindow.addProperty("y", window.getActualY());
			jsonWindow.addProperty("minimized", window.isMinimized());
			jsonWindow.addProperty("pinned", window.isPinned());
			json.add(window.getTitle(), jsonWindow);
		}
		
		try(BufferedWriter writer = Files.newBufferedWriter(windowsFile))
		{
			JsonUtils.PRETTY_GSON.toJson(json, writer);
			
		}catch(IOException e)
		{
			System.out.println("Failed to save " + windowsFile.getFileName());
			e.printStackTrace();
		}
	}
	
	public void handleMouseClick(int mouseX, int mouseY, int mouseButton)
	{
		if(mouseButton == GLFW.GLFW_MOUSE_BUTTON_LEFT)
			leftMouseButtonPressed = true;
		
		boolean popupClicked =
			handlePopupMouseClick(mouseX, mouseY, mouseButton);
		
		if(!popupClicked)
			handleWindowMouseClick(mouseX, mouseY, mouseButton);
		
		for(Popup popup : popups)
			if(popup.getOwner().getParent().isClosing())
				popup.close();
			
		windows.removeIf(Window::isClosing);
		popups.removeIf(Popup::isClosing);
	}
	
	public void handleMouseRelease(double mouseX, double mouseY,
		int mouseButton)
	{
		if(mouseButton == GLFW.GLFW_MOUSE_BUTTON_LEFT)
			leftMouseButtonPressed = false;
	}
	
	public void handleMouseScroll(double mouseX, double mouseY, double delta)
	{
		int dWheel = (int)delta * 4;
		if(dWheel == 0)
			return;
		
		for(int i = windows.size() - 1; i >= 0; i--)
		{
			Window window = windows.get(i);
			
			if(!window.isScrollingEnabled() || window.isMinimized()
				|| window.isInvisible())
				continue;
			
			if(mouseX < window.getX() || mouseY < window.getY() + 13)
				continue;
			if(mouseX >= window.getX() + window.getWidth()
				|| mouseY >= window.getY() + window.getHeight())
				continue;
			
			int scroll = window.getScrollOffset() + dWheel;
			scroll = Math.min(scroll, 0);
			scroll = Math.max(scroll,
				-window.getInnerHeight() + window.getHeight() - 13);
			window.setScrollOffset(scroll);
			break;
		}
	}
	
	public boolean handleNavigatorPopupClick(double mouseX, double mouseY,
		int mouseButton)
	{
		boolean popupClicked =
			handlePopupMouseClick(mouseX, mouseY, mouseButton);
		
		if(popupClicked)
		{
			for(Popup popup : popups)
				if(popup.getOwner().getParent().isClosing())
					popup.close();
				
			popups.removeIf(Popup::isClosing);
		}
		
		return popupClicked;
	}
	
	public void handleNavigatorMouseClick(double cMouseX, double cMouseY,
		int mouseButton, Window window)
	{
		if(mouseButton == GLFW.GLFW_MOUSE_BUTTON_LEFT)
			leftMouseButtonPressed = true;
		
		handleComponentMouseClick(window, cMouseX, cMouseY, mouseButton);
		
		for(Popup popup : popups)
			if(popup.getOwner().getParent().isClosing())
				popup.close();
			
		popups.removeIf(Popup::isClosing);
	}
	
	private boolean handlePopupMouseClick(double mouseX, double mouseY,
		int mouseButton)
	{
		for(int i = popups.size() - 1; i >= 0; i--)
		{
			Popup popup = popups.get(i);
			Component owner = popup.getOwner();
			Window parent = owner.getParent();
			
			int x0 = parent.getX() + owner.getX();
			int y0 =
				parent.getY() + 13 + parent.getScrollOffset() + owner.getY();
			
			int x1 = x0 + popup.getX();
			int y1 = y0 + popup.getY();
			int x2 = x1 + popup.getWidth();
			int y2 = y1 + popup.getHeight();
			
			if(mouseX < x1 || mouseY < y1)
				continue;
			if(mouseX >= x2 || mouseY >= y2)
				continue;
			
			int cMouseX = (int)(mouseX - x0);
			int cMouseY = (int)(mouseY - y0);
			popup.handleMouseClick(cMouseX, cMouseY, mouseButton);
			
			popups.remove(i);
			popups.add(popup);
			return true;
		}
		
		return false;
	}
	
	private void handleWindowMouseClick(int mouseX, int mouseY, int mouseButton)
	{
		for(int i = windows.size() - 1; i >= 0; i--)
		{
			Window window = windows.get(i);
			if(window.isInvisible())
				continue;
			
			int x1 = window.getX();
			int y1 = window.getY();
			int x2 = x1 + window.getWidth();
			int y2 = y1 + window.getHeight();
			int y3 = y1 + 13;
			
			if(mouseX < x1 || mouseY < y1)
				continue;
			if(mouseX >= x2 || mouseY >= y2)
				continue;
			
			if(mouseY < y3)
				handleTitleBarMouseClick(window, mouseX, mouseY, mouseButton);
			else if(!window.isMinimized())
			{
				window.validate();
				
				int cMouseX = mouseX - x1;
				int cMouseY = mouseY - y3;
				
				if(window.isScrollingEnabled() && mouseX >= x2 - 3)
					handleScrollbarMouseClick(window, cMouseX, cMouseY,
						mouseButton);
				else
				{
					if(window.isScrollingEnabled())
						cMouseY -= window.getScrollOffset();
					
					handleComponentMouseClick(window, cMouseX, cMouseY,
						mouseButton);
				}
				
			}else
				continue;
			
			windows.remove(i);
			windows.add(window);
			break;
		}
	}
	
	private void handleTitleBarMouseClick(Window window, int mouseX, int mouseY,
		int mouseButton)
	{
		if(mouseButton != 0)
			return;
		
		if(mouseY < window.getY() + 2 || mouseY >= window.getY() + 11)
		{
			window.startDragging(mouseX, mouseY);
			return;
		}
		
		int x3 = window.getX() + window.getWidth();
		
		if(window.isClosable())
		{
			x3 -= 11;
			if(mouseX >= x3 && mouseX < x3 + 9)
			{
				window.close();
				return;
			}
		}
		
		if(window.isPinnable())
		{
			x3 -= 11;
			if(mouseX >= x3 && mouseX < x3 + 9)
			{
				window.setPinned(!window.isPinned());
				saveWindows();
				return;
			}
		}
		
		if(window.isMinimizable())
		{
			x3 -= 11;
			if(mouseX >= x3 && mouseX < x3 + 9)
			{
				window.setMinimized(!window.isMinimized());
				saveWindows();
				return;
			}
		}
		
		window.startDragging(mouseX, mouseY);
	}
	
	private void handleScrollbarMouseClick(Window window, int mouseX,
		int mouseY, int mouseButton)
	{
		if(mouseButton != GLFW.GLFW_MOUSE_BUTTON_LEFT)
			return;
		
		if(mouseX >= window.getWidth() - 1)
			return;
		
		double outerHeight = window.getHeight() - 13;
		double innerHeight = window.getInnerHeight();
		double maxScrollbarHeight = outerHeight - 2;
		int scrollbarY =
			(int)(outerHeight * (-window.getScrollOffset() / innerHeight) + 1);
		int scrollbarHeight =
			(int)(maxScrollbarHeight * outerHeight / innerHeight);
		
		if(mouseY < scrollbarY || mouseY >= scrollbarY + scrollbarHeight)
			return;
		
		window.startDraggingScrollbar(window.getY() + 13 + mouseY);
	}
	
	private void handleComponentMouseClick(Window window, double mouseX,
		double mouseY, int mouseButton)
	{
		for(int i2 = window.countChildren() - 1; i2 >= 0; i2--)
		{
			Component c = window.getChild(i2);
			
			if(mouseX < c.getX() || mouseY < c.getY())
				continue;
			if(mouseX >= c.getX() + c.getWidth()
				|| mouseY >= c.getY() + c.getHeight())
				continue;
			
			c.handleMouseClick(mouseX, mouseY, mouseButton);
			break;
		}
	}
	
	public void render(DrawContext context, int mouseX, int mouseY,
		float partialTicks)
	{
		updateColors();
		
		MatrixStack matrixStack = context.getMatrices();
		matrixStack.push();
		
		tooltip = "";
		for(Window window : windows)
		{
			if(window.isInvisible())
				continue;
			
			// dragging
			if(window.isDragging())
				if(leftMouseButtonPressed)
					window.dragTo(mouseX, mouseY);
				else
				{
					window.stopDragging();
					saveWindows();
				}
			
			// scrollbar dragging
			if(window.isDraggingScrollbar())
				if(leftMouseButtonPressed)
					window.dragScrollbarTo(mouseY);
				else
					window.stopDraggingScrollbar();
				
			matrixStack.translate(0, 0, 300);
			renderWindow(context, window, mouseX, mouseY, partialTicks);
		}
		
		renderPopups(context, mouseX, mouseY);
		renderTooltip(context, mouseX, mouseY);
		
		matrixStack.pop();
	}
	
	public void renderPopups(DrawContext context, int mouseX, int mouseY)
	{
		MatrixStack matrixStack = context.getMatrices();
		for(Popup popup : popups)
		{
			Component owner = popup.getOwner();
			Window parent = owner.getParent();
			
			int x1 = parent.getX() + owner.getX();
			int y1 =
				parent.getY() + 13 + parent.getScrollOffset() + owner.getY();
			
			matrixStack.push();
			matrixStack.translate(x1, y1, 300);
			
			int cMouseX = mouseX - x1;
			int cMouseY = mouseY - y1;
			popup.render(context, cMouseX, cMouseY);
			
			matrixStack.pop();
		}
	}
	
	public void renderTooltip(DrawContext context, int mouseX, int mouseY)
	{
		MatrixStack matrixStack = context.getMatrices();
		
		if(tooltip.isEmpty())
			return;
		
		String[] lines = tooltip.split("\n");
		TextRenderer tr = MC.textRenderer;
		
		int tw = 0;
		int th = lines.length * tr.fontHeight;
		for(String line : lines)
		{
			int lw = tr.getWidth(line);
			if(lw > tw)
				tw = lw;
		}
		int sw = MC.currentScreen.width;
		int sh = MC.currentScreen.height;
		
		int xt1 = mouseX + tw + 11 <= sw ? mouseX + 8 : mouseX - tw - 8;
		int xt2 = xt1 + tw + 3;
		int yt1 = mouseY + th - 2 <= sh ? mouseY - 4 : mouseY - th - 4;
		int yt2 = yt1 + th + 2;
		
		matrixStack.push();
		matrixStack.translate(0, 0, 300);
		
		// shadow
		RenderUtils.drawRoundedShadow2D(context, xt1, yt1, xt2, yt2, 4,
			0x66000000);
		
		// background
		RenderUtils.fillRoundedRect(context, xt1, yt1, xt2, yt2, 4,
			RenderUtils.toIntColor(RenderUtils.lighten(bgColor, 0.05F),
				Math.min(1F, ttOpacity + 0.15F)));
		
		// outline
		RenderUtils.drawRoundedBorder2D(context, xt1, yt1, xt2, yt2, 4,
			getStroke(0.9F));
		
		// text
		for(int i = 0; i < lines.length; i++)
			context.drawText(tr, lines[i], xt1 + 2, yt1 + 2 + i * tr.fontHeight,
				txtColor, false);
		
		matrixStack.pop();
	}
	
	public void renderPinnedWindows(DrawContext context, float partialTicks)
	{
		MatrixStack matrixStack = context.getMatrices();
		matrixStack.push();
		
		for(Window window : windows)
			if(window.isPinned() && !window.isInvisible())
			{
				matrixStack.translate(0, 0, 300);
				renderWindow(context, window, Integer.MIN_VALUE,
					Integer.MIN_VALUE, partialTicks);
			}
		
		matrixStack.pop();
	}
	
	/**
	 * Renders a single window at the front. Used by screens that let the user
	 * move pinned windows such as the Radar.
	 */
	public void renderWindowPreview(DrawContext context, Window window,
		int mouseX, int mouseY, float partialTicks)
	{
		updateColors();
		
		MatrixStack matrixStack = context.getMatrices();
		matrixStack.push();
		matrixStack.translate(0, 0, 300);
		renderWindow(context, window, mouseX, mouseY, partialTicks);
		matrixStack.pop();
	}
	
	public void updateColors()
	{
		ClickGuiHack clickGui = HARD.getHax().clickGuiHack;
		
		opacity = clickGui.getOpacity();
		ttOpacity = clickGui.getTooltipOpacity();
		bgColor = clickGui.getBackgroundColor();
		txtColor = clickGui.getTextColor();
		maxHeight = clickGui.getMaxHeight();
		maxSettingsHeight = clickGui.getMaxSettingsHeight();
		
		if(HardClient.INSTANCE.getHax().rainbowUiHack.isEnabled())
			acColor = RenderUtils.getRainbowColor();
		else
			acColor = clickGui.getAccentColor();
	}
	
	private static float getOrbitProgress()
	{
		return (System.currentTimeMillis() % ORBIT_PERIOD_MS)
			/ (float)ORBIT_PERIOD_MS;
	}
	
	/**
	 * Returns the accent color, brightened if necessary so that it stays
	 * visible on top of the black GUI.
	 */
	public float[] getAccent()
	{
		float luma =
			0.2126F * acColor[0] + 0.7152F * acColor[1] + 0.0722F * acColor[2];
		
		if(luma >= 0.32F)
			return acColor;
		
		return RenderUtils.lighten(acColor, Math.min(0.6F, 0.32F - luma));
	}
	
	/**
	 * Returns the color used for raised surfaces such as header bars, rows and
	 * buttons.
	 */
	public int getSurface(float brightness, float alpha)
	{
		return RenderUtils.toIntColor(RenderUtils.lighten(bgColor, brightness),
			alpha);
	}
	
	/**
	 * Returns the color used for the outlines around windows and components.
	 */
	public int getStroke(float alpha)
	{
		return RenderUtils.toIntColor(RenderUtils.lighten(bgColor, 0.26F),
			alpha);
	}
	
	private void renderWindow(DrawContext context, Window window, int mouseX,
		int mouseY, float partialTicks)
	{
		int x1 = window.getX();
		int y1 = window.getY();
		int y3 = y1 + TITLE_HEIGHT;
		
		// the window's height can change while validating, so this has to
		// happen before the coordinates are calculated
		if(!window.isMinimized())
		{
			window.setMaxHeight(window instanceof SettingsWindow
				? maxSettingsHeight : maxHeight);
			window.validate();
		}
		
		int x2 = x1 + window.getWidth();
		int y2 = window.isMinimized() ? y3 : y1 + window.getHeight();
		
		if(mouseX >= x1 && mouseY >= y1 && mouseX < x2 && mouseY < y2)
			tooltip = "";
		
		boolean hoveringWindow =
			mouseX >= x1 && mouseY >= y1 && mouseX < x2 && mouseY < y3;
		
		int windowBgColor = RenderUtils.toIntColor(bgColor, opacity);
		int headerTop = getSurface(hoveringWindow ? 0.20F : 0.15F,
			Math.min(1F, opacity + 0.05F));
		int headerBottom = getSurface(0.06F, Math.min(1F, opacity + 0.05F));
		int strokeColor = getStroke(Math.min(1F, opacity + 0.2F));
		int accentFaded = RenderUtils.toIntColor(getAccent(), 0.40F);
		
		// shadow and body
		RenderUtils.drawRoundedShadow2D(context, x1, y1, x2, y2, CORNER_RADIUS,
			0x55000000);
		RenderUtils.fillRoundedRect(context, x1, y1, x2, y2, CORNER_RADIUS,
			windowBgColor);
		
		if(!window.isMinimized())
		{
			// children
			int y4 = y3 + window.getScrollOffset();
			
			context.enableScissor(x1, y3, x2, y2);
			
			MatrixStack matrixStack = context.getMatrices();
			matrixStack.push();
			matrixStack.translate(x1, y4, 0);
			
			int cMouseX = mouseX - x1;
			int cMouseY = mouseY - y4;
			for(int i = 0; i < window.countChildren(); i++)
				window.getChild(i).render(context, cMouseX, cMouseY,
					partialTicks);
			
			matrixStack.pop();
			context.disableScissor();
			
			// scrollbar
			if(window.isScrollingEnabled())
				renderScrollbar(context, window, x2, y2, y3, mouseX, mouseY);
		}
		
		// header
		if(window.isMinimized())
			RenderUtils.fillRoundedRectGradient(context, x1, y1, x2, y3,
				CORNER_RADIUS, headerTop, headerBottom);
		else
			RenderUtils.fillRoundedTopGradient(context, x1, y1, x2, y3,
				CORNER_RADIUS, headerTop, headerBottom);
		
		// accent line underneath the header
		if(!window.isMinimized())
			RenderUtils.fill2D(context, x1 + CORNER_RADIUS, y3 - 1,
				x2 - CORNER_RADIUS, y3, accentFaded);
		
		// outline
		RenderUtils.drawRoundedBorder2D(context, x1, y1, x2, y2, CORNER_RADIUS,
			strokeColor);
		
		// title bar buttons
		int x3 = x2;
		int y4 = y1 + 2;
		int y5 = y3 - 2;
		boolean hoveringY = mouseY >= y4 && mouseY < y5;
		
		if(window.isClosable())
		{
			x3 -= 11;
			int x4 = x3 + 9;
			boolean hovering = hoveringY && mouseX >= x3 && mouseX < x4;
			renderTitleBarButton(context, x3, y4, x4, y5, hovering);
			ClickGuiIcons.drawCross(context, x3, y4, x4, y5, hovering);
		}
		
		if(window.isPinnable())
		{
			x3 -= 11;
			int x4 = x3 + 9;
			boolean hovering = hoveringY && mouseX >= x3 && mouseX < x4;
			renderTitleBarButton(context, x3, y4, x4, y5, hovering);
			ClickGuiIcons.drawPin(context, x3, y4, x4, y5, hovering,
				window.isPinned());
		}
		
		if(window.isMinimizable())
		{
			x3 -= 11;
			int x4 = x3 + 9;
			boolean hovering = hoveringY && mouseX >= x3 && mouseX < x4;
			renderTitleBarButton(context, x3, y4, x4, y5, hovering);
			ClickGuiIcons.drawMinimizeArrow(context, x3, y4, x4, y5, hovering,
				window.isMinimized());
		}
		
		// title
		TextRenderer tr = MC.textRenderer;
		String title =
			tr.trimToWidth(Text.literal(window.getTitle()), x3 - x1 - 12)
				.getString();
		
		// small accent dot in front of the title
		RenderUtils.fillRoundedRect(context, x1 + 4, y1 + 5, x1 + 7, y1 + 8, 1,
			RenderUtils.toIntColor(getAccent(), 1));
		context.drawText(tr, title, x1 + 9, y1 + (TITLE_HEIGHT - 8) / 2,
			txtColor, false);
		
		// orbiting bar: a dark groove with a bright bar running through it
		float progress = getOrbitProgress();
		RenderUtils.drawOrbitBar2D(context, x1, y1, x2, y2, CORNER_RADIUS,
			ORBIT_THICKNESS + 2, progress,
			RenderUtils.toIntColor(new float[]{0, 0, 0}, 0.5F));
		RenderUtils.drawOrbitBar2D(context, x1, y1, x2, y2, CORNER_RADIUS,
			ORBIT_THICKNESS, progress, RenderUtils.blend(
				RenderUtils.toIntColor(getAccent(), 1), 0xFFFFFFFF, 0.12F));
	}
	
	private void renderScrollbar(DrawContext context, Window window, int x2,
		int y2, int y3, int mouseX, int mouseY)
	{
		double outerHeight = y2 - y3;
		double innerHeight = window.getInnerHeight();
		if(outerHeight <= 0 || innerHeight <= 0)
			return;
		
		double maxScrollbarHeight = outerHeight - 3;
		double scrollbarY =
			outerHeight * (-window.getScrollOffset() / innerHeight) + 1;
		double scrollbarHeight = maxScrollbarHeight * outerHeight / innerHeight;
		
		int xs = x2 - 3;
		int ys3 = y3 + (int)scrollbarY;
		int ys4 = Math.min(y2 - 2, ys3 + Math.max(6, (int)scrollbarHeight));
		if(ys4 <= ys3)
			return;
		
		boolean hovering =
			mouseX >= xs - 1 && mouseY >= ys3 && mouseX < x2 && mouseY < ys4;
		
		// track
		RenderUtils.fillRoundedRect(context, xs - 1, y3 + 1, xs + 2, y2 - 1, 1,
			getSurface(0.05F, opacity * 0.6F));
		
		// thumb
		RenderUtils.fillRoundedRect(context, xs, ys3, xs + 2, ys4, 1,
			RenderUtils.toIntColor(getAccent(), hovering ? 0.95F : 0.55F));
	}
	
	private void renderTitleBarButton(DrawContext context, int x1, int y1,
		int x2, int y2, boolean hovering)
	{
		// button background
		RenderUtils.fillRoundedRect(context, x1, y1, x2, y2, 2,
			getSurface(hovering ? 0.22F : 0.09F, opacity));
		
		// button outline
		if(hovering)
			RenderUtils.drawRoundedBorder2D(context, x1, y1, x2, y2, 2,
				RenderUtils.toIntColor(getAccent(), 0.7F));
	}
	
	public float[] getBgColor()
	{
		return bgColor;
	}
	
	public float[] getAcColor()
	{
		return acColor;
	}
	
	public int getTxtColor()
	{
		return txtColor;
	}
	
	public float getOpacity()
	{
		return opacity;
	}
	
	public float getTooltipOpacity()
	{
		return ttOpacity;
	}
	
	public void setTooltip(String tooltip)
	{
		this.tooltip = Objects.requireNonNull(tooltip);
	}
	
	public void addWindow(Window window)
	{
		windows.add(window);
	}
	
	public void addPopup(Popup popup)
	{
		popups.add(popup);
	}
	
	public boolean isLeftMouseButtonPressed()
	{
		return leftMouseButtonPressed;
	}
}
