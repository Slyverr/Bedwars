package com.slyvr.bedwars.settings;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;

import com.slyvr.api.game.GameMode;

public class BedwarsSettings {

	private static BedwarsSettings instance;

	private Map<GameMode, Integer> minimum_players = new HashMap<>();
	private Map<Integer, Integer> levelup_exp = new HashMap<>();

	private int reconnectRespawnTime = 10;
	private int gameCountdown = 20;
	private int respawnTime = 5;

	private boolean autoReconnect = false;
	private int defaultLevelUpExp = 5000;

	private BedwarsSettings() {
	}

	/**
	 * Gets minimum players that can join this mode
	 *
	 * @param mode GameMode
	 *
	 * @return minimum players
	 */
	public int getMinimumPlayers(GameMode mode) {
		if (mode == null)
			return -1;

		return this.minimum_players.containsKey(mode) ? this.minimum_players.get(mode) : mode.getGameMax() - mode.getTeamMax();
	}

	/**
	 * */
	public void setMinimumPlayers(GameMode mode, int value) {
		if (mode != null && value >= 1 /* value >= 2 */)
			this.minimum_players.put(mode, value);
	}

	/**
	 * */
	public int getGameCountdown() {
		return this.gameCountdown;
	}

	/**
	 * */
	public void setGameCountdown(int value) {
		if (value >= 5)
			this.gameCountdown = value;
	}

	/**
	 * Gets default level-up exp
	 *
	 * @return Default level-up exp
	 */
	public int getDefaultLevelUpExp() {
		return this.defaultLevelUpExp;
	}

	/**
	 * */
	public void setDefaultLevelUpExp(int value) {
		if (value > 0)
			this.defaultLevelUpExp = value;
	}

	/**
	 * Gets level-up exp for specified level
	 *
	 * @param level The level
	 *
	 * @return level-up exp for level
	 */
	public int getLevelUpExpFor(int level) {
		return this.levelup_exp.containsKey(level) ? this.levelup_exp.get(level) : this.defaultLevelUpExp;
	}

	/**
	 * */
	public void setLevelUpExpFor(int level, int value) {
		if (level > 0 && value > 0)
			this.levelup_exp.put(level, value);
	}

	public Location getStatsNPCSpawnLocation() {
		return null;
	}

	public void setStatsNPCSpawnLocation(Location loc) {
		return;
	}

	public int getRespawnTime() {
		return this.respawnTime;
	}

	public void setRespawnTime(int time) {
		if (time >= 3)
			this.respawnTime = time;
	}

	public int getReconnectRespawnTime() {
		return this.reconnectRespawnTime;
	}

	public void setReconnectRespawnTime(int time) {
		if (time >= 3)
			this.reconnectRespawnTime = time;
	}

	public boolean isAutoReconnect() {
		return this.autoReconnect;
	}

	public void setAutoReconnect(boolean auto) {
		this.autoReconnect = auto;
	}

	/**
	 * */
	public static BedwarsSettings getInstance() {
		if (instance == null)
			BedwarsSettings.instance = new BedwarsSettings();

		return BedwarsSettings.instance;
	}

}