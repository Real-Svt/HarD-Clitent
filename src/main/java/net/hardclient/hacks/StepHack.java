/*
 * Copyright (c) 2014-2026 Hard-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.hardclient.hacks;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Box;
import net.hardclient.Category;
import net.hardclient.events.UpdateListener;
import net.hardclient.hack.Hack;
import net.hardclient.settings.EnumSetting;
import net.hardclient.settings.SliderSetting;
import net.hardclient.settings.SliderSetting.ValueDisplay;
import net.hardclient.util.BlockUtils;

public final class StepHack extends Hack implements UpdateListener
{
	private final EnumSetting<Mode> mode = new EnumSetting<>("Mode",
		"\u00a7lSimple\u00a7r mode can step up multiple blocks (enables Height slider).\n"
			+ "\u00a7lLegit\u00a7r mode can bypass NoCheat+.",
		Mode.values(), Mode.LEGIT);
	
	private final SliderSetting height =
		new SliderSetting("Height", "Only works in \u00a7lSimple\u00a7r mode.",
			1, 1, 10, 1, ValueDisplay.INTEGER);
	
	public StepHack()
	{
		super("Step");
		setCategory(Category.MOVEMENT);
		addSetting(mode);
		addSetting(height);
	}
	
	@Override
	protected void onEnable()
	{
		EVENTS.add(UpdateListener.class, this);
	}
	
	@Override
	protected void onDisable()
	{
		EVENTS.remove(UpdateListener.class, this);
		MC.player.stepHeight = 0.5F;
	}
	
	@Override
	public void onUpdate()
	{
		if(mode.getSelected() == Mode.SIMPLE)
		{
			// simple mode
			MC.player.stepHeight = height.getValueF();
			return;
		}
		
		// legit mode
		ClientPlayerEntity player = MC.player;
		player.stepHeight = 0.5F;
		
		if(!player.horizontalCollision)
			return;
		
		if(!player.isOnGround() || player.isClimbing()
			|| player.isTouchingWater() || player.isInLava())
			return;
		
		if(player.input.movementForward == 0
			&& player.input.movementSideways == 0)
			return;
		
		if(player.input.jumping)
			return;
		
		Box box = player.getBoundingBox().offset(0, 0.05, 0).expand(0.05);
		
		if(!MC.world.isSpaceEmpty(player, box.offset(0, 1, 0)))
			return;
		
		double stepHeight = BlockUtils.getBlockCollisions(box)
			.mapToDouble(bb -> bb.maxY).max().orElse(Double.NEGATIVE_INFINITY);
		
		stepHeight -= player.getY();
		
		if(stepHeight < 0 || stepHeight > 1)
			return;
		
		ClientPlayNetworkHandler netHandler = player.networkHandler;
		
		netHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
			player.getX(), player.getY() + 0.42 * stepHeight, player.getZ(),
			player.isOnGround()));
		
		netHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
			player.getX(), player.getY() + 0.753 * stepHeight, player.getZ(),
			player.isOnGround()));
		
		player.setPosition(player.getX(), player.getY() + stepHeight,
			player.getZ());
	}
	
	public boolean isAutoJumpAllowed()
	{
		return !isEnabled() && !HARD.getCmds().goToCmd.isActive();
	}
	
	private enum Mode
	{
		SIMPLE("Simple"),
		LEGIT("Legit");
		
		private final String name;
		
		private Mode(String name)
		{
			this.name = name;
		}
		
		@Override
		public String toString()
		{
			return name;
		}
	}
}
