/*
 * Copyright (c) 2014-2026 Hard-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.hardclient.commands;

import net.hardclient.command.CmdException;
import net.hardclient.command.CmdSyntaxError;
import net.hardclient.command.Command;
import net.hardclient.hacks.RemoteViewHack;

public final class RvCmd extends Command
{
	public RvCmd()
	{
		super("rv", "Makes RemoteView target a specific entity.",
			".rv <entity>");
	}
	
	@Override
	public void call(String[] args) throws CmdException
	{
		RemoteViewHack remoteView = HARD.getHax().remoteViewHack;
		
		if(args.length != 1)
			throw new CmdSyntaxError();
		
		remoteView.onToggledByCommand(args[0]);
	}
}
