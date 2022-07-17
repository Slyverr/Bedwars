package com.slyvr.player;

import java.util.HashMap;
import java.util.Map;

import com.slyvr.api.game.GameReward;
import com.slyvr.api.game.player.stats.GameStatistic;
import com.slyvr.api.game.player.stats.GameStatisticManager;
import com.slyvr.reward.CoinReward;
import com.slyvr.reward.ExpReward;

public class StatisticManager implements GameStatisticManager {

	private Map<GameStatistic, Integer> stats = new HashMap<>();

	private GameReward expReward;
	private GameReward coinReward;

	public StatisticManager() {

		this.expReward = new ExpReward();
		this.coinReward = new CoinReward();
	}

	@Override
	public Map<GameStatistic, Integer> getStats() {
		return this.stats;
	}

	@Override
	public int getStatistic(GameStatistic stat) {
		if (stat == null)
			return 0;

		Integer old = this.stats.get(stat);
		return old != null ? old : 0;
	}

	@Override
	public void incrementStatistic(GameStatistic stat, int value) {
		if (stat == null || value <= 0)
			return;

		Integer old = this.stats.get(stat);
		if (old == null)
			this.stats.put(stat, value);
		else
			this.stats.put(stat, old + value);
	}

	@Override
	public void decrementStatistic(GameStatistic stat, int value) {
		if (stat == null || value <= 0)
			return;

		Integer old = this.stats.get(stat);
		if (old == null)
			return;

		this.stats.put(stat, old - value > 0 ? old - value : 0);
	}

	@Override
	public void setStatistic(GameStatistic stat, int value) {
		if (stat != null && value >= 0)
			this.stats.put(stat, value);
	}

	@Override
	public GameReward getExpReward() {
		return this.expReward;
	}

	@Override
	public GameReward getCoinsReward() {
		return this.coinReward;
	}

}