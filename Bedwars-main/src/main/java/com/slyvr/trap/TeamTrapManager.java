package com.slyvr.trap;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;
import com.slyvr.api.trap.Trap;
import com.slyvr.api.trap.TrapManager;

public class TeamTrapManager implements TrapManager {

	private List<Trap> queue;
	private int limit;

	public TeamTrapManager(int limit) {
		Preconditions.checkArgument(limit >= 1, "Traps limit must be greater than 0!");

		this.limit = limit;
		this.queue = new ArrayList<>(limit);
	}

	public TeamTrapManager() {
		this.limit = 3;
		this.queue = new ArrayList<>(3);
	}

	@Override
	public List<Trap> getTraps() {
		return new ArrayList<>(this.queue);
	}

	@Override
	public void addTrap(Trap trap) {
		if (trap != null && this.queue.size() < this.limit)
			this.queue.add(trap);
	}

	@Override
	public void removeTrap(Trap trap) {
		if (trap != null)
			this.queue.remove(trap);

	}

	@Override
	public boolean contains(Trap trap) {
		return trap != null && this.queue.contains(trap);
	}

}