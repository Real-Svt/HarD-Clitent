/*
 * Copyright (c) 2014-2026 Hard-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.hardclient.hacks;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.stream.Collectors;

import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.block.Block;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.hardclient.Category;
import net.hardclient.SearchTags;
import net.hardclient.HardRenderLayers;
import net.hardclient.events.PacketInputListener;
import net.hardclient.events.RenderListener;
import net.hardclient.events.UpdateListener;
import net.hardclient.hack.Hack;
import net.hardclient.settings.BlockSetting;
import net.hardclient.settings.ChunkAreaSetting;
import net.hardclient.settings.SliderSetting;
import net.hardclient.settings.SliderSetting.ValueDisplay;
import net.hardclient.util.BlockVertexCompiler;
import net.hardclient.util.ChatUtils;
import net.hardclient.util.EasyVertexBuffer;
import net.hardclient.util.RegionPos;
import net.hardclient.util.RenderUtils;
import net.hardclient.util.RotationUtils;
import net.hardclient.util.chunk.ChunkSearcher;
import net.hardclient.util.chunk.ChunkSearcherCoordinator;

@SearchTags({"BlockESP", "block esp"})
public final class SearchHack extends Hack
	implements UpdateListener, RenderListener
{
	private final BlockSetting block = new BlockSetting("Block",
		"The type of block to search for.", "minecraft:diamond_ore", false);
	private Block lastBlock;
	
	private final ChunkAreaSetting area = new ChunkAreaSetting("Area",
		"The area around the player to search in.\n"
			+ "Higher values require a faster computer.");
	
	private final SliderSetting limit = new SliderSetting("Limit",
		"The maximum number of blocks to display.\n"
			+ "Higher values require a faster computer.",
		4, 3, 6, 1, ValueDisplay.LOGARITHMIC);
	private int prevLimit;
	private boolean notify;
	
	private final ChunkSearcherCoordinator coordinator =
		new ChunkSearcherCoordinator(area);
	
	private ForkJoinPool forkJoinPool;
	private ForkJoinTask<HashSet<BlockPos>> getMatchingBlocksTask;
	private ForkJoinTask<ArrayList<int[]>> compileVerticesTask;
	
	private EasyVertexBuffer vertexBuffer;
	private RegionPos bufferRegion;
	private boolean bufferUpToDate;
	
	public SearchHack()
	{
		super("Search");
		setCategory(Category.RENDER);
		addSetting(block);
		addSetting(area);
		addSetting(limit);
	}
	
	@Override
	public String getRenderName()
	{
		return getName() + " [" + block.getBlockName().replace("minecraft:", "")
			+ "]";
	}
	
	@Override
	protected void onEnable()
	{
		lastBlock = block.getBlock();
		coordinator.setTargetBlock(lastBlock);
		prevLimit = limit.getValueI();
		notify = true;
		
		forkJoinPool = new ForkJoinPool();
		
		bufferUpToDate = false;
		
		EVENTS.add(UpdateListener.class, this);
		EVENTS.add(PacketInputListener.class, coordinator);
		EVENTS.add(RenderListener.class, this);
	}
	
	@Override
	protected void onDisable()
	{
		EVENTS.remove(UpdateListener.class, this);
		EVENTS.remove(PacketInputListener.class, coordinator);
		EVENTS.remove(RenderListener.class, this);
		
		stopBuildingBuffer();
		coordinator.reset();
		forkJoinPool.shutdownNow();
		
		if(vertexBuffer != null)
			vertexBuffer.close();
		vertexBuffer = null;
		bufferRegion = null;
	}
	
	@Override
	public void onUpdate()
	{
		boolean searchersChanged = false;
		
		// clear ChunkSearchers if block has changed
		Block currentBlock = block.getBlock();
		if(currentBlock != lastBlock)
		{
			lastBlock = currentBlock;
			coordinator.setTargetBlock(lastBlock);
			searchersChanged = true;
		}
		
		if(coordinator.update())
			searchersChanged = true;
		
		if(searchersChanged)
			stopBuildingBuffer();
		
		if(!coordinator.isDone())
			return;
		
		// check if limit has changed
		if(limit.getValueI() != prevLimit)
		{
			stopBuildingBuffer();
			prevLimit = limit.getValueI();
			notify = true;
		}
		
		// build the buffer
		
		if(getMatchingBlocksTask == null)
			startGetMatchingBlocksTask();
		
		if(!getMatchingBlocksTask.isDone())
			return;
		
		if(compileVerticesTask == null)
			startCompileVerticesTask();
		
		if(!compileVerticesTask.isDone())
			return;
		
		if(!bufferUpToDate)
			setBufferFromTask();
	}
	
	@Override
	public void onRender(MatrixStack matrixStack, float partialTicks)
	{
		if(vertexBuffer == null || bufferRegion == null)
			return;
		
		RenderSystem.enableDepthTest();
		RenderSystem.depthFunc(GlConst.GL_ALWAYS);
		
		float[] rainbow = RenderUtils.getRainbowColor();
		RenderUtils.setShaderColor(rainbow, 0.5F);
		
		matrixStack.push();
		RenderUtils.applyRegionalRenderOffset(matrixStack, bufferRegion);
		
		vertexBuffer.draw(matrixStack, HardRenderLayers.ESP_QUADS);
		
		matrixStack.pop();
		
		RenderSystem.setShaderColor(1, 1, 1, 1);
	}
	
	private void stopBuildingBuffer()
	{
		if(getMatchingBlocksTask != null)
			getMatchingBlocksTask.cancel(true);
		getMatchingBlocksTask = null;
		
		if(compileVerticesTask != null)
			compileVerticesTask.cancel(true);
		compileVerticesTask = null;
		
		bufferUpToDate = false;
	}
	
	private void startGetMatchingBlocksTask()
	{
		BlockPos eyesPos = BlockPos.ofFloored(RotationUtils.getEyesPos());
		Comparator<BlockPos> comparator =
			Comparator.comparingInt(pos -> eyesPos.getManhattanDistance(pos));
		
		getMatchingBlocksTask = forkJoinPool.submit(() -> coordinator
			.getMatches().parallel().map(ChunkSearcher.Result::pos)
			.sorted(comparator).limit(limit.getValueLog())
			.collect(Collectors.toCollection(HashSet::new)));
	}
	
	private void startCompileVerticesTask()
	{
		HashSet<BlockPos> matchingBlocks = getMatchingBlocksTask.join();
		
		if(matchingBlocks.size() < limit.getValueLog())
			notify = true;
		else if(notify)
		{
			ChatUtils.warning("Search found \u00a7lA LOT\u00a7r of blocks!"
				+ " To prevent lag, it will only show the closest \u00a76"
				+ limit.getValueString() + "\u00a7r results.");
			notify = false;
		}
		
		compileVerticesTask = forkJoinPool
			.submit(() -> BlockVertexCompiler.compile(matchingBlocks));
	}
	
	private void setBufferFromTask()
	{
		ArrayList<int[]> vertices = compileVerticesTask.join();
		RegionPos region = RenderUtils.getCameraRegion();
		
		if(vertexBuffer != null)
			vertexBuffer.close();
		
		vertexBuffer = EasyVertexBuffer.createAndUpload(DrawMode.QUADS,
			VertexFormats.POSITION_COLOR, buffer -> {
				for(int[] vertex : vertices)
					buffer.vertex(vertex[0] - region.x(), vertex[1],
						vertex[2] - region.z()).color(0xFFFFFFFF).next();
			});
		
		bufferUpToDate = true;
		bufferRegion = region;
	}
}
