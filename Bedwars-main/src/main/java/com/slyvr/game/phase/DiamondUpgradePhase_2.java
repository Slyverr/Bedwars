package com.slyvr.game.phase;

import java.util.Collection;

import com.slyvr.api.game.Game;
import com.slyvr.api.game.GamePhase;
import com.slyvr.api.generator.Resource;
import com.slyvr.api.generator.TieredGenerator;

public class DiamondUpgradePhase_2 extends GamePhase {

	public DiamondUpgradePhase_2(int duration) {
		super("Diamond II", duration);

	}

	@Override
	public boolean apply(Game game) {
		if (game == null)
			return false;

		Collection<TieredGenerator> gens = game.getMapResourceGenerator(Resource.DIAMOND);
		for (TieredGenerator gen : gens)
			gen.setCurrentTier(2);

		game.broadcastMessage("§bDiamond Generators §ehave been upgraded to Tier §cII");
		return false;
	}

}