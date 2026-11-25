/*
 * Copyright (c) 2014-2026 Hard-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.hardclient.hacks;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.minecraft.entity.Entity;
import net.minecraft.util.Hand;
import net.hardclient.Category;
import net.hardclient.SearchTags;
import net.hardclient.events.UpdateListener;
import net.hardclient.hack.Hack;
import net.hardclient.settings.AttackSpeedSliderSetting;
import net.hardclient.settings.PauseAttackOnContainersSetting;
import net.hardclient.settings.SliderSetting;
import net.hardclient.settings.SliderSetting.ValueDisplay;
import net.hardclient.settings.SwingHandSetting;
import net.hardclient.settings.SwingHandSetting.SwingHand;
import net.hardclient.settings.filterlists.EntityFilterList;
import net.hardclient.util.EntityUtils;
import net.hardclient.util.RotationUtils;

@SearchTags({"multi aura", "ForceField", "force field"})
public final class MultiAuraHack extends Hack implements UpdateListener
{
	private final SliderSetting range =
		new SliderSetting("Range", 5, 1, 6, 0.05, ValueDisplay.DECIMAL);
	
	private final AttackSpeedSliderSetting speed =
		new AttackSpeedSliderSetting();
	
	private final SliderSetting fov =
		new SliderSetting("FOV", 360, 30, 360, 10, ValueDisplay.DEGREES);
	
	private final SwingHandSetting swingHand = new SwingHandSetting(
		SwingHandSetting.genericCombatDescription(this), SwingHand.CLIENT);
	
	private final PauseAttackOnContainersSetting pauseOnContainers =
		new PauseAttackOnContainersSetting(false);
	
	private final EntityFilterList entityFilters =
		EntityFilterList.genericCombat();
	
	public MultiAuraHack()
	{
		super("MultiAura");
		setCategory(Category.COMBAT);
		
		addSetting(range);
		addSetting(speed);
		addSetting(fov);
		addSetting(swingHand);
		addSetting(pauseOnContainers);
		
		entityFilters.forEach(this::addSetting);
	}
	
	@Override
	protected void onEnable()
	{
		// disable other killauras
		HARD.getHax().aimAssistHack.setEnabled(false);
		HARD.getHax().clickAuraHack.setEnabled(false);
		HARD.getHax().crystalAuraHack.setEnabled(false);
		HARD.getHax().fightBotHack.setEnabled(false);
		HARD.getHax().killauraLegitHack.setEnabled(false);
		HARD.getHax().killauraHack.setEnabled(false);
		HARD.getHax().protectHack.setEnabled(false);
		HARD.getHax().tpAuraHack.setEnabled(false);
		HARD.getHax().triggerBotHack.setEnabled(false);
		
		speed.resetTimer();
		EVENTS.add(UpdateListener.class, this);
	}
	
	@Override
	protected void onDisable()
	{
		EVENTS.remove(UpdateListener.class, this);
	}
	
	@Override
	public void onUpdate()
	{
		speed.updateTimer();
		if(!speed.isTimeToAttack())
			return;
		
		if(pauseOnContainers.shouldPause())
			return;
		
		// get entities
		Stream<Entity> stream = EntityUtils.getAttackableEntities();
		double rangeSq = Math.pow(range.getValue(), 2);
		stream = stream.filter(e -> MC.player.squaredDistanceTo(e) <= rangeSq);
		
		if(fov.getValue() < 360.0)
			stream = stream.filter(e -> RotationUtils.getAngleToLookVec(
				e.getBoundingBox().getCenter()) <= fov.getValue() / 2.0);
		
		stream = entityFilters.applyTo(stream);
		
		ArrayList<Entity> entities =
			stream.collect(Collectors.toCollection(ArrayList::new));
		if(entities.isEmpty())
			return;
		
		HARD.getHax().autoSwordHack.setSlot(entities.get(0));
		
		// attack entities
		for(Entity entity : entities)
		{
			RotationUtils
				.getNeededRotations(entity.getBoundingBox().getCenter())
				.sendPlayerLookPacket();
			
			MC.interactionManager.attackEntity(MC.player, entity);
		}
		
		swingHand.swing(Hand.MAIN_HAND);
		speed.resetTimer();
	}
}
