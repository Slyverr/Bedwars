package com.slyvr.commands.subcommands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.slyvr.api.generator.GeneratorSpeed;
import com.slyvr.arena.BedwarsArena;
import com.slyvr.commands.SubCommand;
import com.slyvr.game.AbstractGame;
import com.slyvr.util.ChatUtils;

public class SetGenSpeedCommand implements SubCommand {

	@Override
	public String getName() {
		return "setGenSpeed";
	}

	@Override
	public String getDescription() {
		return "Sets arena teams generator speed!";
	}

	@Override
	public String getPermission() {
		return "bedwars.setup";
	}

	@Override
	public String getUsage() {
		return "/bw setGenSpeed <Arena> <Name>";
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

		GeneratorSpeed speed = GeneratorSpeed.getByName(args[2]);
		if (speed == null) {
			player.sendMessage(ChatUtils.error("Invalid generator speed!"));
			player.sendMessage(ChatUtils.info("/bw speeds"));
			return;
		}

		arena.setGeneratorSpeed(speed);

		player.sendMessage(ChatUtils.success("Arena's generator speed has been set to §e" + speed.getName() + "§a!"));
	}

}
