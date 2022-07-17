package com.slyvr.scoreboard;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.NumberConversions;

import com.slyvr.api.game.GameMode;
import com.slyvr.api.game.player.stats.GameStatistic;
import com.slyvr.api.scoreboard.Scoreboard.AnimatedTitle;
import com.slyvr.api.scoreboard.lobby.LobbyScoreboard;
import com.slyvr.api.scoreboard.lobby.LobbyScoreboard.LobbyBoardLineType;
import com.slyvr.api.team.Team;
import com.slyvr.api.user.Statistic;
import com.slyvr.bedwars.Bedwars;
import com.slyvr.configuration.Configuration;
import com.slyvr.scoreboard.game.GameScoreboard;
import com.slyvr.scoreboard.game.GameScoreboard.GameBoardLineType;
import com.slyvr.scoreboard.lobby.BedwarsLobbyScoreboard;
import com.slyvr.scoreboard.waiting.WaitingScoreboard;
import com.slyvr.scoreboard.waiting.WaitingScoreboard.WaitingBoardLineType;

public class ScoreboardConfig extends Configuration {

	private static final AnimatedTitle DEFAULT_TITLE;

	static {
		List<String> titles = Arrays
				.asList(bold(ChatColor.YELLOW) + "BED WARS",
						bold(ChatColor.YELLOW) + "BED WARS",
						bold(ChatColor.YELLOW) + "BED WARS",
						bold(ChatColor.YELLOW) + "BED WARS",
						bold(ChatColor.YELLOW) + "BED WARS",
						bold(ChatColor.YELLOW) + "BED WARS",
						bold(ChatColor.YELLOW) + "BED WARS",
						bold(ChatColor.YELLOW) + "BED WARS",
						bold(ChatColor.YELLOW) + "BED WARS",
						bold(ChatColor.YELLOW) + "BED WARS",
						bold(ChatColor.YELLOW) + "BED WARS",
						bold(ChatColor.YELLOW) + "BED WARS",
						bold(ChatColor.YELLOW) + "BED WARS",
						bold(ChatColor.YELLOW) + "BED WARS",
						bold(ChatColor.YELLOW) + "BED WARS",
						bold(ChatColor.YELLOW) + "BED WARS",
						bold(ChatColor.YELLOW) + "BED WARS",
						bold(ChatColor.YELLOW) + "BED WARS",
						bold(ChatColor.YELLOW) + "BED WARS",
						bold(ChatColor.YELLOW) + "BED WARS",

						/** Animation */
						bold(ChatColor.WHITE) + "" + bold(ChatColor.GOLD) + "B" + bold(ChatColor.YELLOW) + "ED WARS",
						bold(ChatColor.WHITE) + "B" + bold(ChatColor.GOLD) + "E" + bold(ChatColor.YELLOW) + "D WARS",
						bold(ChatColor.WHITE) + "BE" + bold(ChatColor.GOLD) + "D" + bold(ChatColor.YELLOW) + " WARS",
						bold(ChatColor.WHITE) + "BED " + bold(ChatColor.GOLD) + "W" + bold(ChatColor.YELLOW) + "ARS",
						bold(ChatColor.WHITE) + "BED W" + bold(ChatColor.GOLD) + "A" + bold(ChatColor.YELLOW) + "RS",
						bold(ChatColor.WHITE) + "BED WA" + bold(ChatColor.GOLD) + "R" + bold(ChatColor.YELLOW) + "S",
						bold(ChatColor.WHITE) + "BED WAR" + bold(ChatColor.GOLD) + "S" + bold(ChatColor.YELLOW) + "",
						/***/

						bold(ChatColor.WHITE) + "BED WARS",
						bold(ChatColor.WHITE) + "BED WARS",

						bold(ChatColor.YELLOW) + "BED WARS",
						bold(ChatColor.YELLOW) + "BED WARS",

						bold(ChatColor.WHITE) + "BED WARS",
						bold(ChatColor.WHITE) + "BED WARS",

						bold(ChatColor.YELLOW) + "BED WARS",
						bold(ChatColor.YELLOW) + "BED WARS");

		DEFAULT_TITLE = new AnimatedTitle(titles, 5);
	}

	private static String bold(ChatColor color) {
		return color + "" + ChatColor.BOLD;
	}

	private Map<GameMode, GameScoreboard> boards = new HashMap<>();

	private WaitingScoreboard waitingBoard;
	private LobbyScoreboard lobbyBoard;

	private static ScoreboardConfig instance;

	private ScoreboardConfig() {
		super(new File(Bedwars.getInstance().getDataFolder(), "Scoreboard.yml"));

		saveDefaultConfig();
	}

