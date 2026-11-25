/*
 * Copyright (c) 2014-2026 Hard-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.hardclient.commands;

import net.hardclient.HardClient;
import net.hardclient.command.CmdException;
import net.hardclient.command.CmdSyntaxError;
import net.hardclient.command.Command;
import net.hardclient.hack.Hack;
import net.hardclient.other_feature.OtherFeature;
import net.hardclient.util.ChatUtils;

public final class FeaturesCmd extends Command
{
	public FeaturesCmd()
	{
		super("features",
			"Shows the number of features and some other\n" + "statistics.",
			".features");
	}
	
	@Override
	public void call(String[] args) throws CmdException
	{
		if(args.length != 0)
			throw new CmdSyntaxError();
		
		if(HardClient.VERSION.startsWith("7.0pre"))
			ChatUtils.warning(
				"This is just a pre-release! It doesn't (yet) have all of the features of Hard 7.0! See download page for details.");
		
		int hax = HARD.getHax().countHax();
		int cmds = HARD.getCmds().countCmds();
		int otfs = HARD.getOtfs().countOtfs();
		int all = hax + cmds + otfs;
		
		ChatUtils.message("All features: " + all);
		ChatUtils.message("Hacks: " + hax);
		ChatUtils.message("Commands: " + cmds);
		ChatUtils.message("Other features: " + otfs);
		
		int settings = 0;
		for(Hack hack : HARD.getHax().getAllHax())
			settings += hack.getSettings().size();
		for(Command cmd : HARD.getCmds().getAllCmds())
			settings += cmd.getSettings().size();
		for(OtherFeature otf : HARD.getOtfs().getAllOtfs())
			settings += otf.getSettings().size();
		
		ChatUtils.message("Settings: " + settings);
	}
	
	@Override
	public String getPrimaryAction()
	{
		return "Show Statistics";
	}
	
	@Override
	public void doPrimaryAction()
	{
		HARD.getCmdProcessor().process("features");
	}
}
