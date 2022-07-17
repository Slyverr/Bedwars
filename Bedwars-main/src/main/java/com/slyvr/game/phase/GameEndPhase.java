package com.slyvr.game.phase;

import com.slyvr.api.game.Game;
import com.slyvr.api.game.GamePhase;

public class GameEndPhase extends GamePhase {

	public GameEndPhase(int duration) {
		super("Game End", duration);

	}

	@Override
	public boolean apply(Game game) {
		if (game == null)
			return false;

		return game.stopGame();
	}

}