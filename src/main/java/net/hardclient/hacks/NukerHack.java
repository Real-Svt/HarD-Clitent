/*
 * Copyright (c) 2014-2026 Hard-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.hardclient.hacks;

import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Stream;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.hardclient.Category;
import net.hardclient.events.LeftClickListener;
import net.hardclient.events.RenderListener;
import net.hardclient.events.UpdateListener;
import net.hardclient.hack.Hack;
import net.hardclient.hacks.nukers.CommonNukerSettings;
import net.hardclient.settings.SliderSetting;
import net.hardclient.settings.SliderSetting.ValueDisplay;
import net.hardclient.settings.SwingHandSetting;
import net.hardclient.settings.SwingHandSetting.SwingHand;
import net.hardclient.util.BlockBreaker;
import net.hardclient.util.BlockBreaker.BlockBreakingParams;
import net.hardclient.util.BlockBreakingCache;
import net.hardclient.util.BlockUtils;
import net.hardclient.util.OverlayRenderer;
import net.hardclient.util.RotationUtils;

public final class NukerHack extends Hack
	implements UpdateListener, RenderListener
{
	private final SliderSetting range =
		new SliderSetting("Range", 5, 1, 6, 0.05, ValueDisplay.DECIMAL);
	
	private final CommonNukerSettings commonSettings =
		new CommonNukerSettings();
	
	private final SwingHandSetting swingHand = new SwingHandSetting(
		SwingHandSetting.genericMiningDescription(this), SwingHand.SERVER);
	
	private final BlockBreakingCache cache = new BlockBreakingCache();
	private final OverlayRenderer overlay = new OverlayRenderer();
	private BlockPos currentBlock;
	
	public NukerHack()
	{
		super("Nuker");
		setCategory(Category.BLOCKS);
		addSetting(range);
		commonSettings.getSettings().forEach(this::addSetting);
		addSetting(swingHand);
	}
	
	@Override
	public String getRenderName()
	{
		return getName() + commonSettings.getRenderNameSuffix();
	}
	
	@Override
	protected void onEnable()
	{
		HARD.getHax().autoMineHack.setEnabled(false);
		HARD.getHax().excavatorHack.setEnabled(false);
		HARD.getHax().nukerLegitHack.setEnabled(false);
		HARD.getHax().speedNukerHack.setEnabled(false);
		HARD.getHax().tunnellerHack.setEnabled(false);
		HARD.getHax().veinMinerHack.setEnabled(false);
		
		EVENTS.add(UpdateListener.class, this);
		EVENTS.add(LeftClickListener.class, commonSettings);
		EVENTS.add(RenderListener.class, this);
	}
	
	@Override
	protected void onDisable()
	{
		EVENTS.remove(UpdateListener.class, this);
		EVENTS.remove(LeftClickListener.class, commonSettings);
		EVENTS.remove(RenderListener.class, this);
		
		if(currentBlock != null)
		{
			MC.interactionManager.breakingBlock = true;
			MC.interactionManager.cancelBlockBreaking();
			currentBlock = null;
		}
		
		cache.reset();
		overlay.resetProgress();
		commonSettings.reset();
	}
	
	@Override
	public void onUpdate()
	{
		currentBlock = null;
		
		if(MC.options.attackKey.isPressed() || commonSettings.isIdModeWithAir())
			return;
		
		Vec3d eyesVec = RotationUtils.getEyesPos();
		BlockPos eyesBlock = BlockPos.ofFloored(eyesVec);
		double rangeSq = range.getValueSq();
		int blockRange = range.getValueCeil();
		
		Stream<BlockBreakingParams> stream = BlockUtils
			.getAllInBoxStream(eyesBlock, blockRange)
			.filter(commonSettings::shouldBreakBlock)
			.map(BlockBreaker::getBlockBreakingParams).filter(Objects::nonNull);
		
		if(commonSettings.isSphereShape())
			stream = stream.filter(params -> params.distanceSq() <= rangeSq);
		
		stream = stream.sorted(BlockBreaker.comparingParams());
		
		// Break all blocks in creative mode
		if(MC.player.getAbilities().creativeMode)
		{
			MC.interactionManager.cancelBlockBreaking();
			overlay.resetProgress();
			
			ArrayList<BlockPos> blocks = cache
				.filterOutRecentBlocks(stream.map(BlockBreakingParams::pos));
			if(blocks.isEmpty())
				return;
			
			currentBlock = blocks.get(0);
			BlockBreaker.breakBlocksWithPacketSpam(blocks);
			swingHand.swing(Hand.MAIN_HAND);
			return;
		}
		
		// Break the first valid block in survival mode
		currentBlock = stream.filter(this::breakOneBlock)
			.map(BlockBreakingParams::pos).findFirst().orElse(null);
		
		if(currentBlock == null)
		{
			MC.interactionManager.cancelBlockBreaking();
			overlay.resetProgress();
			return;
		}
		
		overlay.updateProgress();
	}
	
	private boolean breakOneBlock(BlockBreakingParams params)
	{
		HARD.getRotationFaker().faceVectorPacket(params.hitVec());
		
		if(!MC.interactionManager.updateBlockBreakingProgress(params.pos(),
			params.side()))
			return false;
		
		swingHand.swing(Hand.MAIN_HAND);
		return true;
	}
	
	@Override
	public void onRender(MatrixStack matrixStack, float partialTicks)
	{
		overlay.render(matrixStack, partialTicks, currentBlock);
	}
}
