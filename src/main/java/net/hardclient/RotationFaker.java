/*
 * Copyright (c) 2014-2026 Hard-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.hardclient;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.hardclient.events.PostMotionListener;
import net.hardclient.events.PreMotionListener;
import net.hardclient.util.Rotation;
import net.hardclient.util.RotationUtils;

public final class RotationFaker
	implements PreMotionListener, PostMotionListener
{
	private boolean fakeRotation;
	private float serverYaw;
	private float serverPitch;
	private float realYaw;
	private float realPitch;
	
	@Override
	public void onPreMotion()
	{
		if(!fakeRotation)
			return;
		
		ClientPlayerEntity player = HardClient.MC.player;
		realYaw = player.getYaw();
		realPitch = player.getPitch();
		player.setYaw(serverYaw);
		player.setPitch(serverPitch);
	}
	
	@Override
	public void onPostMotion()
	{
		if(!fakeRotation)
			return;
		
		ClientPlayerEntity player = HardClient.MC.player;
		player.setYaw(realYaw);
		player.setPitch(realPitch);
		fakeRotation = false;
	}
	
	public void faceVectorPacket(Vec3d vec)
	{
		Rotation needed = RotationUtils.getNeededRotations(vec);
		ClientPlayerEntity player = HardClient.MC.player;
		
		fakeRotation = true;
		serverYaw =
			RotationUtils.limitAngleChange(player.getYaw(), needed.yaw());
		serverPitch = needed.pitch();
	}
	
	public void faceVectorClient(Vec3d vec)
	{
		Rotation needed = RotationUtils.getNeededRotations(vec);
		
		ClientPlayerEntity player = HardClient.MC.player;
		player.setYaw(
			RotationUtils.limitAngleChange(player.getYaw(), needed.yaw()));
		player.setPitch(needed.pitch());
	}
	
	public void faceVectorClientIgnorePitch(Vec3d vec)
	{
		Rotation needed = RotationUtils.getNeededRotations(vec);
		
		ClientPlayerEntity player = HardClient.MC.player;
		player.setYaw(
			RotationUtils.limitAngleChange(player.getYaw(), needed.yaw()));
		player.setPitch(0);
	}
	
	public float getServerYaw()
	{
		return fakeRotation ? serverYaw : HardClient.MC.player.getYaw();
	}
	
	public float getServerPitch()
	{
		return fakeRotation ? serverPitch : HardClient.MC.player.getPitch();
	}
}
