package com.slyvr.upgrade;

import java.util.Collection;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import com.slyvr.api.game.Game;
import com.slyvr.api.game.player.GamePlayer;

public class EffectUpgrade extends AbstractUpgrade {

	private PotionEffect effect;

	public EffectUpgrade(String name, PotionEffect effect) {
		super(name);

	}

	@Override
	public boolean apply(GamePlayer gp) {
		if (gp == null)
			return false;

		Game game = gp.getGame();
		if (game == null)
			return false;

		Collection<GamePlayer> team_players = game.getTeamPlayers(gp.getTeam());
		if (team_players.isEmpty())
			return false;

		for (GamePlayer inGame : team_players) {
			Player player = inGame.getPlayer();

			if (game.isEliminated(player) || game.isSpectator(player))
				continue;

			player.addPotionEffect(this.effect);
		}

		return true;
	}

}