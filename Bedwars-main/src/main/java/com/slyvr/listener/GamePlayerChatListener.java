package com.slyvr.listener;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import com.slyvr.api.event.player.AsyncGamePlayerChatEvent;
import com.slyvr.api.game.Game;
import com.slyvr.api.game.player.GamePlayer;
import com.slyvr.api.level.BedwarsLevel;
import com.slyvr.api.prestige.Prestige;
import com.slyvr.api.team.Team;
import com.slyvr.api.user.User;
import com.slyvr.bedwars.Bedwars;
import com.slyvr.game.AbstractGame;

public class GamePlayerChatListener implements Listener {

	private static final String SPECTATOR_PREFIX = ChatColor.GRAY + "[Spectator]";

	@EventHandler
	public void onGamePlayerChat(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();

		Game game = AbstractGame.getPlayerGame(player);
		if (game == null)
			return;

		event.setCancelled(true);

		if (!game.hasStarted()) {
			onWaitingChat(game, player, event.getMessage());
			return;
		}

		if (game.isSpectator(player)) {
			onSpectatorChat(game, player, event.getMessage());
			return;
		}

		onPlayerChat(game, game.getGamePlayer(player), event.getMessage());
	}

	private void onWaitingChat(Game game, Player player, String message) {
		StringBuilder builder = new StringBuilder().append(ChatColor.GRAY).append(player.getDisplayName()).append(": ").append(message);

		game.broadcastMessage(builder.toString());
	}

	private void onSpectatorChat(Game game, Player player, String message) {
		StringBuilder builder = new StringBuilder()
				.append(SPECTATOR_PREFIX)
				.append(player.getDisplayName())
				.append(ChatColor.GRAY)
				.append(": ")
				.append(message);

		game.broadcastMessage(builder.toString(), p -> game.isSpectator(p));
	}

	private void onPlayerChat(Game game, GamePlayer gp, String message) {
		AsyncGamePlayerChatEvent event = new AsyncGamePlayerChatEvent(gp, message);
		Bukkit.getPluginManager().callEvent(event);

		if (event.isCancelled())
			return;

		String format = event.getFormat();
		if (format == null)
			return;

		User user = Bedwars.getInstance().getUser(gp.getPlayer());

		BedwarsLevel level = user.getDisplayLevel();
		if (level == null)
			level = user.getLevel();

		Prestige prestige = user.getDisplayPrestige();
		if (prestige == null)
			prestige = user.getPrestige();

		Team team = gp.getTeam();

		String text = String.format(format, prestige.formatToChat(level), team.getPrefix(), gp.getPlayer().getDisplayName(), message);

		game.broadcastMessage(text, p -> {
			if (game.isSpectator(p))
				return false;

			GamePlayer gp1 = game.getGamePlayer(p);
			return gp1 != null && gp1.getTeam() == team;
		});
	}

}