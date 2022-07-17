package com.slyvr.v1_13_R1.entity;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_13_R1.CraftWorld;
import org.bukkit.entity.Entity;

import com.slyvr.api.entity.GameEntityType;
import com.slyvr.api.game.Game;
import com.slyvr.api.game.player.GamePlayer;
import com.slyvr.api.team.Team;
import com.slyvr.v1_13_R1.entity.ai.PathfinderGoalLookAtNearbyGamePlayer;
import com.slyvr.v1_13_R1.entity.ai.PathfinderGoalNearestAttackableEnemy;

import net.minecraft.server.v1_13_R1.EntityCreature;
import net.minecraft.server.v1_13_R1.EntityTypes;
import net.minecraft.server.v1_13_R1.PathfinderGoalFloat;
import net.minecraft.server.v1_13_R1.PathfinderGoalMeleeAttack;
import net.minecraft.server.v1_13_R1.PathfinderGoalMoveTowardsTarget;
import net.minecraft.server.v1_13_R1.PathfinderGoalRandomLookaround;
import net.minecraft.server.v1_13_R1.PathfinderGoalRandomStroll;

public class BodyGuard extends EntityCreature implements com.slyvr.api.entity.BodyGuard {

	private Game game;
	private Team team;

	private GamePlayer owner;
	private boolean hasSpawned;

	public BodyGuard(Game game, Team team, GamePlayer owner, Location loc) {
		super(EntityTypes.IRON_GOLEM, ((CraftWorld) loc.getWorld()).getHandle());

		this.game = game;
		this.team = team;
		this.owner = owner;

		this.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
		this.registerGoals();
	}

	private void registerGoals() {
		this.goalSelector.a(1, new PathfinderGoalMeleeAttack(this, 1.0D, true));
		this.goalSelector.a(2, new PathfinderGoalMoveTowardsTarget(this, 0.75D, 32F));
		this.goalSelector.a(3, new PathfinderGoalLookAtNearbyGamePlayer(this, this.game, 16F));
		this.goalSelector.a(4, new PathfinderGoalRandomStroll(this, 0.25D));
		this.goalSelector.a(5, new PathfinderGoalRandomLookaround(this));
		this.goalSelector.a(6, new PathfinderGoalFloat(this));

		this.targetSelector.a(1, new PathfinderGoalNearestAttackableEnemy(this, this.game, this.team));
	}

	@Override
	protected void n() {
		return;
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
