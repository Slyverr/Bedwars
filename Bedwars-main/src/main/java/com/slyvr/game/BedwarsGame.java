package com.slyvr.game;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;

import com.cryptomorin.xseries.XBlock;
import com.cryptomorin.xseries.XSound;
import com.cryptomorin.xseries.messages.Titles;
import com.slyvr.api.arena.BedwarsBed;
import com.slyvr.api.arena.Region;
import com.slyvr.api.event.bed.BedBreakEvent;
import com.slyvr.api.event.game.GameEndEvent;
import com.slyvr.api.event.game.GameJoinEvent;
import com.slyvr.api.event.game.GameQuitEvent;
import com.slyvr.api.event.player.GamePlayerDisconnectEvent;
import com.slyvr.api.event.player.GamePlayerEliminateEvent;
import com.slyvr.api.event.player.GamePlayerReconnectEvent;
import com.slyvr.api.event.player.GamePlayerRespawnEvent;
import com.slyvr.api.event.team.TeamEliminationEvent;
import com.slyvr.api.game.Game;
import com.slyvr.api.game.GameManager;
import com.slyvr.api.game.GameMode;
import com.slyvr.api.game.GameState;
import com.slyvr.api.game.player.ArmorType;
import com.slyvr.api.game.player.GamePlayer;
import com.slyvr.api.game.player.stats.GameStatistic;
import com.slyvr.api.game.player.stats.GameStatisticManager;
import com.slyvr.api.generator.DropItem;
import com.slyvr.api.generator.GeneratorSpeed;
import com.slyvr.api.generator.GeneratorTier;
import com.slyvr.api.generator.Resource;
import com.slyvr.api.generator.TeamGenerator;
import com.slyvr.api.generator.TieredGenerator;
import com.slyvr.api.group.Group;
import com.slyvr.api.level.BedwarsLevel;
import com.slyvr.api.npc.Shopkeeper;
import com.slyvr.api.npc.Upgrader;
import com.slyvr.api.prestige.Prestige;
import com.slyvr.api.shop.item.TieredItemStack;
import com.slyvr.api.team.GameTeam;
import com.slyvr.api.team.Team;
import com.slyvr.api.user.User;
import com.slyvr.api.user.UserStatistics;
import com.slyvr.arena.BedwarsArena;
import com.slyvr.bedwars.Bedwars;
import com.slyvr.bedwars.BedwarsItems;
import com.slyvr.bedwars.settings.TeamForgeSettings;
import com.slyvr.generator.MapResourceGenerator;
import com.slyvr.generator.TeamResourceGenerator;
import com.slyvr.hologram.BedwarsHologram;
import com.slyvr.hologram.Hologram;
import com.slyvr.listener.MapListener;
import com.slyvr.manager.PlayerManager;
import com.slyvr.player.BedwarsPlayer;
import com.slyvr.team.BedwarsTeam;
import com.slyvr.util.BedUtils;
import com.slyvr.util.ChatUtils;
import com.slyvr.util.LevelUtils;
import com.slyvr.util.TeamUtils;

public final class BedwarsGame extends AbstractGame {

	private final Map<Resource, List<TieredGenerator>> generators = new HashMap<>();

	private final Map<Team, TeamData> team_data = new EnumMap<>(Team.class);
	private final Map<Team, TeamData> eliminated = new EnumMap<>(Team.class);

	private final Map<UUID, PlayerData> player_data = new HashMap<>();

	private final List<Shopkeeper> shopkeepers = new ArrayList<>();
	private final List<Upgrader> upgraders = new ArrayList<>();

	private final List<Hologram> holograms = new ArrayList<>();

	private final Set<Group> groups = new HashSet<>();
	private final Set<Player> players = new HashSet<>();

	private final Map<Block, Material> broken_beds = new HashMap<>();
	private final Map<Block, BlockFace> beds_faces = new HashMap<>();

	private Map<Block, Material> waiting_blocks;

	private GameCountdown gameCountdown = new GameCountdown(this);
	private BedwarsGameManager gameManager = new BedwarsGameManager(this);

	private UUID uuid;

	private boolean isLocked;

	public BedwarsGame(BedwarsArena arena) {
		super(arena);

		this.uuid = UUID.randomUUID();
	}

	private boolean canStart() {
		if (this.isLocked || this.hasStarted || (this.players.size() < Bedwars.getInstance().getSettings().getMinimumPlayers(this.mode)))
			return false;

		return true;
	}

