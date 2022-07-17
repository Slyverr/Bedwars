package com.slyvr.util;

import java.time.LocalDate;
import java.util.Collection;

import org.bukkit.ChatColor;

import com.slyvr.api.game.Game;
import com.slyvr.api.game.GameManager;
import com.slyvr.api.game.GamePhase;
import com.slyvr.api.game.GameState;
import com.slyvr.api.game.player.GamePlayer;
import com.slyvr.api.team.Team;

public class ScoreboardUtils {

	public static String formatTeam(Game game, Team team, GamePlayer gp) {
		String result = formatTeam(game, team);

		return gp.getTeam() != team ? result : result + "§7YOU";
	}

	public static String getTeamState(Game game, GamePlayer gp, Team team) {
		StringBuilder teamState = new StringBuilder().append(getTeamState(game, team));

		if (gp.getTeam() == team)
			teamState.append(ChatColor.GRAY).append(" YOU");

		return teamState.toString();
	}

	public static String getTeamState(Game game, Team team) {
		if (game.getGameState() == GameState.ENDED || game.isEliminated(team))
			return ChatColor.RED + "✘";

		if (game.hasBed(team))
			return ChatColor.GREEN + "✓";

		Collection<GamePlayer> players = game.getTeamPlayers(team);
		int count = 0;
		for (GamePlayer gp : players) {
			if (!game.isEliminated(gp.getPlayer()))
				count++;
		}

		if (count > 0)
			return ChatColor.GREEN + Integer.toString(count);

		return ChatColor.RED + "✘";

	}

	public static String formatTeam(Game game, Team team) {
		return team.getScoreboardText() + " " + getTeamState(game, team);
	}

	public static String formatPhase(Game game) {
		GameManager manager = game.getManager();
		if (manager == null)
			return "";

		GamePhase current = manager.getCurrentPhase();
		if (current == null)
			return "";

		StringBuilder builder = new StringBuilder(current.getName())
				.append(" in ")
				.append(ChatColor.GREEN)
				.append(formatCountdown(manager.timeLeftForNextPhase()));

		return builder.toString();
	}

	public static String formatCountdown(long time) {
		return String.format("%d:%02d", time / 60, time % 60);
	}

	public static String formatDate() {
		LocalDate d = LocalDate.now();

		return ChatColor.GRAY + String.format("§7%02d/%02d/%02d", d.getDayOfMonth(), d.getMonthValue(), d.getYear() % 2000);
	}

	public static String formatDecimal(int num) {
		StringBuilder builder = new StringBuilder();

		String number = Integer.toString(num);

		int length = number.length();
		for (int i = length; i >= 1; i--) {
			if (i != length && i % 3 == 0)
				builder.append(',');

			builder.append(number.charAt(length - i));
		}

		return builder.toString();
	}

	public static String getEmptyLine(int length) {
		StringBuilder builder = new StringBuilder();

		for (int i = 0; i <= length; i++)
			builder.append(' ');

		return builder.toString();
	}

}