package com.slyvr.commands.subcommands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.slyvr.arena.BedwarsArena;
import com.slyvr.commands.SubCommand;
import com.slyvr.game.AbstractGame;
import com.slyvr.util.ChatUtils;

public class RemoveCommand implements SubCommand {

	@Override
	public String getName() {
		return "remove";
	}

	@Override
	public String getDescription() {
		return "Removes existing arena!";
	}

	@Override
	public String getPermission() {
		return "bedwars.setup";
	}

	@Override
	public String getUsage() {
		return "/bw remove <Arena>";
	}

	@Override
	public void perform(Player player, String[] args) {
		if (args.length < 2) {
			player.sendMessage(ChatUtils.usage(getUsage()));
			return;
		}

		BedwarsArena arena = BedwarsArena.getArena(args[1]);
		if (arena == null || !arena.exists()) {
			player.sendMessage(ChatUtils.error("Arena with name §e" + args[1] + " §cdoesn't exist!"));
			return;
		}

		if (AbstractGame.isArenaOccuped(arena)) {
			player.sendMessage(ChatUtils.error("§e" + args[1] + " §cis already in use and cannot be edited!"));
			return;
		}

		if (!arena.remove()) {
			player.sendMessage(ChatUtils.error("Could not delete arena " + ChatColor.YELLOW + args[1] + ChatColor.RED + "!"));
			return;
		}

		player.sendMessage(ChatUtils.success(ChatColor.YELLOW + args[1] + ChatColor.GREEN + " has been removed!"));
	}

}