	@Override
	public boolean startGame() {
		if (!canStart())
			return false;

		TeamForgeSettings settings = Bedwars.getInstance().getTeamForgeSettings();

		Set<DropItem> items = new HashSet<>();
		for (Resource rsc : settings.getTeamDrops()) {
			GeneratorSpeed speed = this.arena.getGeneratorSpeed();

			ItemStack item = new ItemStack(rsc.getMaterial());

			items.add(new DropItem(item, speed.getDropsPerMinute(rsc), settings.getDropLimit(this.mode, rsc)));
		}

		List<Team> emptyTeams = new ArrayList<>();

		Iterator<Team> teams = this.arena.getTeams().iterator();
		Iterator<Player> players = this.players.iterator();
		Iterator<Group> groups = this.groups.iterator();

		while (teams.hasNext()) {
			Team team = teams.next();

			Location loc = this.arena.getTeamSpawnPoint(team);

			Set<GamePlayer> team_players = new HashSet<>(this.mode.getTeamMax());
			if (groups.hasNext()) {
				Group group = groups.next();

				for (Player player : group.getPlayers()) {
					if (!this.players.contains(player))
						continue;

					team_players.add(createGamePlayer(player, team, loc));
				}

			} else {
				for (int i = 0; i < this.mode.getTeamMax() && players.hasNext(); i++)
					team_players.add(createGamePlayer(players.next(), team, loc));
			}

			Location genLoc = this.arena.getTeamGenLocation(team);

			TeamGenerator gen = null;
			if (genLoc != null) {
				gen = new TeamResourceGenerator(genLoc, items);
				gen.start();
			}

			Location teamShop = this.arena.getTeamShop(team);
			if (teamShop != null)
				spawnTeamShop(teamShop);

			Location teamUpgr = this.arena.getTeamUpgrade(team);
			if (teamUpgr != null)
				spawnTeamUpgrade(teamUpgr);

			this.team_data.put(team, new TeamData(new BedwarsTeam(this, team), team_players, this.arena.getTeamBed(team), gen, loc));

			if (team_players.isEmpty())
				emptyTeams.add(team);
		}

		Bukkit.getScheduler().runTaskLater(Bedwars.getInstance(), () -> {
			for (Team team : emptyTeams) {
				if (getTeamPlayers(team).isEmpty())
					this.breakTeamBed(team);
			}

		}, 120 * 20);

		for (Resource resource : Resource.values()) {
			Collection<Location> drop_loc = this.arena.getResourceGenLocations(resource);
			if (drop_loc == null || drop_loc.isEmpty())
				continue;

			List<TieredGenerator> gens = new ArrayList<>(drop_loc.size());

			List<GeneratorTier> tiers = Bedwars.getInstance().getMapForgeSettings().getGeneratorTiers(this.mode, resource);
			for (Location loc : drop_loc) {
				TieredGenerator gen = new MapResourceGenerator(resource, loc, tiers);
				gen.start();

				gens.add(gen);
			}

			this.generators.put(resource, gens);
		}

		this.destroyWaitingRoom();
		this.gameManager.start();

		this.hasStarted = true;
		this.state = GameState.RUNNING;

		return true;
	}

	private GamePlayer createGamePlayer(Player player, Team team, Location loc) {
		BedwarsPlayer gp = new BedwarsPlayer(player, this, team);
		gp.setArmorType(ArmorType.LEATHER);
		gp.getInventory().addItem(BedwarsItems.getInstance().getSword());

		PlayerManager.clear(player);
		PlayerManager.resetHealth(player);

		TeamUtils.setPlayerArmor(player, team, gp.getArmorType());

		this.player_data.put(player.getUniqueId(), new PlayerData(gp, team));
		player.getInventory().addItem(BedwarsItems.getInstance().getSword());
		player.teleport(loc);
		player.setGameMode(org.bukkit.GameMode.SURVIVAL);

		return gp;
	}

	private void spawnTeamShop(Location loc) {
		Shopkeeper shopkeeper = Bedwars.getInstance().getNPCManager().createShopKeeper(this, loc);
		shopkeeper.spawn();

		Hologram hologram = new BedwarsHologram(loc.add(0, 1, 0), .3);
		hologram.addLine("&e&lRIGHT CLICK");
		hologram.addLine("&bITEM SHOP");

		this.shopkeepers.add(shopkeeper);
		this.holograms.add(hologram);
	}

	private void spawnTeamUpgrade(Location loc) {
		Upgrader upgrader = Bedwars.getInstance().getNPCManager().createUpgrader(this, loc);
		upgrader.spawn();

		Hologram hologram = new BedwarsHologram(loc.add(0, 1, 0), .3);
		hologram.addLine("&e&lRIGHT CLICK");
		hologram.addLine("&bSOLO UPGRADES");

		this.upgraders.add(upgrader);
		this.holograms.add(hologram);
	}

