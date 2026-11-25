/*
 * Copyright (c) 2014-2026 Hard-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.hardclient.commands;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import net.hardclient.Category;
import net.hardclient.command.CmdException;
import net.hardclient.command.CmdSyntaxError;
import net.hardclient.command.Command;
import net.hardclient.events.GUIRenderListener;
import net.hardclient.events.UpdateListener;
import net.hardclient.util.RenderUtils;

public final class TacoCmd extends Command
	implements GUIRenderListener, UpdateListener
{
	private final Identifier[] tacos =
		{new Identifier("hard", "dancingtaco1.png"),
			new Identifier("hard", "dancingtaco2.png"),
			new Identifier("hard", "dancingtaco3.png"),
			new Identifier("hard", "dancingtaco4.png")};
	
	private boolean enabled;
	private int ticks = 0;
	
	public TacoCmd()
	{
		super("taco", "Spawns a dancing taco on your hotbar.\n"
			+ "\"I love that little guy. So cute!\" -WiZARD");
		setCategory(Category.FUN);
	}
	
	@Override
	public void call(String[] args) throws CmdException
	{
		if(args.length != 0)
			throw new CmdSyntaxError("Tacos don't need arguments!");
		
		enabled = !enabled;
		
		if(enabled)
		{
			EVENTS.add(GUIRenderListener.class, this);
			EVENTS.add(UpdateListener.class, this);
			
		}else
		{
			EVENTS.remove(GUIRenderListener.class, this);
			EVENTS.remove(UpdateListener.class, this);
		}
	}
	
	@Override
	public String getPrimaryAction()
	{
		return "Be a BOSS!";
	}
	
	@Override
	public void doPrimaryAction()
	{
		HARD.getCmdProcessor().process("taco");
	}
	
	@Override
	public void onUpdate()
	{
		if(ticks >= 31)
			ticks = 0;
		else
			ticks++;
	}
	
	@Override
	public void onRenderGUI(DrawContext context, float partialTicks)
	{
		if(HARD.getHax().rainbowUiHack.isEnabled())
			RenderUtils.setShaderColor(HARD.getGui().getAcColor(), 1);
		else
			RenderSystem.setShaderColor(1, 1, 1, 1);
		
		int x = context.getScaledWindowWidth() / 2 - 32 + 76;
		int y = context.getScaledWindowHeight() - 32 - 19;
		int w = 64;
		int h = 32;
		context.drawTexture(tacos[ticks / 8], x, y, 0, 0, w, h, w, h);
	}
}
