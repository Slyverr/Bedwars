package com.slyvr.player;

import java.util.Objects;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.google.common.base.Preconditions;
import com.slyvr.api.game.Game;
import com.slyvr.api.game.player.ArmorType;
import com.slyvr.api.game.player.GameInventory;
import com.slyvr.api.game.player.GamePlayer;
import com.slyvr.api.game.player.stats.GameStatisticManager;
import com.slyvr.api.team.Team;

public final class BedwarsPlayer implements GamePlayer {

	private Player player;
	private Game game;

	private GameInventory inventory;
	private GameStatisticManager stats;

	private ArmorType armor = ArmorType.LEATHER;
	private Team team;

	public BedwarsPlayer(Player player, Game game, Team team) {
		Preconditions.checkNotNull(player, "Player cannot be null");
		Preconditions.checkNotNull(game, "Game cannot be null");
		Preconditions.checkNotNull(team, "Team cannot be null");

		this.player = player;
		this.game = game;
		this.team = team;

		this.inventory = new BedwarsInventory();
		this.stats = new StatisticManager();
	}

	@Override
	public Player getPlayer() {
		return this.player;
	}

	@Override
	public Game getGame() {
		return this.game;
	}

	@Override
	public Team getTeam() {
		return this.team;
	}

	@Override
	public ArmorType getArmorType() {
		return this.armor;
	}

	@Override
	public void setArmorType(ArmorType type) {
		if (type != null)
			this.armor = type;
	}

	@Override
	public GameInventory getInventory() {
		return this.inventory;
	}

	@Override
	public void setInventory(GameInventory inv) {
		if (inv != null)
			this.inventory = inv;
	}

	@Override
	public GameStatisticManager getStatisticManager() {
		return this.stats;
	}

	@Override
	public void setStatistics(GameStatisticManager statistics) {
		if (statistics != null)
			this.stats = statistics;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.player, this.game);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (!(obj instanceof BedwarsPlayer))
			return false;

		BedwarsPlayer other = (BedwarsPlayer) obj;
		return this.game.equals(other.game) && this.player.equals(other.player);
	}

	@Override
	public String toString() {
		return "BedwarsPlayer [Player=" + this.player.getName() + "]";
	}

	public void initPlayer() {
		this.player = Bukkit.getPlayer(this.player.getUniqueId());
	}

}