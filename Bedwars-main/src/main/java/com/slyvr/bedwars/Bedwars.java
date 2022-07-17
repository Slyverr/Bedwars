package com.slyvr.bedwars;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitWorker;
import org.bukkit.util.NumberConversions;

import com.slyvr.api.arena.Arena;
import com.slyvr.api.arena.Region;
import com.slyvr.api.bedwars.BedwarsPlugin;
import com.slyvr.api.bedwars.UpgradesManager;
import com.slyvr.api.entity.GameEntityManager;
import com.slyvr.api.game.Game;
import com.slyvr.api.game.GameMode;
import com.slyvr.api.game.GamePhase;
import com.slyvr.api.generator.GeneratorSpeed;
import com.slyvr.api.generator.GeneratorTier;
import com.slyvr.api.generator.Resource;
import com.slyvr.api.level.BedwarsLevel;
import com.slyvr.api.npc.NPCManager;
import com.slyvr.api.prestige.Prestige;
import com.slyvr.api.scoreboard.lobby.LobbyScoreboard;
import com.slyvr.api.shop.Shop;
import com.slyvr.api.trap.Trap;
import com.slyvr.api.trap.TrapTarget;
import com.slyvr.api.upgrade.TieredUpgrade;
import com.slyvr.api.upgrade.Upgrade;
import com.slyvr.api.upgrade.shop.UpgradeShop;
import com.slyvr.api.user.User;
import com.slyvr.api.util.Version;
import com.slyvr.arena.BedwarsArena;
import com.slyvr.bedwars.settings.BedwarsSettings;
import com.slyvr.bedwars.settings.GameSettings;
import com.slyvr.bedwars.settings.MapForgeSettings;
import com.slyvr.bedwars.settings.TeamForgeSettings;
import com.slyvr.commands.BedwarsCommand;
import com.slyvr.commands.PlayCommand;
import com.slyvr.commands.RejoinCommand;
import com.slyvr.commands.ShoutCommand;
import com.slyvr.database.Database;
import com.slyvr.game.AbstractGame;
import com.slyvr.game.BedwarsGame;
import com.slyvr.game.phase.BedBreakPhase;
import com.slyvr.game.phase.DiamondUpgradePhase_2;
import com.slyvr.game.phase.DiamondUpgradePhase_3;
import com.slyvr.game.phase.EmeraldUpgradePhase_2;
import com.slyvr.game.phase.EmeraldUpgradePhase_3;
import com.slyvr.game.phase.GameEndPhase;
import com.slyvr.listener.GameEntityListener;
import com.slyvr.listener.GameMechanicsListener;
import com.slyvr.listener.GamePlayerChatListener;
import com.slyvr.listener.GamePlayerListener;
import com.slyvr.listener.HologramListener;
import com.slyvr.listener.MapListener;
import com.slyvr.listener.ShopListener;
import com.slyvr.listener.UserListener;
import com.slyvr.prestige.PrestigeConfig;
import com.slyvr.scoreboard.ScoreboardConfig;
import com.slyvr.scoreboard.game.GameScoreboard;
import com.slyvr.scoreboard.waiting.WaitingScoreboard;
import com.slyvr.shop.ShopConfig;
import com.slyvr.trap.EffectTrap;
import com.slyvr.upgrade.EnchantmentUpgrade;
import com.slyvr.upgrade.ForgeUpgrade;
import com.slyvr.upgrade.HealPoolUpgrade;
import com.slyvr.upgrade.TieredEffectUpgrade;
import com.slyvr.upgrade.TieredEnchantmentUpgrade;
import com.slyvr.upgrade.shop.UpgradeShopConfig;
import com.slyvr.user.BedwarsUser;
import com.slyvr.util.ChatUtils;
import com.slyvr.util.metrics.Metrics;
import com.slyvr.util.metrics.Metrics.AdvancedPie;

public class Bedwars extends JavaPlugin implements BedwarsPlugin {

	private static Bedwars instance;

	private Map<UUID, BedwarsUser> loaded_users = new ConcurrentHashMap<>();

	private ScoreboardConfig scoreboardConfig;
	private PrestigeConfig prestigeConfig;

	private UpgradeShopConfig upgradeConfig;
	private ShopConfig shopConfig;

	private Database database;

	private BedwarsSettings settings;
	private GameSettings gameSettings;

	private MapForgeSettings mapForgeSettings;
	private TeamForgeSettings teamForgeSettings;

