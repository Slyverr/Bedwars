package com.slyvr.scoreboard.lobby;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import com.slyvr.api.level.BedwarsLevel;
import com.slyvr.api.prestige.Prestige;
import com.slyvr.api.scoreboard.lobby.LobbyScoreboard;
import com.slyvr.api.user.Statistic;
import com.slyvr.api.user.User;
import com.slyvr.api.user.UserStatistics;
import com.slyvr.bedwars.Bedwars;
import com.slyvr.scoreboard.AbstractScoreboard;
import com.slyvr.util.ChatUtils;
import com.slyvr.util.ScoreboardUtils;

public class BedwarsLobbyScoreboard extends AbstractScoreboard implements LobbyScoreboard {

	private static final Map<UUID, Scoreboard> PLAYERS_BOARDS = new ConcurrentHashMap<>();

	private Map<Integer, LobbyBoardLineType> line_type = new HashMap<>();
	private Map<Integer, Statistic> stats_line = new HashMap<>();

	public BedwarsLobbyScoreboard(AnimatedTitle title) {
		super(title);
	}

	@Override
	public void setText(int line, String text) {
		if (text == null || !isValidLine(line))
			return;

		this.lines.put(line, ChatUtils.format(text));
		this.line_type.put(line, LobbyBoardLineType.TEXT);
	}

	@Override
	public Map<Integer, Statistic> getStatistics() {
		return new HashMap<>(this.stats_line);
	}

	@Override
	public Statistic getStatistic(int line) {
		return this.stats_line.get(line);
	}

	@Override
	public void setStatistic(int line, Statistic stat) {
		if (stat == null || !isValidLine(line))
			return;

		this.stats_line.put(line, stat);
		this.line_type.put(line, LobbyBoardLineType.STATISTIC);
	}

	@Override
	public Statistic removeStatistic(int line) {
		return this.stats_line.remove(line);
	}

	@Override
	public LobbyBoardLineType getLineType(int line) {
		return this.line_type.get(line);
	}

	@Override
	public void setLineType(int line, LobbyBoardLineType type) {
		if (type == null || !isValidLine(line) || type == LobbyBoardLineType.STATISTIC || type == LobbyBoardLineType.TEXT)
			return;

		this.line_type.put(line, type);
	}

	private Scoreboard getBukkitBoard(Player player) {
		Scoreboard board = PLAYERS_BOARDS.get(player.getUniqueId());
		if (board != null)
			return board;

		PLAYERS_BOARDS.put(player.getUniqueId(), board = Bukkit.getScoreboardManager().getNewScoreboard());

		Objective obj = board.registerNewObjective("Scoreboard", "dummy");
		obj.setDisplayName(this.display);
		obj.setDisplaySlot(this.slot);

		int empty = 0;
		for (Entry<Integer, LobbyBoardLineType> entry : this.line_type.entrySet()) {
			int line = entry.getKey();

			switch (entry.getValue()) {
				case TEXT:
					obj.getScore(this.lines.get(line)).setScore(line);
					break;
				case EMPTY:
					obj.getScore(ScoreboardUtils.getEmptyLine(empty++)).setScore(line);
					break;
				case COINS:
					if (board.getTeam("Coins") != null)
						continue;

					board.registerNewTeam("Coins").addEntry("Coins: " + ChatColor.GOLD);
					obj.getScore("Coins: " + ChatColor.GOLD).setScore(line);
					break;
				case LEVEL:
					board.registerNewTeam("Level").addEntry("Your Level: ");
					obj.getScore("Your Level: ").setScore(line);
					break;
				case PROGRESS:
					if (board.getTeam("Progress") != null)
						continue;

					Team progress = board.registerNewTeam("Progress");
					progress.addEntry(ChatColor.AQUA.toString());

					obj.getScore(ChatColor.AQUA.toString()).setScore(line);
					break;
				case PROGRESS_BAR:
					if (board.getTeam("Progress-Bar") != null)
						continue;

					Team progress_bar = board.registerNewTeam("Progress-Bar");
					progress_bar.addEntry("");

					obj.getScore("").setScore(line);
					break;
				case STATISTIC:
					String name = this.stats_line.get(line).toString();
					if (board.getTeam(name) != null)
						continue;

					Team stat = board.registerNewTeam(name);
					stat.addEntry("Total " + name + ": ");

					obj.getScore("Total " + name + ": ").setScore(line);
					break;
				default:
					break;
			}

		}

		return board;
	}