	public GameScoreboard getScoreboard(GameMode mode) {
		if (mode == null)
			return null;

		GameScoreboard existing = this.boards.get(mode);
		if (existing != null)
			return existing;

		ConfigurationSection section = getConfig().getConfigurationSection("Game-Scoreboards");
		if (section == null)
			return null;

		for (String key : section.getKeys(false)) {
			if (!key.equalsIgnoreCase(mode.getName()))
				continue;

			ConfigurationSection lineSection = section.getConfigurationSection(key + ".lines");
			if (lineSection == null)
				return null;

			AnimatedTitle title = getTitle("Game-Scoreboards." + key + ".Title", ScoreboardConfig.DEFAULT_TITLE);

			GameScoreboard board = new GameScoreboard(title);
			for (String lineKey : this.config.getConfigurationSection("Game-Scoreboards." + key + ".lines").getKeys(false)) {
				int score = NumberConversions.toInt(lineKey.replace("line-", ""));
				if (score < 1 || score > 15)
					continue;

				GameBoardLineType lineType = GameBoardLineType.getByName(lineSection.getString(lineKey + ".type"));
				if (lineType == null)
					continue;

				String value = lineSection.getString(lineKey + ".value");

				switch (lineType) {
					case TEXT:
						board.setText(score, value);
						break;
					case TEAM:
						board.setTeam(score, Team.getByName(value));
						break;
					case STATISTIC:
						board.setStatistic(score, GameStatistic.fromString(value));
						break;
					default:
						break;
				}

				board.setLineType(score, lineType);
			}

			this.boards.put(mode, board);
			return board;
		}

		return null;

	}

	public AnimatedTitle getTitle(String path) {
		return getTitle(path, null);
	}

	public AnimatedTitle getTitle(String path, AnimatedTitle def) {
		List<String> titles = getConfig().getStringList(path + ".titles");

		return !titles.isEmpty() ? new AnimatedTitle(titles, this.config.getLong(path + ".update-ticks")) : def;
	}

	public WaitingScoreboard getWaitingScoreboard() {
		if (this.waitingBoard != null)
			return this.waitingBoard;

		ConfigurationSection section = getConfig().getConfigurationSection("Waiting-Scoreboard.lines");
		if (section == null)
			return null;

		AnimatedTitle title = getTitle("Waiting-Scoreboard.Title", ScoreboardConfig.DEFAULT_TITLE);

		this.waitingBoard = new WaitingScoreboard(title);

		for (String lineKey : section.getKeys(false)) {
			int score = NumberConversions.toInt(lineKey.replace("line-", ""));
			if (score <= 0 || score > 15)
				continue;

			WaitingBoardLineType type = WaitingBoardLineType.getByName(section.getString(lineKey + ".type"));
			if (type == null)
				continue;

			String value = section.getString(lineKey + ".value");
			if (value != null && type == WaitingBoardLineType.TEXT)
				this.waitingBoard.setText(score, value);

			this.waitingBoard.setLineType(score, type);
		}

		return this.waitingBoard;
	}

	public LobbyScoreboard getLobbyScoreboard() {
		if (this.lobbyBoard != null)
			return this.lobbyBoard;

		ConfigurationSection section = getConfig().getConfigurationSection("Lobby-Scoreboard.lines");
		if (section == null)
			return null;

		AnimatedTitle title = getTitle("Lobby-Scoreboard.Title", ScoreboardConfig.DEFAULT_TITLE);

		this.lobbyBoard = new BedwarsLobbyScoreboard(title);

		for (String lineKey : section.getKeys(false)) {
			int score = NumberConversions.toInt(lineKey.replace("line-", ""));
			if (score < 1 || score > 15)
				continue;

			LobbyBoardLineType type = LobbyBoardLineType.fromString(section.getString(lineKey + ".type"));
			if (type == null)
				continue;

			String value = section.getString(lineKey + ".value");
			if (value != null) {
				if (type == LobbyBoardLineType.STATISTIC)
					this.lobbyBoard.setStatistic(score, Statistic.getByName(value));
				else if (type == LobbyBoardLineType.TEXT)
					this.lobbyBoard.setText(score, value);

			}

			this.lobbyBoard.setLineType(score, type);
		}

		return this.lobbyBoard;
	}

	@Override
	public void saveDefaultConfig() {
		if (!this.file.exists())
			Bedwars.getInstance().saveResource("Scoreboard.yml", false);
	}

	public static ScoreboardConfig getInstance() {
		if (instance == null)
			instance = new ScoreboardConfig();

		return ScoreboardConfig.instance;
	}

}