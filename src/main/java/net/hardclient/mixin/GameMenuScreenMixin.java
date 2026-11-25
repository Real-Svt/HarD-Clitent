/*
 * Copyright (c) 2014-2026 Hard-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.hardclient.mixin;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.hardclient.HardClient;
import net.hardclient.hud.HardLogo;
import net.hardclient.options.HardOptionsScreen;

@Mixin(GameMenuScreen.class)
public abstract class GameMenuScreenMixin extends Screen
{
	@Unique
	private static final int LOGO_SIZE = 16;
	
	@Unique
	private ButtonWidget hardOptionsButton;
	
	private GameMenuScreenMixin(HardClient hard, Text title)
	{
		super(title);
	}
	
	@Inject(at = @At("TAIL"), method = "initWidgets()V")
	private void onInitWidgets(CallbackInfo ci)
	{
		if(!HardClient.INSTANCE.isEnabled())
			return;
		
		addHardOptionsButton();
	}
	
	@Inject(at = @At("TAIL"),
		method = "render(Lnet/minecraft/client/gui/DrawContext;IIF)V")
	private void onRender(DrawContext context, int mouseX, int mouseY,
		float partialTicks, CallbackInfo ci)
	{
		if(!HardClient.INSTANCE.isEnabled() || hardOptionsButton == null)
			return;
		
		int textWidth = textRenderer.getWidth(hardOptionsButton.getMessage());
		int textX = hardOptionsButton.getX()
			+ (hardOptionsButton.getWidth() - textWidth) / 2;
		int x = textX - LOGO_SIZE - 4;
		int y = hardOptionsButton.getY()
			+ (hardOptionsButton.getHeight() - LOGO_SIZE) / 2;
		
		HardLogo.drawLogo(context, x, y, LOGO_SIZE, 0xFFFFFFFF);
	}
	
	@Unique
	private void addHardOptionsButton()
	{
		List<ClickableWidget> buttons = Screens.getButtons(this);
		
		// Fallback position
		int buttonX = width / 2 - 102;
		int buttonY = 60;
		int buttonWidth = 204;
		int buttonHeight = 20;
		
		for(ClickableWidget button : buttons)
		{
			// If feedback button exists, use its position
			if(isTrKey(button, "menu.sendFeedback")
				|| isTrKey(button, "menu.feedback"))
			{
				buttonY = button.getY();
				break;
			}
			
			// If options button exists, go 24px above it
			if(isTrKey(button, "menu.options"))
			{
				buttonY = button.getY() - 24;
				break;
			}
		}
		
		// Clear required space for Hard Options
		hideFeedbackReportAndServerLinksButtons();
		ensureSpaceAvailable(buttonX, buttonY, buttonWidth, buttonHeight);
		
		// Create Hard Options button
		MutableText buttonText = Text.literal("Options");
		hardOptionsButton = ButtonWidget
			.builder(buttonText, b -> openHardOptions())
			.dimensions(buttonX, buttonY, buttonWidth, buttonHeight).build();
		buttons.add(hardOptionsButton);
	}
	
	@Unique
	private void hideFeedbackReportAndServerLinksButtons()
	{
		for(ClickableWidget button : Screens.getButtons(this))
			if(isTrKey(button, "menu.sendFeedback")
				|| isTrKey(button, "menu.reportBugs")
				|| isTrKey(button, "menu.feedback")
				|| isTrKey(button, "menu.server_links"))
				button.visible = false;
	}
	
	@Unique
	private void ensureSpaceAvailable(int x, int y, int width, int height)
	{
		// Check if there are any buttons in the way
		ArrayList<ClickableWidget> buttonsInTheWay = new ArrayList<>();
		for(ClickableWidget button : Screens.getButtons(this))
		{
			if(button.getX() + button.getWidth() < x
				|| button.getX() > x + width
				|| button.getY() + button.getHeight() < y
				|| button.getY() > y + height)
				continue;
			
			if(!button.visible)
				continue;
			
			buttonsInTheWay.add(button);
		}
		
		// If not, we're done
		if(buttonsInTheWay.isEmpty())
			return;
		
		// If yes, clear space below and move the buttons there
		ensureSpaceAvailable(x, y + 24, width, height);
		for(ClickableWidget button : buttonsInTheWay)
			button.setY(button.getY() + 24);
	}
	
	@Unique
	private void openHardOptions()
	{
		client.setScreen(new HardOptionsScreen(this));
	}
	
	@Unique
	private boolean isTrKey(ClickableWidget button, String key)
	{
		String message = button.getMessage().getString();
		return message != null && message.equals(I18n.translate(key));
	}
}
