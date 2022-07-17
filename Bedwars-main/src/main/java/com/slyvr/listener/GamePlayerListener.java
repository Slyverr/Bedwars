package com.slyvr.listener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.LazyMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import com.cryptomorin.xseries.XBlock;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import com.slyvr.api.arena.Arena;
import com.slyvr.api.arena.BedwarsBed;
import com.slyvr.api.entity.BedBug;
import com.slyvr.api.entity.BodyGuard;
import com.slyvr.api.entity.GameEntity;
import com.slyvr.api.event.player.GamePlayerDamageByGameEntityEvent;
import com.slyvr.api.event.player.GamePlayerDamageByGamePlayerEvent;
import com.slyvr.api.event.player.GamePlayerDeathByGameEntityEvent;
import com.slyvr.api.event.player.GamePlayerDeathByGamePlayerEvent;
import com.slyvr.api.event.player.GamePlayerDeathEvent;
import com.slyvr.api.game.Game;
import com.slyvr.api.game.player.GameInventory;
import com.slyvr.api.game.player.GamePlayer;
import com.slyvr.api.game.player.stats.GameStatistic;
import com.slyvr.api.generator.Resource;
import com.slyvr.api.generator.TeamGenerator;
import com.slyvr.api.generator.TieredGenerator;
import com.slyvr.api.team.GameTeam;
import com.slyvr.api.team.Team;
import com.slyvr.api.util.Version;
import com.slyvr.bedwars.Bedwars;
import com.slyvr.bedwars.BedwarsItems;
import com.slyvr.game.AbstractGame;
import com.slyvr.util.BedUtils;
import com.slyvr.util.ChatUtils;
import com.slyvr.util.TeamUtils;

public class GamePlayerListener implements Listener {

	private static final Map<UUID, Integer> TRAP_SAFE = new HashMap<>();

	private static final Effect FOOTSTEP;
	private static final Effect CLOUD;

	public static final MetadataValue EMPTY = new FixedMetadataValue(Bedwars.getInstance(), null);

	static {
		if (!Version.getVersion().isNewAPI()) {
			FOOTSTEP = Enum.valueOf(Effect.class, "FOOTSTEP");
			CLOUD = Enum.valueOf(Effect.class, "CLOUD");
		} else {
			FOOTSTEP = null;
			CLOUD = null;
		}

	}

	// Bed Listeners
	@EventHandler
	public void onGamePlayerBedBreak(BlockBreakEvent event) {
		if (!BedUtils.isBed(event.getBlock()))
			return;

		Player player = event.getPlayer();

		Game game = AbstractGame.getPlayerGame(player);
		if (game == null || game.isSpectator(player) || game.isEliminated(player))
			return;

		BedwarsBed bed = BedUtils.getArenaBed(game.getArena(), event.getBlock());
		if (bed == null)
			return;

		game.breakTeamBed(bed.getTeam(), player);
		event.setCancelled(true);
	}

	@EventHandler
	public void onGamePlayerBedInteract(PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getPlayer().isSneaking() || !BedUtils.isBed(event.getClickedBlock()))
			return;

