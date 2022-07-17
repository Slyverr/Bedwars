package com.slyvr.upgrade;

import com.slyvr.api.game.player.GamePlayer;

public final class DragonBuffUpgrade extends AbstractUpgrade {

	public DragonBuffUpgrade() {
		super("Dragon Buff");

	}

	@Override
	public String getName() {
		return "Dragon Buff";
	}

	@Override
	public boolean apply(GamePlayer gp) {
		return true;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof DragonBuffUpgrade;
	}

}