	private UpgradesManager upgradeManager;

	private String prefix;
	private Version version;

	private GameEntityManager gameEntityManager;
	private NPCManager npcmanager;

	@Override
	public void onLoad() {
		if ((this.version = Version.getVersion()) != Version.UNSUPPORTED)
			return;

		ConsoleCommandSender console = Bukkit.getConsoleSender();
		console.sendMessage("§cUnsupported version detected: §e" + Version.getVersionName());
		console.sendMessage("§cDisabling!");

		Bukkit.getPluginManager().disablePlugin(this);
	}

	@Override
	public void onEnable() {
		Bedwars.instance = this;
		this.prefix = ChatColor.GOLD + "[" + ChatColor.AQUA + "Bedwars" + ChatColor.GOLD + "] " + ChatColor.RESET;

		init();
		initMetrics();

		ConsoleCommandSender console = Bukkit.getConsoleSender();

		try {
			initNPCManager();
		} catch (Exception e) {
			console.sendMessage("§cCould not initialize NPC Manager (§e" + this.version + "§c)");
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}

		try {
			initEntityManager();
		} catch (Exception e) {
			console.sendMessage("§cCould not initialize Entity Manager (§e" + this.version + "§c)");
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}

		console.sendMessage(this.prefix + ChatColor.GRAY + "Registering commands!");
		registerCommands();

		console.sendMessage(this.prefix + ChatColor.GRAY + "Registering listeners!");
		registerListeners();

		console.sendMessage(this.prefix + ChatColor.GRAY + "Registering upgrades!");
		registerUpgrades();

		console.sendMessage(this.prefix + ChatColor.GRAY + "Registering game phases!");
		registerGamePhases();

		registerConfiguration();

		console.sendMessage(this.prefix + ChatColor.GRAY + "Loading bedwars settings!");
		loadSettings();

		console.sendMessage(this.prefix + ChatColor.GRAY + "Loading game settings!");
		loadGameSettings();

		console.sendMessage(this.prefix + ChatColor.GRAY + "Loading team forge settings!");
		loadTeamForgeSettings();

		console.sendMessage(this.prefix + ChatColor.GRAY + "Loading map forge settings!");
		loadMapForgeSettings();

		console.sendMessage(this.prefix + ChatColor.GRAY + "Loading database info!");
		loadDatabase();

		console.sendMessage(this.prefix + ChatColor.GRAY + "Loading prestiges!");
		loadPrestiges();

		console.sendMessage(this.prefix + ChatColor.GRAY + "Loading arenas!");
		loadArenas();

		console.sendMessage(this.prefix + ChatColor.GRAY + "Loading users!");
		loadUsers();

		console.sendMessage(this.prefix + "§6Detected NMS Version: " + this.version);
		console.sendMessage(this.prefix + "§aBedwars has been enabled!");
	}

	@Override
	public void onDisable() {
		for (BukkitWorker worker : Bukkit.getScheduler().getActiveWorkers()) {
			if (!worker.getOwner().equals(this))
				continue;

			try {
				worker.getThread().start();
			} catch (Throwable e) {
			}
		}

		Bukkit.getScheduler().cancelTasks(this);

		ConsoleCommandSender console = Bukkit.getConsoleSender();

		console.sendMessage(this.prefix + "§cShutting down all available games!");
		for (Entry<Arena, Game> entry : AbstractGame.getGames().entrySet())
			entry.getValue().stopGame();

		console.sendMessage(this.prefix + "§cSaving loaded arenas!");
		for (Arena arena : BedwarsArena.getArenas())
			arena.saveArena();

		console.sendMessage(this.prefix + "§cSaving loaded users data!");
		for (BedwarsUser user : this.loaded_users.values()) {
			user.saveInDatabase();
			user.saveData();
		}

		if (this.database != null)
			this.database.disconnect();

		Bukkit.getConsoleSender().sendMessage(this.prefix + "§cBedwars has been disabled!");
	}

	private void init() {
		saveDefaultConfig();

		this.scoreboardConfig = ScoreboardConfig.getInstance();
		this.prestigeConfig = PrestigeConfig.getInstance();

		this.upgradeConfig = UpgradeShopConfig.getInstance();
		this.shopConfig = ShopConfig.getInstance();

		this.settings = BedwarsSettings.getInstance();
		this.gameSettings = GameSettings.getInstance();
		this.teamForgeSettings = TeamForgeSettings.getInstance();
		this.mapForgeSettings = MapForgeSettings.getInstance();

		this.upgradeManager = UpgradesManager.getInstance();
	}

