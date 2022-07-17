package com.slyvr.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.slyvr.api.game.Game;
import com.slyvr.api.game.GameMode;
import com.slyvr.bedwars.Bedwars;
import com.slyvr.game.AbstractGame;
import com.slyvr.game.BedwarsGame;
import com.slyvr.util.ChatUtils;

public class PlayCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player) || args.length == 0)
			return true;

		new BukkitRunnable() {
			private Game game;

			@Override
			public void run() {
				Player player = (Player) sender;

				if (!player.hasPermission("bw.admin") && !player.hasPermission("bw.play")) {
					player.sendMessage(ChatUtils.error("You don't have the permission to execute this command!"));
					return;
				}

				GameMode mode = null;

				if (args[0].equalsIgnoreCase("bedwars_eight_one"))
					mode = GameMode.SOLO;
				else if (args[0].equalsIgnoreCase("bedwars_eight_two"))
					mode = GameMode.DUO;
				else if (args[0].equalsIgnoreCase("bedwars_four_three"))
					mode = GameMode.TRIO;
				else if (args[0].equalsIgnoreCase("bedwars_four_four"))
					mode = GameMode.QUATUOR;

				if (mode == null)
					return;

				for (Game existing : AbstractGame.getGames().values()) {
					if (existing.hasStarted() || (existing.getMode() != mode))
						continue;

					if (existing.canAddPlayer(player))
						this.game = existing;
				}

				if (this.game == null)
					this.game = BedwarsGame.randomGame(mode);

				if (this.game == null) {
					player.sendMessage(ChatColor.RED + "Could not find a game! Try again later.");
					return;
				}

				Bukkit.getScheduler().runTask(Bedwars.getInstance(), () -> this.game.addPlayer(player));
			}
		}.runTaskAsynchronously(Bedwars.getInstance());

		return true;
	}

}
