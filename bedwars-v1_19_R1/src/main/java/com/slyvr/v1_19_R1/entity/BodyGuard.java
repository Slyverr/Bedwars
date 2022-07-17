package com.slyvr.v1_19_R1.entity;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

import com.slyvr.api.entity.GameEntityType;
import com.slyvr.api.game.Game;
import com.slyvr.api.game.player.GamePlayer;
import com.slyvr.api.team.Team;
import com.slyvr.v1_19_R1.entity.ai.LookAtNearbyGamePlayerGoal;
import com.slyvr.v1_19_R1.entity.ai.NearestAttackableEnemyGoal;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsTargetGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;

public class BodyGuard extends PathfinderMob implements com.slyvr.api.entity.BodyGuard {

	private Game game;
	private Team team;

	private GamePlayer owner;
	private boolean hasSpawned;

	public BodyGuard(Game game, Team team, GamePlayer owner, Location loc) {
		super(EntityType.IRON_GOLEM, ((CraftWorld) loc.getWorld()).getHandle());

		this.game = game;
		this.team = team;
		this.owner = owner;

		this.moveTo(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
		this.initPathfinder();
	}

	private void initPathfinder() {
		this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, true));
		this.goalSelector.addGoal(2, new MoveTowardsTargetGoal(this, 0.75D, 32F));
		this.goalSelector.addGoal(3, new LookAtNearbyGamePlayerGoal(this, this.game, 16F));
		this.goalSelector.addGoal(4, new RandomStrollGoal(this, 0.25D));
		this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
		this.goalSelector.addGoal(6, new FloatGoal(this));

		this.targetSelector.addGoal(1, new NearestAttackableEnemyGoal(this, this.game, this.team));
	}

	@Override
	protected void registerGoals() {

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
		return GameEntityType.BODY_GUARD;
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
			this.hasSpawned = this.level.addFreshEntity(this, SpawnReason.CUSTOM);

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
