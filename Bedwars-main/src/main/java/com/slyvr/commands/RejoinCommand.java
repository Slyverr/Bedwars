package com.slyvr.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.slyvr.api.game.Game;
import com.slyvr.game.AbstractGame;

public class RejoinCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("§cOnly players who can execute this command!");
			return true;
		}

		Player player = (Player) sender;

		if (!player.hasPermission("bw.admin") && !player.hasPermission("bw.rejoin")) {
			player.sendMessage("§cYou don't have the permission to execute this command!");
			return true;
		}

		Game game = AbstractGame.getDisconnectedPlayerGame(player);
		if (game == null)
			return true;

		game.reconnect(player);

		return true;
	}

}
