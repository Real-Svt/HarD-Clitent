/*
 * Copyright (c) 2014-2026 Hard-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.hardclient.util;

import java.util.List;
import java.util.OptionalDouble;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.block.Blocks;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.MatrixStack.Entry;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.Chunk;
import net.hardclient.HardClient;
import net.hardclient.HardRenderLayers;

public enum RenderUtils
{
	;
	
	/**
	 * Similar to {@link RenderLayer#getDebugLineStrip(double)}, but as a
	 * non-srip version with support for transparency.
	 *
	 * @implNote Just like {@link RenderLayer#getDebugLineStrip(double)}, this
	 *           layer doesn't support any other line width than 1px. Changing
	 *           the line width number does nothing.
	 */
	public static final RenderLayer.MultiPhase ONE_PIXEL_LINES =
		RenderLayer.of("hard:1px_lines", VertexFormats.POSITION_COLOR,
			DrawMode.DEBUG_LINES, 256,
			RenderLayer.MultiPhaseParameters.builder()
				.program(RenderLayer.COLOR_PROGRAM)
				.lineWidth(new RenderPhase.LineWidth(OptionalDouble.of(1)))
				.transparency(RenderLayer.TRANSLUCENT_TRANSPARENCY)
				.cull(RenderLayer.DISABLE_CULLING).build(false));
	
	/**
	 * Similar to {@link RenderLayer#getDebugLineStrip(double)}, but with
	 * support for transparency.
	 *
	 * @implNote Just like {@link RenderLayer#getDebugLineStrip(double)}, this
	 *           layer doesn't support any other line width than 1px. Changing
	 *           the line width number does nothing.
	 */
	public static final RenderLayer.MultiPhase ONE_PIXEL_LINE_STRIP =
		RenderLayer.of("hard:1px_line_strip", VertexFormats.POSITION_COLOR,
			DrawMode.DEBUG_LINE_STRIP, 256,
			RenderLayer.MultiPhaseParameters.builder()
				.program(RenderLayer.COLOR_PROGRAM)
				.lineWidth(new RenderPhase.LineWidth(OptionalDouble.of(1)))
				.transparency(RenderLayer.TRANSLUCENT_TRANSPARENCY)
				.cull(RenderLayer.DISABLE_CULLING).build(false));
	
	public static void applyRegionalRenderOffset(MatrixStack matrixStack)
	{
		applyRegionalRenderOffset(matrixStack, getCameraRegion());
	}
	
	public static void applyRegionalRenderOffset(MatrixStack matrixStack,
		Chunk chunk)
	{
		applyRegionalRenderOffset(matrixStack, RegionPos.of(chunk.getPos()));
	}
	
	public static void applyRegionalRenderOffset(MatrixStack matrixStack,
		RegionPos region)
	{
		Vec3d offset = region.toVec3d().subtract(getCameraPos());
		matrixStack.translate(offset.x, offset.y, offset.z);
	}
	
	public static void applyRenderOffset(MatrixStack matrixStack)
	{
		Vec3d camPos = getCameraPos();
		matrixStack.translate(-camPos.x, -camPos.y, -camPos.z);
	}
	
	public static Vec3d getCameraPos()
	{
		Camera camera = HardClient.MC.getBlockEntityRenderDispatcher().camera;
		if(camera == null)
			return Vec3d.ZERO;
		
		return camera.getPos();
	}
	
	public static BlockPos getCameraBlockPos()
	{
		Camera camera = HardClient.MC.getBlockEntityRenderDispatcher().camera;
		if(camera == null)
			return BlockPos.ORIGIN;
		
		return camera.getBlockPos();
	}
	
	public static RegionPos getCameraRegion()
	{
		return RegionPos.of(getCameraBlockPos());
	}
	
	public static VertexConsumerProvider.Immediate getVCP()
	{
		return HardClient.MC.getBufferBuilders().getEntityVertexConsumers();
	}
	
	public static float[] getRainbowColor()
	{
		float x = System.currentTimeMillis() % 2000 / 1000F;
		float pi = (float)Math.PI;
		
		float[] rainbow = new float[3];
		rainbow[0] = 0.5F + 0.5F * MathHelper.sin(x * pi);
		rainbow[1] = 0.5F + 0.5F * MathHelper.sin((x + 4F / 3F) * pi);
		rainbow[2] = 0.5F + 0.5F * MathHelper.sin((x + 8F / 3F) * pi);
		return rainbow;
	}
	
	public static void setShaderColor(float[] rgb, float opacity)
	{
		RenderSystem.setShaderColor(rgb[0], rgb[1], rgb[2], opacity);
	}
	
	public static int toIntColor(float[] rgb, float opacity)
	{
		return (int)(MathHelper.clamp(opacity, 0, 1) * 255) << 24
			| (int)(MathHelper.clamp(rgb[0], 0, 1) * 255) << 16
			| (int)(MathHelper.clamp(rgb[1], 0, 1) * 255) << 8
			| (int)(MathHelper.clamp(rgb[2], 0, 1) * 255);
	}
	
	public static void drawLine(MatrixStack matrices, Vec3d start, Vec3d end,
		int color, boolean depthTest)
	{
		int depthFunc = depthTest ? GlConst.GL_LEQUAL : GlConst.GL_ALWAYS;
		RenderSystem.enableDepthTest();
		RenderSystem.depthFunc(depthFunc);
		
		VertexConsumerProvider.Immediate vcp = getVCP();
		RenderLayer layer = HardRenderLayers.getLines(depthTest);
		VertexConsumer buffer = vcp.getBuffer(layer);
		
		Vec3d offset = getCameraPos().negate();
		drawLine(matrices, buffer, start.add(offset), end.add(offset), color);
		
		vcp.draw(layer);
	}
	
	private static Vec3d getTracerOrigin(float partialTicks)
	{
		Vec3d start = RotationUtils.getClientLookVec(partialTicks).multiply(10);
		if(HardClient.MC.options
			.getPerspective() == Perspective.THIRD_PERSON_FRONT)
			start = start.negate();
		
		return start;
	}
	
	public static void drawTracer(MatrixStack matrices, float partialTicks,
		Vec3d end, int color, boolean depthTest)
	{
		int depthFunc = depthTest ? GlConst.GL_LEQUAL : GlConst.GL_ALWAYS;
		RenderSystem.enableDepthTest();
		RenderSystem.depthFunc(depthFunc);
		
		VertexConsumerProvider.Immediate vcp = getVCP();
		RenderLayer layer = HardRenderLayers.getLines(depthTest);
		VertexConsumer buffer = vcp.getBuffer(layer);
		
		Vec3d start = getTracerOrigin(partialTicks);
		Vec3d offset = getCameraPos().negate();
		drawLine(matrices, buffer, start, end.add(offset), color);
		
		vcp.draw(layer);
	}
	
	public static void drawTracers(MatrixStack matrices, float partialTicks,
		List<Vec3d> ends, int color, boolean depthTest)
	{
		int depthFunc = depthTest ? GlConst.GL_LEQUAL : GlConst.GL_ALWAYS;
		RenderSystem.enableDepthTest();
		RenderSystem.depthFunc(depthFunc);
		
		VertexConsumerProvider.Immediate vcp = getVCP();
		RenderLayer layer = HardRenderLayers.getLines(depthTest);
		VertexConsumer buffer = vcp.getBuffer(layer);
		
		Vec3d start = getTracerOrigin(partialTicks);
		Vec3d offset = getCameraPos().negate();
		for(Vec3d end : ends)
			drawLine(matrices, buffer, start, end.add(offset), color);
		
		vcp.draw(layer);
	}
	
	public static void drawTracers(MatrixStack matrices, float partialTicks,
		List<ColoredPoint> ends, boolean depthTest)
	{
		int depthFunc = depthTest ? GlConst.GL_LEQUAL : GlConst.GL_ALWAYS;
		RenderSystem.enableDepthTest();
		RenderSystem.depthFunc(depthFunc);
		
		VertexConsumerProvider.Immediate vcp = getVCP();
		RenderLayer layer = HardRenderLayers.getLines(depthTest);
		VertexConsumer buffer = vcp.getBuffer(layer);
		
		Vec3d start = getTracerOrigin(partialTicks);
		Vec3d offset = getCameraPos().negate();
		for(ColoredPoint end : ends)
			drawLine(matrices, buffer, start, end.point().add(offset),
				end.color());
		
		vcp.draw(layer);
	}
	
	public static void drawLine(MatrixStack matrices, VertexConsumer buffer,
		Vec3d start, Vec3d end, int color)
	{
		Entry entry = matrices.peek();
		float x1 = (float)start.x;
		float y1 = (float)start.y;
		float z1 = (float)start.z;
		float x2 = (float)end.x;
		float y2 = (float)end.y;
		float z2 = (float)end.z;
		drawLine(entry, buffer, x1, y1, z1, x2, y2, z2, color);
	}
	
	public static void drawLine(MatrixStack.Entry entry, VertexConsumer buffer,
		float x1, float y1, float z1, float x2, float y2, float z2, int color)
	{
		Vector3f normal = new Vector3f(x2, y2, z2).sub(x1, y1, z1).normalize();
		buffer.vertex(entry.getPositionMatrix(), x1, y1, z1).color(color)
			.normal(entry.getNormalMatrix(), normal.x, normal.y, normal.z)
			.next();
		
		// If the line goes through the screen, add another vertex there. This
		// works around a bug in Minecraft's line shader.
		float t = new Vector3f(x1, y1, z1).negate().dot(normal);
		float length = new Vector3f(x2, y2, z2).sub(x1, y1, z1).length();
		if(t > 0 && t < length)
		{
			Vector3f closeToCam = new Vector3f(normal).mul(t).add(x1, y1, z1);
			buffer
				.vertex(entry.getPositionMatrix(), closeToCam.x, closeToCam.y,
					closeToCam.z)
				.color(color)
				.normal(entry.getNormalMatrix(), normal.x, normal.y, normal.z)
				.next();
			buffer
				.vertex(entry.getPositionMatrix(), closeToCam.x, closeToCam.y,
					closeToCam.z)
				.color(color)
				.normal(entry.getNormalMatrix(), normal.x, normal.y, normal.z)
				.next();
		}
		
		buffer.vertex(entry.getPositionMatrix(), x2, y2, z2).color(color)
			.normal(entry.getNormalMatrix(), normal.x, normal.y, normal.z)
			.next();
	}
	
	public static void drawLine(VertexConsumer buffer, float x1, float y1,
		float z1, float x2, float y2, float z2, int color)
	{
		Vector3f n = new Vector3f(x2, y2, z2).sub(x1, y1, z1).normalize();
		buffer.vertex(x1, y1, z1).color(color).normal(n.x, n.y, n.z).next();
		buffer.vertex(x2, y2, z2).color(color).normal(n.x, n.y, n.z).next();
	}
	
	public static void drawCurvedLine(MatrixStack matrices, List<Vec3d> points,
		int color, boolean depthTest)
	{
		int depthFunc = depthTest ? GlConst.GL_LEQUAL : GlConst.GL_ALWAYS;
		RenderSystem.enableDepthTest();
		RenderSystem.depthFunc(depthFunc);
		
		VertexConsumerProvider.Immediate vcp = getVCP();
		RenderLayer layer = HardRenderLayers.getLineStrip(depthTest);
		VertexConsumer buffer = vcp.getBuffer(layer);
		
		Vec3d offset = getCameraPos().negate();
		List<Vec3d> points2 = points.stream().map(v -> v.add(offset)).toList();
		drawCurvedLine(matrices, buffer, points2, color);
		
		vcp.draw(layer);
	}
	
	public static void drawCurvedLine(MatrixStack matrices,
		VertexConsumer buffer, List<Vec3d> points, int color)
	{
		if(points.size() < 2)
			return;
		
		MatrixStack.Entry entry = matrices.peek();
		Vector3f first = points.get(0).toVector3f();
		Vector3f second = points.get(1).toVector3f();
		Vector3f normal = new Vector3f(first).sub(second).normalize();
		buffer.vertex(entry.getPositionMatrix(), first.x, first.y, first.z)
			.color(color)
			.normal(entry.getNormalMatrix(), normal.x, normal.y, normal.z)
			.next();
		
		for(int i = 1; i < points.size(); i++)
		{
			Vector3f prev = points.get(i - 1).toVector3f();
			Vector3f current = points.get(i).toVector3f();
			normal = new Vector3f(current).sub(prev).normalize();
			buffer
				.vertex(entry.getPositionMatrix(), current.x, current.y,
					current.z)
				.color(color)
				.normal(entry.getNormalMatrix(), normal.x, normal.y, normal.z)
				.next();
		}
	}
	
	public static void drawSolidBox(MatrixStack matrices, Box box, int color,
		boolean depthTest)
	{
		int depthFunc = depthTest ? GlConst.GL_LEQUAL : GlConst.GL_ALWAYS;
		RenderSystem.enableDepthTest();
		RenderSystem.depthFunc(depthFunc);
		
		VertexConsumerProvider.Immediate vcp = getVCP();
		RenderLayer layer = HardRenderLayers.getQuads(depthTest);
		VertexConsumer buffer = vcp.getBuffer(layer);
		
		drawSolidBox(matrices, buffer, box.offset(getCameraPos().negate()),
			color);
		
		vcp.draw(layer);
	}
	
	public static void drawSolidBoxes(MatrixStack matrices, List<Box> boxes,
		int color, boolean depthTest)
	{
		int depthFunc = depthTest ? GlConst.GL_LEQUAL : GlConst.GL_ALWAYS;
		RenderSystem.enableDepthTest();
		RenderSystem.depthFunc(depthFunc);
		
		VertexConsumerProvider.Immediate vcp = getVCP();
		RenderLayer layer = HardRenderLayers.getQuads(depthTest);
		VertexConsumer buffer = vcp.getBuffer(layer);
		
		Vec3d camOffset = getCameraPos().negate();
		for(Box box : boxes)
			drawSolidBox(matrices, buffer, box.offset(camOffset), color);
		
		vcp.draw(layer);
	}
	
	public static void drawSolidBoxes(MatrixStack matrices,
		List<ColoredBox> boxes, boolean depthTest)
	{
		int depthFunc = depthTest ? GlConst.GL_LEQUAL : GlConst.GL_ALWAYS;
		RenderSystem.enableDepthTest();
		RenderSystem.depthFunc(depthFunc);
		
		VertexConsumerProvider.Immediate vcp = getVCP();
		RenderLayer layer = HardRenderLayers.getQuads(depthTest);
		VertexConsumer buffer = vcp.getBuffer(layer);
		
		Vec3d camOffset = getCameraPos().negate();
		for(ColoredBox box : boxes)
			drawSolidBox(matrices, buffer, box.box().offset(camOffset),
				box.color());
		
		vcp.draw(layer);
	}
	
	public static void drawSolidBox(VertexConsumer buffer, Box box, int color)
	{
		drawSolidBox(new MatrixStack(), buffer, box, color);
	}
	
	public static void drawSolidBox(MatrixStack matrices, VertexConsumer buffer,
		Box box, int color)
	{
		MatrixStack.Entry entry = matrices.peek();
		float x1 = (float)box.minX;
		float y1 = (float)box.minY;
		float z1 = (float)box.minZ;
		float x2 = (float)box.maxX;
		float y2 = (float)box.maxY;
		float z2 = (float)box.maxZ;
		
		buffer.vertex(entry.getPositionMatrix(), x1, y1, z1).color(color)
			.next();
		buffer.vertex(entry.getPositionMatrix(), x2, y1, z1).color(color)
			.next();
		buffer.vertex(entry.getPositionMatrix(), x2, y1, z2).color(color)
			.next();
		buffer.vertex(entry.getPositionMatrix(), x1, y1, z2).color(color)
			.next();
		
		buffer.vertex(entry.getPositionMatrix(), x1, y2, z1).color(color)
			.next();
		buffer.vertex(entry.getPositionMatrix(), x1, y2, z2).color(color)
			.next();
		buffer.vertex(entry.getPositionMatrix(), x2, y2, z2).color(color)
			.next();
		buffer.vertex(entry.getPositionMatrix(), x2, y2, z1).color(color)
			.next();
		
		buffer.vertex(entry.getPositionMatrix(), x1, y1, z1).color(color)
			.next();
		buffer.vertex(entry.getPositionMatrix(), x1, y2, z1).color(color)
			.next();
		buffer.vertex(entry.getPositionMatrix(), x2, y2, z1).color(color)
			.next();
		buffer.vertex(entry.getPositionMatrix(), x2, y1, z1).color(color)
			.next();
		
		buffer.vertex(entry.getPositionMatrix(), x2, y1, z1).color(color)
			.next();
		buffer.vertex(entry.getPositionMatrix(), x2, y2, z1).color(color)
			.next();
		buffer.vertex(entry.getPositionMatrix(), x2, y2, z2).color(color)
			.next();
		buffer.vertex(entry.getPositionMatrix(), x2, y1, z2).color(color)
			.next();
		
		buffer.vertex(entry.getPositionMatrix(), x1, y1, z2).color(color)
			.next();
		buffer.vertex(entry.getPositionMatrix(), x2, y1, z2).color(color)
			.next();
		buffer.vertex(entry.getPositionMatrix(), x2, y2, z2).color(color)
			.next();
		buffer.vertex(entry.getPositionMatrix(), x1, y2, z2).color(color)
			.next();
		
		buffer.vertex(entry.getPositionMatrix(), x1, y1, z1).color(color)
			.next();
		buffer.vertex(entry.getPositionMatrix(), x1, y1, z2).color(color)
			.next();
		buffer.vertex(entry.getPositionMatrix(), x1, y2, z2).color(color)
			.next();
		buffer.vertex(entry.getPositionMatrix(), x1, y2, z1).color(color)
			.next();
	}
	
	public static void drawOutlinedBox(MatrixStack matrices, Box box, int color,
		boolean depthTest)
	{
		int depthFunc = depthTest ? GlConst.GL_LEQUAL : GlConst.GL_ALWAYS;
		RenderSystem.enableDepthTest();
		RenderSystem.depthFunc(depthFunc);
		
		VertexConsumerProvider.Immediate vcp = getVCP();
		RenderLayer layer = HardRenderLayers.getLines(depthTest);
		VertexConsumer buffer = vcp.getBuffer(layer);
		
		drawOutlinedBox(matrices, buffer, box.offset(getCameraPos().negate()),
			color);
		
		vcp.draw(layer);
	}
	
	public static void drawOutlinedBoxes(MatrixStack matrices, List<Box> boxes,
		int color, boolean depthTest)
	{
		int depthFunc = depthTest ? GlConst.GL_LEQUAL : GlConst.GL_ALWAYS;
		RenderSystem.enableDepthTest();
		RenderSystem.depthFunc(depthFunc);
		
		VertexConsumerProvider.Immediate vcp = getVCP();
		RenderLayer layer = HardRenderLayers.getLines(depthTest);
		VertexConsumer buffer = vcp.getBuffer(layer);
		
		Vec3d camOffset = getCameraPos().negate();
		for(Box box : boxes)
			drawOutlinedBox(matrices, buffer, box.offset(camOffset), color);
		
		vcp.draw(layer);
	}
	
	public static void drawOutlinedBoxes(MatrixStack matrices,
		List<ColoredBox> boxes, boolean depthTest)
	{
		int depthFunc = depthTest ? GlConst.GL_LEQUAL : GlConst.GL_ALWAYS;
		RenderSystem.enableDepthTest();
		RenderSystem.depthFunc(depthFunc);
		
		VertexConsumerProvider.Immediate vcp = getVCP();
		RenderLayer layer = HardRenderLayers.getLines(depthTest);
		VertexConsumer buffer = vcp.getBuffer(layer);
		
		Vec3d camOffset = getCameraPos().negate();
		for(ColoredBox box : boxes)
			drawOutlinedBox(matrices, buffer, box.box().offset(camOffset),
				box.color());
		
		vcp.draw(layer);
	}
	
	public static void drawOutlinedBox(VertexConsumer buffer, Box box,
		int color)
	{
		drawOutlinedBox(new MatrixStack(), buffer, box, color);
	}
	
	public static void drawOutlinedBox(MatrixStack matrices,
		VertexConsumer buffer, Box box, int color)
	{
		MatrixStack.Entry entry = matrices.peek();
		float x1 = (float)box.minX;
		float y1 = (float)box.minY;
		float z1 = (float)box.minZ;
		float x2 = (float)box.maxX;
		float y2 = (float)box.maxY;
		float z2 = (float)box.maxZ;
		
		// bottom lines
		buffer.vertex(entry.getPositionMatrix(), x1, y1, z1).color(color)
			.normal(entry.getNormalMatrix(), 1, 0, 0).next();
		buffer.vertex(entry.getPositionMatrix(), x2, y1, z1).color(color)
			.normal(entry.getNormalMatrix(), 1, 0, 0).next();
		buffer.vertex(entry.getPositionMatrix(), x1, y1, z1).color(color)
			.normal(entry.getNormalMatrix(), 0, 0, 1).next();
		buffer.vertex(entry.getPositionMatrix(), x1, y1, z2).color(color)
			.normal(entry.getNormalMatrix(), 0, 0, 1).next();
		buffer.vertex(entry.getPositionMatrix(), x2, y1, z1).color(color)
			.normal(entry.getNormalMatrix(), 0, 0, 1).next();
		buffer.vertex(entry.getPositionMatrix(), x2, y1, z2).color(color)
			.normal(entry.getNormalMatrix(), 0, 0, 1).next();
		buffer.vertex(entry.getPositionMatrix(), x1, y1, z2).color(color)
			.normal(entry.getNormalMatrix(), 1, 0, 0).next();
		buffer.vertex(entry.getPositionMatrix(), x2, y1, z2).color(color)
			.normal(entry.getNormalMatrix(), 1, 0, 0).next();
		
		// top lines
		buffer.vertex(entry.getPositionMatrix(), x1, y2, z1).color(color)
			.normal(entry.getNormalMatrix(), 1, 0, 0).next();
		buffer.vertex(entry.getPositionMatrix(), x2, y2, z1).color(color)
			.normal(entry.getNormalMatrix(), 1, 0, 0).next();
		buffer.vertex(entry.getPositionMatrix(), x1, y2, z1).color(color)
			.normal(entry.getNormalMatrix(), 0, 0, 1).next();
		buffer.vertex(entry.getPositionMatrix(), x1, y2, z2).color(color)
			.normal(entry.getNormalMatrix(), 0, 0, 1).next();
		buffer.vertex(entry.getPositionMatrix(), x2, y2, z1).color(color)
			.normal(entry.getNormalMatrix(), 0, 0, 1).next();
		buffer.vertex(entry.getPositionMatrix(), x2, y2, z2).color(color)
			.normal(entry.getNormalMatrix(), 0, 0, 1).next();
		buffer.vertex(entry.getPositionMatrix(), x1, y2, z2).color(color)
			.normal(entry.getNormalMatrix(), 1, 0, 0).next();
		buffer.vertex(entry.getPositionMatrix(), x2, y2, z2).color(color)
			.normal(entry.getNormalMatrix(), 1, 0, 0).next();
		
		// side lines
		buffer.vertex(entry.getPositionMatrix(), x1, y1, z1).color(color)
			.normal(entry.getNormalMatrix(), 0, 1, 0).next();
		buffer.vertex(entry.getPositionMatrix(), x1, y2, z1).color(color)
			.normal(entry.getNormalMatrix(), 0, 1, 0).next();
		buffer.vertex(entry.getPositionMatrix(), x2, y1, z1).color(color)
			.normal(entry.getNormalMatrix(), 0, 1, 0).next();
		buffer.vertex(entry.getPositionMatrix(), x2, y2, z1).color(color)
			.normal(entry.getNormalMatrix(), 0, 1, 0).next();
		buffer.vertex(entry.getPositionMatrix(), x1, y1, z2).color(color)
			.normal(entry.getNormalMatrix(), 0, 1, 0).next();
		buffer.vertex(entry.getPositionMatrix(), x1, y2, z2).color(color)
			.normal(entry.getNormalMatrix(), 0, 1, 0).next();
		buffer.vertex(entry.getPositionMatrix(), x2, y1, z2).color(color)
			.normal(entry.getNormalMatrix(), 0, 1, 0).next();
		buffer.vertex(entry.getPositionMatrix(), x2, y2, z2).color(color)
			.normal(entry.getNormalMatrix(), 0, 1, 0).next();
	}
	
	public static void drawCrossBox(MatrixStack matrices, Box box, int color,
		boolean depthTest)
	{
		int depthFunc = depthTest ? GlConst.GL_LEQUAL : GlConst.GL_ALWAYS;
		RenderSystem.enableDepthTest();
		RenderSystem.depthFunc(depthFunc);
		
		VertexConsumerProvider.Immediate vcp = getVCP();
		RenderLayer layer = HardRenderLayers.getLines(depthTest);
		VertexConsumer buffer = vcp.getBuffer(layer);
		
		drawCrossBox(matrices, buffer, box.offset(getCameraPos().negate()),
			color);
		
		vcp.draw(layer);
	}
	
	public static void drawCrossBoxes(MatrixStack matrices, List<Box> boxes,
		int color, boolean depthTest)
	{
		int depthFunc = depthTest ? GlConst.GL_LEQUAL : GlConst.GL_ALWAYS;
		RenderSystem.enableDepthTest();
		RenderSystem.depthFunc(depthFunc);
		
		VertexConsumerProvider.Immediate vcp = getVCP();
		RenderLayer layer = HardRenderLayers.getLines(depthTest);
		VertexConsumer buffer = vcp.getBuffer(layer);
		
		Vec3d camOffset = getCameraPos().negate();
		for(Box box : boxes)
			drawCrossBox(matrices, buffer, box.offset(camOffset), color);
		
		vcp.draw(layer);
	}
	
	public static void drawCrossBoxes(MatrixStack matrices,
		List<ColoredBox> boxes, boolean depthTest)
	{
		int depthFunc = depthTest ? GlConst.GL_LEQUAL : GlConst.GL_ALWAYS;
		RenderSystem.enableDepthTest();
		RenderSystem.depthFunc(depthFunc);
		
		VertexConsumerProvider.Immediate vcp = getVCP();
		RenderLayer layer = HardRenderLayers.getLines(depthTest);
		VertexConsumer buffer = vcp.getBuffer(layer);
		
		Vec3d camOffset = getCameraPos().negate();
		for(ColoredBox box : boxes)
			drawCrossBox(matrices, buffer, box.box().offset(camOffset),
				box.color());
		
		vcp.draw(layer);
	}
	
	public static void drawCrossBox(VertexConsumer buffer, Box box, int color)
	{
		drawCrossBox(new MatrixStack(), buffer, box, color);
	}
	
	public static void drawCrossBox(MatrixStack matrices, VertexConsumer buffer,
		Box box, int color)
	{
		MatrixStack.Entry entry = matrices.peek();
		float x1 = (float)box.minX;
		float y1 = (float)box.minY;
		float z1 = (float)box.minZ;
		float x2 = (float)box.maxX;
		float y2 = (float)box.maxY;
		float z2 = (float)box.maxZ;
		
		// back
		buffer.vertex(entry.getPositionMatrix(), x1, y1, z1).color(color)
			.normal(entry.getNormalMatrix(), 1, 1, 0).next();
		buffer.vertex(entry.getPositionMatrix(), x2, y2, z1).color(color)
			.normal(entry.getNormalMatrix(), 1, 1, 0).next();
		buffer.vertex(entry.getPositionMatrix(), x2, y1, z1).color(color)
			.normal(entry.getNormalMatrix(), -1, 1, 0).next();
		buffer.vertex(entry.getPositionMatrix(), x1, y2, z1).color(color)
			.normal(entry.getNormalMatrix(), -1, 1, 0).next();
		
		// left
		buffer.vertex(entry.getPositionMatrix(), x2, y1, z1).color(color)
			.normal(entry.getNormalMatrix(), 0, 1, 1).next();
		buffer.vertex(entry.getPositionMatrix(), x2, y2, z2).color(color)
			.normal(entry.getNormalMatrix(), 0, 1, 1).next();
		buffer.vertex(entry.getPositionMatrix(), x2, y1, z2).color(color)
			.normal(entry.getNormalMatrix(), 0, 1, -1).next();
		buffer.vertex(entry.getPositionMatrix(), x2, y2, z1).color(color)
			.normal(entry.getNormalMatrix(), 0, 1, -1).next();
		
		// front
		buffer.vertex(entry.getPositionMatrix(), x2, y1, z2).color(color)
			.normal(entry.getNormalMatrix(), -1, 1, 0).next();
		buffer.vertex(entry.getPositionMatrix(), x1, y2, z2).color(color)
			.normal(entry.getNormalMatrix(), -1, 1, 0).next();
		buffer.vertex(entry.getPositionMatrix(), x1, y1, z2).color(color)
			.normal(entry.getNormalMatrix(), 1, 1, 0).next();
		buffer.vertex(entry.getPositionMatrix(), x2, y2, z2).color(color)
			.normal(entry.getNormalMatrix(), 1, 1, 0).next();
		
		// right
		buffer.vertex(entry.getPositionMatrix(), x1, y1, z2).color(color)
			.normal(entry.getNormalMatrix(), 0, 1, -1).next();
		buffer.vertex(entry.getPositionMatrix(), x1, y2, z1).color(color)
			.normal(entry.getNormalMatrix(), 0, 1, -1).next();
		buffer.vertex(entry.getPositionMatrix(), x1, y1, z1).color(color)
			.normal(entry.getNormalMatrix(), 0, 1, 1).next();
		buffer.vertex(entry.getPositionMatrix(), x1, y2, z2).color(color)
			.normal(entry.getNormalMatrix(), 0, 1, 1).next();
		
		// top
		buffer.vertex(entry.getPositionMatrix(), x1, y2, z2).color(color)
			.normal(entry.getNormalMatrix(), 1, 0, -1).next();
		buffer.vertex(entry.getPositionMatrix(), x2, y2, z1).color(color)
			.normal(entry.getNormalMatrix(), 1, 0, -1).next();
		buffer.vertex(entry.getPositionMatrix(), x1, y2, z1).color(color)
			.normal(entry.getNormalMatrix(), 1, 0, 1).next();
		buffer.vertex(entry.getPositionMatrix(), x2, y2, z2).color(color)
			.normal(entry.getNormalMatrix(), 1, 0, 1).next();
		
		// bottom
		buffer.vertex(entry.getPositionMatrix(), x2, y1, z1).color(color)
			.normal(entry.getNormalMatrix(), -1, 0, 1).next();
		buffer.vertex(entry.getPositionMatrix(), x1, y1, z2).color(color)
			.normal(entry.getNormalMatrix(), -1, 0, 1).next();
		buffer.vertex(entry.getPositionMatrix(), x1, y1, z1).color(color)
			.normal(entry.getNormalMatrix(), 1, 0, 1).next();
		buffer.vertex(entry.getPositionMatrix(), x2, y1, z2).color(color)
			.normal(entry.getNormalMatrix(), 1, 0, 1).next();
	}
	
	public static void drawNode(MatrixStack matrices, Box box, int color,
		boolean depthTest)
	{
		int depthFunc = depthTest ? GlConst.GL_LEQUAL : GlConst.GL_ALWAYS;
		RenderSystem.enableDepthTest();
		RenderSystem.depthFunc(depthFunc);
		
		VertexConsumerProvider.Immediate vcp = getVCP();
		RenderLayer layer = HardRenderLayers.getLines(depthTest);
		VertexConsumer buffer = vcp.getBuffer(layer);
		
		drawNode(matrices, buffer, box.offset(getCameraPos().negate()), color);
		
		vcp.draw(layer);
	}
	
	public static void drawNode(VertexConsumer buffer, Box box, int color)
	{
		drawNode(new MatrixStack(), buffer, box, color);
	}
	
	public static void drawNode(MatrixStack matrices, VertexConsumer buffer,
		Box box, int color)
	{
		MatrixStack.Entry entry = matrices.peek();
		float x1 = (float)box.minX;
		float y1 = (float)box.minY;
		float z1 = (float)box.minZ;
		float x2 = (float)box.maxX;
		float y2 = (float)box.maxY;
		float z2 = (float)box.maxZ;
		float x3 = (x1 + x2) / 2F;
		float y3 = (y1 + y2) / 2F;
		float z3 = (z1 + z2) / 2F;
		
		// middle part
		drawLine(entry, buffer, x3, y3, z2, x1, y3, z3, color);
		drawLine(entry, buffer, x1, y3, z3, x3, y3, z1, color);
		drawLine(entry, buffer, x3, y3, z1, x2, y3, z3, color);
		drawLine(entry, buffer, x2, y3, z3, x3, y3, z2, color);
		
		// top part
		drawLine(entry, buffer, x3, y2, z3, x2, y3, z3, color);
		drawLine(entry, buffer, x3, y2, z3, x1, y3, z3, color);
		drawLine(entry, buffer, x3, y2, z3, x3, y3, z1, color);
		drawLine(entry, buffer, x3, y2, z3, x3, y3, z2, color);
		
		// bottom part
		drawLine(entry, buffer, x3, y1, z3, x2, y3, z3, color);
		drawLine(entry, buffer, x3, y1, z3, x1, y3, z3, color);
		drawLine(entry, buffer, x3, y1, z3, x3, y3, z1, color);
		drawLine(entry, buffer, x3, y1, z3, x3, y3, z2, color);
	}
	
	public static void drawArrow(MatrixStack matrices, VertexConsumer buffer,
		BlockPos from, BlockPos to, RegionPos region, int color)
	{
		Vec3d fromVec = from.toCenterPos().subtract(region.x(), 0, region.z());
		Vec3d toVec = to.toCenterPos().subtract(region.x(), 0, region.z());
		drawArrow(matrices, buffer, fromVec, toVec, color, 1 / 16F);
	}
	
	public static void drawArrow(VertexConsumer buffer, Vec3d from, Vec3d to,
		int color, float headSize)
	{
		drawArrow(new MatrixStack(), buffer, from, to, color, headSize);
	}
	
	public static void drawArrow(MatrixStack matrices, VertexConsumer buffer,
		Vec3d from, Vec3d to, int color, float headSize)
	{
		matrices.push();
		MatrixStack.Entry entry = matrices.peek();
		Matrix4f matrix = entry.getPositionMatrix();
		
		// main line
		drawLine(matrices, buffer, from, to, color);
		
		matrices.translate(to.x, to.y, to.z);
		matrices.scale(headSize, headSize, headSize);
		
		double xDiff = to.x - from.x;
		double yDiff = to.y - from.y;
		double zDiff = to.z - from.z;
		
		float xAngle = (float)(Math.atan2(yDiff, -zDiff) + Math.toRadians(90));
		matrix.rotate(xAngle, new Vector3f(1, 0, 0));
		
		double yzDiff = Math.sqrt(yDiff * yDiff + zDiff * zDiff);
		float zAngle = (float)Math.atan2(xDiff, yzDiff);
		matrix.rotate(zAngle, new Vector3f(0, 0, 1));
		
		// arrow head
		drawLine(entry, buffer, 0, 2, 1, -1, 2, 0, color);
		drawLine(entry, buffer, -1, 2, 0, 0, 2, -1, color);
		drawLine(entry, buffer, 0, 2, -1, 1, 2, 0, color);
		drawLine(entry, buffer, 1, 2, 0, 0, 2, 1, color);
		drawLine(entry, buffer, 1, 2, 0, -1, 2, 0, color);
		drawLine(entry, buffer, 0, 2, 1, 0, 2, -1, color);
		drawLine(entry, buffer, 0, 0, 0, 1, 2, 0, color);
		drawLine(entry, buffer, 0, 0, 0, -1, 2, 0, color);
		drawLine(entry, buffer, 0, 0, 0, 0, 2, -1, color);
		drawLine(entry, buffer, 0, 0, 0, 0, 2, 1, color);
		
		matrices.pop();
	}
	
	public static void drawItem(DrawContext context, ItemStack stack, int x,
		int y, boolean large)
	{
		MatrixStack matrixStack = context.getMatrices();
		
		matrixStack.push();
		matrixStack.translate(x, y, 0);
		if(large)
			matrixStack.scale(1.5F, 1.5F, 1.5F);
		else
			matrixStack.scale(0.75F, 0.75F, 0.75F);
		
		ItemStack renderStack = stack.isEmpty() || stack.getItem() == null
			? new ItemStack(Blocks.GRASS_BLOCK) : stack;
		
		DiffuseLighting.enableGuiDepthLighting();
		context.drawItem(renderStack, 0, 0);
		DiffuseLighting.disableGuiDepthLighting();
		
		matrixStack.pop();
		
		if(stack.isEmpty())
		{
			matrixStack.push();
			matrixStack.translate(x, y, 250);
			if(large)
				matrixStack.scale(2, 2, 2);
			
			TextRenderer tr = HardClient.MC.textRenderer;
			context.drawText(tr, "?", 3, 2, 0xf0f0f0, true);
			
			matrixStack.pop();
		}
		
		RenderSystem.setShaderColor(1, 1, 1, 1);
	}
	
	/**
	 * Similar to {@link DrawContext#fill(int, int, int, int, int)}, but uses
	 * floating-point coordinates instead of integers.
	 */
	public static void fill2D(DrawContext context, float x1, float y1, float x2,
		float y2, int color)
	{
		Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
		VertexConsumer buffer = getVCP().getBuffer(RenderLayer.getGui());
		buffer.vertex(matrix, x1, y1, 0).color(color).next();
		buffer.vertex(matrix, x1, y2, 0).color(color).next();
		buffer.vertex(matrix, x2, y2, 0).color(color).next();
		buffer.vertex(matrix, x2, y1, 0).color(color).next();
	}
	
	/**
	 * Renders the given vertices in QUADS draw mode.
	 *
	 * @apiNote Due to back-face culling, quads will be invisible if their
	 *          vertices are not supplied in counter-clockwise order.
	 */
	public static void fillQuads2D(DrawContext context, float[][] vertices,
		int color)
	{
		Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
		VertexConsumer buffer = getVCP().getBuffer(RenderLayer.getGui());
		for(float[] vertex : vertices)
			buffer.vertex(matrix, vertex[0], vertex[1], 0).color(color).next();
	}
	
	/**
	 * Renders the given vertices in TRIANGLE_STRIP draw mode.
	 *
	 * @apiNote Due to back-face culling, triangles will be invisible if their
	 *          vertices are not supplied in counter-clockwise order.
	 */
	public static void fillTriangle2D(DrawContext context, float[][] vertices,
		int color)
	{
		Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
		VertexConsumer buffer =
			getVCP().getBuffer(RenderLayer.getDebugFilledBox());
		for(float[] vertex : vertices)
			buffer.vertex(matrix, vertex[0], vertex[1], 1).color(color).next();
	}
	
	/**
	 * Similar to {@link DrawContext#drawHorizontalLine(int, int, int, int)} and
	 * {@link DrawContext#drawVerticalLine(int, int, int, int)}, but supports
	 * diagonal lines, uses floating-point coordinates instead of integers, is
	 * one actual pixel wide instead of one scaled pixel, uses fewer draw calls
	 * than the vanilla method, and uses a z value of 1 to ensure that lines
	 * show up above fills.
	 */
	
	/**
	 * Fills a rectangle with rounded corners.
	 */
	public static void fillRoundedRect(DrawContext context, int x1, int y1,
		int x2, int y2, int radius, int color)
	{
		if(x2 <= x1 || y2 <= y1)
			return;
		
		int r = Math.max(0,
			Math.min(radius, Math.min((x2 - x1) / 2, (y2 - y1) / 2)));
		if(r < 1)
		{
			context.fill(x1, y1, x2, y2, color);
			return;
		}
		
		// middle
		context.fill(x1, y1 + r, x2, y2 - r, color);
		
		// corners
		for(int i = 0; i < r; i++)
		{
			int inset = getCornerInset(r, i);
			context.fill(x1 + inset, y1 + i, x2 - inset, y1 + i + 1, color);
			context.fill(x1 + inset, y2 - i - 1, x2 - inset, y2 - i, color);
		}
	}
	
	/**
	 * Fills a rectangle whose top corners are rounded.
	 */
	public static void fillRoundedTop(DrawContext context, int x1, int y1,
		int x2, int y2, int radius, int color)
	{
		if(x2 <= x1 || y2 <= y1)
			return;
		
		int r = Math.max(0,
			Math.min(radius, Math.min((x2 - x1) / 2, (y2 - y1) / 2)));
		if(r < 1)
		{
			context.fill(x1, y1, x2, y2, color);
			return;
		}
		
		context.fill(x1, y1 + r, x2, y2, color);
		
		for(int i = 0; i < r; i++)
		{
			int inset = getCornerInset(r, i);
			context.fill(x1 + inset, y1 + i, x2 - inset, y1 + i + 1, color);
		}
	}
	
	private static int getCornerInset(int radius, int row)
	{
		double dy = radius - row - 0.5;
		return (int)Math.round(radius - Math.sqrt(radius * radius - dy * dy));
	}
	
	/**
	 * Mixes the given color towards white.
	 */
	public static float[] lighten(float[] rgb, float amount)
	{
		return new float[]{rgb[0] + (1 - rgb[0]) * amount,
			rgb[1] + (1 - rgb[1]) * amount, rgb[2] + (1 - rgb[2]) * amount};
	}
	
	/**
	 * Blends the given ARGB colors together. A ratio of 0 returns the first
	 * color, a ratio of 1 returns the second color.
	 */
	public static int blend(int color1, int color2, float ratio)
	{
		float t = Math.max(0, Math.min(1, ratio));
		float inv = 1 - t;
		
		int a = (int)(((color1 >>> 24) & 0xFF) * inv
			+ ((color2 >>> 24) & 0xFF) * t);
		int r = (int)(((color1 >>> 16) & 0xFF) * inv
			+ ((color2 >>> 16) & 0xFF) * t);
		int g =
			(int)(((color1 >>> 8) & 0xFF) * inv + ((color2 >>> 8) & 0xFF) * t);
		int b = (int)((color1 & 0xFF) * inv + (color2 & 0xFF) * t);
		
		return (a << 24) | (r << 16) | (g << 8) | b;
	}
	
	/**
	 * Fills a rectangle with rounded corners and a vertical gradient.
	 */
	public static void fillRoundedRectGradient(DrawContext context, int x1,
		int y1, int x2, int y2, int radius, int topColor, int bottomColor)
	{
		if(x2 <= x1 || y2 <= y1)
			return;
		
		int height = y2 - y1;
		int r = Math.max(0,
			Math.min(radius, Math.min((x2 - x1) / 2, Math.min(height / 2, 8))));
		
		for(int row = 0; row < height; row++)
		{
			int inset = 0;
			if(r > 0)
			{
				if(row < r)
					inset = getCornerInset(r, row);
				else if(row >= height - r)
					inset = getCornerInset(r, height - row - 1);
			}
			
			int color = blend(topColor, bottomColor,
				row / (float)Math.max(1, height - 1));
			context.fill(x1 + inset, y1 + row, x2 - inset, y1 + row + 1, color);
		}
	}
	
	/**
	 * Fills a rectangle whose top corners are rounded and that has a vertical
	 * gradient.
	 */
	public static void fillRoundedTopGradient(DrawContext context, int x1,
		int y1, int x2, int y2, int radius, int topColor, int bottomColor)
	{
		if(x2 <= x1 || y2 <= y1)
			return;
		
		int height = y2 - y1;
		int r = Math.max(0,
			Math.min(radius, Math.min((x2 - x1) / 2, Math.min(height / 2, 8))));
		
		for(int row = 0; row < height; row++)
		{
			int inset = r > 0 && row < r ? getCornerInset(r, row) : 0;
			int color = blend(topColor, bottomColor,
				row / (float)Math.max(1, height - 1));
			context.fill(x1 + inset, y1 + row, x2 - inset, y1 + row + 1, color);
		}
	}
	
	/**
	 * Draws a soft shadow around the given rectangle. The alpha of the given
	 * color is used as the maximum shadow opacity.
	 */
	public static void drawRoundedShadow2D(DrawContext context, int x1, int y1,
		int x2, int y2, int radius, int color)
	{
		int alpha = (color >>> 24) & 0xFF;
		if(alpha <= 0)
			return;
		
		for(int i = 2; i >= 1; i--)
		{
			int layerAlpha = Math.max(1, alpha / (i * i + 1));
			int layerColor = (layerAlpha << 24) | (color & 0xFFFFFF);
			fillRoundedRect(context, x1 - i, y1 - i + 1, x2 + i, y2 + i,
				radius + i, layerColor);
		}
	}
	
	/**
	 * Similar to
	 * {@link #drawBorder2D(DrawContext, float, float, float, float, int)},
	 * but with rounded corners.
	 */
	public static void drawRoundedBorder2D(DrawContext context, float x1,
		float y1, float x2, float y2, float radius, int color)
	{
		float r = Math.max(0,
			Math.min(radius, Math.min((x2 - x1) / 2F, (y2 - y1) / 2F)));
		
		Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
		VertexConsumer buffer =
			getVCP().getBuffer(HardRenderLayers.ONE_PIXEL_LINE_STRIP);
		
		if(r < 1)
		{
			addLineVertex(buffer, matrix, x1, y1, color);
			addLineVertex(buffer, matrix, x2, y1, color);
			addLineVertex(buffer, matrix, x2, y2, color);
			addLineVertex(buffer, matrix, x1, y2, color);
			addLineVertex(buffer, matrix, x1, y1, color);
			return;
		}
		
		addLineVertex(buffer, matrix, x1 + r, y1, color);
		addLineVertex(buffer, matrix, x2 - r, y1, color);
		addCornerArc(buffer, matrix, x2 - r, y1 + r, r, -90, 0, color);
		addLineVertex(buffer, matrix, x2, y2 - r, color);
		addCornerArc(buffer, matrix, x2 - r, y2 - r, r, 0, 90, color);
		addLineVertex(buffer, matrix, x1 + r, y2, color);
		addCornerArc(buffer, matrix, x1 + r, y2 - r, r, 90, 180, color);
		addLineVertex(buffer, matrix, x1, y1 + r, color);
		addCornerArc(buffer, matrix, x1 + r, y1 + r, r, 180, 270, color);
	}
	
	private static void addCornerArc(VertexConsumer buffer, Matrix4f matrix,
		float cx, float cy, float r, float startDeg, float endDeg, int color)
	{
		int segments = Math.max(4, (int)Math.ceil(r) * 2);
		for(int i = 0; i <= segments; i++)
		{
			double angle = Math.toRadians(
				startDeg + (endDeg - startDeg) * i / (double)segments);
			addLineVertex(buffer, matrix, (float)(cx + Math.cos(angle) * r),
				(float)(cy + Math.sin(angle) * r), color);
		}
	}
	
	private static void addLineVertex(VertexConsumer buffer, Matrix4f matrix,
		float x, float y, int color)
	{
		buffer.vertex(matrix, x, y, 1).color(color).next();
	}
	
	/**
	 * Draws a bar that travels around the outline of the given rectangle. The
	 * bar is brightest at the front and fades out towards the back, creating a
	 * comet-like trail.
	 *
	 * @param thickness
	 *            the thickness of the bar in pixels
	 * @param progress
	 *            how far around the outline the bar has travelled (0 to
	 *            1)
	 */
	public static void drawOrbitBar2D(DrawContext context, int x1, int y1,
		int x2, int y2, int radius, int thickness, float progress, int color)
	{
		int w = x2 - x1;
		int h = y2 - y1;
		if(w <= 2 * radius || h <= 2 * radius || thickness < 1)
			return;
			
		// path that the bar travels along, inset by half its thickness so
		// that the bar sits exactly on the outline
		float inset = thickness / 2F;
		float ix1 = x1 + inset;
		float iy1 = y1 + inset;
		float ix2 = x2 - inset;
		float iy2 = y2 - inset;
		float r = Math.max(1, radius - inset);
		float straightW = Math.max(0, (ix2 - ix1) - 2 * r);
		float straightH = Math.max(0, (iy2 - iy1) - 2 * r);
		float arc = (float)(Math.PI * r / 2);
		float perimeter = 2 * straightW + 2 * straightH + 4 * arc;
		if(perimeter <= 0)
			return;
		
		int barLength = Math.max(12, (int)(perimeter / 5F));
		float head = progress * perimeter;
		
		for(int i = 0; i < barLength; i++)
		{
			float d = head - i;
			while(d < 0)
				d += perimeter;
			while(d >= perimeter)
				d -= perimeter;
			
			float[] p = getOrbitPoint(d, ix1, iy1, ix2, iy2, r, straightW,
				straightH, arc);
			if(p == null)
				continue;
			
			float fade = 1 - i / (float)barLength;
			fade *= fade;
			int alphaBits = (int)(((color >>> 24) & 0xFF) * fade);
			if(alphaBits < 2)
				continue;
			
			int c = (alphaBits << 24) | (color & 0xFFFFFF);
			context.fill((int)(p[0] - inset), (int)(p[1] - inset),
				(int)(p[0] - inset) + thickness,
				(int)(p[1] - inset) + thickness, c);
		}
		
		// bright head, makes the motion readable even on a black background
		float[] headPoint = getOrbitPoint(head % perimeter, ix1, iy1, ix2, iy2,
			r, straightW, straightH, arc);
		if(headPoint != null)
		{
			int glowColor = RenderUtils.blend(color, 0xFFFFFFFF, 0.45F);
			int glowAlpha = (int)(((color >>> 24) & 0xFF) * 0.55F);
			int g = (glowAlpha << 24) | (glowColor & 0xFFFFFF);
			
			for(int gy = -1; gy <= thickness; gy++)
				for(int gx = -1; gx <= thickness; gx++)
					context.fill((int)(headPoint[0] - inset) + gx,
						(int)(headPoint[1] - inset) + gy,
						(int)(headPoint[0] - inset) + gx + 1,
						(int)(headPoint[1] - inset) + gy + 1, g);
		}
	}
	
	private static float[] getOrbitPoint(float d, float x1, float y1, float x2,
		float y2, float r, float straightW, float straightH, float arc)
	{
		// top edge, left to right
		if(d < straightW)
			return new float[]{x1 + r + d, y1};
		d -= straightW;
		
		// top right corner
		if(d < arc)
			return getArcPoint(x2 - r, y1 + r, r, -90, 90 * d / arc);
		d -= arc;
		
		// right edge, top to bottom
		if(d < straightH)
			return new float[]{x2, y1 + r + d};
		d -= straightH;
		
		// bottom right corner
		if(d < arc)
			return getArcPoint(x2 - r, y2 - r, r, 0, 90 * d / arc);
		d -= arc;
		
		// bottom edge, right to left
		if(d < straightW)
			return new float[]{x2 - r - d, y2};
		d -= straightW;
		
		// bottom left corner
		if(d < arc)
			return getArcPoint(x1 + r, y2 - r, r, 90, 90 * d / arc);
		d -= arc;
		
		// left edge, bottom to top
		if(d < straightH)
			return new float[]{x1, y2 - r - d};
		d -= straightH;
		
		// top left corner
		if(d <= arc)
			return getArcPoint(x1 + r, y1 + r, r, 180, 90 * d / arc);
		
		return null;
	}
	
	private static float[] getArcPoint(float cx, float cy, float r,
		float startDeg, float offsetDeg)
	{
		double angle = Math.toRadians(startDeg + offsetDeg);
		return new float[]{(float)(cx + Math.cos(angle) * r),
			(float)(cy + Math.sin(angle) * r)};
	}
	
	public static void drawLine2D(DrawContext context, float x1, float y1,
		float x2, float y2, int color)
	{
		Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
		VertexConsumer buffer =
			getVCP().getBuffer(HardRenderLayers.ONE_PIXEL_LINES);
		buffer.vertex(matrix, x1, y1, 1).color(color).next();
		buffer.vertex(matrix, x2, y2, 1).color(color).next();
	}
	
	/**
	 * Similar to {@link DrawContext#drawBorder(int, int, int, int, int)}, but
	 * uses floating-point coordinates instead of integers, is one actual pixel
	 * wide instead of one scaled pixel, uses fewer draw calls than the vanilla
	 * method, and uses a z value of 1 to ensure that lines show up above fills.
	 */
	public static void drawBorder2D(DrawContext context, float x1, float y1,
		float x2, float y2, int color)
	{
		Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
		VertexConsumer buffer =
			getVCP().getBuffer(HardRenderLayers.ONE_PIXEL_LINE_STRIP);
		buffer.vertex(matrix, x1, y1, 1).color(color).next();
		buffer.vertex(matrix, x2, y1, 1).color(color).next();
		buffer.vertex(matrix, x2, y2, 1).color(color).next();
		buffer.vertex(matrix, x1, y2, 1).color(color).next();
		buffer.vertex(matrix, x1, y1, 1).color(color).next();
	}
	
	/**
	 * Draws a 1px border around the given polygon.
	 */
	public static void drawLineStrip2D(DrawContext context, float[][] vertices,
		int color)
	{
		Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
		VertexConsumer buffer =
			getVCP().getBuffer(HardRenderLayers.ONE_PIXEL_LINE_STRIP);
		for(float[] vertex : vertices)
			buffer.vertex(matrix, vertex[0], vertex[1], 1).color(color).next();
		buffer.vertex(matrix, vertices[0][0], vertices[0][1], 1).color(color)
			.next();
	}
	
	/**
	 * Draws a box shadow around the given rectangle.
	 */
	public static void drawBoxShadow2D(DrawContext context, int x1, int y1,
		int x2, int y2)
	{
		float[] acColor = HardClient.INSTANCE.getGui().getAcColor();
		
		// outline
		float xo1 = x1 - 0.1F;
		float xo2 = x2 + 0.1F;
		float yo1 = y1 - 0.1F;
		float yo2 = y2 + 0.1F;
		
		int outlineColor = toIntColor(acColor, 0.5F);
		drawBorder2D(context, xo1, yo1, xo2, yo2, outlineColor);
		
		// shadow
		float xs1 = x1 - 1;
		float xs2 = x2 + 1;
		float ys1 = y1 - 1;
		float ys2 = y2 + 1;
		
		int shadowColor1 = toIntColor(acColor, 0.75F);
		int shadowColor2 = 0x00000000;
		
		MatrixStack matrixStack = context.getMatrices();
		Matrix4f matrix = matrixStack.peek().getPositionMatrix();
		
		VertexConsumer buffer = getVCP().getBuffer(RenderLayer.getGui());
		
		// top
		buffer.vertex(matrix, x1, y1, 0).color(shadowColor1).next();
		buffer.vertex(matrix, x2, y1, 0).color(shadowColor1).next();
		buffer.vertex(matrix, xs2, ys1, 0).color(shadowColor2).next();
		buffer.vertex(matrix, xs1, ys1, 0).color(shadowColor2).next();
		
		// left
		buffer.vertex(matrix, xs1, ys1, 0).color(shadowColor2).next();
		buffer.vertex(matrix, xs1, ys2, 0).color(shadowColor2).next();
		buffer.vertex(matrix, x1, y2, 0).color(shadowColor1).next();
		buffer.vertex(matrix, x1, y1, 0).color(shadowColor1).next();
		
		// right
		buffer.vertex(matrix, x2, y1, 0).color(shadowColor1).next();
		buffer.vertex(matrix, x2, y2, 0).color(shadowColor1).next();
		buffer.vertex(matrix, xs2, ys2, 0).color(shadowColor2).next();
		buffer.vertex(matrix, xs2, ys1, 0).color(shadowColor2).next();
		
		// bottom
		buffer.vertex(matrix, x2, y2, 0).color(shadowColor1).next();
		buffer.vertex(matrix, x1, y2, 0).color(shadowColor1).next();
		buffer.vertex(matrix, xs1, ys2, 0).color(shadowColor2).next();
		buffer.vertex(matrix, xs2, ys2, 0).color(shadowColor2).next();
	}
	
	public record ColoredPoint(Vec3d point, int color)
	{}
	
	public record ColoredBox(Box box, int color)
	{}
}
