package com.slyvr.user;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.google.common.base.Preconditions;
import com.slyvr.api.game.Game;
import com.slyvr.api.game.GameMode;
import com.slyvr.api.level.BedwarsLevel;
import com.slyvr.api.prestige.Prestige;
import com.slyvr.api.scoreboard.lobby.LobbyScoreboard;
import com.slyvr.api.shop.QuickBuy;
import com.slyvr.api.user.User;
import com.slyvr.api.user.UserStatistics;
import com.slyvr.bedwars.Bedwars;
import com.slyvr.database.Database;
import com.slyvr.game.AbstractGame;

public class BedwarsUser implements User {

	private Player player;
	private UserData data;

	private BedwarsLevel level;
	private Prestige prestige;

	private LobbyScoreboard board;

	public BedwarsUser(Player player) {
		Preconditions.checkNotNull(player, "Player cannot be null");

		this.player = player;
		this.data = new UserData(player);
	}

	@Override
	public Player getPlayer() {
		return this.player;
	}

	@Override
	public Game getGame() {
		return AbstractGame.getPlayerGame(this.player);
	}

	@Override
	public BedwarsLevel getLevel() {
		return this.data.getLevel();
	}

	@Override
	public void setLevel(BedwarsLevel level) {
		this.data.setLevel(level);
	}

	@Override
	public BedwarsLevel getDisplayLevel() {
		return this.level != null ? this.level.clone() : null;
	}

	@Override
	public void setDisplayLevel(BedwarsLevel level) {
		if (level != null)
			this.level = level.clone();
	}

	@Override
	public Prestige getPrestige() {
		return this.data.getPrestige();
	}

	@Override
	public void setPrestige(Prestige prestige) {
		this.data.setPrestige(prestige);
	}

	@Override
	public Prestige getDisplayPrestige() {
		return this.prestige;
	}

	@Override
	public void setDisplayPrestige(Prestige prestige) {
		if (prestige != null)
			this.prestige = prestige;
	}

	@Override
	public UserStatistics getStatistics(GameMode mode) {
		return this.data.getStats(mode);
	}

	@Override
	public UserStatistics getOverallStatistics() {
		return this.data.getOverallStats();
	}

	@Override
	public int getCoinsBalance() {
		return this.data.getBalance();
	}

	@Override
	public void setCoinsBalance(int balance) {
		this.data.setBalance(balance);
	}

	@Override
	public LobbyScoreboard getScoreboard() {
		return this.board;
	}

	@Override
	public QuickBuy getQuickBuy(GameMode mode) {
		return this.data.getQuickBuy(mode);
	}

	@Override
	public void setScoreboard(LobbyScoreboard board) {
		this.board = board;
	}

	@Override
	public void updateScoreboard() {
		if (this.board == null)
			return;

		Bukkit.getScheduler().runTask(Bedwars.getInstance(), () -> this.board.update(this.player));
	}

	@Override
	public void loadData() {
		this.data.loadData();
	}

	@Override
	public void saveData() {
		this.data.saveData();
	}

	public void saveInDatabase() {
		Database db = Bedwars.getInstance().getDataBase();
		if (db == null || !db.isConnected())
			return;

		for (GameMode mode : GameMode.values())
			db.setUserStats(mode, this);

		db.setUserCoins(this);
	}

}