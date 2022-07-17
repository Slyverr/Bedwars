package com.slyvr.upgrade;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import com.google.common.base.Preconditions;
import com.slyvr.api.game.Game;
import com.slyvr.api.game.player.GamePlayer;

public class TieredEffectUpgrade extends AbstractTieredUpgrade {

	private List<PotionEffect> effects;

	public TieredEffectUpgrade(String name, List<PotionEffect> effects) {
		super(name, 0, effects.size());

		Preconditions.checkNotNull(effects, "Effects cannot be null!");

		List<PotionEffect> result = new ArrayList<>(effects.size());
		for (PotionEffect effect : effects) {
			if (effect != null)
				result.add(effect);

		}

		if (result.isEmpty())
			throw new IllegalArgumentException("Effects cannot be empty!");

		this.effects = effects;
	}

	@Override
	public boolean apply(GamePlayer gp) {
		if (this.current == 0 || gp == null)
			return false;

		Game game = gp.getGame();

		Collection<GamePlayer> team_players = game.getTeamPlayers(gp.getTeam());
		if (team_players.isEmpty())
			return false;

		PotionEffect effect = this.effects.get(this.current - 1);
		for (GamePlayer inGame : team_players) {
			Player player = inGame.getPlayer();

			if (game.isEliminated(player) || game.isSpectator(player))
				continue;

			player.addPotionEffect(effect);
		}

		return true;
	}

}