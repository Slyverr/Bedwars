package com.slyvr.v1_9_R2.entity;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

import com.slyvr.api.entity.GameEntityType;
import com.slyvr.api.game.Game;
import com.slyvr.api.game.player.GamePlayer;
import com.slyvr.api.team.Team;
import com.slyvr.v1_9_R2.entity.ai.PathfinderGoalLookAtNearbyGamePlayer;
import com.slyvr.v1_9_R2.entity.ai.PathfinderGoalNearestAttackableEnemy;

import net.minecraft.server.v1_9_R2.EntityCreature;
import net.minecraft.server.v1_9_R2.PathfinderGoalFloat;
import net.minecraft.server.v1_9_R2.PathfinderGoalMeleeAttack;
import net.minecraft.server.v1_9_R2.PathfinderGoalMoveTowardsTarget;
import net.minecraft.server.v1_9_R2.PathfinderGoalRandomStroll;
import net.minecraft.server.v1_9_R2.World;

public class BedBug extends EntityCreature implements com.slyvr.api.entity.BedBug {

	private Game game;
	private GamePlayer owner;
	private Team team;

	private boolean hasSpawned;

	public BedBug(Game game, Team team, GamePlayer owner, Location loc) {
		super(((CraftWorld) loc.getWorld()).getHandle());

		this.game = game;
		this.team = team;
		this.owner = owner;

		setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
		initPathfinder();
	}

	public BedBug(World world) {
		super(world);
	}

	private void initPathfinder() {
		this.goalSelector.a(1, new PathfinderGoalMeleeAttack(this, 1.0D, false));
		this.goalSelector.a(2, new PathfinderGoalMoveTowardsTarget(this, 0.25D, 32F));
		this.goalSelector.a(3, new PathfinderGoalLookAtNearbyGamePlayer(this, this.game, 16F));
		this.goalSelector.a(4, new PathfinderGoalRandomStroll(this, 0.25D));
		this.goalSelector.a(5, new PathfinderGoalFloat(this));

		this.targetSelector.a(1, new PathfinderGoalNearestAttackableEnemy(this, this.game, this.team));
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
		return GameEntityType.BED_BUG;
	}

	@Override
	public Entity getEntity() {
		return this.bukkitEntity;
	}

	@Override
	public Entity spawn() {
		if (!this.hasSpawned)
			this.hasSpawned = this.world.addEntity(this, SpawnReason.CUSTOM);

		return getBukkitEntity();
	}

	@Override
	public void remove() {
		this.dead = true;
	}

	@Override
	public boolean hasSpawned() {
		return hasSpawned();
	}

}