package com.slyvr.scoreboard.game;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import com.slyvr.api.game.player.GamePlayer;
import com.slyvr.api.game.player.stats.GameStatistic;
import com.slyvr.api.team.Team;
import com.slyvr.scoreboard.AbstractScoreboard;
import com.slyvr.util.ChatUtils;
import com.slyvr.util.ScoreboardUtils;

public class GameScoreboard extends AbstractScoreboard {

	public enum GameBoardLineType {

		STATISTIC,
		PHASE,
		EMPTY,
		DATE,
		TEXT,
		TEAM;

		private static final Map<String, GameBoardLineType> BY_NAME = new HashMap<>(6);

		static {
			for (GameBoardLineType type : values())
				BY_NAME.put(type.name().toLowerCase(), type);
		}

		public static GameBoardLineType getByName(String type) {
			return type != null ? BY_NAME.get(type.toLowerCase()) : null;
		}

	}

	private static final Map<UUID, Scoreboard> BOARDS = new HashMap<>();

	private Map<Integer, GameBoardLineType> line_type = new HashMap<>(15);
	private Map<Integer, GameStatistic> stat_line = new HashMap<>(15);
	private Map<Integer, Team> team_line = new HashMap<>(15);

	private String phase;

	public GameScoreboard(AnimatedTitle title) {
		super(title);
	}

	@Override
	public void setText(int line, String text) {
		if (text == null || !isValidLine(line))
			return;

		this.lines.put(line, ChatUtils.format(text));
		this.line_type.put(line, GameBoardLineType.TEXT);
	}

	public void setStatistic(int line, GameStatistic stat) {
		if (stat == null || !isValidLine(line))
			return;

		this.stat_line.put(line, stat);
		this.line_type.put(line, GameBoardLineType.STATISTIC);
	}

	public void setTeam(int line, Team team) {
		if (team == null || !isValidLine(line))
			return;

		this.team_line.put(line, team);
		this.line_type.put(line, GameBoardLineType.TEAM);
	}

	public void setPhaseText(String text) {
		this.phase = text;
	}

	public void setLineType(int line, GameBoardLineType type) {
		if (type == null || !isValidLine(line))
			return;

		switch (type) {
			case STATISTIC:
			case TEAM:
			case TEXT:
				return;
			default:
				break;
		}

		this.line_type.put(line, type);
	}

	private Scoreboard getBukkitBoard(Player player) {
		Scoreboard board = BOARDS.get(player.getUniqueId());

		if (board != null)
			return board;

		BOARDS.put(player.getUniqueId(), board = Bukkit.getScoreboardManager().getNewScoreboard());

		Objective obj = board.registerNewObjective("Scoreboard", "dummy");
		obj.setDisplayName(this.display);
		obj.setDisplaySlot(this.slot);

		int empty = 0;
		for (Entry<Integer, GameBoardLineType> entry : this.line_type.entrySet()) {
			int line = entry.getKey();

			switch (entry.getValue()) {
				case EMPTY:
					obj.getScore(ScoreboardUtils.getEmptyLine(++empty)).setScore(line);
					break;

				case DATE:
					obj.getScore(ScoreboardUtils.formatDate()).setScore(line);
					break;

				case TEXT:
					obj.getScore(this.lines.get(line)).setScore(line);
					break;

				case STATISTIC:
					String stat = this.stat_line.get(line).toString();
					if (board.getTeam(stat) != null)
						continue;

					board.registerNewTeam(stat).addEntry(stat + ": " + ChatColor.GREEN);
					obj.getScore(stat + ": " + ChatColor.GREEN).setScore(line);
					break;

				case PHASE:
					if (board.getTeam("GamePhase") != null)
						continue;

					board.registerNewTeam("GamePhase").addEntry("");
					obj.getScore("").setScore(line);
					break;

				case TEAM:
					String team = this.team_line.get(line).toString();
					if (board.getTeam(team) != null)
						continue;

					board.registerNewTeam(team).addEntry(" " + team + ": ");
					obj.getScore(" " + team + ": ").setScore(line);
					break;

				default:
					continue;
			}

		}

		return board;
	}

	public void update(GamePlayer gp) {
		if (gp == null)
			return;

		Scoreboard board = getBukkitBoard(gp.getPlayer());

		for (Entry<Integer, GameBoardLineType> entry : this.line_type.entrySet()) {
			int line = entry.getKey();

			switch (entry.getValue()) {
				case STATISTIC:
					makeStatistic(board, gp, this.stat_line.get(line));
					break;

				case PHASE:
					makePhase(board, board.getObjective("Scoreboard"), line);
					break;

				case TEAM:
					makeTeam(board, gp, this.team_line.get(line));
					break;

				default:
					continue;
			}
		}

		gp.getPlayer().setScoreboard(board);
	}

	private void makeStatistic(Scoreboard board, GamePlayer gp, GameStatistic stat) {
		org.bukkit.scoreboard.Team statistic = board.getTeam(stat.toString());

		statistic.setSuffix(Integer.toString(gp.getStatisticManager().getStatistic(stat)));
	}

	private void makePhase(Scoreboard board, Objective obj, int line) {
		if (this.phase == null)
			return;

		org.bukkit.scoreboard.Team team = board.getTeam("GamePhase");

		for (String entry : team.getEntries())
			board.resetScores(entry);

		team.addEntry(this.phase);
		obj.getScore(this.phase).setScore(line);
	}

	private void makeTeam(Scoreboard board, GamePlayer gp, Team game_team) {
		org.bukkit.scoreboard.Team team = board.getTeam(game_team.toString());

		team.setPrefix(game_team.getColoredChar());

		team.setSuffix(ScoreboardUtils.getTeamState(gp.getGame(), gp, game_team));
	}

	private boolean isValidLine(int line) {
		return line >= 1 && line <= 15;
	}

}