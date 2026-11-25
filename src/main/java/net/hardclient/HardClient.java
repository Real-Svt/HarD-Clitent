/*
 * Copyright (c) 2014-2026 Hard-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.hardclient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.mojang.blaze3d.systems.RenderSystem;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.hardclient.altmanager.AltManager;
import net.hardclient.altmanager.Encryption;
import net.hardclient.analytics.PlausibleAnalytics;
import net.hardclient.clickgui.ClickGui;
import net.hardclient.command.CmdList;
import net.hardclient.command.CmdProcessor;
import net.hardclient.command.Command;
import net.hardclient.event.EventManager;
import net.hardclient.events.ChatOutputListener;
import net.hardclient.events.GUIRenderListener;
import net.hardclient.events.GUIRenderListener.GUIRenderEvent;
import net.hardclient.events.KeyPressListener;
import net.hardclient.events.PostMotionListener;
import net.hardclient.events.PreMotionListener;
import net.hardclient.events.UpdateListener;
import net.hardclient.hack.Hack;
import net.hardclient.hack.HackList;
import net.hardclient.hud.IngameHUD;
import net.hardclient.keybinds.KeybindList;
import net.hardclient.keybinds.KeybindProcessor;
import net.hardclient.mixinterface.IMinecraftClient;
import net.hardclient.navigator.Navigator;
import net.hardclient.other_feature.OtfList;
import net.hardclient.other_feature.OtherFeature;
import net.hardclient.settings.SettingsFile;
import net.hardclient.update.ProblematicResourcePackDetector;
import net.hardclient.update.HardUpdater;
import net.hardclient.util.json.JsonException;

public enum HardClient
{
	INSTANCE;
	
	public static MinecraftClient MC;
	public static IMinecraftClient IMC;
	
	public static final String VERSION = "7.50.3";
	public static final String MC_VERSION = "1.20.1";
	
	private PlausibleAnalytics plausible;
	private EventManager eventManager;
	private AltManager altManager;
	private HackList hax;
	private CmdList cmds;
	private OtfList otfs;
	private SettingsFile settingsFile;
	private Path settingsProfileFolder;
	private KeybindList keybinds;
	private ClickGui gui;
	private Navigator navigator;
	private CmdProcessor cmdProcessor;
	private IngameHUD hud;
	private RotationFaker rotationFaker;
	private FriendsList friends;
	private HardTranslator translator;
	
	private boolean enabled = true;
	private static boolean guiInitialized;
	private HardUpdater updater;
	private ProblematicResourcePackDetector problematicPackDetector;
	private Path hardFolder;
	
	public void initialize()
	{
		System.out.println("Starting Hard Client...");
		
		MC = MinecraftClient.getInstance();
		IMC = (IMinecraftClient)MC;
		hardFolder = createHardFolder();
		
		Path analyticsFile = hardFolder.resolve("analytics.json");
		plausible = new PlausibleAnalytics(analyticsFile);
		plausible.pageview("/");
		
		eventManager = new EventManager(this);
		
		Path enabledHacksFile = hardFolder.resolve("enabled-hacks.json");
		hax = new HackList(enabledHacksFile);
		
		cmds = new CmdList();
		
		otfs = new OtfList();
		
		Path settingsFile = hardFolder.resolve("settings.json");
		settingsProfileFolder = hardFolder.resolve("settings");
		this.settingsFile = new SettingsFile(settingsFile, hax, cmds, otfs);
		this.settingsFile.load();
		hax.tooManyHaxHack.loadBlockedHacksFile();
		
		Path keybindsFile = hardFolder.resolve("keybinds.json");
		keybinds = new KeybindList(keybindsFile);
		
		Path guiFile = hardFolder.resolve("windows.json");
		gui = new ClickGui(guiFile);
		
		Path preferencesFile = hardFolder.resolve("preferences.json");
		navigator = new Navigator(preferencesFile, hax, cmds, otfs);
		
		Path friendsFile = hardFolder.resolve("friends.json");
		friends = new FriendsList(friendsFile);
		friends.load();
		
		translator = new HardTranslator();
		
		cmdProcessor = new CmdProcessor(cmds);
		eventManager.add(ChatOutputListener.class, cmdProcessor);
		
		KeybindProcessor keybindProcessor =
			new KeybindProcessor(hax, keybinds, cmdProcessor);
		eventManager.add(KeyPressListener.class, keybindProcessor);
		
		hud = new IngameHUD();
		eventManager.add(GUIRenderListener.class, hud);
		
		rotationFaker = new RotationFaker();
		eventManager.add(PreMotionListener.class, rotationFaker);
		eventManager.add(PostMotionListener.class, rotationFaker);
		
		updater = new HardUpdater();
		eventManager.add(UpdateListener.class, updater);
		
		problematicPackDetector = new ProblematicResourcePackDetector();
		problematicPackDetector.start();
		
		Path altsFile = hardFolder.resolve("alts.encrypted_json");
		Path encFolder = Encryption.chooseEncryptionFolder();
		altManager = new AltManager(altsFile, encFolder);
		
		if(FabricLoader.getInstance().isModLoaded("connectormod"))
		{
			System.out.println("[Hard] Applying fixes for Sinytra Connector");
			HudRenderCallback.EVENT.register(this::onHudRender);
		}
	}
	
	// Alternative HUD rendering when using Sinytra Connector
	private void onHudRender(DrawContext context, float tickDelta)
	{
		if(MC.options.debugEnabled)
			return;
		
		RenderSystem.disableBlend();
		EventManager.fire(new GUIRenderEvent(context, tickDelta));
		RenderSystem.disableBlend();
	}
	
	private Path createHardFolder()
	{
		Path dotMinecraftFolder = MC.runDirectory.toPath().normalize();
		Path hardFolder = dotMinecraftFolder.resolve("hard");
		
		try
		{
			Files.createDirectories(hardFolder);
			
		}catch(IOException e)
		{
			throw new RuntimeException(
				"Couldn't create .minecraft/hard folder.", e);
		}
		
		return hardFolder;
	}
	
	public String translate(String key, Object... args)
	{
		return translator.translate(key, args);
	}
	
	public PlausibleAnalytics getPlausible()
	{
		return plausible;
	}
	
	public EventManager getEventManager()
	{
		return eventManager;
	}
	
	public void saveSettings()
	{
		settingsFile.save();
	}
	
	public ArrayList<Path> listSettingsProfiles()
	{
		if(!Files.isDirectory(settingsProfileFolder))
			return new ArrayList<>();
		
		try(Stream<Path> files = Files.list(settingsProfileFolder))
		{
			return files.filter(Files::isRegularFile)
				.collect(Collectors.toCollection(ArrayList::new));
			
		}catch(IOException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public void loadSettingsProfile(String fileName)
		throws IOException, JsonException
	{
		settingsFile.loadProfile(settingsProfileFolder.resolve(fileName));
	}
	
	public void saveSettingsProfile(String fileName)
		throws IOException, JsonException
	{
		settingsFile.saveProfile(settingsProfileFolder.resolve(fileName));
	}
	
	public HackList getHax()
	{
		return hax;
	}
	
	public CmdList getCmds()
	{
		return cmds;
	}
	
	public OtfList getOtfs()
	{
		return otfs;
	}
	
	public Feature getFeatureByName(String name)
	{
		Hack hack = getHax().getHackByName(name);
		if(hack != null)
			return hack;
		
		Command cmd = getCmds().getCmdByName(name.substring(1));
		if(cmd != null)
			return cmd;
		
		OtherFeature otf = getOtfs().getOtfByName(name);
		return otf;
	}
	
	public KeybindList getKeybinds()
	{
		return keybinds;
	}
	
	public ClickGui getGui()
	{
		if(!guiInitialized)
		{
			guiInitialized = true;
			gui.init();
		}
		
		return gui;
	}
	
	public Navigator getNavigator()
	{
		return navigator;
	}
	
	public CmdProcessor getCmdProcessor()
	{
		return cmdProcessor;
	}
	
	public IngameHUD getHud()
	{
		return hud;
	}
	
	public RotationFaker getRotationFaker()
	{
		return rotationFaker;
	}
	
	public FriendsList getFriends()
	{
		return friends;
	}
	
	public HardTranslator getTranslator()
	{
		return translator;
	}
	
	public boolean isEnabled()
	{
		return enabled;
	}
	
	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
		
		if(!enabled)
		{
			hax.panicHack.setEnabled(true);
			hax.panicHack.onUpdate();
		}
	}
	
	public HardUpdater getUpdater()
	{
		return updater;
	}
	
	public ProblematicResourcePackDetector getProblematicPackDetector()
	{
		return problematicPackDetector;
	}
	
	public Path getHardFolder()
	{
		return hardFolder;
	}
	
	public AltManager getAltManager()
	{
		return altManager;
	}
}
