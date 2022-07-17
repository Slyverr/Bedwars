package com.slyvr.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import com.google.common.base.Preconditions;
import com.slyvr.api.game.GameMode;
import com.slyvr.api.user.Statistic;
import com.slyvr.api.user.User;
import com.slyvr.api.user.UserStatistics;

public class Database {

	private final String username;
	private final String password;
	private final String url;

	private Connection connection;

	public Database(String username, String password, String url) {
		Preconditions.checkNotNull(username, "Username cannot be null");
		Preconditions.checkNotNull(password, "Password cannot be null");
		Preconditions.checkNotNull(url, "Database url cannot be null");

		this.username = username;
		this.password = password;
		this.url = url;
	}

	public boolean connect() {
		try {
			this.connection = DriverManager.getConnection(this.url, this.username, this.password);
		} catch (SQLException e) {
			return false;
		}

		return true;
	}

	public boolean isConnected() {
		return this.connection != null;
	}

	public boolean disconnect() {
		if (!isConnected())
			return false;

		try {
			this.connection.close();
		} catch (SQLException e) {
			return false;
		}

		return true;
	}

	public void createCoinsTable() {
		if (!isConnected())
			return;

		try {
			StringBuilder builder = new StringBuilder("CREATE TABLE IF NOT EXISTS Coins ");
			builder.append("(UUID VARCHAR(255), Name VARCHAR(32), Coins INT)");

			PreparedStatement statement = this.connection.prepareStatement(builder.toString());
			statement.executeUpdate();
		} catch (Exception e) {

		}

	}

	public void addCoinsUser(User user) {
		if (!isConnected() || user == null)
			return;

		try {
			StringBuilder builder = new StringBuilder("INSERT IGNORE INTO Coins (UUID,Name,Coins) VALUES (?,?,?)");

			PreparedStatement statement = this.connection.prepareStatement(builder.toString());
			statement.setString(1, user.getPlayer().getUniqueId().toString());
			statement.setString(2, user.getPlayer().getName());
			statement.setInt(3, user.getCoinsBalance());

			statement.executeUpdate();
		} catch (Exception e) {

		}

	}

	public void setUserCoins(User user) {
		if (!isConnected() || user == null)
			return;

		try {
			StringBuilder builder = new StringBuilder("UPDATE Coins SET Coins=? WHERE UUID=?");

			PreparedStatement statement = this.connection.prepareStatement(builder.toString());
			statement.setInt(1, user.getCoinsBalance());
			statement.setString(2, user.getPlayer().getUniqueId().toString());

			statement.executeUpdate();
		} catch (Exception e) {

		}
	}

	public boolean containsCoinsUser(User user) {
		if (!isConnected() || user == null)
			return false;

		try {
			StringBuilder builder = new StringBuilder("SELECT * FROM Coins WHERE UUID=?");

			PreparedStatement statement = this.connection.prepareStatement(builder.toString());
			statement.setString(1, user.getPlayer().getUniqueId().toString());

			return statement.executeQuery().next();
		} catch (Exception e) {
			return false;
		}

	}

	public void createStatsTable(GameMode mode) {
		if (!isConnected() || mode == null)
			return;

		try {
			StringBuilder builder = new StringBuilder("CREATE TABLE IF NOT EXISTS ").append(mode.getName()).append("_Stats ");
			builder.append("(UUID VARCHAR(255),");
			builder.append("Kills INT,");
			builder.append("Deaths INT,");
			builder.append("KillDeathRatio DOUBLE(64, 2),");

			builder.append("Final_Kills INT,");
			builder.append("Final_Deaths INT,");
			builder.append("FinalKillDeathRatio DOUBLE(64, 2),");

			builder.append("Wins INT,");
			builder.append("Losses INT,");
			builder.append("PRIMARY KEY (UUID))");

			PreparedStatement statement = this.connection.prepareStatement(builder.toString());
			statement.executeUpdate();
		} catch (Exception e) {

		}
	}

	public UserStatistics getUserStats(GameMode mode, UUID uuid) {
		if (!isConnected() || mode == null || uuid == null)
			return null;

		try {
			if (!containsStatsUser(mode, uuid))
				return null;

			StringBuilder builder = new StringBuilder("SELECT * FROM " + mode.getName() + "_Stats WHERE UUID=?");

			PreparedStatement statement = this.connection.prepareStatement(builder.toString());
			ResultSet result = statement.executeQuery();

			UserStatistics stats = new UserStatistics();
			stats.setStatistic(Statistic.KILLS, result.getInt("Kills"));
			stats.setStatistic(Statistic.DEATHS, result.getInt("Deaths"));
			stats.setStatistic(Statistic.FINAL_KILLS, result.getInt("Final_Kills"));
			stats.setStatistic(Statistic.FINAL_DEATHS, result.getInt("Final_Deaths"));
			stats.setStatistic(Statistic.WINS, result.getInt("Wins"));
			stats.setStatistic(Statistic.LOSSES, result.getInt("Losses"));

			return stats;
		} catch (Exception e) {
			return null;
		}

	}