	private void destroyWaitingRoom() {
		Region region = this.arena.getWaitingRoomRegion();
		if (region == null)
			return;

		World world = region.getWorld();

		Bukkit.getScheduler().runTask(Bedwars.getInstance(), () -> {
			int minX = (int) region.getMinX();
			int minY = (int) region.getMinY();
			int minZ = (int) region.getMinZ();

			int maxX = (int) region.getMaxX();
			int maxY = (int) region.getMaxY();
			int maxZ = (int) region.getMaxZ();

			int size = (maxX - minX) * (maxY - minY) * (maxZ - minZ);

			Map<Block, Material> blocks = new HashMap<>(size);
			for (int x = minX; x < maxX; x++) {
				for (int y = minY; y < maxY; y++) {
					for (int z = minZ; z < maxZ; z++) {
						Block block = world.getBlockAt(x, y, z);
						if (block.getType() == Material.AIR)
							continue;

						blocks.put(block, block.getType());
						block.setType(Material.AIR);
					}
				}

			}

			this.waiting_blocks = blocks;
		});

	}

	@Override
	public boolean stopGame() {
		if (this.isLocked || !this.hasStarted)
			return false;

		this.setState(GameState.ENDED);

		this.gameManager.stop();
		this.stop();

		return true;
	}

	private void stop() {
		Bedwars instance = Bedwars.getInstance();

		List<GamePlayer> winners = new ArrayList<>();
		List<GamePlayer> losers = new ArrayList<>();

		Team winner = isGameEnded() ? this.team_data.keySet().iterator().next() : null;

		GameSummary summary = new GameSummary(BedwarsGame.this, winner);
		summary.send();

		// Stats & Rewards
		for (PlayerData data : this.player_data.values()) {
			GamePlayer gp = data.getOwner();
			Player player = gp.getPlayer();

			User user = Bedwars.getInstance().getUser(player);

			UserStatistics userStats = user.getStatistics(this.mode);
			if (userStats == null)
				userStats = new UserStatistics();

			GameStatisticManager stats = gp.getStatisticManager();
			userStats.incrementStatistics(stats);

			user.setCoinsBalance(user.getCoinsBalance() + stats.getCoinsReward().getAmount());

			BedwarsLevel level = user.getLevel();
			Prestige next = LevelUtils.levelUp(level, stats.getExpReward().getAmount());

			user.setLevel(level);
			user.setPrestige(next);
			user.saveData();

			if (!this.players.contains(player))
				continue;

			if (isGameEnded() && gp.getTeam() == winner) {
				Titles.sendTitle(player, 10, 80, 10, "§6§lVICTORY!", "");
				winners.add(gp);

			} else {
				Titles.sendTitle(player, 10, 80, 10, "§cGAME OVER!", "");
				losers.add(gp);
			}

			player.setGameMode(org.bukkit.GameMode.ADVENTURE);

			if (instance.isEnabled())
				Bukkit.getScheduler().runTaskLater(instance, () -> remove(player), 20 * 30);
			else
				remove(player);
		}

		GameEndEvent event = new GameEndEvent(BedwarsGame.this, winners, losers);
		Bukkit.getPluginManager().callEvent(event);

		for (Shopkeeper shopKeeper : this.shopkeepers)
			shopKeeper.remove();

		this.shopkeepers.clear();

		for (Upgrader upgrader : this.upgraders)
			upgrader.remove();

		this.upgraders.clear();

		for (Hologram hologram : this.holograms)
			hologram.remove();

		this.holograms.clear();

		for (TeamData team : this.team_data.values()) {
			if (team.gen != null)
				team.gen.stop();
		}

		this.team_data.clear();

		for (TeamData eliminated_team : this.eliminated.values()) {

			if (eliminated_team.gen != null)
				eliminated_team.gen.stop();
		}

		this.eliminated.clear();

		for (List<TieredGenerator> generators : this.generators.values()) {
			for (TieredGenerator gen : generators)
				gen.stop();

			generators.clear();
		}

		this.generators.clear();

		this.isLocked = true;
		this.setState(GameState.RESETTING);

		Runnable reset = () -> {
			this.hasStarted = false;
			this.resetArena();

			this.isLocked = false;
			this.setState(GameState.WAITING);
		};

		if (instance.isEnabled())
			Bukkit.getScheduler().runTaskLater(instance, reset, 20 * 40);
		else
			reset.run();
	}

