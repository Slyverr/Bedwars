package com.slyvr.v1_18_R1.entity.ai;

import com.slyvr.api.game.Game;
import com.slyvr.api.game.player.GamePlayer;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;

public class LookAtNearbyGamePlayerGoal extends LookAtPlayerGoal {

	private Game game;

	public LookAtNearbyGamePlayerGoal(Mob entity, Game game, float lookDistance) {
		super(entity, ServerPlayer.class, lookDistance);

		this.game = game;
	}

	public boolean a() {
		super.canUse();

		if (!(this.lookAt instanceof ServerPlayer))
			return false;

		GamePlayer gp = this.game.getGamePlayer(((ServerPlayer) this.lookAt).getBukkitEntity());
		return gp != null && !this.game.isSpectator(gp.getPlayer());
	}

}