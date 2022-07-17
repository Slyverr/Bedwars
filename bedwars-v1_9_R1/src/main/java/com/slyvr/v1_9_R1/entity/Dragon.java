package com.slyvr.v1_9_R1.entity;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_9_R1.CraftWorld;
import org.bukkit.entity.Entity;

import com.slyvr.api.entity.GameEntityType;
import com.slyvr.api.game.Game;
import com.slyvr.api.game.player.GamePlayer;
import com.slyvr.api.team.Team;

import net.minecraft.server.v1_9_R1.EntityEnderDragon;
import net.minecraft.server.v1_9_R1.World;

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

	public Dragon(World world) {
		super(world);
	}

	@Override
	protected void dropDeathLoot(boolean flag, int i) {
		if (this.game == null)
			super.dropDeathLoot(flag, i);
	}

	@Override
	protected void dropEquipment(boolean flag, int i) {
		if (this.game == null)
			super.dropEquipment(flag, i);
	}

	@Override
	public int getExpReward() {
		return this.game == null ? super.getExpReward() : 0;
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
		return this.bukkitEntity;
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