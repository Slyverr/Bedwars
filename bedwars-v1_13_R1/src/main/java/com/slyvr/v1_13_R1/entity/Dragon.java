package com.slyvr.v1_13_R1.entity;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_13_R1.CraftWorld;
import org.bukkit.entity.Entity;

import com.slyvr.api.entity.GameEntityType;
import com.slyvr.api.game.Game;
import com.slyvr.api.game.player.GamePlayer;
import com.slyvr.api.team.Team;

import net.minecraft.server.v1_13_R1.EntityEnderDragon;

public class Dragon extends EntityEnderDragon implements com.slyvr.api.entity.Dragon {

	private Game game;
	private GamePlayer owner;
	private Team team;

	private boolean hasSpawned;

	public Dragon(Game game, Team team, GamePlayer owner, Location loc) {
		super(((CraftWorld) loc.getWorld()).getHandle());

		this.game = game;
		this.team = team;
		this.owner = owner;

		this.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
	}

	@Override
	protected void dropDeathLoot(boolean flag, int i) {
		return;
	}

	@Override
	protected void dropEquipment(boolean flag, int i) {
		return;
	}

	@Override
	public int getExpReward() {
		return 0;
	}

	@Override
	public Game getGame() {
		return this.game;
	}

	@Override
	public Team getGameTeam() {
		return this.team;
	}

	@Override
	public GamePlayer getOwner() {
		return this.owner;
	}

	@Override
	public GameEntityType getGameEntityType() {
		return GameEntityType.DRAGON;
	}

	@Override
	public Entity getEntity() {
		if (this.hasSpawned)
			return getBukkitEntity();

		return null;
	}

	@Override
	public Entity spawn() {
		if (!this.hasSpawned)
			this.hasSpawned = this.world.addEntity(this);

		return getBukkitEntity();
	}

	@Override
	public void remove() {
		this.dead = true;
	}

	@Override
	public boolean hasSpawned() {
		return this.hasSpawned;
	}

}