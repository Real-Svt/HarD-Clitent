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

@SearchTags({"languages", "localizations", "localisations",
	"internationalization", "internationalisation", "i18n", "sprachen",
	"übersetzungen", "force english"})
@DontBlock
public final class TranslationsOtf extends OtherFeature
{
	private final CheckboxSetting forceEnglish = new CheckboxSetting(
		"Force English",
		"Displays the Hard Client in English, even if Minecraft is set to a different language.",
		true);
	
	public TranslationsOtf()
	{
		super("Translations", "Allows text in Hard to be displayed"
			+ " in other languages than English. It will use the same language"
			+ " that Minecraft is set to.\n\n"
			+ "This is an experimental feature!");
		addSetting(forceEnglish);
	}
	
	public CheckboxSetting getForceEnglish()
	{
		return forceEnglish;
	}
}
