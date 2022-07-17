package com.slyvr.game.phase;

import com.slyvr.api.arena.Arena;
import com.slyvr.api.game.Game;
import com.slyvr.api.game.GamePhase;
import com.slyvr.api.team.Team;

public class BedBreakPhase extends GamePhase {

	public BedBreakPhase(int duration) {
		super("Bed Break", duration);
	}

	@Override
	public boolean apply(Game game) {
		if (game == null)
			return false;

		Arena arena = game.getArena();
		for (Team team : arena.getTeams())
			game.breakTeamBed(team);

		return true;
	}

}