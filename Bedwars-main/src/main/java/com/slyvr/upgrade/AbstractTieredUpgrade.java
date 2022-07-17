package com.slyvr.upgrade;

import com.slyvr.api.upgrade.TieredUpgrade;

public abstract class AbstractTieredUpgrade implements TieredUpgrade {

	protected String name;
	protected int min;
	protected int max;

	protected int current;

	public AbstractTieredUpgrade(String name, int min, int max) {

		this.name = name;
		this.min = min;
		this.max = max;

		this.current = min;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public int getMaximumTier() {
		return this.max;
	}

	@Override
	public int getNextTier() {
		int next = this.current + 1;
		return next <= this.max ? next : this.max;
	}

	@Override
	public int getCurrentTier() {
		return this.current;
	}

	@Override
	public int getPreviousTier() {
		int previous = this.current - 1;
		return previous >= this.min ? previous : this.min;
	}

	@Override
	public void setCurrentTier(int tier) {
		if (tier >= this.min && tier <= this.max)
			this.current = tier;
	}

	@Override
	public TieredUpgrade clone() {
		try {
			return (AbstractTieredUpgrade) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}

		return null;
	}

}