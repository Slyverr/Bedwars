package com.slyvr.v1_16_R3.npc;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;

import com.slyvr.api.game.Game;
import com.slyvr.v1_16_R3.entity.ai.PathfinderGoalLookAtNearbyGamePlayer;

import net.minecraft.server.v1_16_R3.DamageSource;
import net.minecraft.server.v1_16_R3.Entity;
import net.minecraft.server.v1_16_R3.EntityCreature;
import net.minecraft.server.v1_16_R3.EntityTypes;
import net.minecraft.server.v1_16_R3.EnumMoveType;
import net.minecraft.server.v1_16_R3.Vec3D;

public class Shopkeeper extends EntityCreature implements com.slyvr.api.npc.Shopkeeper {

	private Game game;
	private boolean hasSpawned;

	public Shopkeeper(Game game, Location loc) {
		super(EntityTypes.VILLAGER, ((CraftWorld) loc.getWorld()).getHandle());

		this.game = game;
		this.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());

		this.setInvulnerable(true);
		this.setSilent(true);

		this.registerGoals();
	}

	private void registerGoals() {
		this.goalSelector.a(1, new PathfinderGoalLookAtNearbyGamePlayer(this, this.game, 32F));
	}

	@Override
	protected void initPathfinder() {
	}

	@Override
	public void checkDespawn() {
		return;
	}

	@Override
	public void collide(Entity entity) {
		return;
	}

	@Override
	public void move(EnumMoveType enummovetype, Vec3D vec3d) {
		return;
	}

	@Override
	public boolean damageEntity(DamageSource damagesource, float f) {
		return false;
	}

	@Override
	public Game getGame() {
		return this.game;
	}

	@Override
	public void spawn() {
		if (!this.hasSpawned)
			this.hasSpawned = this.world.addEntity(this);
	}

	@Override
	public void remove() {
		if (hasSpawned)
			this.dead = true;
	}

	@Override
	public void teleport(Location loc) {
		if (loc != null)
			setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
	}

}