	public void resetArena() {
		MapListener.resetArena(this);

		if (this.waiting_blocks != null) {
			for (Entry<Block, Material> entry : this.waiting_blocks.entrySet())
				entry.getKey().setType(entry.getValue());

			this.waiting_blocks.clear();
		}

		for (Entry<Block, Material> entry : this.broken_beds.entrySet()) {
			Block block = entry.getKey();

			BedUtils.placeBed(block, this.beds_faces.get(block), entry.getValue());
		}

		this.broken_beds.clear();
		this.beds_faces.clear();
	}

	@Override
	public boolean addPlayer(Player player) {
		if (!canAddPlayer(player))
			return false;

		String message = "§7" + player.getDisplayName() + " §ehas joined (§b" + (size() + 1) + "§e/&b" + this.mode.getGameMax() + "§e)!";

		GameJoinEvent event = new GameJoinEvent(this, player, message);
		Bukkit.getPluginManager().callEvent(event);

		if (event.isCancelled())
			return false;

		Game oldGame = getPlayerGame(player);
		if (oldGame != null)
			oldGame.removePlayer(player);

		PlayerManager.clear(player);
		PlayerManager.resetHealth(player);
		PlayerManager.resetFoodLevel(player);
		PlayerManager.resetScoreboard(player);

		player.teleport(this.arena.getWaitingRoomSpawnPoint());
		player.setGameMode(org.bukkit.GameMode.ADVENTURE);
		player.setPlayerTime(this.arena.getTime(), false);
		player.getEnderChest().clear();

		BedwarsLevel.setForPlayer(player, Bedwars.getInstance().getUser(player).getDisplayLevel());

		this.gameCountdown.addPlayer(player);
		this.players.add(player);

		this.broadcastMessage(event.getJoinMessage());

		AbstractGame.PLAYERS_GAME.put(player.getUniqueId(), this);
		return true;
	}

	@Override
	public boolean canAddPlayer(Player player) {
		return !this.isLocked && !this.hasStarted && player != null && !contains(player);
	}

	private void remove(Player player) {
		PlayerManager.clear(player);
		PlayerManager.resetScoreboard(player);
		PlayerManager.resetHealth(player);
		PlayerManager.resetLevel(player);
		PlayerManager.resetTime(player);

		player.getEnderChest().clear();
		Bedwars.getInstance().sendPlayerToLobby(this, player, "Removed from game");

		this.players.remove(player);
		AbstractGame.PLAYERS_GAME.remove(player.getUniqueId());
	}

	@Override
	public boolean removePlayer(Player player) {
		if (this.isLocked || !contains(player))
			return false;

		String message = ChatColor.GRAY + player.getDisplayName() + ChatColor.YELLOW + "has quit!";

		GameQuitEvent event = new GameQuitEvent(this, player, message);
		Bukkit.getPluginManager().callEvent(event);

		this.gameCountdown.removePlayer(player);
		this.remove(player);

		this.broadcastMessage(event.getQuitMessage());
		return true;

	}

	@Override
	public boolean addGroup(Group group) {
		if (!canAddGroup(group))
			return false;

		for (Player player : group.getPlayers())
			this.addPlayer(player);

		return this.groups.add(group);
	}

	@Override
	public boolean canAddGroup(Group group) {
		if (this.isLocked || this.hasStarted || group == null || isFull())
			return false;

		if (group.size() + this.players.size() > this.mode.getGameMax())
			return false;

		return true;
	}

	@Override
	public boolean removeGroup(Group group) {
		if (this.isLocked || group == null)
			return false;

		for (Player player : group.getPlayers())
			removePlayer(player);

		return this.groups.remove(group);
	}

	@Override
	public boolean killPlayer(Player player, String message, int respawn) {
		if (player == null || respawn < 0 || !contains(player))
			return false;

		GamePlayer gpDead = getGamePlayer(player);
		gpDead.getStatisticManager().incrementStatistic(GameStatistic.DEATHS, 1);

		if (message == null)
			message = getDisplayName(gpDead) + " §7died!";

		boolean hasBed = hasBed(gpDead.getTeam());
		if (!hasBed)
			message += " §b§lFINAL KILL!";

		this.broadcastMessage(message);

		if (!hasBed)
			this.eliminate(gpDead);
		else
			setRespawnSpectator(player, respawn);

		checkTeamElimination(gpDead.getTeam());
		return true;
	}

	@Override
	public boolean killPlayer(Player player, String message) {
		return killPlayer(player, message, Bedwars.getInstance().getSettings().getRespawnTime());
	}