	private void initMetrics() {
		Metrics metrics = new Metrics(this, 14317);

		metrics.addCustomChart(new AdvancedPie("popular_modes", new Callable<Map<String, Integer>>() {

			@Override
			public Map<String, Integer> call() throws Exception {
				Map<String, Integer> result = new HashMap<>(4);
				result.put("Solo", getModesPlayersCount(GameMode.SOLO));
				result.put("Doubles", getModesPlayersCount(GameMode.DUO));
				result.put("3v3v3v3", getModesPlayersCount(GameMode.TRIO));
				result.put("4v4v4v4", getModesPlayersCount(GameMode.QUATUOR));

				return result;
			}

			private int getModesPlayersCount(GameMode mode) {
				int result = 0;
				for (Game game : AbstractGame.getGames().values())
					if (game.getMode() == mode)
						result += game.getGamePlayers().size();

				return result;
			}

		}));
	}

	private void loadArenas() {
		for (String name : BedwarsArena.getArenasNameList()) {
			Arena arena = new BedwarsArena(name);
			arena.reloadArena();

			Region region = arena.getRegion();
			if (region != null)
				region.getWorld().setAutoSave(false);
		}

	}

	private void loadUsers() {
		LobbyScoreboard board = getLobbyScoreboard();

		for (Player player : Bukkit.getOnlinePlayers()) {
			User user = loadUser(player);
			if (user == null)
				continue;

			BedwarsLevel.setForPlayer(player, user.getLevel());

			user.setScoreboard(board);

			UserListener.addPlayerToUpdatingBoard(player);
		}

	}

	private void registerCommands() {
		getCommand("Bedwars").setExecutor(new BedwarsCommand());
		getCommand("Rejoin").setExecutor(new RejoinCommand());
		getCommand("Play").setExecutor(new PlayCommand());
		getCommand("Shout").setExecutor(new ShoutCommand());
	}

	private void registerListeners() {
		PluginManager manager = Bukkit.getPluginManager();

		manager.registerEvents(new GameEntityListener(), this);
		manager.registerEvents(new GameMechanicsListener(), this);
		manager.registerEvents(new GamePlayerChatListener(), this);
		manager.registerEvents(new GamePlayerListener(), this);

		manager.registerEvents(new HologramListener(), this);
		// manager.registerEvents(new QuickBuyListener(), this);
		manager.registerEvents(new ShopListener(), this);
		manager.registerEvents(new UserListener(), this);
		manager.registerEvents(new MapListener(), this);
	}

	private void registerConfiguration() {
		// ConfigurationSerialization.registerClass(Statistics.class);
		// ConfigurationSerialization.registerClass(Level.class);
		// ConfigurationSerialization.registerClass(Region.class);

	}

