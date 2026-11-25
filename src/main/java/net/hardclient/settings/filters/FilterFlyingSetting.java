/*
 * Copyright (c) 2014-2026 Hard-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.hardclient.settings.filters;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.hardclient.HardClient;
import net.hardclient.settings.Setting;
import net.hardclient.settings.SliderSetting;
import net.hardclient.settings.filterlists.EntityFilterList.EntityFilter;

public final class FilterFlyingSetting extends SliderSetting
	implements EntityFilter
{
	public FilterFlyingSetting(String description, double value)
	{
		super("Filter flying", description, value, 0, 2, 0.05,
			ValueDisplay.DECIMAL.withLabel(0, "off"));
	}
	
	@Override
	public boolean test(Entity e)
	{
		if(!(e instanceof PlayerEntity))
			return true;
		
		Box box = e.getBoundingBox();
		box = box.union(box.offset(0, -getValue(), 0));
		return !HardClient.MC.world.isSpaceEmpty(box);
	}
	
	@Override
	public boolean isFilterEnabled()
	{
		return getValue() > 0;
	}
	
	@Override
	public Setting getSetting()
	{
		return this;
	}
	
	public static FilterFlyingSetting genericCombat(double value)
	{
		return new FilterFlyingSetting(
			"description.hard.setting.generic.filter_flying_combat", value);
	}
}