	@Override
	public boolean killPlayer(Player player) {
		return killPlayer(player, null);
	}

	@Override
	public boolean eliminatePlayer(Player player) {
		GamePlayer gp = getGamePlayer(player);
		if (gp == null)
			return false;

		this.eliminate(gp);
		checkTeamElimination(gp.getTeam());
		return true;
	}

	private void checkTeamElimination(Team team) {
		TeamData data = this.team_data.get(team);
		if (isTeamEmpty(data))
			this.eliminate(data.players, team);
	}

	@Override
	public boolean isEliminated(Player player) {
		if (player == null)
			return false;

		PlayerData data = this.player_data.get(player.getUniqueId());
		return data != null ? data.isEliminated : true;
	}

	@Override
	public boolean eliminateTeam(Team team) {
		TeamData data = this.team_data.get(team);
		if (data == null)
			return false;

		this.eliminate(getTeamPlayers(team), team);
		return true;
	}

	@Override
	public boolean isEliminated(Team team) {
		return this.eliminated.containsKey(team);
	}

	@Override
	public boolean breakTeamBed(Team team, Player destroyer) {
		if (team == null)
			return false;

		TeamData teamData = this.team_data.get(team);
		if (teamData == null)
			return false;

		BedwarsBed bed = teamData.bed;
		if (bed == null || !BedUtils.isBed(bed))
			return false;

		this.putInBeds(teamData.bed);

		if (destroyer == null) {
			BedUtils.breakBed(bed);

			alertBedDestruction(team, null, null);

		} else {
			PlayerData data = this.player_data.get(destroyer.getUniqueId());
			if (data == null || data.isSpectator)
				return false;

			GamePlayer owner = data.getOwner();
			if (owner.getTeam() == team) {
				destroyer.sendMessage("§cYou can't destroy your own bed!");
				return false;
			}

			String message = getBedDestructionMessage(owner, team);

			BedBreakEvent bwEvent = new BedBreakEvent(owner, bed, message);
			Bukkit.getPluginManager().callEvent(bwEvent);

			if (bwEvent.isCancelled())
				return false;

			BedUtils.breakBed(bed);
			owner.getStatisticManager().incrementStatistic(GameStatistic.BED_BROKEN, 1);

			String breakMessage = "§lBED DESTRUCTION > " + bwEvent.getBreakMessage();
			String lostMessage = "§lBED DESTRUCTION > §7Your bed was destroyed by " + getDisplayName(owner);

			alertBedDestruction(team, breakMessage, lostMessage);

			owner.getStatisticManager().getCoinsReward().increment(20); // TODO: Settings rewards
			destroyer.sendMessage("§6+20 coins! (Bed Destroyed)");
		}

		if (isTeamEmpty(teamData))
			this.eliminate(teamData.players, team);

		return false;
	}

	private void putInBeds(BedwarsBed bed) {
		Block foot = bed.getFoot();

		if (BedUtils.isBedHead(foot))
			foot = bed.getHead();

		this.broken_beds.put(foot, foot.getType());
		this.beds_faces.put(foot, XBlock.getDirection(foot));
	}

	private boolean isTeamEmpty(TeamData data) {
		for (GamePlayer gp : data.players)
			if (!isEliminated(gp.getPlayer()))
				return false;

		return true;
	}

	private void alertBedDestruction(Team team, String breakMessage, String lostMessage) {
		for (PlayerData data : this.player_data.values()) {
			if (data.isEliminated)
				continue;

			GamePlayer owner = data.getOwner();
			Player p = owner.getPlayer();

			if (data.team == team) {
				owner.getStatisticManager().incrementStatistic(GameStatistic.BED_LOST, 1);
				if (data.isDisconnected)
					continue;

				if (lostMessage != null) {
					p.sendMessage((String) null);
					p.sendMessage(lostMessage);
					p.sendMessage((String) null);
				}

				XSound.ENTITY_WITHER_DEATH.play(p, 1F, 1F);

				Titles.sendTitle(p, 5, 30, 5, "§c§lBED DESTROYED!", "You will no longer respawn!");

			} else {
				if (data.isDisconnected || breakMessage == null)
					continue;

				XSound.ENTITY_ENDER_DRAGON_GROWL.play(p, 1F, 1F);

				p.sendMessage((String) null);
				p.sendMessage(breakMessage);
				p.sendMessage((String) null);
			}
		}

	}

	@Override
	public boolean breakTeamBed(Team team) {
		return breakTeamBed(team, null);
	}

