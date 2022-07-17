package com.slyvr.trap;

import java.util.Arrays;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import com.google.common.base.Preconditions;
import com.slyvr.api.game.Game;
import com.slyvr.api.game.player.GamePlayer;
import com.slyvr.api.team.Team;
import com.slyvr.api.trap.Trap;
import com.slyvr.api.trap.TrapTarget;
import com.slyvr.listener.GamePlayerListener;

public class EffectTrap extends AbstractTrap implements Trap {

	private PotionEffect[] effects;

	public EffectTrap(String name, TrapTarget target, int duration, PotionEffect... effects) {
		super(name, target, duration);

		Preconditions.checkNotNull(effects, "Effect cannot be null!");

		this.effects = effects;
	}

	@Override
	public boolean onTrigger(GamePlayer gp, Team team) {
		if (gp == null || team == null)
			return false;

		Game game = gp.getGame();

		Player player = gp.getPlayer();
		if (GamePlayerListener.isTrapSafe(player))
			return false;

		switch (this.target) {
			case ENEMY:
				this.applyEffects(gp);
				return true;
			case ENEMY_TEAM:
				this.applyEffects(game, gp.getTeam());
				return true;
			case PLAYER_TEAM:
				this.applyEffects(game, team);
				return true;
			default:
				return false;
		}

	}

	private void applyEffects(GamePlayer gp) {
		for (PotionEffect effect : this.effects)
			effect.apply(gp.getPlayer());
	}

	private void applyEffects(Game game, Team team) {
		for (GamePlayer gp : game.getTeamPlayers(team))
			this.applyEffects(gp);
	}

	@Override
	public int hashCode() {
		return super.hashCode() * 31 + Arrays.hashCode(this.effects);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (!super.equals(obj) || !(obj instanceof EffectTrap))
			return false;

		EffectTrap other = (EffectTrap) obj;
		return Arrays.equals(this.effects, other.effects);
	}

}
