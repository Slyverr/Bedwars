package com.slyvr.arena;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import com.google.common.base.Preconditions;
import com.slyvr.api.arena.Arena;
import com.slyvr.api.arena.BedwarsBed;
import com.slyvr.api.arena.Region;
import com.slyvr.api.game.GameMode;
import com.slyvr.api.generator.GeneratorSpeed;
import com.slyvr.api.generator.Resource;
import com.slyvr.api.team.Team;
import com.slyvr.api.util.LocationUtils;
import com.slyvr.bedwars.Bedwars;
import com.slyvr.configuration.Configuration;
import com.slyvr.util.ConfigUtils;

public final class BedwarsArena extends Configuration implements Arena {

	private static final Map<String, BedwarsArena> ARENAS = new HashMap<>();

	private Map<Resource, List<Location>> resource_gen = new HashMap<>();

	private Map<Team, BedwarsBed> team_bed = new EnumMap<>(Team.class);
	private Map<Team, Location> team_spawn = new EnumMap<>(Team.class);
	private Map<Team, Location> team_shop = new EnumMap<>(Team.class);
	private Map<Team, Location> team_upgr = new EnumMap<>(Team.class);
	private Map<Team, Location> team_gen = new EnumMap<>(Team.class);
	private Map<Team, Chest> team_chest = new EnumMap<>(Team.class);

	private Set<Team> teams = EnumSet.noneOf(Team.class);

	private Location spectator;
	private Location waiting;
	private Location dragon;
	private Location lobby;

	private Region waitingRegion;
	private Region arenaRegion;

	private GeneratorSpeed speed;
	private GameMode mode;

	private String mapName;
	private String name;

	private int time;
	private boolean enabled;

	private ConfigUtils utils;

	public BedwarsArena(String name) {
		super(new File(Bedwars.getInstance().getDataFolder() + "/Arenas", name + ".yml"));

		Preconditions.checkNotNull(name, "Arena name cannot be null!");

		this.name = name;
		initArena();
	}

