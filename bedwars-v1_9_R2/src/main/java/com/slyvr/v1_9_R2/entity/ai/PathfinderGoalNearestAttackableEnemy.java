package com.slyvr.v1_9_R2.entity.ai;

import org.bukkit.entity.Player;

import com.google.common.base.Predicate;
import com.slyvr.api.game.Game;
import com.slyvr.api.game.player.GamePlayer;
import com.slyvr.api.team.Team;

import net.minecraft.server.v1_9_R2.EntityCreature;
import net.minecraft.server.v1_9_R2.EntityPlayer;
import net.minecraft.server.v1_9_R2.PathfinderGoalNearestAttackableTarget;

public class PathfinderGoalNearestAttackableEnemy extends PathfinderGoalNearestAttackableTarget<EntityPlayer> {

	public PathfinderGoalNearestAttackableEnemy(EntityCreature mob, Game game, Team team) {
		super(mob, EntityPlayer.class, 0, false, true, getPredicate(game, team));

	}

	private static Predicate<EntityPlayer> getPredicate(Game game, Team team) {
		return entity -> {
			Player player = entity.getBukkitEntity();

			if (game.isSpectator(player))
				return false;

			GamePlayer gp = game.getGamePlayer(player);
			return gp != null && gp.getTeam() != team;
		};
	}

}