package com.slyvr.game;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import com.cryptomorin.xseries.messages.Titles;
import com.slyvr.api.game.Game;
import com.slyvr.api.game.GameState;
import com.slyvr.api.scoreboard.Scoreboard.AnimatedTitle;
import com.slyvr.bedwars.Bedwars;
import com.slyvr.scoreboard.waiting.WaitingScoreboard;

public class GameCountdown {

	private static final WaitingScoreboard BOARD = Bedwars.getInstance().getWaitingScoreboard();
	private static final String NOT_ENOUGH_PLAYERS = "§cWe don't have enough players! Start cancelled.";

	private Set<Player> players = new HashSet<>();

	private Game game;

	private BukkitTask task;
	private BukkitTask title;

	private boolean isLocked = false;
	private int minimum;

	public GameCountdown(Game game) {
		this.game = game;
		this.minimum = Bedwars.getInstance().getSettings().getMinimumPlayers(game.getMode());
	}

	public void start() {
		if (isLocked)
			return;

		int countdown = Bedwars.getInstance().getSettings().getGameCountdown();

		this.game.setGameState(GameState.COUNTDOWN);
		if (BOARD == null) {
			this.task = Bukkit.getScheduler().runTaskLater(Bedwars.getInstance(), () -> game.startGame(), 20 * countdown);
			return;
		}

		this.task = new BukkitRunnable() {
			private int timeLeft = countdown;

			@Override
			public void run() {
				if (timeLeft == 0) {
					GameCountdown.this.game.startGame();
					GameCountdown.this.stop();
					return;
				}

				GameCountdown.BOARD.setCountdownText(game, formatStartingCountdown(timeLeft));
				GameCountdown.BOARD.update(game, players);

				switch (timeLeft) {
					case 20:
						sendMessageAndTitle(formatCountdown(timeLeft), ChatColor.GREEN + "20", "");
						break;
					case 10:
						sendMessageAndTitle(formatCountdown(timeLeft), ChatColor.GOLD + "10", "");
						break;
					case 5:
					case 4:
						sendMessageAndTitle(formatCountdown(timeLeft), ChatColor.YELLOW + "" + timeLeft, "");
						break;
					case 3:
					case 2:
					case 1:
						sendMessageAndTitle(formatCountdown(timeLeft), ChatColor.RED + "" + timeLeft, "");
						break;

					default:
						break;
				}

				this.timeLeft--;
			}

		}.runTaskTimer(Bedwars.getInstance(), 0, 20);

		AnimatedTitle title = BOARD.getTitle();
		if (title.getUpdateTicks() > 0) {
			this.title = new BukkitRunnable() {

				@Override
				public void run() {
					String next = title.next();

					for (Player player : players) {
						Scoreboard board = player.getScoreboard();

						Objective obj = board.getObjective(DisplaySlot.SIDEBAR);
						if (obj == null)
							continue;

						obj.setDisplayName(next);
					}

				}

			}.runTaskTimerAsynchronously(Bedwars.getInstance(), 0, title.getUpdateTicks());
		}

		this.isLocked = true;
	}

	private void sendMessageAndTitle(String message, String title, String subTitle) {
		for (Player player : players) {
			Titles.sendTitle(player, 10, 20, 10, title, subTitle);
			player.sendMessage(message);
		}

	}

	private String formatStartingCountdown(int time) {
		return new StringBuilder().append("Starting in ").append(ChatColor.GREEN).append(time).append("s").toString();
	}

	private String formatCountdown(int time) {
		StringBuilder builder = new StringBuilder()
				.append(ChatColor.YELLOW)
				.append("The game starts in ")
				.append(ChatColor.RED)
				.append(time)
				.append(ChatColor.YELLOW)
				.append(" seconds!");

		return builder.toString();
	}

	public void addPlayer(Player player) {
		this.players.add(player);

		GameCountdown.BOARD.setCountdownText(game, "Waiting...");
		GameCountdown.BOARD.update(game, players);
		if (players.size() >= minimum)
			this.start();
	}

	public void removePlayer(Player player) {
		this.players.remove(player);

		if (players.size() >= minimum)
			return;

		for (Player p : players)
			Titles.sendTitle(p, 10, 20, 10, "", NOT_ENOUGH_PLAYERS);

		this.cancel();
	}

	public void cancel() {
		if (!isLocked)
			return;

		this.game.setGameState(GameState.WAITING);

		if (BOARD != null) {
			BOARD.setCountdownText(game, "Waiting...");
			BOARD.update(game, players);
		}

		if (title != null)
			this.title.cancel();

		this.task.cancel();
		this.isLocked = false;
	}

	public void stop() {
		if (!isLocked)
			return;

		this.players.clear();
		this.cancel();
	}

}