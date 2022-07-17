package com.slyvr.commands.subcommands;

import org.bukkit.entity.Player;

import com.slyvr.arena.BedwarsArena;
import com.slyvr.commands.SubCommand;
import com.slyvr.util.ChatUtils;

public class CreateCommand implements SubCommand {

	@Override
	public String getName() {
		return "create";
	}

	@Override
	public String getDescription() {
		return "Create a new arena with the given name!";
	}

	@Override
	public String getPermission() {
		return "bedwars.setup";
	}

	@Override
	public String getUsage() {
		return "/bw create <Name>";
	}

	@Override
	public void perform(Player player, String[] args) {
		if (args.length < 2) {
			player.sendMessage(ChatUtils.usage(getUsage()));
			return;
		}

		BedwarsArena arena = new BedwarsArena(args[1]);
		if (arena.exists()) {
			player.sendMessage(ChatUtils.error("§e" + args[1] + " §calready exists!"));
			return;
		}

		arena.createFile();
		player.sendMessage(ChatUtils.success("§e" + args[1] + " §ahas been created!"));
	}

}
