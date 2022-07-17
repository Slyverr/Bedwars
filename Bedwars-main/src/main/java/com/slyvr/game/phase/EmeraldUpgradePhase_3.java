package com.slyvr.game.phase;

import java.util.Collection;

import com.slyvr.api.game.Game;
import com.slyvr.api.game.GamePhase;
import com.slyvr.api.generator.Resource;
import com.slyvr.api.generator.TieredGenerator;

public class EmeraldUpgradePhase_3 extends GamePhase {

	public EmeraldUpgradePhase_3(int duration) {
		super("Emerald III", duration);

	}

	@Override
	public boolean apply(Game game) {
		if (game == null)
			return false;

		Collection<TieredGenerator> gens = game.getMapResourceGenerator(Resource.EMERALD);
		for (TieredGenerator gen : gens)
			gen.setCurrentTier(3);

		game.broadcastMessage("§2Emerald Generators §ehave been upgraded to Tier §cIII");
		return false;
	}

}