		if (AbstractGame.inGame(event.getPlayer()))
			event.setCancelled(true);
	}

	// Player Listeners

	@EventHandler
	public void onGamePlayerReconnect(PlayerJoinEvent event) {
		if (!Bedwars.getInstance().getSettings().isAutoReconnect())
			return;

		Player player = event.getPlayer();

		Game game = AbstractGame.getDisconnectedPlayerGame(player);
		if (game == null)
			return;

		game.reconnect(player);
		event.setJoinMessage(null);
	}

	@EventHandler
	public void onGamePlayerDisconnect(PlayerQuitEvent event) {
		Player player = event.getPlayer();

		Game game = AbstractGame.getPlayerGame(player);
		if (game == null)
			return;

		game.disconnect(player);
		event.setQuitMessage(null);
	}

	@EventHandler
	public void onGamePlayerItemDrop(PlayerDropItemEvent event) {
		Player player = event.getPlayer();

		Game game = AbstractGame.getPlayerGame(player);
		if (game == null)
			return;

		ItemStack drop = event.getItemDrop().getItemStack();

		GamePlayer gp = game.getGamePlayer(player);

		GameInventory inv = gp.getInventory();
		if (!inv.contains(drop, item -> item.getType() == drop.getType() && item.getAmount() == drop.getAmount())) {
			checkForSword(gp);
			updateTeam(gp);
			return;
		}

		event.setCancelled(true);
	}

	@EventHandler
	public void onGamePlayerArmorClick(InventoryClickEvent event) {
		if (event.getSlotType() != SlotType.ARMOR)
			return;

		if (AbstractGame.inGame((Player) event.getWhoClicked()))
			event.setCancelled(true);
	}

	@EventHandler
	public void onGamePlayerSwordPickUp(PlayerPickupItemEvent event) {
		Player player = event.getPlayer();

		Game game = AbstractGame.getPlayerGame(player);
		if (game == null || !game.hasStarted() || game.isSpectator(player))
			return;

		if (isOtherSword(event.getItem().getItemStack().getType()))
			player.getInventory().remove(BedwarsItems.getInstance().getSword().getType());

		updateTeam(game.getGamePlayer(player));
	}

	private static boolean isOtherSword(Material type) {
		if (type == Material.STONE_SWORD || type == Material.IRON_SWORD || type == Material.DIAMOND_SWORD)
			return true;

		return Version.getVersion().isNewAPI() ? type == Material.GOLDEN_SWORD : type == Material.getMaterial("GOLD_SWORD");
	}

	public static void updateTeam(GamePlayer gp) {
		Game game = gp.getGame();

		GameTeam team = game.getGameTeam(gp.getTeam());
		if (team == null)
			return;

		team.getUpgradeManager().apply(gp);
	}

	public static void checkForSword(GamePlayer gp) {
		Inventory inv = gp.getPlayer().getInventory();

		ItemStack sword = BedwarsItems.getInstance().getSword();

		boolean hasOtherSword = false;
		int sword_index = -1;

		ItemStack[] content = inv.getContents();
		for (int i = 0; i < content.length; i++) {
			ItemStack item = content[i];

			if (item == null)
				continue;

			if (item.getType() == sword.getType()) {
				if (sword_index != -1)
					content[sword_index] = null;

				sword_index = i;
				continue;
			}

			if (isOtherSword(item.getType())) {
				if (sword_index != -1)
					content[sword_index] = null;

				hasOtherSword = true;
				continue;
			}

		}

		if (!hasOtherSword && sword_index == -1)
			inv.addItem(BedwarsItems.getInstance().getSword());
	}

	@EventHandler
	public void onGamePlayerItemPickUp(PlayerPickupItemEvent event) {
		Player player = event.getPlayer();

		Game game = AbstractGame.getPlayerGame(player);
		if (game == null || !game.hasStarted())
			return;

		if (game.isSpectator(player)) {
			event.setCancelled(true);
			return;
		}

		if (!Bedwars.getInstance().getTeamForgeSettings().isResourceSplitting())
			return;

		org.bukkit.entity.Item item = event.getItem();
		if (!item.hasMetadata("bedwars"))
			return;

		ItemStack stack = item.getItemStack();

		GamePlayer gp = game.getGamePlayer(player);
		Team team = gp.getTeam();

		double radius = Bedwars.getInstance().getTeamForgeSettings().getSplitRadius();
		for (Entity nearby : item.getNearbyEntities(radius, radius, radius)) {
			if (!(nearby instanceof Player) || nearby.getUniqueId().equals(player.getUniqueId()))
				continue;

			Player p = (Player) nearby;

			GamePlayer nearbyGP = game.getGamePlayer(p);
			if (nearbyGP == null || nearbyGP.getTeam() != team || game.isSpectator(p))
				continue;

			p.getInventory().addItem(stack);
		}

	}

	@EventHandler
	public void onGamePlayerPermItemCheck(InventoryCloseEvent event) {
		Inventory inv = event.getInventory();
		if (inv == null || inv.getType() == InventoryType.PLAYER)
			return;

		Player player = (Player) event.getPlayer();

		Game game = AbstractGame.getPlayerGame(player);
		if (game == null || !game.hasStarted())
			return;

		GamePlayer gp = game.getGamePlayer(player);
		GameInventory game_inv = gp.getInventory();

		for (int i = 0; i < inv.getSize(); i++) {
			ItemStack item = inv.getItem(i);
			if (item == null)
				continue;

			if (!game_inv.contains(item, (inv_item) -> inv_item.getType() == item.getType()))
				continue;

			if (player.getInventory().contains(item.getType()))
				continue;

			inv.setItem(i, null);
			player.getInventory().addItem(item);
		}

		checkForSword(gp);
		updateTeam(gp);
	}

	@EventHandler
	public void onGamePlayerInvisConsume(PlayerItemConsumeEvent event) {
		if (event.getItem().getType() != Material.POTION)
			return;

		Player player = event.getPlayer();

		Game game = AbstractGame.getPlayerGame(player);
		if (game == null || !game.hasStarted() || game.isSpectator(player))
			return;

		PotionMeta meta = (PotionMeta) event.getItem().getItemMeta();
		if (!meta.hasCustomEffect(PotionEffectType.INVISIBILITY))
			return;

		GamePlayer gp = game.getGamePlayer(player);

		Team team = gp.getTeam();
		for (GamePlayer other : game.getGamePlayers()) {
			if (other.getTeam() != team)
				other.getPlayer().hidePlayer(player);
		}

		if (Version.getVersion().isNewAPI())
			return;

		new BukkitRunnable() {
			Location loc = player.getLocation();

			int time = 0;
			int duration = getDuration();

			private int getDuration() {
				for (PotionEffect effect : meta.getCustomEffects()) {
					if (effect.getType() == PotionEffectType.INVISIBILITY)
						return effect.getDuration();
				}

				return 0;
			}

			@Override
			public void run() {
				if (this.time >= this.duration || game.isSpectator(player)) {
					cancel();
					return;
				}

				this.time += 20;
				player.getWorld().playEffect(this.loc, FOOTSTEP, 0);
			}
		}.runTaskTimerAsynchronously(Bedwars.getInstance(), 0, 20);

	}

	@EventHandler
	public void onGamePlayerBridgeEggThrow(ProjectileLaunchEvent event) {
		if (event.getEntityType() != EntityType.EGG)
			return;

		Projectile projectile = event.getEntity();
		if (!(projectile.getShooter() instanceof Player))
			return;

		Bukkit.getScheduler().runTaskAsynchronously(Bedwars.getInstance(), () -> {
			Player player = (Player) projectile.getShooter();

			Game game = AbstractGame.getPlayerGame(player);
			if (game == null || !game.hasStarted() || game.isSpectator(player))
				return;

			GamePlayer gp = game.getGamePlayer(player);

			new BukkitRunnable() {
				Collection<Block> blocks = new ArrayList<>();

				XMaterial wool = TeamUtils.getTeamColoredWool(gp.getTeam());
				int amount = 0;

				@Override
				public void run() {
					if (projectile.isDead() || this.amount >= 35) {
						cancel();
						MapListener.addBlocks(game, this.blocks);
						return;
					}

					Location loc = projectile.getLocation();

					Block center = loc.getBlock();

					Block north = center.getRelative(BlockFace.NORTH);
					Block south = center.getRelative(BlockFace.SOUTH);
					Block east = center.getRelative(BlockFace.EAST);
					Block west = center.getRelative(BlockFace.WEST);

					this.amount++;
					Bukkit.getScheduler().runTaskLater(Bedwars.getInstance(), () -> {
						if (center.getType() == Material.AIR) {
							XBlock.setType(center, this.wool);
							this.blocks.add(center);
						}

						if (north.getType() == Material.AIR) {
							XBlock.setType(north, this.wool);
							this.blocks.add(north);
						}

						if (south.getType() == Material.AIR) {
							XBlock.setType(south, this.wool);
							this.blocks.add(south);
						}

						if (east.getType() == Material.AIR) {
							XBlock.setType(east, this.wool);
							this.blocks.add(east);
						}

						if (west.getType() == Material.AIR) {
							XBlock.setType(west, this.wool);
							this.blocks.add(west);
						}

					}, 1);

					// TODO: Adjust sound
					loc.getWorld().playSound(loc, Sound.BLOCK_BUBBLE_COLUMN_BUBBLE_POP, 100F, 0F);
				}
			}.runTaskTimerAsynchronously(Bedwars.getInstance(), 2, 1);
		});

	}

	@EventHandler
	public void onGamePlayerMagicMilkConsume(PlayerItemConsumeEvent event) {
		if (event.getItem().getType() != Material.MILK_BUCKET)
			return;

		Player player = event.getPlayer();
		if (!AbstractGame.inRunningGame(player))
			return;

		player.getInventory().setItemInHand(null);

		GamePlayerListener.TRAP_SAFE.put(player.getUniqueId(), 30);

		Bukkit.getScheduler().runTaskLaterAsynchronously(Bedwars.getInstance(), () -> {
			GamePlayerListener.TRAP_SAFE.remove(player.getUniqueId());
		}, 20 * 30);

	}

	@EventHandler
	public void onGamePlayerSpongePlace(BlockPlaceEvent event) {
		if (event.getBlock().getType() != Material.SPONGE)
			return;

		Bukkit.getScheduler().runTaskAsynchronously(Bedwars.getInstance(), () -> {
			Player player = event.getPlayer();
			Block block = event.getBlock();

			Game game = AbstractGame.getPlayerGame(player);
			if (game == null || !game.hasStarted() || game.isSpectator(player))
				return;

			block.setMetadata("bedwars-sponge", new LazyMetadataValue(Bedwars.getInstance(), () -> game));

			Location center = block.getLocation().add(.5, 0, .5);
			new BukkitRunnable() {
				private World world = center.getWorld();
				private int radius = 1;
				private int time = 0;

				@Override
				public void run() {
					if (this.time == 50) {
						cancel();
						Bukkit.getScheduler().runTask(Bedwars.getInstance(), () -> block.setType(Material.AIR));
						return;
					}

					this.time += 10;
					this.radius++;

					if (Version.getVersion().isNewAPI())
						this.world.spawnParticle(Particle.CLOUD, center, this.radius * this.time, this.radius, this.radius, this.radius);
					else
						this.world.playEffect(center, CLOUD, this.radius * this.time, this.radius);

					XSound.BLOCK_NOTE_BLOCK_PLING.play(center, 1F, 0F);
				}
			}.runTaskTimerAsynchronously(Bedwars.getInstance(), 0, 10);

		});

	}

	@EventHandler
	public void onGamePlayerSpongeBreak(BlockBreakEvent event) {
		if ((event.getBlock().getType() != Material.SPONGE) || !event.getBlock().hasMetadata("bedwars-sponge"))
			return;

		event.setCancelled(true);
	}

	@EventHandler
	public void onGamePlayerDamageEntity(EntityDamageByEntityEvent event) {
		if (!(event.getDamager() instanceof Player))
			return;

		Player damager = (Player) event.getDamager();

		Game game = AbstractGame.getPlayerGame(damager);
		if (game == null)
			return;

		if (!game.hasStarted() || game.isSpectator(damager)) {
			event.setCancelled(true);
			return;
		}

		if (!game.isInvincible(damager))
			return;

		game.setInvincible(damager, false);
		damager.sendMessage(ChatUtils.format("&cYou attacked someone and lost your invincibility!"));
	}

	@EventHandler
	public void onGamePlayerDamageByEntity(EntityDamageByEntityEvent event) {
		if (!(event.getEntity() instanceof Player))
			return;

		Player player = (Player) event.getEntity();

		Game game = AbstractGame.getPlayerGame(player);
		if (game == null)
			return;

		if (!game.hasStarted() || game.isSpectator(player) || game.isInvincible(player)) {
			event.setCancelled(true);
			return;
		}

		String deathMessage = null;

		GamePlayer damaged = game.getGamePlayer(player);
		GamePlayer damager;
		if (event.getDamager() instanceof Player) {
			if ((damager = game.getGamePlayer((Player) event.getDamager())) == null)
				return;

			GamePlayerDamageByGamePlayerEvent bwEvent = new GamePlayerDamageByGamePlayerEvent(damaged, damager, event.getCause());
			Bukkit.getPluginManager().callEvent(bwEvent);

			if (bwEvent.isCancelled()) {
				event.setCancelled(true);
				return;
			}

			if (player.getHealth() - event.getFinalDamage() > 0)
				return;

			String message = getDeathMessage(damaged, damager);

			GamePlayerDeathByGamePlayerEvent bwEvent2 = new GamePlayerDeathByGamePlayerEvent(damaged, damager, event.getCause(), message);
			Bukkit.getPluginManager().callEvent(bwEvent2);

			deathMessage = bwEvent2.getDeathMessage();

			checkDropsAndEnderChestContent(game, damaged, damager);
		} else {
			GameEntity entity = Bedwars.getInstance().getEntityManager().getGameEntity(event.getDamager());
			if (entity == null)
				return;

			GamePlayerDamageByGameEntityEvent bwEvent = new GamePlayerDamageByGameEntityEvent(damaged, entity, event.getCause());
			Bukkit.getPluginManager().callEvent(bwEvent);

			if (bwEvent.isCancelled()) {
				event.setCancelled(true);
				return;
			}

			if (player.getHealth() - event.getFinalDamage() > 0)
				return;

			String message = getDeathMessage(damaged, entity.getOwner());

			GamePlayerDeathByGameEntityEvent bwEvent2 = new GamePlayerDeathByGameEntityEvent(damaged, entity, event.getCause(), message);
			Bukkit.getPluginManager().callEvent(bwEvent2);

			deathMessage = bwEvent2.getDeathMessage();

			checkDropsAndEnderChestContent(game, damaged, damager = entity.getOwner());
		}

		if (!game.hasBed(damaged.getTeam()))
			damager.getStatisticManager().incrementStatistic(GameStatistic.FINAL_KILLS, 1);
		else
			damager.getStatisticManager().incrementStatistic(GameStatistic.KILLS, 1);

		game.killPlayer(player, deathMessage);
		event.setCancelled(true);
	}

	@EventHandler
	public void onGamePlayerDamage(EntityDamageEvent event) {
		if (!(event.getEntity() instanceof Player))
			return;

		DamageCause cause = event.getCause();
		if (cause == DamageCause.ENTITY_ATTACK || Bedwars.getInstance().getVersion().isNewAPI() && cause == DamageCause.ENTITY_SWEEP_ATTACK)
			return;

		Player player = (Player) event.getEntity();

		Game game = AbstractGame.getPlayerGame(player);
		if (game == null)
			return;

		if (!game.hasStarted() || game.isSpectator(player) || game.isInvincible(player)) {
			event.setCancelled(true);
			return;
		}

		GamePlayer gp = game.getGamePlayer(player);
		if (player.getHealth() - event.getFinalDamage() > 0)
			return;

		// Player killer = player.getLastDamageCause().getEntity();
		Player killer = player.getKiller();

		GamePlayer gpKiller = null;
		if (killer != null) {
			GamePlayer gpKiller2 = game.getGamePlayer(killer);
			if (gpKiller2 != null && game.isSpectator(killer))
				gpKiller = gpKiller2;
		}

		String message = getDisplayName(gp) + " §7died!";
		switch (cause) {
			case FALL:
				if (gpKiller == null)
					break;

				message = getDisplayName(gp) + " §7died from fall damage while running from " + getDisplayName(gpKiller);
				break;
			case BLOCK_EXPLOSION:
				break;
			case ENTITY_EXPLOSION:
				break;
			case CONTACT:
				break;
			case CUSTOM:
				break;
			case DROWNING:
				message = getDisplayName(gp) + " §7drowned!";
				break;
			case FALLING_BLOCK:
				break;
			case FIRE:
				break;
			case FIRE_TICK:
				break;
			case LAVA:
				break;
			case PROJECTILE:
				break;
			case SUFFOCATION:
				message = getDisplayName(gp) + "§7suffocated!";
				break;
			case SUICIDE:
				break;
			case THORNS:
				break;
			default:
				break;
		}

		GamePlayerDeathEvent bwEvent = new GamePlayerDeathEvent(gp, cause, message);
		Bukkit.getPluginManager().callEvent(bwEvent);

		checkDropsAndEnderChestContent(game, gp, gpKiller);

		game.killPlayer(player, bwEvent.getDeathMessage());
		if (gpKiller != null)
			if (game.hasBed(gp.getTeam()))
				gpKiller.getStatisticManager().incrementStatistic(GameStatistic.KILLS, 1);
			else {
				gpKiller.getStatisticManager().incrementStatistic(GameStatistic.FINAL_KILLS, 1);
				gpKiller.getStatisticManager().getCoinsReward().increment(5);

				killer.sendMessage("§6+5 coins! (Final Kill)");
			}

		event.setCancelled(true);
	}

	@EventHandler
	public void onGamePlayerVoidFall(PlayerMoveEvent event) {
		if (event.getTo().getY() > 0)
			return;

		Player player = event.getPlayer();

		Game game = AbstractGame.getPlayerGame(player);
		if (game == null)
			return;

		if (!game.hasStarted()) {
			player.teleport(game.getArena().getWaitingRoomSpawnPoint());
			return;
		}

		if (game.isSpectator(player)) {
			player.teleport(game.getArena().getSpectatorSpawnPoint());
			return;
		}

		GamePlayer gp = game.getGamePlayer(player);

		StringBuilder message = new StringBuilder().append(getDisplayName(gp));

		GamePlayer killer = game.getGamePlayer(player.getKiller());
		if (killer == null)
			message.append(" §7fell into the void!");
		else
			message.append(" §7was thrown into the void by ").append(getDisplayName(killer)).append("§7!");

		checkDropsAndEnderChestContent(game, gp, killer);

		GamePlayerDeathEvent bwEvent = new GamePlayerDeathEvent(killer, DamageCause.VOID, message.toString());
		Bukkit.getPluginManager().callEvent(bwEvent);

		game.killPlayer(player, bwEvent.getDeathMessage());
	}

	@EventHandler
	public void onGamePlayerChestInteract(PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK || !(event.getClickedBlock().getState() instanceof Chest))
			return;

		Player player = event.getPlayer();

		Game game = AbstractGame.getPlayerGame(player);
		if (game == null || !game.hasStarted())
			return;

		if (game.isSpectator(player)) {
			event.setCancelled(true);
			return;
		}

		Arena arena = game.getArena();

		Team chestTeam = null;
		for (Team team : Team.values()) {
			Chest chest = arena.getTeamChest(team);

			if (chest != null && chest.getLocation().equals(event.getClickedBlock().getLocation())) {
				chestTeam = team;
				break;
			}
		}

		GamePlayer gp = game.getGamePlayer(player);
		if (chestTeam == null || chestTeam == gp.getTeam() || game.isEliminated(chestTeam))
			return;

		String message = "§cYou can't open " + chestTeam.getColoredString() + "team §cchest! Team is not eliminated!";
		player.sendMessage(message);

		event.setCancelled(true);
	}

	@EventHandler
	public void onGamePlayerBodyGuardSpawn(PlayerInteractEvent event) {
		if (!event.hasBlock() || !event.getMaterial().toString().contains("_SPAWN_EGG"))
			return;

		Player player = event.getPlayer();

		Game game = AbstractGame.getPlayerGame(player);
		if (game == null || !game.hasStarted() || game.isSpectator(player))
			return;

		GamePlayer gp = game.getGamePlayer(player);

		BodyGuard guard = Bedwars.getInstance().getEntityManager().createBodyGuard(game, gp.getTeam(), gp, event.getClickedBlock().getLocation().add(0, 1, 0));
		guard.spawn();

		player.sendMessage("§aYou spawned a Body Guard! It will be removed after 60 seconds!");
		Bukkit.getScheduler().runTaskLaterAsynchronously(Bedwars.getInstance(), () -> guard.remove(), 60 * 20);
	}

	@EventHandler
	public void onGamePlayerHungerLost(FoodLevelChangeEvent event) {
		if (!(event.getEntity() instanceof Player))
			return;

		if (AbstractGame.inGame((Player) event.getEntity()))
			event.setCancelled(true);
	}

	@EventHandler
	public void onGamePlayerItemCraft(CraftItemEvent event) {
		if (AbstractGame.inGame((Player) event.getWhoClicked()))
			event.setCancelled(true);
	}

	@EventHandler
	public void onGamePlayerPortalEnter(PlayerPortalEvent event) {
		if (AbstractGame.inGame(event.getPlayer()))
			event.setCancelled(true);
	}

	// Game Entity Listeners

	@EventHandler
	public void onGamePlayerBedBugSpawn(ProjectileHitEvent event) {
		if (event.getEntityType() != EntityType.SNOWBALL || !(event.getEntity().getShooter() instanceof Player))
			return;

		Projectile projectile = event.getEntity();
		Player player = (Player) projectile.getShooter();

		Game game = AbstractGame.getPlayerGame(player);
		if (game == null || !game.hasStarted() || game.isSpectator(player))
			return;

		GamePlayer gp = game.getGamePlayer(player);

		BedBug bedbug = Bedwars.getInstance().getEntityManager().createBedBug(game, gp.getTeam(), gp, projectile.getLocation());
		bedbug.spawn();

		player.sendMessage("§aYou spawned a Bed Bug! It will be removed after 15 seconds!");
		Bukkit.getScheduler().runTaskLaterAsynchronously(Bedwars.getInstance(), () -> bedbug.remove(), 15 * 20);
	}

	// Generator Listeners

	@EventHandler
	public void onGamePlayerPlaceBlockNearbyGenerator(BlockPlaceEvent event) {
		Player player = event.getPlayer();

		Game game = AbstractGame.getPlayerGame(player);
		if (game == null)
			return;

		Block block = event.getBlock();
		for (Resource rsc : Resource.values()) {
			Collection<TieredGenerator> gens = game.getMapResourceGenerator(rsc);
			if (gens == null || gens.isEmpty())
				continue;

			for (TieredGenerator gen : gens) {
				if (gen.getDropLocation().getBlock().getLocation().distanceSquared(block.getLocation()) > 9)
					continue;

				player.sendMessage("§cYou can't place blocks here!");
				event.setCancelled(true);
				return;
			}
		}

	}

	public static boolean isTrapSafe(Player player) {
		return player != null && GamePlayerListener.TRAP_SAFE.containsKey(player.getUniqueId());
	}

	private void checkDropsAndEnderChestContent(Game game, GamePlayer deadgp, GamePlayer killergp) {
		Player dead = deadgp.getPlayer();
		Player killer = killergp != null ? killergp.getPlayer() : null;

		Map<Resource, Integer> drops = killer != null ? checkDrops(dead, killer) : null;

		Bukkit.getScheduler().runTaskLaterAsynchronously(Bedwars.getInstance(), () -> {
			if (!game.hasBed(deadgp.getTeam())) {
				TeamGenerator gen = game.getTeamGenerator(deadgp.getTeam());
				if (gen == null)
					return;

				Bukkit.getScheduler().runTask(Bedwars.getInstance(), () -> {
					dropEnderChestContent(dead, gen.getDropLocation());
				});

				if (killer != null)
					killer.sendMessage("§aContents of " + dead.getDisplayName() + "'s Ender Chest have been dropped in their fountain.");
			}

			if (killer != null) {
				Inventory toInv = killer.getInventory();
				for (Entry<Resource, Integer> entry : drops.entrySet()) {
					Resource rsc = entry.getKey();
					int amount = entry.getValue();

					toInv.addItem(new ItemStack(rsc.getMaterial(), amount));

					killer.sendMessage(rsc.getChatColor() + "+" + amount + " " + rsc.toString());
				}
			}

		}, 1);

	}

	private Map<Resource, Integer> checkDrops(Player from, Player to) {
		Map<Resource, Integer> result = new HashMap<>(4);

		for (ItemStack item : from.getInventory()) {
			if (item == null)
				continue;

			Resource rsc = Resource.getByMaterial(item.getType());
			if (rsc == null)
				continue;

			Integer amount = result.get(rsc);
			if (amount == null)
				result.put(rsc, item.getAmount());
			else
				result.put(rsc, amount + item.getAmount());
		}

		return result;
	}

	private void dropEnderChestContent(Player player, Location loc) {
		World world = loc.getWorld();
		for (ItemStack item : player.getEnderChest())
			if (item != null)
				world.dropItemNaturally(loc, item);

	}

	private String getDeathMessage(GamePlayer dead, GamePlayer killer) {
		return getDisplayName(dead) + " §7was killed by " + getDisplayName(killer) + "§7!";
	}

	private String getDisplayName(GamePlayer gp) {
		return gp.getTeam().getChatColor() + gp.getPlayer().getDisplayName();
	}

}