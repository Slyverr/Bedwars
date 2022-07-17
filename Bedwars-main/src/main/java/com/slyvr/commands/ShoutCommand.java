package com.slyvr.commands;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.slyvr.api.event.player.AsyncGamePlayerShoutEvent;
import com.slyvr.api.game.Game;
import com.slyvr.api.game.player.GamePlayer;
import com.slyvr.bedwars.Bedwars;
import com.slyvr.game.AbstractGame;
import com.slyvr.util.ChatUtils;

public class ShoutCommand implements CommandExecutor {

	private static final Map<UUID, Integer> COUNTDOWN = new HashMap<>();

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "Only players who can execute this command!");
			return false;
		}

		if (args.length == 0)
			return true;

		Player player = (Player) sender;

		Bukkit.getScheduler().runTaskAsynchronously(Bedwars.getInstance(), () -> {
			Game game = AbstractGame.getPlayerGame(player);
			if (game != null) {
				if (game.getMode().getTeamMax() == 1)
					return;

				UUID id = player.getUniqueId();

				Integer time = ShoutCommand.COUNTDOWN.get(id);
				if (time != null) {
					player.sendMessage(ChatUtils.format("&cYou have to wait &e" + time + "&c to use this command again!"));
					return;
				}

				GamePlayer gp = game.getGamePlayer(player);
				if (game.isSpectator(player))
					return;

				AsyncGamePlayerShoutEvent bwEvent = new AsyncGamePlayerShoutEvent(gp, Arrays.toString(args));
				Bukkit.getPluginManager().callEvent(bwEvent);

				String result = String.format(bwEvent.getFormat(), gp.getTeam().getPrefix(), "&r " + player.getDisplayName(), ": &r" + bwEvent.getMessage());
				game.broadcastMessage(ChatUtils.format("&6[SHOUT] " + result));

				startCountdown(player);
			}

		});

		return true;

	}

	private void startCountdown(Player player) {
		ShoutCommand.COUNTDOWN.put(player.getUniqueId(), 60);

		new BukkitRunnable() {

			@Override
			public void run() {
				int time = ShoutCommand.COUNTDOWN.get(player.getUniqueId());
				if (time == 0) {
					ShoutCommand.COUNTDOWN.remove(player.getUniqueId());
					cancel();
					return;
				}

				ShoutCommand.COUNTDOWN.put(player.getUniqueId(), time - 1);
			}
		}.runTaskTimerAsynchronously(Bedwars.getInstance(), 0, 20);

	}

}