package com.slyvr.v1_17_R1.npc;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

import com.slyvr.api.game.Game;
import com.slyvr.v1_17_R1.entity.ai.LookAtNearbyGamePlayerGoal;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.Vec3;

public class Upgrader extends PathfinderMob implements com.slyvr.api.npc.Upgrader {

	private Game game;

	private boolean hasSpawned;

	public Upgrader(Game game, Location loc) {
		super(EntityType.VILLAGER, ((CraftWorld) loc.getWorld()).getHandle());

		this.game = game;
		this.moveTo(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());

		this.setInvulnerable(true);
		this.setSilent(true);
		this.collides = false;

		this.initPathfinder();
	}

	private void initPathfinder() {
		this.goalSelector.addGoal(0, new LookAtNearbyGamePlayerGoal(this, this.game, 32F));
	}

	@Override
	protected void registerGoals() {
	}

	@Override
	public void checkDespawn() {
		return;
	}

	@Override
	public void move(MoverType enummovetype, Vec3 vec3d) {
		return;
	}

	@Override
	public Game getGame() {
		return this.game;
	}

	@Override
	public void spawn() {
		if (!this.hasSpawned)
			this.hasSpawned = this.level.addEntity(this, SpawnReason.CUSTOM);
	}

	@Override
	public void remove() {
		if (hasSpawned)
			this.discard();
	}

	@Override
	public void teleport(Location loc) {
		if (loc != null)
			this.moveTo(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
	}

}