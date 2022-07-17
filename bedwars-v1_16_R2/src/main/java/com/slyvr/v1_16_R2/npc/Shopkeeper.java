package com.slyvr.v1_16_R2.npc;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R2.CraftWorld;

import com.slyvr.api.game.Game;
import com.slyvr.v1_16_R2.entity.ai.PathfinderGoalLookAtNearbyGamePlayer;

import net.minecraft.server.v1_16_R2.DamageSource;
import net.minecraft.server.v1_16_R2.Entity;
import net.minecraft.server.v1_16_R2.EntityCreature;
import net.minecraft.server.v1_16_R2.EntityTypes;
import net.minecraft.server.v1_16_R2.EnumMoveType;
import net.minecraft.server.v1_16_R2.Vec3D;

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
	public void collide(Entity entity) {
	}

	@Override
	public void checkDespawn() {
	}

	@Override
	public void move(EnumMoveType enummovetype, Vec3D vec3d) {
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