	private String getBedDestructionMessage(GamePlayer destroyer, Team bedTeam) {
		StringBuilder builder = new StringBuilder()
				.append(bedTeam.getColoredString())
				.append(" Bed§r §7was destroyed by ")
				.append(getDisplayName(destroyer))
				.append("§7!");

		return builder.toString();
	}

	private String getDisplayName(GamePlayer gp) {
		return gp.getTeam().getChatColor() + gp.getPlayer().getDisplayName();
	}

	@Override
	public boolean hasBed(Team team) {
		if (team == null)
			return false;

		BedwarsBed bed = this.arena.getTeamBed(team);
		return bed != null && BedUtils.isBed(bed);
	}

	@Override
	public boolean disconnect(Player player) {
		if (this.isLocked)
			return false;

		if (!this.hasStarted)
			return removePlayer(player);

		PlayerData data = this.player_data.get(player.getUniqueId());
		if (data == null || data.isDisconnected)
			return false;

		GamePlayer gp = data.getOwner();

		PlayerManager.clear(player);
		PlayerManager.resetHealth(player);
		PlayerManager.resetScoreboard(player);

		Bedwars.getInstance().sendPlayerToLobby(this, player, "Disconnected from game!");

		String message = getDisplayName(gp) + " &edisconnected!";

		GamePlayerDisconnectEvent event = new GamePlayerDisconnectEvent(gp, ChatUtils.format(message));
		Bukkit.getPluginManager().callEvent(event);

		Team team = gp.getTeam();
		new BukkitRunnable() {

			@Override
			public void run() {
				Collection<GamePlayer> players = getTeamPlayers(team);
				if (!players.isEmpty())
					return;

				breakTeamBed(team);
			}
		}.runTaskLater(Bedwars.getInstance(), 20 * 120);

		this.players.remove(player);
		AbstractGame.DISCONNECTED.put(player.getUniqueId(), this);

		this.broadcastMessage(event.getDisconnectMessage());

		if (!hasBed(gp.getTeam()))
			eliminatePlayer(player);

		return true;
	}

	@Override
	public boolean isDisconnected(Player player) {
		if (player == null)
			return false;

		PlayerData data = this.player_data.get(player.getUniqueId());
		return data != null ? data.isDisconnected : false;
	}

	@Override
	public boolean reconnect(Player player) {
		if (this.isLocked || player == null)
			return false;

		PlayerData data = this.player_data.get(player.getUniqueId());
		if (data == null || !data.isDisconnected)
			return false;

		BedwarsPlayer gp = data.getOwner();
		gp.initPlayer();

		player.setPlayerTime(this.arena.getTime(), false);

		if (isEliminated(player))
			return setSpectator(player, true);

		String message = getDisplayName(data.getOwner()) + " &ereconnected!";

		GamePlayerReconnectEvent event = new GamePlayerReconnectEvent(gp, ChatUtils.format(message));
		Bukkit.getPluginManager().callEvent(event);

		if (event.isCancelled())
			return false;

		setRespawnSpectator(player, Bedwars.getInstance().getSettings().getReconnectRespawnTime());
		this.broadcastMessage(event.getReconnectMessage());

		this.players.add(player);
		AbstractGame.DISCONNECTED.remove(player.getUniqueId());
		return true;
	}

	@Override
	public boolean broadcastMessage(String message) {
		return broadcastMessage(message, null);
	}

	@Override
	public boolean broadcastMessage(String message, Predicate<Player> predicate) {
		if (message == null)
			return false;

		message = ChatUtils.format(message);
		for (PlayerData data : this.player_data.values()) {
			if (data.isDisconnected)
				continue;

			data.getOwner().getPlayer().sendMessage(message);
		}

		return true;
	}

	@Override
	public GameManager getManager() {
		return this.gameManager;
	}

	@Override
	public GamePlayer getGamePlayer(Player player) {
		if (player == null)
			return null;

		PlayerData data = this.player_data.get(player.getUniqueId());
		return data != null ? data.getOwner() : null;
	}

	@Override
	public void setInvincible(Player player, boolean invincible) {
		if (player == null)
			return;

		PlayerData data = this.player_data.get(player.getUniqueId());
		if (data != null)
			data.setInvincible(invincible);
	}

	@Override
	public boolean isInvincible(Player player) {
		if (player == null)
			return false;

		// TODO: change team and reset player

		PlayerData data = this.player_data.get(player.getUniqueId());
		return data != null ? data.isInvincible : false;
	}

