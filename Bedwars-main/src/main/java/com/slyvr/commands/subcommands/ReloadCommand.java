package com.slyvr.commands.subcommands;

import org.bukkit.entity.Player;

import com.slyvr.arena.BedwarsArena;
import com.slyvr.commands.SubCommand;
import com.slyvr.game.AbstractGame;
import com.slyvr.util.ChatUtils;

public class ReloadCommand implements SubCommand {

	@Override
	public String getName() {
		return "reloadArena";
	}

	@Override
	public String getDescription() {
		return "Reloads arena!";
	}

	@Override
	public String getPermission() {
		return "bedwars.setup";
	}

	@Override
	public String getUsage() {
		return "/bw reloadArena <Arena>";
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
			player.sendMessage(ChatUtils.error("§e" + args[1] + " §cis already in use and cannot be reloaded!"));
			return;
		}

		arena.reloadArena();
		player.sendMessage(ChatUtils.success("Arena region has been set!"));
	}

}