	private void registerUpgrades() {
		/** Upgrades **/

		Upgrade sharpness = new EnchantmentUpgrade("Sharpnened Swords", Enchantment.DAMAGE_ALL, 1, false);
		this.upgradeManager.registerUpgrade("Sharpness", sharpness);

		// Upgrade dragon = new DragonBuffUpgrade();
		// this.upgradeManager.registerUpgrade("Dragon Buff", dragon);
		// this.upgradeManager.registerUpgrade("Dragon_Buff", dragon);

		Upgrade healpool = new HealPoolUpgrade();
		this.upgradeManager.registerUpgrade("Heal Pool", healpool);
		this.upgradeManager.registerUpgrade("Heal_Pool", healpool);

		/** Tiered Upgrades **/

		TieredUpgrade protection = new TieredEnchantmentUpgrade("Protection", Enchantment.PROTECTION_ENVIRONMENTAL, 4, false);
		this.upgradeManager.registerTieredUpgrade("Protection", protection);

		TieredUpgrade forge = new ForgeUpgrade();
		this.upgradeManager.registerTieredUpgrade("Forge", forge);

		PotionEffect haste1 = new PotionEffect(PotionEffectType.FAST_DIGGING, Integer.MAX_VALUE, 0);
		PotionEffect haste2 = new PotionEffect(PotionEffectType.FAST_DIGGING, Integer.MAX_VALUE, 1);

		TieredUpgrade fast_miner = new TieredEffectUpgrade("Maniac Miner", Arrays.asList(haste1, haste2));
		this.upgradeManager.registerTieredUpgrade("Maniac Miner", fast_miner);
		this.upgradeManager.registerTieredUpgrade("Maniac_Miner", fast_miner);

		/** Traps **/

		PotionEffect speed = new PotionEffect(PotionEffectType.SPEED, 15 * 20, 1);
		PotionEffect jump = new PotionEffect(PotionEffectType.JUMP, 15 * 20, 1);

		Trap counter = new EffectTrap("Counter Offensive", TrapTarget.PLAYER_TEAM, 15, speed, jump);
		this.upgradeManager.registerTrapUpgrade("Counter_Offensive", counter);
		this.upgradeManager.registerTrapUpgrade("Counter-Offensive", counter);
		this.upgradeManager.registerTrapUpgrade("Counter Offensive", counter);

		PotionEffect blind = new PotionEffect(PotionEffectType.BLINDNESS, 20 * 8, 0);

		Trap blindness = new EffectTrap("Blindness", TrapTarget.ENEMY, 8, blind);
		this.upgradeManager.registerTrapUpgrade("Blindness", blindness);

		PotionEffect slow_dig = new PotionEffect(PotionEffectType.SLOW_DIGGING, 20 * 10, 0);

		Trap fatigue = new EffectTrap("Mining Fatigue", TrapTarget.ENEMY, 10, slow_dig);
		this.upgradeManager.registerTrapUpgrade("Mining Fatigue", fatigue);
		this.upgradeManager.registerTrapUpgrade("Mining_Fatigue", fatigue);

	}

	private void registerGamePhases() {
		registerGamePhase(new DiamondUpgradePhase_2(60 * 6));
		registerGamePhase(new DiamondUpgradePhase_3(60 * 6));
		registerGamePhase(new EmeraldUpgradePhase_2(60 * 6));
		registerGamePhase(new EmeraldUpgradePhase_3(60 * 6));
		registerGamePhase(new BedBreakPhase(60 * 10));
		// registerGamePhase(new SuddenDeathPhase(60 * 10));
		registerGamePhase(new GameEndPhase(60 * 10));
	}

	private void loadPrestiges() {
		PrestigeConfig.getInstance().loadPrestiges();
	}

	private void loadSettings() {
		FileConfiguration config = getConfig();

		ConfigurationSection section = config.getConfigurationSection("Bedwars-Settings.Minimum-Players");
		if (section != null) {
			for (String key : section.getKeys(false)) {
				GameMode mode = GameMode.getByName(key);

				if (mode == null)
					continue;

				int min = config.getInt(section.getCurrentPath() + "." + key);
				if (min > mode.getGameMax())
					min = mode.getGameMax() - mode.getTeamMax();

				this.settings.setMinimumPlayers(mode, min >= 2 ? min : 2);
			}
		}

		this.settings.setGameCountdown(config.getInt("Bedwars-Settings.Countdown"));
		this.settings.setDefaultLevelUpExp(config.getInt("Bedwars-Settings.LevelUp-Exp.default"));

		ConfigurationSection section2 = config.getConfigurationSection("Bedwars-Settings.LevelUp-Exp.levels");
		if (section2 != null) {
			for (String key : section2.getKeys(false)) {
				int level = NumberConversions.toInt(key);

				if (level > 0)
					this.settings.setLevelUpExpFor(level, config.getInt(section2.getCurrentPath() + "." + key));
			}
		}

		int respawnTime = config.getInt("Bedwars-Settings.Respawn-Time");
		int reconnectRespawnTime = config.getInt("Bedwars-Settings.Reconnect.respawn-time");

		this.settings.setRespawnTime(respawnTime);
		this.settings.setReconnectRespawnTime(reconnectRespawnTime);

		boolean autoReconnect = config.getBoolean("Bedwars-Settings.Reconnect.auto-reconnect");
		this.settings.setAutoReconnect(autoReconnect);
	}

