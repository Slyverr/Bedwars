package com.slyvr.listener;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import com.slyvr.api.level.BedwarsLevel;
import com.slyvr.api.prestige.Prestige;
import com.slyvr.api.scoreboard.Scoreboard.AnimatedTitle;
import com.slyvr.api.scoreboard.lobby.LobbyScoreboard;
import com.slyvr.api.user.User;
import com.slyvr.bedwars.Bedwars;
import com.slyvr.game.AbstractGame;

public class UserListener implements Listener {

	private static final Set<UUID> PLAYERS = new HashSet<>();

	private static BukkitTask lobbyUpdate;
	private static BukkitTask titleUpdate;

	@EventHandler
	public void onUserJoin(PlayerJoinEvent event) {
		Bukkit.getScheduler().runTaskLaterAsynchronously(Bedwars.getInstance(), () -> {
			Player player = event.getPlayer();

			User user = Bedwars.getInstance().getUser(player);
			user.setScoreboard(Bedwars.getInstance().getLobbyScoreboard());

			BedwarsLevel.setForPlayer(player, user.getLevel());
			UserListener.addPlayerToUpdatingBoard(player);
		}, 1);

	}

	@EventHandler
	public void onUserQuit(PlayerQuitEvent event) {
		Bukkit.getScheduler().runTaskAsynchronously(Bedwars.getInstance(), () -> {
			User user = Bedwars.getInstance().getUser(event.getPlayer());
			user.saveData();

			removePlayerFromUpdatingBoard(event.getPlayer());
		});

	}

	@EventHandler
	public void onUserChat(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		if (AbstractGame.inGame(player))
			return;

		User user = Bedwars.getInstance().getUser(player);

		BedwarsLevel level = user.getDisplayLevel();
		if (level == null)
			level = user.getLevel();

		Prestige prestige = user.getDisplayPrestige();
		if (prestige == null)
			prestige = Prestige.DEFAULT;

		StringBuilder message = new StringBuilder()
				.append(prestige.formatToChat(level))
				.append(" §r§7")
				.append(player.getDisplayName())
				.append("§r: ")
				.append(event.getMessage());

		event.setFormat(message.toString());
	}

	public static void addPlayerToUpdatingBoard(Player player) {
		UserListener.PLAYERS.add(player.getUniqueId());

		User user = Bedwars.getInstance().getUser(player);
		user.updateScoreboard();

		if (UserListener.lobbyUpdate == null)
			UserListener.lobbyUpdate = new BukkitRunnable() {

				@Override
				public void run() {
					for (UUID uuid : UserListener.PLAYERS) {
						Player player = Bukkit.getPlayer(uuid);
						if (player == null || AbstractGame.inGame(player))
							continue;

						User user = Bedwars.getInstance().getUser(player);
						user.updateScoreboard();
					}

				}
			}.runTaskTimerAsynchronously(Bedwars.getInstance(), 0, 100);

		if (UserListener.titleUpdate == null) {
			LobbyScoreboard board = Bedwars.getInstance().getLobbyScoreboard();
			if (board == null)
				return;

			AnimatedTitle title = board.getTitle();
			if (title.getUpdateTicks() > 0)
				UserListener.titleUpdate = new BukkitRunnable() {

					@Override
					public void run() {
						String next = title.next();

						for (UUID uuid : UserListener.PLAYERS) {
							Player player = Bukkit.getPlayer(uuid);
							if (player == null || AbstractGame.inGame(player))
								continue;

							Scoreboard board = player.getScoreboard();
							if (board == null)
								return;

							Objective obj = board.getObjective(DisplaySlot.SIDEBAR);
							if (obj == null)
								return;

							obj.setDisplayName(next);
						}

					}
				}.runTaskTimerAsynchronously(Bedwars.getInstance(), 0, title.getUpdateTicks());

		}
	}

	public static void removePlayerFromUpdatingBoard(Player player) {
		if (player == null)
			return;

		UserListener.PLAYERS.remove(player.getUniqueId());
		if (!UserListener.PLAYERS.isEmpty())
			return;

		if (UserListener.lobbyUpdate != null)
			UserListener.lobbyUpdate.cancel();

		if (UserListener.titleUpdate != null)
			UserListener.titleUpdate.cancel();

	}

}