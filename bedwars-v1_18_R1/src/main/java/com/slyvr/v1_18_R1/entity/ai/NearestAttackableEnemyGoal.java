package com.slyvr.v1_18_R1.entity.ai;

import java.util.function.Predicate;

import com.slyvr.api.game.Game;
import com.slyvr.api.game.player.GamePlayer;
import com.slyvr.api.team.Team;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;

public class NearestAttackableEnemyGoal extends NearestAttackableTargetGoal<Player> {

	public NearestAttackableEnemyGoal(Mob mob, Game game, Team team) {
		super(mob, Player.class, 0, false, true, getPredicate(game, team));
	}

	private static Predicate<LivingEntity> getPredicate(Game game, Team team) {
		return entity -> {
			org.bukkit.entity.Player player = ((ServerPlayer) entity).getBukkitEntity();

			if (game.isSpectator(player))
				return false;

			GamePlayer gp = game.getGamePlayer(player);
			return gp != null && gp.getTeam() == team;
		};
	}

}