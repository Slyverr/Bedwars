package com.slyvr.upgrade;

import java.util.Objects;

import com.slyvr.api.upgrade.Upgrade;

public abstract class AbstractUpgrade implements Upgrade {

	private String name;

	public AbstractUpgrade(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.name);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (!(obj instanceof AbstractUpgrade))
			return false;

		AbstractUpgrade other = (AbstractUpgrade) obj;
		return this.name.equalsIgnoreCase(other.name);
	}

	@Override
	public String toString() {
		return "Upgrade [Name=" + this.name + "]";
	}

}