	@Override
	public void update(Player player) {
		if (player == null)
			return;

		Scoreboard board = getBukkitBoard(player);
		User user = Bedwars.getInstance().getUser(player);

		for (Entry<Integer, LobbyBoardLineType> entry : this.line_type.entrySet()) {
			int line = entry.getKey();

			switch (entry.getValue()) {
				case COINS:
					makeCoins(board, user.getCoinsBalance());
					break;
				case LEVEL:
					makeLevel(board, user.getLevel(), user.getPrestige());
					break;
				case PROGRESS:
					makeProgress(board, board.getObjective("Scoreboard"), line, user.getLevel());
					break;
				case PROGRESS_BAR:
					makeProgressBar(board, board.getObjective("Scoreboard"), line, user.getLevel());
					break;
				case STATISTIC:
					makeStat(board, this.stats_line.get(line), user.getOverallStatistics());
					break;
				default:
					break;
			}

		}

		player.setScoreboard(board);
	}

	private void makeLevel(Scoreboard board, BedwarsLevel level, Prestige prestige) {
		Team team = board.getTeam("Level");

		if (level == null)
			level = new BedwarsLevel(1, 0, Bedwars.getInstance().getSettings().getLevelUpExpFor(1));

		if (prestige == null)
			prestige = Prestige.DEFAULT;

		team.setSuffix(prestige.formatToScoreboard(level));
	}

	private void makeProgress(Scoreboard board, Objective obj, int line, BedwarsLevel level) {
		Team team = board.getTeam("Progress");

		if (level == null)
			return;

		StringBuilder builder = new StringBuilder()
				.append("Progress: ")
				.append(ChatColor.AQUA)
				.append(level.getProgressExp())
				.append(ChatColor.GRAY)
				.append("/")
				.append(ChatColor.GREEN)
				.append(level.getExpToLevelUp());

		String text = builder.toString();

		team.setPrefix(text.substring(0, 16));

		if (text.length() > 32)
			team.setSuffix(text.substring(16, 32));
		else
			team.setSuffix(text.substring(16));
	}

	private void makeProgressBar(Scoreboard board, Objective obj, int line, BedwarsLevel level) {
		Team team = board.getTeam("Progress-Bar");

		String text = level.getProgressBar(10);

		String prefix = text.substring(0, 16);
		team.setPrefix(prefix);

		String suffix = getLastColor(prefix) + text.substring(16, 20);
		team.setSuffix(suffix);
	}

	private ChatColor getLastColor(String text) {
		for (int i = text.length() - 1; i >= 1; i--) {
			char before = text.charAt(i - 1);

			if (before != ChatColor.COLOR_CHAR)
				continue;

			char current = text.charAt(i);

			ChatColor color = ChatColor.getByChar(current);
			if (color != null)
				return color;
		}

		return ChatColor.RESET;
	}

	private void makeStat(Scoreboard board, Statistic stat, UserStatistics stats) {
		Team team = board.getTeam(stat.toString());
		team.setSuffix(ChatColor.GREEN + Integer.toString(stats.getStatistic(stat)));
	}

	private void makeCoins(Scoreboard board, int amount) {
		Team team = board.getTeam("Coins");
		team.setSuffix(ChatColor.GOLD + Integer.toString(amount));
	}

	private boolean isValidLine(int line) {
		return line >= 1 && line <= 15;
	}

}