	private void loadGameSettings() {
		FileConfiguration config = getConfig();

		List<GamePhase> phases = new ArrayList<>();
		for (String key : config.getStringList("Game-Settings.Game-Phases")) {
			GamePhase phase = GamePhase.getByName(key);

			if (phase == null)
				getLogger().log(Level.WARNING, "Could not recognize game phase with the name of " + key);
			else
				phases.add(phase);
		}

		this.gameSettings.setDefaultGamePhases(phases);

		float tntPower = (float) config.getDouble("Game-Settings.Game-Mechanics.TNT.power");
		float tntKb = (float) config.getDouble("Game-Settings.Game-Mechanics.TNT.kb");

		this.gameSettings.setTNTExplosionPower(tntPower);
		this.gameSettings.setTNTExplosionKb(tntKb);
		this.gameSettings.setTNTFuseTicks(config.getInt("Game-Settings.Game-Mechanics.TNT.fuse-ticks"));
		this.gameSettings.setShowTNTFuseTicks(config.getBoolean("Game-Settings.Game-Mechanics.TNT.show-fuse-ticks"));

		float fbPower = (float) config.getDouble("Game-Settings.Game-Mechanics.Fireball.power");
		float fbSpeed = (float) config.getDouble("Game-Settings.Game-Mechanics.Fireball.speed");
		float fbKb = (float) config.getDouble("Game-Settings.Game-Mechanics.Fireball.kb");

		this.gameSettings.setFireballExplosionPower(fbPower);
		this.gameSettings.setFireballExplosionKb(fbKb);
		this.gameSettings.setFireballSpeed(fbSpeed);

		long timePlayedForExp = config.getLong("Game-Settings.Time-Played-Rewards.Exp.time-played");
		int expReward = config.getInt("Game-Settings.Time-Played-Rewards.Exp.amount");

		long timePlayedForCoins = config.getLong("Game-Settings.Time-Played-Rewards.Coins.time-played");
		int coinsReward = config.getInt("Game-Settings.Time-Played-Rewards.Coins.amount");

		this.gameSettings.setTimeForExpReward(timePlayedForExp);
		this.gameSettings.setExpReward(expReward);

		this.gameSettings.setTimeForCoinsReward(timePlayedForCoins);
		this.gameSettings.setCoinsReward(coinsReward);
	}

	private void loadTeamForgeSettings() {
		FileConfiguration config = getConfig();

		String path = "Forge-Settings.Team-Forge";

		// Resource splitting
		this.teamForgeSettings.setResourceSplitting(config.getBoolean(path + ".Resource-Splitting.enabled"));
		this.teamForgeSettings.setSplitRadius(config.getDouble(path + ".Resource-Splitting.radius"));

		ConfigurationSection limitSection = config.getConfigurationSection(path + ".Drop-Limit");
		if (limitSection != null) {
			for (String limitKey : limitSection.getKeys(false)) {
				Resource resource = Resource.getByName(limitKey);
				if (resource == null)
					continue;

				for (GameMode mode : GameMode.values())
					this.teamForgeSettings.setDropLimit(mode, resource, limitSection.getInt(limitKey));
			}
		}

		ConfigurationSection speedSection = config.getConfigurationSection(path + ".Speeds");
		if (speedSection != null) {
			for (String speedKey : speedSection.getKeys(false)) {
				GeneratorSpeed speed = new GeneratorSpeed(speedKey);

				ConfigurationSection resourceSection = speedSection.getConfigurationSection(speedKey);
				if (resourceSection != null)
					for (String resourceKey : resourceSection.getKeys(false)) {
						Resource resource = Resource.getByName(resourceKey);
						if (resource == null)
							continue;

						int value = resourceSection.getInt(resourceKey);
						if (value <= 0)
							continue;

						speed.setDropsPerMinute(resource, value);
					}
				else
					for (Resource rsc : Resource.values()) {
						int value = speedSection.getInt(speedKey);
						if (value <= 0)
							continue;

						speed.setDropsPerMinute(rsc, value);
					}

				this.teamForgeSettings.setGeneratorSpeed(speed);

				GeneratorSpeed.registerSpeed(speed);
			}
		}

		Set<Resource> drops = new HashSet<>();
		for (String s : config.getStringList(path + ".Drops")) {
			Resource resource = Resource.getByName(s);
			if (resource != null)
				drops.add(resource);
		}

		if (!drops.isEmpty())
			this.teamForgeSettings.setTeamDrops(drops);

	}

