/*
 * Copyright (c) 2014-2026 Hard-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.hardclient.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.StatsListener;
import net.minecraft.client.gui.screen.StatsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.Text;
import net.hardclient.HardClient;

@Mixin(StatsScreen.class)
public abstract class StatsScreenMixin extends Screen implements StatsListener
{
	private StatsScreenMixin(HardClient hard, Text title)
	{
		super(title);
	}
	
	@Inject(at = @At("TAIL"), method = "createButtons()V")
	private void onCreateButtons(CallbackInfo ci)
	{
		if(HardClient.INSTANCE.getOtfs().disableOtf.shouldHideEnableButton())
			return;
		
		ButtonWidget toggleHardButton =
			ButtonWidget.builder(Text.literal(""), this::toggleHard)
				.dimensions(width / 2 - 152, height - 28, 150, 20).build();
		
		updateHardButtonText(toggleHardButton);
		addDrawableChild(toggleHardButton);
		
		for(ClickableWidget button : Screens.getButtons(this))
		{
			if(!button.getMessage().getString()
				.equals(I18n.translate("gui.done")))
				continue;
			
			button.setX(width / 2 + 2);
			button.setWidth(150);
		}
	}
	
	private void toggleHard(ButtonWidget button)
	{
		HardClient hard = HardClient.INSTANCE;
		hard.setEnabled(!hard.isEnabled());
		
		updateHardButtonText(button);
	}
	
	private void updateHardButtonText(ButtonWidget button)
	{
		HardClient hard = HardClient.INSTANCE;
		String text = (hard.isEnabled() ? "Disable" : "Enable") + " Hard";
		button.setMessage(Text.literal(text));
	}
}