	private void initArena() {
		String name = this.name.toLowerCase();

		BedwarsArena arena = BedwarsArena.ARENAS.get(name);
		if (arena == null) {
			ARENAS.put(name, this);
			return;
		}

		this.resource_gen = arena.resource_gen;

		this.team_bed = arena.team_bed;
		this.team_spawn = arena.team_spawn;
		this.team_shop = arena.team_shop;
		this.team_upgr = arena.team_upgr;
		this.team_gen = arena.team_gen;
		this.team_chest = arena.team_chest;
		this.teams = arena.teams;

		this.spectator = arena.spectator;
		this.waiting = arena.waiting;
		this.dragon = arena.dragon;
		this.lobby = arena.lobby;

		this.waitingRegion = arena.waitingRegion;
		this.arenaRegion = arena.arenaRegion;

		this.speed = arena.speed;
		this.mode = arena.mode;

		this.mapName = arena.mapName;
		this.name = arena.name;

		this.time = arena.time;
		this.enabled = arena.enabled;

		this.config = arena.config;
		this.utils = arena.utils;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String getMapName() {
		return this.mapName;
	}

	@Override
	public int getTime() {
		return this.time;
	}

	@Override
	public GameMode getMode() {
		return this.mode;
	}

	@Override
	public Set<Team> getTeams() {
		return new HashSet<>(this.teams);
	}

	@Override
	public List<Location> getResourceGenLocations(Resource resource) {
		if (resource == null)
			return new ArrayList<>(0);

		List<Location> locations = this.resource_gen.get(resource);
		if (locations == null)
			return new ArrayList<>(0);

		List<Location> result = new ArrayList<>();

		for (Location loc : locations)
			result.add(loc.clone());

		return result;
	}

	@Override
	public GeneratorSpeed getGeneratorSpeed() {
		return this.speed;
	}

	@Override
	public Location getTeamShop(Team team) {
		if (team == null)
			return null;

		Location result = this.team_shop.get(team);
		return result != null ? result.clone() : null;
	}

	@Override
	public Location getTeamUpgrade(Team team) {
		if (team == null)
			return null;

		Location result = this.team_upgr.get(team);
		return result != null ? result.clone() : null;
	}

	@Override
	public Location getTeamGenLocation(Team team) {
		if (team == null)
			return null;

		Location result = this.team_gen.get(team);
		return result != null ? result.clone() : null;
	}

	@Override
	public Location getTeamSpawnPoint(Team team) {
		if (team == null)
			return null;

		Location result = this.team_spawn.get(team);
		return result != null ? result.clone() : null;
	}

	@Override
	public BedwarsBed getTeamBed(Team team) {
		return team != null ? this.team_bed.get(team) : null;
	}

	@Override
	public Chest getTeamChest(Team team) {
		return this.team_chest.get(team);
	}

	@Override
	public Location getDragonSpawnPoint() {
		return this.dragon != null ? this.dragon.clone() : null;
	}

	@Override
	public Location getLobbySpawnPoint() {
		return this.lobby != null ? this.lobby.clone() : null;
	}

	@Override
	public Location getSpectatorSpawnPoint() {
		return this.spectator != null ? this.spectator.clone() : null;
	}

	@Override
	public Location getWaitingRoomSpawnPoint() {
		return this.waiting != null ? this.waiting.clone() : null;
	}

	@Override
	public Region getWaitingRoomRegion() {
		return this.waitingRegion;
	}

	@Override
	public Region getRegion() {
		return this.arenaRegion;
	}

	@Override
	public void setMapName(String name) {
		this.mapName = name;
	}

	@Override
	public void setArenaTime(int time) {
		this.time = time;
	}

	@Override
	public void setMode(GameMode mode) {
		if (mode != null)
			this.mode = mode;
	}

	@Override
	public void setTeamShop(Team team, Location loc) {
		if (team != null && loc != null)
			this.team_shop.put(team, loc.clone());
	}

	@Override
	public void setTeamUpgrade(Team team, Location loc) {
		if (team != null && loc != null)
			this.team_upgr.put(team, loc.clone());
	}

	@Override
	public void addResourceGenerator(Resource resource, Location loc) {
		if (resource == null || loc == null)
			return;

		List<Location> locations = this.resource_gen.get(resource);
		if (locations == null)
			this.resource_gen.put(resource, locations = new ArrayList<>());

		locations.add(loc.clone());
	}

	@Override
	public boolean removeResourceGenerator(Resource resource, int index) {
		if (resource == null || index < 0)
			return false;

		List<Location> locations = this.resource_gen.get(resource);
		if (locations == null || index >= locations.size())
			return false;

		return locations.remove(index) != null;
	}

	@Override
	public void setGeneratorSpeed(GeneratorSpeed speed) {
		if (speed != null)
			this.speed = speed;
	}

	@Override
	public void setTeamGenLocation(Team team, Location loc) {
		if (team != null && loc != null)
			this.team_gen.put(team, loc.clone());
	}

	@Override
	public void setTeamSpawnPoint(Team team, Location loc) {
		if (team != null && loc != null)
			this.team_spawn.put(team, loc.clone());
	}

	@Override
	public void setTeamBed(BedwarsBed bed) {
		if (bed != null)
			this.team_bed.put(bed.getTeam(), bed);
	}

	@Override
	public void setTeamChest(Team team, Chest chest) {
		if (team != null && chest != null)
			this.team_chest.put(team, chest);
	}

	@Override
	public void setDragonSpawnPoint(Location loc) {
		if (loc != null)
			this.dragon = loc.clone();
	}

	@Override
	public void setLobbySpawnPoint(Location loc) {
		if (loc != null)
			this.lobby = loc.clone();
	}

	@Override
	public void setSpectatorSpawnPoint(Location loc) {
		if (loc != null)
			this.spectator = loc.clone();
	}

	@Override
	public void setWaitingRoomLocation(Location loc) {
		if (loc != null)
			this.waiting = loc.clone();
	}

	@Override
	public void setWaitingRoomRegion(Region region) {
		if (region != null)
			this.waitingRegion = region;
	}

	@Override
	public void setArenaRegion(Region region) {
		if (region != null)
			this.arenaRegion = region;
	}

	@Override
	public void reloadArena() {
		reloadConfig();

		ConfigUtils utils = getConfigUtils();

		ConfigurationSection section = this.config.getConfigurationSection("Teams");
		if (section != null) {
			for (String teamKey : section.getKeys(false)) {
				Team team = Team.getByName(teamKey);
				if (team == null)
					continue;

				// Spawn
				Location spawnPoint = utils.getLocation("Teams." + team + ".team-spawn");
				if (spawnPoint != null)
					this.team_spawn.put(team, spawnPoint);

				// Upgrade
				Location upgr = utils.getLocation("Teams." + team + ".team-upgrade");
				if (upgr != null)
					this.team_upgr.put(team, upgr);

				// Shop
				Location shop = utils.getLocation("Teams." + team + ".team-shop");
				if (shop != null)
					this.team_shop.put(team, shop);

				// Generator
				Location generator = utils.getLocation("Teams." + team + ".generator");
				if (generator != null)
					this.team_gen.put(team, generator);

				// Chest
				Location chestLoc = utils.getLocation("Teams." + team + ".team-chest");
				if (chestLoc != null) {
					Block block = chestLoc.getBlock();

					if (block instanceof Chest)
						this.team_chest.put(team, (Chest) block);
				}

				// Bed
				Location bedhead = utils.getLocation("Teams." + team + ".bed.head");
				Location bedfoot = utils.getLocation("Teams." + team + ".bed.foot");

				if (bedhead != null && bedfoot != null)
					this.team_bed.put(team, new BedwarsBed(team, bedhead.getBlock(), bedfoot.getBlock()));

				this.teams.add(team);
			}

		}

		ConfigurationSection genSection = this.config.getConfigurationSection("Generators");
		if (genSection != null) {
			for (String genKey : genSection.getKeys(false)) {
				Resource resource = Resource.getByName(genKey);
				if (resource == null || resource == Resource.FREE)
					continue;

				this.resource_gen.put(resource, utils.getLocationList("Generators." + genKey));
			}

		}

		this.waiting = utils.getLocation("Arena-info.Spawns.Waiting-room");
		this.spectator = utils.getLocation("Arena-info.Spawns.Spectator");
		this.dragon = utils.getLocation("Arena-info.Spawns.Dragon");
		this.lobby = utils.getLocation("Arena-info.Spawn.Lobby");

		this.waitingRegion = utils.getRegion("Arena-info.Regions.Waiting-room");
		this.arenaRegion = utils.getRegion("Arena-info.Regions.Map");

		this.speed = GeneratorSpeed.getByName(this.config.getString("Arena-settings.Generator-speed"));
		this.mode = GameMode.getByName(this.config.getString("Arena-settings.Mode"));

		this.time = this.config.getInt("Arena-settings.Time", 1000);
		this.mapName = this.config.getString("Arena-settings.Map-name");
		this.enabled = this.config.getBoolean("Arena-settings.Enabled");

	}

	@Override
	public void saveArena() {
		this.config = new YamlConfiguration();

		ConfigurationSection info = this.config.createSection("Arena-info");

		if (this.waitingRegion != null) {
			info.set("Regions.Waiting-room.pos-1", LocationUtils.serialize(this.waitingRegion.getFirstPosition(), false));
			info.set("Regions.Waiting-room.pos-2", LocationUtils.serialize(this.waitingRegion.getSecondPosition(), false));
		}

		if (this.arenaRegion != null) {
			info.set("Regions.Map.pos-1", LocationUtils.serialize(this.arenaRegion.getFirstPosition(), false));
			info.set("Regions.Map.pos-2", LocationUtils.serialize(this.arenaRegion.getSecondPosition(), false));
		}

		if (this.waiting != null)
			info.set("Spawns.Waiting-room", LocationUtils.serialize(this.waiting, true));

		if (this.spectator != null)
			info.set("Spawns.Spectator", LocationUtils.serialize(this.spectator, true));

		if (this.dragon != null)
			info.set("Spawns.Dragon", LocationUtils.serialize(this.dragon, true));

		ConfigurationSection settings = this.config.createSection("Arena-settings");

		if (this.speed != null)
			settings.set("Generator-speed", this.speed.getName());

		if (this.mapName != null)
			settings.set("Map-name", this.mapName);

		if (this.mode != null)
			settings.set("Mode", this.mode.getName());

		settings.set("Time", this.time);
		settings.set("Enabled", this.enabled);

		for (Team team : Team.values()) {
			// Spawn
			Location spawn = this.team_spawn.get(team);
			if (spawn != null)
				this.config.set("Teams." + team + ".team-spawn", LocationUtils.serialize(spawn, true));

			// Upgrade
			Location upgrade = this.team_upgr.get(team);
			if (upgrade != null)
				this.config.set("Teams." + team + ".team-upgrade", LocationUtils.serialize(upgrade, true));

			// Shop
			Location shop = this.team_shop.get(team);
			if (shop != null)
				this.config.set("Teams." + team + ".team-shop", LocationUtils.serialize(shop, true));

			// Chest
			Chest chest = this.team_chest.get(team);
			if (chest != null)
				this.config.set("Teams." + team + ".chest", LocationUtils.serialize(chest.getLocation(), false));

			// Generator
			Location gen = this.team_gen.get(team);
			if (gen != null)
				this.config.set("Teams." + team + ".generator", LocationUtils.serialize(gen, false));

			// Bed
			BedwarsBed bed = this.team_bed.get(team);
			if (bed != null) {
				this.config.set("Teams." + team + ".bed.head", LocationUtils.serialize(bed.getHead().getLocation(), false));
				this.config.set("Teams." + team + ".bed.foot", LocationUtils.serialize(bed.getFoot().getLocation(), false));
			}

		}

		for (Entry<Resource, List<Location>> entry : this.resource_gen.entrySet()) {
			List<Location> list = entry.getValue();

			List<String> locations = new ArrayList<>(list.size());
			for (Location loc : list)
				locations.add(LocationUtils.serialize(loc, false));

			this.config.set("Generators." + entry.getKey().getName(), locations);
		}

		saveConfig();
	}

	@Override
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public boolean isEnabled() {
		return this.enabled;
	}

	@Override
	public boolean exists() {
		return this.file.exists();
	}

	@Override
	public boolean remove() {
		if (!this.file.delete())
			return false;

		ARENAS.remove(this.name.toLowerCase());
		return true;
	}

	@Override
	public boolean isReady() {
		if (!this.enabled || !exists() || (this.mode == null) || (this.speed == null))
			return false;

		if (this.spectator == null)
			return false;

		if (this.waiting == null)
			return false;

		if (this.arenaRegion == null)
			return false;

		return isTeamsReady();
	}

	private boolean isTeamsReady() {
		if (this.teams.size() < 2)
			return false;

		int readyCount = 0;
		for (Team team : this.teams) {
			if (!isTeamReady(team))
				continue;

			readyCount++;
			if (readyCount >= 2)
				return true;
		}

		return readyCount >= 2;
	}

	private boolean isTeamReady(Team team) {
		if ((this.team_spawn.get(team) == null) || (this.team_shop.get(team) == null) || (this.team_upgr.get(team) == null) || (this.team_gen.get(team) == null))
			return false;

		if (this.team_bed.get(team) == null)
			return false;

		return true;
	}

	@Override
	public void saveDefaultConfig() {
		return;
	}

	private ConfigUtils getConfigUtils() {
		if (this.utils == null)
			this.utils = new ConfigUtils(getConfig());

		return this.utils;
	}

	@Override
	public String toString() {
		return "BedwarsArena [Name=" + this.name + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.name.toLowerCase());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (!(obj instanceof BedwarsArena))
			return false;

		BedwarsArena other = (BedwarsArena) obj;
		return this.name.equalsIgnoreCase(other.name);
	}

	public static Set<String> getArenasNameList() {
		Set<String> result = new HashSet<>();

		File directory = new File(Bedwars.getInstance().getDataFolder() + "/Arenas");

		String[] list = directory.list();
		if (list != null) {
			for (String name : list) {
				if (name.endsWith(".yml"))
					result.add(name.substring(0, name.length() - 4));
			}

		}

		return result;
	}

	public static Set<Arena> getReadyArenas() {
		Set<Arena> result = new HashSet<>(8);

		for (BedwarsArena arena : BedwarsArena.ARENAS.values()) {
			if (arena.isReady())
				result.add(arena);
		}

		return result;
	}

	public static Set<BedwarsArena> getArenas() {
		return new HashSet<>(BedwarsArena.ARENAS.values());
	}

	public static BedwarsArena getArena(String name) {
		return name != null ? BedwarsArena.ARENAS.get(name.toLowerCase()) : null;
	}

}