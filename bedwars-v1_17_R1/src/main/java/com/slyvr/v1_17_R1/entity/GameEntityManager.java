package com.slyvr.v1_17_R1.entity;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;

import com.slyvr.api.entity.BedBug;
import com.slyvr.api.entity.BodyGuard;
import com.slyvr.api.entity.Dragon;
import com.slyvr.api.entity.GameEntity;
import com.slyvr.api.game.Game;
import com.slyvr.api.game.player.GamePlayer;
import com.slyvr.api.team.Team;

public class GameEntityManager implements com.slyvr.api.entity.GameEntityManager {

	@Override
	public BedBug createBedBug(Game game, Team team, GamePlayer owner, Location loc) {
		return new com.slyvr.v1_17_R1.entity.BedBug(game, team, owner, loc);
	}

	@Override
	public BedBug getBedBug(Entity entity) {
		net.minecraft.world.entity.Entity result = ((CraftEntity) entity).getHandle();
		return result instanceof BedBug ? (BedBug) result : null;
	}

	@Override
	public boolean isBedBug(Entity entity) {
		return entity != null ? ((CraftEntity) entity).getHandle() instanceof BedBug : false;
	}

	@Override
	public BodyGuard createBodyGuard(Game game, Team team, GamePlayer owner, Location loc) {
		return new com.slyvr.v1_17_R1.entity.BodyGuard(game, team, owner, loc);
	}

	@Override
	public BodyGuard getBodyGuard(Entity entity) {
		net.minecraft.world.entity.Entity result = ((CraftEntity) entity).getHandle();
		return result instanceof BodyGuard ? (BodyGuard) result : null;
	}

	@Override
	public boolean isBodyGuard(Entity entity) {
		return entity != null ? ((CraftEntity) entity).getHandle() instanceof BodyGuard : false;
	}

	@Override
	public Dragon createDragon(Game game, Team team, GamePlayer owner, Location loc) {
		return new com.slyvr.v1_17_R1.entity.Dragon(game, team, owner, loc);
	}

	@Override
	public Dragon getDragon(Entity entity) {
		net.minecraft.world.entity.Entity result = ((CraftEntity) entity).getHandle();
		return result instanceof Dragon ? (Dragon) result : null;
	}

	@Override
	public boolean isDragon(Entity entity) {
		return entity != null ? ((CraftEntity) entity).getHandle() instanceof Dragon : false;
	}

	@Override
	public GameEntity getGameEntity(Entity entity) {
		net.minecraft.world.entity.Entity result = ((CraftEntity) entity).getHandle();
		return result instanceof GameEntity ? (GameEntity) result : null;
	}

	@Override
	public boolean isGameEntity(Entity entity) {
		return entity != null ? ((CraftEntity) entity).getHandle() instanceof GameEntity : false;
	}

}