package com.slyvr.scoreboard.waiting;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import com.slyvr.api.game.Game;
import com.slyvr.bedwars.Bedwars;
import com.slyvr.scoreboard.AbstractScoreboard;
import com.slyvr.util.ChatUtils;
import com.slyvr.util.ScoreboardUtils;

public class WaitingScoreboard extends AbstractScoreboard {

	public static enum WaitingBoardLineType {

		COUNTDOWN,
		PLAYERS,
		VERSION,
		EMPTY,
		MODE,
		DATE,
		TEXT,
		MAP;

		private static final Map<String, WaitingBoardLineType> BY_NAME = new HashMap<>();

		static {
			for (WaitingBoardLineType type : values())
				WaitingBoardLineType.BY_NAME.put(type.name().toLowerCase(), type);
		}

		public static WaitingBoardLineType getByName(String name) {
			return name != null ? WaitingBoardLineType.BY_NAME.get(name.toLowerCase()) : null;
		}

	}

	private static final Map<Game, Scoreboard> GAME_BOARD = new ConcurrentHashMap<>();
	private static final Map<Game, String> COUNTDOWN_TEXT = new ConcurrentHashMap<>();

	private Map<Integer, WaitingBoardLineType> line_types = new HashMap<>();

	public WaitingScoreboard(AnimatedTitle title) {
		super(title);
	}

	@Override
	public void setText(int line, String text) {
		if (text == null || !isValidLine(line))
			return;

		this.lines.put(line, ChatUtils.format(text));
		this.line_types.put(line, WaitingBoardLineType.TEXT);
	}

	public WaitingBoardLineType getLineType(int line) {
		return this.line_types.get(line);
	}

	public void setLineType(int line, WaitingBoardLineType type) {
		if (type == null || type == WaitingBoardLineType.TEXT || !isValidLine(line))
			return;

		WaitingBoardLineType old = this.line_types.get(line);
		if (old != null)
			return;

		this.line_types.put(line, type);
	}

	public String getCountdownText(Game game) {
		return COUNTDOWN_TEXT.get(game);
	}

	public void setCountdownText(Game game, String text) {
		COUNTDOWN_TEXT.put(game, ChatUtils.format(text));
	}

	public Scoreboard getBukkitBoard(Game game) {
		Scoreboard board = GAME_BOARD.get(game);
		if (board != null)
			return board;

		GAME_BOARD.put(game, board = Bukkit.getScoreboardManager().getNewScoreboard());

		Objective obj = board.registerNewObjective("Scoreboard", "dummy");
		obj.setDisplayName(this.display);
		obj.setDisplaySlot(this.slot);

		int empty = 0;
		for (Entry<Integer, WaitingBoardLineType> entry : line_types.entrySet()) {
			int line = entry.getKey();

			switch (entry.getValue()) {
				case EMPTY:
					obj.getScore(ScoreboardUtils.getEmptyLine(++empty)).setScore(line);
					break;
				case DATE:
					obj.getScore(ScoreboardUtils.formatDate()).setScore(line);
					break;
				case TEXT:
					obj.getScore(lines.get(line)).setScore(line);
					break;
				case MODE:
					obj.getScore("Mode: " + ChatColor.GREEN + game.getArena().getMode().getName()).setScore(line);
					break;
				case VERSION:
					obj.getScore("Version: " + ChatColor.GREEN + getVersion()).setScore(line);
					break;
				case MAP:
					String name = game.getArena().getMapName();
					if (name == null)
						continue;

					obj.getScore("Map: " + ChatColor.GREEN + name).setScore(line);
					break;
				case PLAYERS:
					if (board.getTeam("Players") != null)
						continue;

					Team players = board.registerNewTeam("Players");
					players.addEntry("Players: ");

					obj.getScore("Players: ").setScore(line);
					break;
				case COUNTDOWN:
					if (board.getTeam("Countdown") != null)
						continue;

					Team countdown = board.registerNewTeam("Countdown");
					countdown.addEntry("");

					obj.getScore("").setScore(line);
					break;
				default:
					break;
			}

		}

		return board;
	}

	private String getVersion() {
		return "v" + Bedwars.getInstance().getDescription().getVersion();
	}

	public void update(Game game, Set<Player> players) {
		if (game == null || players == null)
			return;

		Scoreboard board = getBukkitBoard(game);

		Team players_counter = board.getTeam("Players");
		players_counter.setSuffix(ChatColor.GREEN + "" + players.size() + "/" + game.getMode().getGameMax());

		String text = getCountdownText(game);
		if (text == null)
			text = "Waiting...";

		Team countdown = board.getTeam("Countdown");
		int length = text.length();
		if (length <= 16) {
			countdown.setPrefix(text);
		} else {
			countdown.setPrefix(text.substring(0, 16));
			countdown.setSuffix(text.substring(16, length > 32 ? 32 : length));
		}

		for (Player player : players)
			player.setScoreboard(board);
	}

	private boolean isValidLine(int line) {
		return line >= 1 && line <= 15;
	}

}