	@Override
	public boolean isSpectator(Player player) {
		if (player == null)
			return false;

		// TODO: change team and reset player

		PlayerData data = this.player_data.get(player.getUniqueId());
		return data != null ? data.isSpectator : false;
	}

	@Override
	public GameTeam getGameTeam(Team team) {
		TeamData data = this.team_data.get(team);
		return data != null ? data.owner : null;
	}

	@Override
	public TeamGenerator getTeamGenerator(Team team) {
		TeamData data = this.team_data.get(team);
		return data != null ? data.gen : null;
	}

	@Override
	public Collection<TieredGenerator> getMapResourceGenerator(Resource resource) {
		List<TieredGenerator> gens = this.generators.get(resource);

		return gens != null ? new ArrayList<>(gens) : new ArrayList<>(0);
	}

	@Override
	public Collection<Player> getPlayers() {
		return new HashSet<>(this.players);
	}

	@Override
	public Collection<GamePlayer> getGamePlayers() {
		Set<GamePlayer> result = new HashSet<>(this.player_data.size());

		for (PlayerData gp : this.player_data.values())
			if (!gp.isDisconnected)
				result.add(gp.owner);

		return result;
	}

	@Override
	public Collection<GamePlayer> getTeamPlayers(Team team) {
		if (team == null)
			return null;

		TeamData data = this.team_data.get(team);
		return data != null ? new HashSet<>(data.players) : new HashSet<>(0);
	}

	@Override
	public Collection<GameTeam> getTeams() {
		Set<GameTeam> result = new HashSet<>();

		for (TeamData data : this.team_data.values())
			result.add(data.owner);

		return result;
	}

	@Override
	public boolean isFull() {
		if (!this.hasStarted)
			return this.players.size() == this.mode.getGameMax();

		return this.player_data.size() == this.mode.getGameMax();
	}

	@Override
	public boolean contains(Player player) {
		return this.players.contains(player);
	}

	@Override
	public int size() {
		return this.players.size();
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.uuid);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (!(obj instanceof BedwarsGame))
			return false;

