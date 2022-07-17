package com.slyvr.game;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.Player;

import com.slyvr.api.group.Group;

public class BedwarsGroup implements Group {

	private Set<Player> players = new HashSet<>();

	public BedwarsGroup(Collection<Player> players) {
		addPlayers(players);
	}

	public BedwarsGroup() {

	}

	@Override
	public Set<Player> getPlayers() {
		return new HashSet<>(this.players);
	}

	@Override
	public void addPlayers(Collection<Player> players) {
		if (players != null)
			this.players.addAll(players);
	}

	@Override
	public boolean addPlayer(Player player) {
		return player != null ? this.players.add(player) : false;
	}

	@Override
	public boolean removePlayer(Player player) {
		return player != null ? this.players.remove(player) : false;
	}

	@Override
	public boolean isEmpty() {
		return this.players.isEmpty();
	}

	@Override
	public boolean contains(Player player) {
		return this.players != null ? this.players.contains(player) : false;
	}

	@Override
	public void clear() {
		this.players.clear();
	}

	@Override
	public int size() {
		return this.players.size();
	}

}