	private void loadMapForgeSettings() {
		FileConfiguration config = getConfig();

		ConfigurationSection section = config.getConfigurationSection("Forge-Settings.Map-Forge");
		if (section == null)
			return;

		for (String key : section.getKeys(false)) {
			Resource resource = Resource.getByName(key);
			if (resource == null)
				continue;

			Map<GameMode, Integer> limit = new HashMap<>();

			ConfigurationSection limitSection = section.getConfigurationSection(key + ".limit");
			if (limitSection != null) {
				for (String limitKey : limitSection.getKeys(false)) {
					GameMode mode = GameMode.getByName(limitKey);
					if (mode == null)
						continue;

					limit.put(mode, limitSection.getInt(limitKey, 0));
				}
			} else
				for (GameMode mode : GameMode.values())
					limit.put(mode, section.getInt(key + ".limit", 0));

			ConfigurationSection tiersSection = section.getConfigurationSection(key + ".tiers");
			if (tiersSection == null)
				continue;

			for (GameMode mode : GameMode.values()) {
				List<GeneratorTier> tiers = new ArrayList<>();

				for (String tierKey : tiersSection.getKeys(false)) {
					String title = tiersSection.getString(tierKey + ".title");
					if (title == null)
						continue;

					int time = tiersSection.getInt(tierKey + ".drop-time");
					if (time <= 0)
						continue;

					Integer dropLimit = limit.get(mode);
					if (dropLimit == null || dropLimit <= 0)
						continue;

					tiers.add(new GeneratorTier(title, time, dropLimit));
				}

				this.mapForgeSettings.setGeneratorTiers(mode, resource, tiers);
			}

		}

	}

	private void loadDatabase() {
		FileConfiguration config = getConfig();

		ConsoleCommandSender console = Bukkit.getConsoleSender();

		boolean enabled = config.getBoolean("Database.enabled");
		if (!enabled) {
			console.sendMessage(ChatUtils.info("Connecting to database is disabled! "));
			console.sendMessage(ChatUtils.info("You can always enable database at config.yml"));
			return;
		}

		String name = config.getString("Database.name", "");
		String password = config.getString("Database.password", "");
		String url = config.getString("Database.url", "");

		this.database = new Database(name, password, url);
		this.database.connect();

		if (!this.database.isConnected())
			console.sendMessage(ChatUtils.error("Could not connect to database! Please check your database info at config.yml"));
		else
			console.sendMessage(ChatUtils.success("Connected to database!"));

		if (!this.database.isConnected())
			return;

		this.database.createCoinsTable();

		for (GameMode mode : GameMode.values())
			this.database.createStatsTable(mode);

		if (config.getBoolean("save-userdata")) {
			new BukkitRunnable() {

				@Override
				public void run() {
					try {
						for (BedwarsUser user : Bedwars.this.loaded_users.values())
							user.saveInDatabase();

						getLogger().info("Saved users data to database!");
					} catch (Exception e) {
						getLogger().severe("An error occured while saving users data!");
					}

				}
			}.runTaskTimerAsynchronously(this, 0, 20 * 60 * 30); // TODO: configurable
		}

	}

	private void initNPCManager() throws Exception {
		this.npcmanager = (NPCManager) Class.forName("com.slyvr." + this.version + ".npc.NPCManager").newInstance();
	}

	private void initEntityManager() throws Exception {
		this.gameEntityManager = (GameEntityManager) Class.forName("com.slyvr." + this.version + ".entity.GameEntityManager").newInstance();
	}

	/**
	 * Gets instance of plugin
	 *
	 * @return instance of plugin
	 */
	public static Bedwars getInstance() {
		return Bedwars.instance;
	}

	/**
	 * Gets the NMS version of this server
	 *
	 * @return NMS version of this server
	 */
	public Version getVersion() {
		return this.version;
	}

	@Override
	public String getPluginPrefix() {
		return this.prefix;
	}

	/**
	 * Gets plugin database if exists
	 *
	 * @return plugin database if exists
	 */
	public Database getDataBase() {
		return this.database;
	}

	@Override
	public Prestige getDefaultPrestige() {
		return this.prestigeConfig.getDefaultPrestige();
	}

	/**
	 * Gets prestige config
	 *
	 * @return Prestige config
	 */
	public PrestigeConfig getPrestigeConfig() {
		return this.prestigeConfig;
	}

	/**
	 * Gets settings
	 *
	 * @return settings
	 */
	public BedwarsSettings getSettings() {
		return this.settings;
	}

	/**
	 * Gets game settings
	 *
	 * @return game settings
	 */
	public GameSettings getGameSettings() {
		return this.gameSettings;
	}

	/**
	 * Gets forge settings
	 *
	 * @return forge settings
	 */
	public TeamForgeSettings getTeamForgeSettings() {
		return this.teamForgeSettings;
	}

