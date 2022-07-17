package com.slyvr.team;

import com.google.common.base.Preconditions;
import com.slyvr.api.game.Game;
import com.slyvr.api.team.GameTeam;
import com.slyvr.api.team.Team;
import com.slyvr.api.trap.TrapManager;
import com.slyvr.api.upgrade.UpgradeManager;
import com.slyvr.trap.TeamTrapManager;

public class BedwarsTeam implements GameTeam {

	private final UpgradeManager upgradeManager = new TeamUpgradeManager();
	private final TrapManager trapsManager = new TeamTrapManager();
	private final Game game;
	private final Team team;

	public BedwarsTeam(Game game, Team team) {
		Preconditions.checkNotNull(game, "Game cannot be null");
		Preconditions.checkNotNull(team, "Team cannot be null");

		this.game = game;
		this.team = team;
	}

	@Override
	public Game getGame() {
		return this.game;
	}

	@Override
	public Team getTeam() {
		return this.team;
	}

	@Override
	public UpgradeManager getUpgradeManager() {
		return this.upgradeManager;
	}

	@Override
	public TrapManager getTrapManager() {
		return this.trapsManager;
	}

}