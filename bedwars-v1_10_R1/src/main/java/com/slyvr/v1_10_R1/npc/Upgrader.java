package com.slyvr.v1_10_R1.npc;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_10_R1.CraftWorld;

import com.slyvr.api.game.Game;
import com.slyvr.v1_10_R1.entity.ai.PathfinderGoalLookAtNearbyGamePlayer;

import net.minecraft.server.v1_10_R1.DamageSource;
import net.minecraft.server.v1_10_R1.Entity;
import net.minecraft.server.v1_10_R1.EntityCreature;
import net.minecraft.server.v1_10_R1.World;

public class Upgrader extends EntityCreature implements com.slyvr.api.npc.Upgrader {

	private Game game;

	private boolean hasSpawned;

	public Upgrader(Game game, Location loc) {
		super(((CraftWorld) loc.getWorld()).getHandle());

		this.game = game;
		this.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());

		this.setInvulnerable(true);
		this.setSilent(true);

		this.initPathfinder();
	}

	public Upgrader(World world) {
		super(world);
	}

	private void initPathfinder() {
		this.goalSelector.a(1, new PathfinderGoalLookAtNearbyGamePlayer(this, this.game, 32F));
	}

	@Override
	public void collide(Entity entity) {
		if (this.game == null)
			super.collide(entity);
	}

	@Override
	public void move(double d0, double d1, double d2) {
		if (this.game == null)
			super.move(d0, d1, d2);
	}

	@Override
	public boolean damageEntity(DamageSource damagesource, float f) {
		return this.game == null && super.damageEntity(damagesource, f);
	}

	@Override
	public Game getGame() {
		return this.game;
	}

	@Override
	public void spawn() {
		if (!this.hasSpawned)
			this.world.addEntity(this);
	}

	@Override
	public void remove() {
		if (this.hasSpawned)
			this.dead = true;
	}

	@Override
	public void teleport(Location loc) {
		if (loc != null)
			setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
	}

}