	/**
	 * Gets map forge settings
	 *
	 * @return Map forge settings
	 */
	public MapForgeSettings getMapForgeSettings() {
		return this.mapForgeSettings;
	}

	/**
	 * Gets scoreboard config
	 *
	 * @return scoreboard config
	 */
	public ScoreboardConfig getScoreboardConfig() {
		return this.scoreboardConfig;
	}

	/**
	 * Gets game scoreboard
	 *
	 * @param mode GameMode of the scoreboard
	 *
	 * @return Scoreboard if exists, otherwise null
	 */
	public GameScoreboard getGameScoreboard(GameMode mode) {
		return this.scoreboardConfig.getScoreboard(mode);
	}

	/**
	 * Gets waiting-room scoreboard
	 *
	 * @return Scoreboard if exists, otherwise null
	 */
	public WaitingScoreboard getWaitingScoreboard() {
		return this.scoreboardConfig.getWaitingScoreboard();
	}

	@Override
	public LobbyScoreboard getLobbyScoreboard() {
		return this.scoreboardConfig.getLobbyScoreboard();
	}

	@Override
	public GamePhase getGamePhase(String name) {
		return GamePhase.getByName(name);
	}

	@Override
	public void registerGamePhase(GamePhase phase) {
		GamePhase.registerGamePhase(phase);
	}

	@Override
	public NPCManager getNPCManager() {
		return this.npcmanager;
	}

	@Override
	public GameEntityManager getEntityManager() {
		return this.gameEntityManager;
	}

	@Override
	public Shop getTeamShop(GameMode mode) {
		return this.shopConfig.getShop(mode);
	}

	@Override
	public UpgradeShop getTeamUpgradeShop(GameMode mode) {
		return this.upgradeConfig.getUpgradeShop(mode);
	}

	@Override
	public UpgradesManager getUpgradesManager() {
		return this.upgradeManager;
	}

	/**
	 * Gets ItemShop config
	 *
	 * @return ItemShop config
	 */
	public ShopConfig getShopConfig() {
		return this.shopConfig;
	}

	/**
	 * Gets UpgradeShop config
	 *
	 * @return UpgradeShop config
	 */
	public UpgradeShopConfig getUpgradeShopConfig() {
		return this.upgradeConfig;
	}

	@Override
	public User loadUser(Player player) {
		if (player == null)
			return null;

		BedwarsUser user = this.loaded_users.get(player.getUniqueId());
		if (user != null)
			return user;

		user = new BedwarsUser(player);
		user.loadData();

		this.loaded_users.put(player.getUniqueId(), user);
		return user;
	}

	@Override
	public User getUser(Player player) {
		return loadUser(player);
	}

	@Override
	public Arena getArena(String name) {
		return BedwarsArena.getArena(name);
	}

	@Override
	public Game addPlayerToRandomGame(Player player, GameMode mode) {
		if (mode != null)
			for (Game game : AbstractGame.getGames().values())
				if (game.getMode().equals(mode) && !game.hasStarted() && game.addPlayer(player))
					return game;

		Game game = BedwarsGame.randomGame(mode);
		return game != null && game.addPlayer(player) ? game : null;
	}

	@Override
	public Game addPlayerToRandomGame(Player player) {
		return addPlayerToRandomGame(player, null);
	}

	@Override
	public Game getRandomGame(GameMode mode) {
		if (mode != null)
			for (Game game : AbstractGame.getGames().values())
				if (game.getMode().equals(mode) && !game.hasStarted())
					return game;

		return BedwarsGame.randomGame(mode);
	}

	@Override
	public Game getRandomGame() {
		return getRandomGame(null);
	}

	@Override
	public Game getPlayerGame(Player player) {
		return AbstractGame.getPlayerGame(player);
	}

	@Override
	public boolean inGame(Player player) {
		return AbstractGame.inGame(player);
	}

	@Override
	public boolean inRunningGame(Player player) {
		return AbstractGame.inRunningGame(player);
	}

	@Override
	public boolean sendPlayerToLobby(Game game, Player player, String message) {
		if (game == null || player == null)
			return false;

		Arena arena = game.getArena();

		Location lobby = arena.getLobbySpawnPoint();
		if (lobby != null)
			return player.teleport(lobby);

		// player.kickPlayer(message);
		return true;
	}

}