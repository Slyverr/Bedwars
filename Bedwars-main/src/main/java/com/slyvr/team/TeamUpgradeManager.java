package com.slyvr.team;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import com.slyvr.api.game.player.GamePlayer;
import com.slyvr.api.upgrade.Upgrade;
import com.slyvr.api.upgrade.UpgradeManager;

public class TeamUpgradeManager implements UpgradeManager {

	private Map<String, Upgrade> upgrades = new HashMap<>(4);

	@Override
	public Set<Upgrade> getUpgrades() {
		return new HashSet<>(this.upgrades.values());
	}

	@Override
	public Upgrade getUpgrade(String name) {
		return name != null ? this.upgrades.get(name.toLowerCase()) : null;
	}

	@Override
	public void add(Upgrade upgrade) {
		if (upgrade != null)
			this.upgrades.put(upgrade.getName().toLowerCase(), upgrade);

	}

	@Override
	public void remove(Upgrade upgrade) {
		if (upgrade != null)
			this.upgrades.remove(upgrade.getName().toLowerCase());
	}

	@Override
	public boolean contains(Upgrade upgrade) {
		return upgrade != null ? this.upgrades.containsKey(upgrade.getName().toLowerCase()) : false;
	}

	@Override
	public void apply(GamePlayer gp) {
		this.apply(gp, null);
	}

	@Override
	public void apply(GamePlayer gp, Predicate<Upgrade> predicate) {
		if (gp == null)
			return;

		for (Upgrade upgrade : this.upgrades.values())
			if (predicate == null || predicate.test(upgrade))
				upgrade.apply(gp);

	}

}