		BedwarsGame other = (BedwarsGame) obj;
		return this.uuid.equals(other.uuid);
	}

	public static BedwarsGame randomGame(Player player, GameMode mode) {
		return null;
	}

	/**
	 * Generates a random game if possible
	 *
	 * @param mode GameMode of the arena or null for random mode
	 *
	 * @return BedwarsGame if created, otherwise null
	 */
	public static BedwarsGame randomGame(GameMode mode) {
		BedwarsArena arena = randomArena(mode);

		return arena != null ? new BedwarsGame(arena) : null;
	}

	//
	private void hidePlayer(Player player) {
		for (PlayerData data : this.player_data.values()) {
			if (!data.isSpectator)
				continue;

			data.getOwner().getPlayer().hidePlayer(player);
		}

	}

	private boolean setSpectator(Player player, boolean eliminated) {
		PlayerManager.clear(player);
		PlayerManager.resetHealth(player);
		PlayerManager.resetFoodLevel(player);
		PlayerManager.setFlying(player, true);

		PlayerData data = this.player_data.get(player.getUniqueId());
		data.setInvincible(true);
		data.setSpectator(true);

		if (!eliminated)
			player.setPlayerListName(ChatColor.GRAY + player.getDisplayName());
		else {
			data.setEliminated(true);
			player.setPlayerListName(ChatColor.WHITE + player.getDisplayName());
		}

		player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0));
		player.teleport(this.arena.getSpectatorSpawnPoint());
		player.setCanPickupItems(false);
		hidePlayer(player);

		return true;
	}

	private void setRespawnSpectator(Player player, int time) {
		setSpectator(player, false);

		new BukkitRunnable() {
			int cd = time;

			@Override
			public void run() {
				if (this.cd != 0) {
					String subTitle = "§eYou will respawn in §c" + this.cd-- + " §eseconds";

					Titles.sendTitle(player, 5, 20, 5, "§cYOU DIED!", subTitle);
					player.sendMessage(subTitle);
					return;
				}

				cancel();

				PlayerData data = BedwarsGame.this.player_data.get(player.getUniqueId());

				BukkitScheduler scheduler = Bukkit.getScheduler();
				scheduler.runTask(Bedwars.getInstance(), () -> {
					BedwarsGame.this.spawn(player);
					PlayerManager.setFlying(player, false);

					GamePlayerRespawnEvent event = new GamePlayerRespawnEvent(data.owner, "§eYou have respawned!");
					Bukkit.getPluginManager().callEvent(event);

					Titles.sendTitle(player, 5, 20, 5, "§aRESPAWNED!", "");
					player.sendMessage(event.getRespawnMessage());

				});

				player.setCanPickupItems(true);
				data.setSpectator(false);

				scheduler.runTaskLaterAsynchronously(Bedwars.getInstance(), () -> data.setInvincible(false), 100);
			}
		}.runTaskTimerAsynchronously(Bedwars.getInstance(), 0, 20);
	}

	private void spawn(Player player) {
		PlayerManager.clear(player);
		PlayerManager.resetHealth(player);

		PlayerData data = this.player_data.get(player.getUniqueId());

		GamePlayer gp = data.getOwner();
		Team team = gp.getTeam();

		player.setPlayerListName(team.getColoredChar() + " " + player.getDisplayName());

		TeamUtils.setPlayerArmor(player, team, gp.getArmorType());
		for (ItemStack item : gp.getInventory().getPermanentItems())
			player.getInventory().addItem(item);

		for (TieredItemStack tiered : gp.getInventory().getTieredItems()) {
			if (tiered.hasPrevious())
				tiered.setCurrentTier(tiered.getPreviousTier());

			ItemStack current = tiered.current();
			if (current != null)
				player.getInventory().addItem(current);
		}

		TeamData teamData = this.team_data.get(team);
		if (teamData == null)
			teamData = this.eliminated.get(team);

		if (teamData == null)
			return;

		GameTeam gameTeam = teamData.owner;
		gameTeam.getUpgradeManager().apply(gp);

		player.teleport(teamData.spawn);
		player.setGameMode(org.bukkit.GameMode.SURVIVAL);

	}

	private void eliminate(Collection<GamePlayer> players, Team team) {
		String message = team.getColoredString() + " Team §chas been eliminated!";

		TeamEliminationEvent event = new TeamEliminationEvent(this, team, message);
		Bukkit.getPluginManager().callEvent(event);

		for (GamePlayer gp : players) {
			if (isEliminated(gp.getPlayer()))
				continue;

			this.eliminate(gp);
		}

		for (PlayerData data : this.player_data.values()) {
			if (data.isDisconnected)
				continue;

			Player player = data.getOwner().getPlayer();

			player.sendMessage((String) null);
			player.sendMessage("§lTEAM ELIMINATED > §r" + event.getEliminationMessage());
			player.sendMessage((String) null);
		}

		this.eliminated.put(team, this.team_data.remove(team));

		if (isGameEnded())
			stopGame();
	}

	private void eliminate(GamePlayer gp) {
		GamePlayerEliminateEvent event = new GamePlayerEliminateEvent(gp);
		Bukkit.getPluginManager().callEvent(event);

		gp.getStatisticManager().incrementStatistic(GameStatistic.FINAL_DEATHS, 1);

		setSpectator(gp.getPlayer(), true);
		gp.getPlayer().sendMessage("§cYou have been eliminated!");
	}

	private boolean isGameEnded() {
		return this.team_data.size() == 1;
	}

	static class PlayerData {
		private BedwarsPlayer owner;
		private Team team;

		private boolean isDisconnected;
		private boolean isSpectator;
		private boolean isInvincible;
		private boolean isEliminated;

		public PlayerData(BedwarsPlayer owner, Team team) {

			this.owner = owner;
			this.team = team;
		}

		public BedwarsPlayer getOwner() {
			return this.owner;
		}

		public Team getTeam() {
			return this.team;
		}

		public void setTeam(Team team) {
			this.team = team;
		}

		public void setDisconnected(boolean isDisconnected) {
			this.isDisconnected = isDisconnected;
		}

		public boolean isDisconnected() {
			return this.isDisconnected;
		}

		public void setSpectator(boolean isSpectator) {
			this.isSpectator = isSpectator;
		}

		public boolean isSpectator() {
			return this.isSpectator;
		}

		public void setInvincible(boolean isInvincible) {
			this.isInvincible = isInvincible;
		}

		public boolean isInvincible() {
			return this.isInvincible;
		}

		public void setEliminated(boolean isEliminated) {
			this.isEliminated = isEliminated;
		}

		public boolean isEliminated() {
			return this.isEliminated;
		}

	}

	static class TeamData {

		private GameTeam owner;
		private TeamGenerator gen;
		private Collection<GamePlayer> players;

		private BedwarsBed bed;
		private Location spawn;

		public TeamData(GameTeam owner, Collection<GamePlayer> players, BedwarsBed bed, TeamGenerator gen, Location spawn) {

			this.owner = owner;
			this.players = players;
			this.bed = bed;
			this.gen = gen;
			this.spawn = spawn;
		}

	}

}