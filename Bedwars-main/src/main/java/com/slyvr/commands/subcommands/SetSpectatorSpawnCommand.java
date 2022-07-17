package com.slyvr.commands.subcommands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.slyvr.arena.BedwarsArena;
import com.slyvr.commands.SubCommand;
import com.slyvr.game.AbstractGame;
import com.slyvr.util.ChatUtils;

public class SetSpectatorSpawnCommand implements SubCommand {

	@Override
	public String getName() {
		return "setSpectatorSpawn";
	}

	@Override
	public String getDescription() {
		return "Sets spectator's spawn point!";
	}

	@Override
	public String getPermission() {
		return "bedwars.setup";
	}

	@Override
	public String getUsage() {
		return "/bw setSpectatorSpawn <Arena>";
	}

	@Override
	public void perform(Player player, String[] args) {
		if (args.length < 2) {
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

		arena.setSpectatorSpawnPoint(player.getLocation());
		player.sendMessage(ChatUtils.success(getSuccessMessage(args[1])));
	}

	private String getSuccessMessage(String name) {
		StringBuilder builder = new StringBuilder("Spectator spawn point has been set for ")
				.append(ChatColor.YELLOW)
				.append(name)
				.append(ChatColor.GREEN)
				.append("!");

		return builder.toString();
	}

}
