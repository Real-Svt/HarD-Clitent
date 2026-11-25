/*
 * Copyright (c) 2014-2026 Hard-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.hardclient.hacks.chattranslator;

import java.util.regex.Pattern;

import net.hardclient.HardClient;
import net.hardclient.settings.CheckboxSetting;

public class FilterOwnMessagesSetting extends CheckboxSetting
{
	private Pattern ownMessagePattern;
	private String lastUsername;
	
	public FilterOwnMessagesSetting()
	{
		super("Filter own messages",
			"description.hard.setting.chattranslator.filter_own_messages",
			true);
	}
	
	public boolean isOwnMessage(String message)
	{
		updateOwnMessagePattern();
		return ownMessagePattern.matcher(message).find();
	}
	
	private void updateOwnMessagePattern()
	{
		String username = HardClient.MC.getSession().getUsername();
		if(username.equals(lastUsername))
			return;
		
		String rankPattern = "(?:\\[[^\\]]+\\] ?){0,2}";
		String namePattern = Pattern.quote(username);
		String regex = "^" + rankPattern + "[<\\[]?" + namePattern + "[>\\]:]";
		
		ownMessagePattern = Pattern.compile(regex);
		lastUsername = username;
	}
}
