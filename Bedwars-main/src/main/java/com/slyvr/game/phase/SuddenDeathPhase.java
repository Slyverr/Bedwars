package com.slyvr.game.phase;

import org.bukkit.Location;

import com.slyvr.api.game.Game;
import com.slyvr.api.game.GamePhase;
import com.slyvr.api.team.GameTeam;
import com.slyvr.api.team.Team;
import com.slyvr.bedwars.Bedwars;

public class SuddenDeathPhase extends GamePhase {

	public SuddenDeathPhase(int duration) {
		super("Sudden Death", duration);

	}

	@Override
	public boolean apply(Game game) {
		if (game == null)
			return false;

		Location loc = game.getArena().getDragonSpawnPoint();
		if (loc == null)
			return false;

		for (GameTeam team : game.getTeams()) {
			this.spawnDragon(game, team.getTeam(), loc);

			if (team.getUpgradeManager().contains(Bedwars.getInstance().getUpgradesManager().getUpgrade("Dragon Buff")))
				this.spawnDragon(game, team.getTeam(), loc);
		}

		return true;
	}

	private void spawnDragon(Game game, Team team, Location loc) {
		Bedwars.getInstance().getEntityManager().createDragon(game, team, null, loc).spawn();
	}

}