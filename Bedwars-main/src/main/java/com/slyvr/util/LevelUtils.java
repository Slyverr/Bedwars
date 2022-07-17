package com.slyvr.util;

import com.slyvr.api.level.BedwarsLevel;
import com.slyvr.api.prestige.Prestige;
import com.slyvr.bedwars.Bedwars;

public class LevelUtils {

	public static Prestige levelUp(BedwarsLevel level, int amount) {
		Prestige next = null;

		int lvl = level.getLevel();
		while (level.isLeveling(amount)) {
			level.setExpToLevel(Bedwars.getInstance().getSettings().getLevelUpExpFor(++lvl));
			amount -= level.getExpToLevelUp();

			Prestige lvlPrestige = Prestige.getByStartLevel(lvl);
			if (lvlPrestige != null)
				next = lvlPrestige;
		}

		level.setLevel(lvl);
		level.incrementProgressExp(amount);

		return next;
	}
}
