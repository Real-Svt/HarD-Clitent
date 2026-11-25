/*
 * Copyright (c) 2014-2026 Hard-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.hardclient.hud;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import net.hardclient.HardClient;
import net.hardclient.other_features.HardLogoOtf;
import net.hardclient.util.RenderUtils;

public final class HardLogo
{
	private static final HardClient HARD = HardClient.INSTANCE;
	private static final Identifier LOGO_TEXTURE =
		new Identifier("hard", "hard_logo.png");
	private static final int TEXTURE_SIZE = 64;
	private static final int LOGO_SIZE = 16;
	
	public void render(DrawContext context)
	{
		HardLogoOtf otf = HARD.getOtfs().hardLogoOtf;
		if(!otf.isVisible())
			return;
		
		String version = getVersionString();
		TextRenderer tr = HardClient.MC.textRenderer;
		
		int top = 2;
		int bottom = top + LOGO_SIZE + 2;
		int logoX = 2;
		int logoY = top + (bottom - top - LOGO_SIZE) / 2;
		int textX = logoX + LOGO_SIZE + 4;
		
		// background
		int bgColor;
		if(HARD.getHax().rainbowUiHack.isEnabled())
			bgColor = RenderUtils.toIntColor(HARD.getGui().getAcColor(), 0.5F);
		else
			bgColor = otf.getBackgroundColor();
		context.fill(0, top, textX + tr.getWidth(version) + 4, bottom, bgColor);
		
		// version string
		int textColor = otf.getTextColor();
		context.drawText(tr, version, textX,
			top + (bottom - top - tr.fontHeight) / 2, textColor, false);
		
		// Hard logo, tinted like the text so it stays visible on the bar
		drawLogo(context, logoX, logoY, LOGO_SIZE, textColor);
	}
	
	/**
	 * Draws the logo as a square of the given size, so that its width and
	 * height always stay consistent with its shape.
	 */
	public static void drawLogo(DrawContext context, int x, int y, int size,
		int argb)
	{
		float red = (argb >> 16 & 0xFF) / 255F;
		float green = (argb >> 8 & 0xFF) / 255F;
		float blue = (argb & 0xFF) / 255F;
		float alpha = (argb >> 24 & 0xFF) / 255F;
		float scale = size / (float)TEXTURE_SIZE;
		
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShaderColor(red, green, blue, alpha);
		context.getMatrices().push();
		context.getMatrices().translate(x, y, 0);
		context.getMatrices().scale(scale, scale, 1);
		context.drawTexture(LOGO_TEXTURE, 0, 0, 0F, 0F, TEXTURE_SIZE,
			TEXTURE_SIZE, TEXTURE_SIZE, TEXTURE_SIZE);
		context.getMatrices().pop();
		RenderSystem.setShaderColor(1, 1, 1, 1);
	}
	
	private String getVersionString()
	{
		String version = "v" + HardClient.VERSION;
		version += " MC" + HardClient.MC_VERSION;
		
		if(HARD.getUpdater().isOutdated())
			version += " (outdated)";
		
		return version;
	}
}
