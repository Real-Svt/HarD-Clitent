/*
 * Copyright (c) 2014-2026 Hard-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.hardclient.hacks;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.hardclient.Category;
import net.hardclient.events.RenderListener;
import net.hardclient.events.UpdateListener;
import net.hardclient.hack.Hack;
import net.hardclient.util.OverlayRenderer;

public final class OverlayHack extends Hack
	implements UpdateListener, RenderListener
{
	private final OverlayRenderer renderer = new OverlayRenderer();
	
	public OverlayHack()
	{
		super("Overlay");
		setCategory(Category.RENDER);
	}
	
	@Override
	protected void onEnable()
	{
		EVENTS.add(UpdateListener.class, this);
		EVENTS.add(RenderListener.class, this);
	}
	
	@Override
	protected void onDisable()
	{
		EVENTS.remove(UpdateListener.class, this);
		EVENTS.remove(RenderListener.class, this);
		renderer.resetProgress();
	}
	
	@Override
	public void onUpdate()
	{
		if(MC.interactionManager.isBreakingBlock())
			renderer.updateProgress();
		else
			renderer.resetProgress();
	}
	
	@Override
	public void onRender(MatrixStack matrixStack, float partialTicks)
	{
		if(!MC.interactionManager.isBreakingBlock())
			return;
		
		if(!(MC.crosshairTarget instanceof BlockHitResult blockHitResult)
			|| blockHitResult.getType() != HitResult.Type.BLOCK)
			return;
		
		renderer.render(matrixStack, partialTicks,
			blockHitResult.getBlockPos());
	}
}