	public void createStatsUser(GameMode mode, User user) {
		if (!isConnected() || mode == null || user == null)
			return;

		try {
			StringBuilder builder = new StringBuilder("INSERT IGNORE INTO " + mode.getName() + "_Stats ");
			builder.append("(UUID,Kills, Deaths, KillDeathRatio, Final_Kills, Final_Deaths, FinalKillDeathRatio, Wins, Losses) ");
			builder.append("VALUES (?,?,?,?,?,?,?,?,?)");

			UserStatistics stats = user.getStatistics(mode);

			PreparedStatement statement = this.connection.prepareStatement(builder.toString());
			statement.setString(1, user.getPlayer().getUniqueId().toString());
			statement.setInt(2, stats.getStatistic(Statistic.KILLS));
			statement.setInt(3, stats.getStatistic(Statistic.DEATHS));
			statement.setFloat(4, stats.getKillDeathRatio());

			statement.setInt(5, stats.getStatistic(Statistic.FINAL_KILLS));
			statement.setInt(6, stats.getStatistic(Statistic.FINAL_DEATHS));
			statement.setFloat(7, stats.getFinalKillDeathRatio());

			statement.setInt(8, stats.getStatistic(Statistic.WINS));
			statement.setInt(9, stats.getStatistic(Statistic.LOSSES));

			statement.executeUpdate();
		} catch (Exception e) {

		}

	}

	public void setUserStats(GameMode mode, User user) {
		if (!isConnected() || mode == null || user == null)
			return;

		try {
			if (!containsStatsUser(mode, user.getPlayer().getUniqueId())) {
				createStatsUser(mode, user);
				return;
			}

			StringBuilder builder = new StringBuilder("UPDATE " + mode.getName() + "_Stats SET ");
			builder.append("Kills INT=?,");
			builder.append("Deaths=?,");
			builder.append("KillDeathRatio=?,");

			builder.append("Final_Kills=?,");
			builder.append("Final_Deaths=?,");
			builder.append("FinalKillDeathRatio=?,");

			builder.append("Wins=?,");
			builder.append("Losses=? ");
			builder.append("WHERE UUID=?");

			UserStatistics stats = user.getStatistics(mode);

			PreparedStatement statement = this.connection.prepareStatement(builder.toString());
			statement.setInt(1, stats.getStatistic(Statistic.KILLS));
			statement.setInt(2, stats.getStatistic(Statistic.DEATHS));
			statement.setFloat(3, stats.getKillDeathRatio());

			statement.setInt(4, stats.getStatistic(Statistic.FINAL_KILLS));
			statement.setInt(5, stats.getStatistic(Statistic.FINAL_DEATHS));
			statement.setFloat(6, stats.getFinalKillDeathRatio());

			statement.setInt(7, stats.getStatistic(Statistic.WINS));
			statement.setInt(8, stats.getStatistic(Statistic.LOSSES));
			statement.setString(9, user.getPlayer().getUniqueId().toString());

			statement.executeUpdate();
		} catch (Exception e) {

		}

	}

	public boolean containsStatsUser(GameMode mode, UUID uuid) {
		if (!isConnected() || mode == null || uuid == null)
			return false;

		try {
			StringBuilder builder = new StringBuilder("SELECT * FROM " + mode.getName() + "_Stats WHERE UUID=?");

			PreparedStatement statement = this.connection.prepareStatement(builder.toString());
			statement.setString(1, uuid.toString());

			return statement.executeQuery().next();
		} catch (SQLException e) {
			return false;
		}

	}

	public int getUserStats(GameMode mode, String stat, UUID uuid) {
		if (!isConnected() || mode == null || stat == null || uuid == null)
			return 0;

		try {
			StringBuilder builder = new StringBuilder("SELECT " + stat + "FROM ").append(mode.getName()).append("Stats ");
			builder.append("WHERE UUID=?");

			PreparedStatement statement = this.connection.prepareStatement(builder.toString());
			statement.setString(1, uuid.toString());

			return statement.executeQuery().getInt(1);
		} catch (Exception e) {
			return 0;
		}

	}

}