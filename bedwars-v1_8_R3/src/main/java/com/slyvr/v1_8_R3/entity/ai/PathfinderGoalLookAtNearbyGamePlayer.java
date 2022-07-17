package com.slyvr.v1_8_R3.entity.ai;

import org.bukkit.entity.Player;

import com.slyvr.api.game.Game;

import net.minecraft.server.v1_8_R3.EntityInsentient;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.PathfinderGoalLookAtPlayer;

public class PathfinderGoalLookAtNearbyGamePlayer extends PathfinderGoalLookAtPlayer {

	private Game game;

	public PathfinderGoalLookAtNearbyGamePlayer(EntityInsentient entity, Game game, float radius) {
		super(entity, EntityPlayer.class, radius);

		this.game = game;
	}

	@Override
	public boolean a() {
		super.a();

		if (!(this.b instanceof EntityPlayer))
			return false;

		Player player = ((EntityPlayer) this.b).getBukkitEntity();
		return !this.game.isSpectator(player) && this.game.getGamePlayer(player) != null;
	}

}