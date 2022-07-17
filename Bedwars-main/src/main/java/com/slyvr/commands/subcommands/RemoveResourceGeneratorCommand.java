package com.slyvr.commands.subcommands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.util.NumberConversions;

import com.slyvr.api.generator.Resource;
import com.slyvr.arena.BedwarsArena;
import com.slyvr.commands.SubCommand;
import com.slyvr.game.AbstractGame;
import com.slyvr.util.ChatUtils;

public class RemoveResourceGeneratorCommand implements SubCommand {

	@Override
	public String getName() {
		return "removeResourceGen";
	}

	@Override
	public String getDescription() {
		return "Removes resource generator at the given index!";
	}

	@Override
	public String getPermission() {
		return "bedwars.setup";
	}

	@Override
	public String getUsage() {
		return "/Bw setResourceGen <Arena> <Resource> <index>";
	}

	@Override
	public void perform(Player player, String[] args) {
		if (args.length < 4) {
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

		int index = NumberConversions.toInt(args[3]);

		if (!arena.removeResourceGenerator(resource, index)) {
			player.sendMessage(ChatUtils.error("Could not remove resource generator at index §e" + index + "§c!"));
			return;
		}

		player.sendMessage(ChatUtils.success(resource.getColoredName() + " §agenerator at index §e" + index + " §ahas been removed!"));
	}

}
