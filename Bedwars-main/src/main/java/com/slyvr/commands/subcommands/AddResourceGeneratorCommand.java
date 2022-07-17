package com.slyvr.commands.subcommands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.slyvr.api.generator.Resource;
import com.slyvr.arena.BedwarsArena;
import com.slyvr.commands.SubCommand;
import com.slyvr.game.AbstractGame;
import com.slyvr.util.ChatUtils;

public class AddResourceGeneratorCommand implements SubCommand {

	@Override
	public String getName() {
		return "addResourceGenerator";
	}

	@Override
	public String getDescription() {
		return "Adds resource generator!";
	}

	@Override
	public String getPermission() {
		return "bedwars.setup";
	}

	@Override
	public String getUsage() {
		return "/Bw addResourceGen <Arena> <Resource>";
	}

	@Override
	public void perform(Player player, String[] args) {
		if (args.length < 3) {
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

		Resource resource = Resource.getByName(args[2]);
		if (resource == null) {
			player.sendMessage(ChatUtils.error("Invalid Resource!"));
			player.sendMessage(ChatUtils.info("/Bw resources"));
			return;
		}

		arena.addResourceGenerator(resource, player.getLocation());

		player.sendMessage(ChatUtils.success(resource.getColoredName() + " §agenerator has been added!"));
	}

}
