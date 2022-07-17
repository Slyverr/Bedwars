package com.slyvr.commands.subcommands;

import org.bukkit.entity.Player;

import com.slyvr.arena.BedwarsArena;
import com.slyvr.commands.SubCommand;
import com.slyvr.util.ChatUtils;

public class DisableCommand implements SubCommand {

	@Override
	public String getName() {
		return "disable";
	}

	@Override
	public String getDescription() {
		return "Disable arena!";
	}

	@Override
	public String getPermission() {
		return "bedwars.setup";
	}

	@Override
	public String getUsage() {
		return "/bw disable <Arena>";
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

		arena.setEnabled(false);
		player.sendMessage(ChatUtils.success("Arena has been disabled!"));
	}

}
