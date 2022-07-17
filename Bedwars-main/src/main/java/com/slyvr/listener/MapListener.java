package com.slyvr.listener;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Explosive;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.SplashPotion;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.util.Vector;

import com.slyvr.api.arena.Region;
import com.slyvr.api.entity.GameEntityManager;
import com.slyvr.api.event.fireball.FireballExplodeEvent;
import com.slyvr.api.event.player.GamePlayerBlockBreakEvent;
import com.slyvr.api.event.player.GamePlayerBlockPlaceEvent;
import com.slyvr.api.event.tnt.TNTExplodeEvent;
import com.slyvr.api.game.Game;
import com.slyvr.api.game.player.GamePlayer;
import com.slyvr.api.npc.NPCManager;
import com.slyvr.bedwars.Bedwars;
import com.slyvr.game.AbstractGame;
import com.slyvr.util.BedUtils;
import com.slyvr.util.ChatUtils;

public class MapListener implements Listener {

	private static final Map<Game, Set<Block>> GAME_BLOCKS = new HashMap<>();

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onGamePlayerMapBlockPlace(BlockPlaceEvent event) {
		Material type = event.getBlock().getType();

		if (type == Material.TNT || type == Material.SPONGE || BedUtils.isBed(event.getBlock()))
			return;

		Player player = event.getPlayer();

		Game game = AbstractGame.getPlayerGame(player);
		if (game == null)
			return;

		if (!game.hasStarted() || game.isSpectator(player)) {
			event.setCancelled(true);
			return;
		}

		Block block = event.getBlock();

		Region region = game.getArena().getRegion();
		if (block.getY() > region.getMaxY()) {
			player.sendMessage("§cYou have reached the build limit!");
			event.setCancelled(true);
			return;
		}

		if (!region.isInside(block)) {
			player.sendMessage("§cYou can't place a block here!");
			event.setCancelled(true);
			return;
		}

		if (callEvent(new GamePlayerBlockPlaceEvent(game.getGamePlayer(player), block)).isCancelled()) {
			event.setCancelled(true);
			return;
		}

		MapListener.addBlock(game, event.getBlock());
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onGamePlayerMapBlockBreak(BlockBreakEvent event) {
		if (BedUtils.isBed(event.getBlock().getType()))
			return;

		Player player = event.getPlayer();

		Game game = AbstractGame.getPlayerGame(player);
		if (game == null)
			return;

		if (game.isSpectator(player) || game.isEliminated(player)) {
			event.setCancelled(true);
			return;
		}

		Block block = event.getBlock();
		if (block.hasMetadata("bedwars")) {
			if (callEvent(new GamePlayerBlockBreakEvent(game.getGamePlayer(player), block)).isCancelled()) {
				event.setCancelled(true);
				return;
			}

			block.removeMetadata("bedwars", Bedwars.getInstance());
			return;
		}

		ChatUtils.sendMessage(player, "&cYou can only break blocks placed by a player!");
		event.setCancelled(true);
	}

	@EventHandler
	public void onEntitySpawn(EntitySpawnEvent event) {
		Entity entity = event.getEntity();

		if ((entity instanceof org.bukkit.entity.Item) || (entity instanceof Projectile) || (entity instanceof Explosive) || (entity instanceof ArmorStand))
			return;

		if (entity instanceof SplashPotion)
			return;

		NPCManager manager = Bedwars.getInstance().getNPCManager();
		if (manager.isNPC(event.getEntity()))
			return;

		GameEntityManager manager2 = Bedwars.getInstance().getEntityManager();
		if (manager2.isGameEntity(event.getEntity()))
			return;

		event.setCancelled(true);
	}

	@EventHandler
	public void onTNTExplosion(EntityExplodeEvent event) {
		if (event.getEntityType() != EntityType.PRIMED_TNT)
			return;

		Entity entity = event.getEntity();

		if (!entity.hasMetadata("game") || !entity.hasMetadata("player"))
			return;

		Game game = (Game) entity.getMetadata("game").get(0).value();
		GamePlayer owner = (GamePlayer) entity.getMetadata("player").get(0).value();

		// handling blocks to break
		Iterator<Block> iterator = event.blockList().iterator();
		while (iterator.hasNext()) {
			Block block = iterator.next();
			if (!block.hasMetadata("bedwars") || isStainedGlass(block))
				iterator.remove();
		}

		Collection<Entity> entities = entity.getNearbyEntities(5, 5, 5);

		Cancellable cancellable = callEvent(new TNTExplodeEvent(owner, (TNTPrimed) entity, entities));
		if (cancellable.isCancelled()) {
			event.setCancelled(true);
			return;
		}

		float kb = Bedwars.getInstance().getGameSettings().getTNTExplosionKb();

		for (Entity nearby : entities) {
			if (nearby.getType() == EntityType.DROPPED_ITEM)
				continue;

			Location entityLoc = entity.getLocation();

			Vector vec = null;
			if (nearby.getLocation().distanceSquared(entityLoc) > 1)
				vec = nearby.getLocation().toVector().subtract(entityLoc.toVector()).normalize().multiply(kb);
			else
				vec = new Vector(0, 1, 0);

			if (!(nearby instanceof Player)) {
				nearby.setVelocity(vec);
				continue;
			}

			Player player = (Player) nearby;
			GamePlayer gp = game.getGamePlayer(player);

			if (gp != null && game.isSpectator(player))
				player.setVelocity(vec);

		}

	}

	@EventHandler
	public void onFireballExplosion(EntityExplodeEvent event) {
		if (event.getEntityType() != EntityType.FIREBALL)
			return;

		Entity entity = event.getEntity();

		if (!entity.hasMetadata("game") || !entity.hasMetadata("player"))
			return;

		Game game = (Game) entity.getMetadata("game").get(0).value();
		GamePlayer owner = (GamePlayer) entity.getMetadata("player").get(0).value();

		Iterator<Block> iterator = event.blockList().iterator();
		while (iterator.hasNext()) {
			Block block = iterator.next();
			if (!block.hasMetadata("bedwars") || isStainedGlass(block))
				iterator.remove();
		}

		Collection<Entity> entities = entity.getNearbyEntities(5, 5, 5);

		Cancellable cancellable = callEvent(new FireballExplodeEvent(owner, (Fireball) entity, entities));
		if (cancellable.isCancelled()) {
			event.setCancelled(true);
			return;
		}

		float kb = Bedwars.getInstance().getGameSettings().getFireballExplosionKb();

		for (Entity nearby : entities) {
			if (nearby.getType() == EntityType.DROPPED_ITEM)
				continue;

			Location entityLoc = entity.getLocation();

			Vector vec = null;
			if (nearby.getLocation().distanceSquared(entityLoc) > 1)
				vec = nearby.getLocation().toVector().subtract(entityLoc.toVector()).normalize().multiply(kb);
			else
				vec = new Vector(0, 1, 0);

			if (!(nearby instanceof Player)) {
				nearby.setVelocity(vec);
				continue;
			}

			Player player = (Player) nearby;
			GamePlayer gp = game.getGamePlayer(player);

			if (gp != null && game.isSpectator(player))
				player.setVelocity(vec);

		}

	}

	private Cancellable callEvent(Event event) {
		Bukkit.getPluginManager().callEvent(event);

		return (Cancellable) event;
	}

	@EventHandler
	public void onBlockFire(BlockBurnEvent event) {
		event.setCancelled(true);
	}

	@EventHandler
	public void onBlockIgnite(BlockIgniteEvent event) {
		if (event.getCause() == IgniteCause.SPREAD)
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onEntitySpawn(ItemSpawnEvent event) {
		if (BedUtils.isBed(event.getEntity().getItemStack().getType()))
			event.setCancelled(true);
	}

	public static void addBlock(Game game, Block block) {
		Set<Block> blocks = MapListener.GAME_BLOCKS.get(game);

		if (blocks == null)
			MapListener.GAME_BLOCKS.put(game, blocks = new HashSet<>());

		block.setMetadata("bedwars", GamePlayerListener.EMPTY);
		blocks.add(block);
	}

	public static void addBlocks(Game game, Collection<Block> blocks) {
		Set<Block> blocks1 = MapListener.GAME_BLOCKS.get(game);

		if (blocks1 == null)
			MapListener.GAME_BLOCKS.put(game, blocks1 = new HashSet<>());

		for (Block block : blocks) {
			block.setMetadata("bedwars", GamePlayerListener.EMPTY);

			blocks1.add(block);
		}
	}

	public static void resetArena(Game game) {
		Set<Block> blocks = MapListener.GAME_BLOCKS.get(game);

		if (blocks != null) {
			Bedwars instance = Bedwars.getInstance();

			for (Block block : blocks) {
				if (block.getType() != Material.AIR)
					block.setType(Material.AIR);

				block.removeMetadata("bedwars", instance);
			}

			blocks.clear();
		}

	}

	private boolean isStainedGlass(Block block) {
		return block.getType().toString().contains("STAINED_GLASS");
	}

}