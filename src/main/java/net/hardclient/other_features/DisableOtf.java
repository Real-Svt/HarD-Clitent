/*
 * Copyright (c) 2014-2026 Hard-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.hardclient.other_features;

import net.hardclient.DontBlock;
import net.hardclient.SearchTags;
import net.hardclient.other_feature.OtherFeature;
import net.hardclient.settings.CheckboxSetting;

@SearchTags({"turn off", "hide hard logo", "ghost mode", "stealth mode",
	"vanilla Minecraft"})
@DontBlock
public final class DisableOtf extends OtherFeature
{
	private final CheckboxSetting hideEnableButton = new CheckboxSetting(
		"Hide enable button",
		"Removes the \"Enable Hard\" button as soon as you close the Statistics screen."
			+ " You will have to restart the game to re-enable Hard.",
		false);
	
	public DisableOtf()
	{
		super("Disable Hard",
			"To disable Hard, go to the Statistics screen and press the \"Disable Hard\" button.\n"
				+ "It will turn into an \"Enable Hard\" button once pressed.");
		addSetting(hideEnableButton);
	}
	
	public boolean shouldHideEnableButton()
	{
		return !HARD.isEnabled() && hideEnableButton.isChecked();
	}
}
