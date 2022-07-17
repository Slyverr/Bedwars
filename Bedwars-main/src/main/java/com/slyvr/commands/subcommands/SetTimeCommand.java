package com.slyvr.commands.subcommands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.util.NumberConversions;

import com.slyvr.arena.BedwarsArena;
import com.slyvr.commands.SubCommand;
import com.slyvr.game.AbstractGame;
import com.slyvr.util.ChatUtils;

public class SetTimeCommand implements SubCommand {

	@Override
	public String getName() {
		return "setTime";
	}

	@Override
	public String getDescription() {
		return "Set time of the arena!";
	}

	@Override
	public String getPermission() {
		return "bedwars.setup";
	}

	@Override
	public String getUsage() {
		return "/bw setTime <Arena> <Time>";
	}

	@Override
	public void perform(Player player, String[] args) {
		if (args.length > 3) {
			player.sendMessage(ChatUtils.usage(getUsage()));
			return;
		}

		BedwarsArena arena = BedwarsArena.getArena(args[1]);
		if (arena == null || !arena.exists()) {
			player.sendMessage(ChatUtils.error("Arena with name " + ChatColor.YELLOW + args[1] + ChatColor.RED + " doesn't exist!"));
			return;
		}

		if (AbstractGame.isArenaOccuped(arena)) {
			player.sendMessage(ChatUtils.error(ChatColor.YELLOW + args[1] + ChatColor.RED + " is already in use and cannot be edited!"));
			return;
		}

		int time = NumberConversions.toInt(args[2]);

		if (time < 0) {
			player.sendMessage(ChatUtils.error("Please enter a valid number!"));
			return;
		}

		arena.setArenaTime(time);

		player.sendMessage(ChatUtils.success("Arena time has been set to §e" + time + "§a!"));
	}

}
