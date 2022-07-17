package com.slyvr.commands.subcommands;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.slyvr.api.arena.BedwarsBed;
import com.slyvr.api.team.Team;
import com.slyvr.arena.BedwarsArena;
import com.slyvr.commands.SubCommand;
import com.slyvr.game.AbstractGame;
import com.slyvr.util.BedUtils;
import com.slyvr.util.ChatUtils;

public class SetTeamBedCommand implements SubCommand {

	@Override
	public String getName() {
		return "setTeamBed";
	}

	@Override
	public String getDescription() {
		return "Sets team's bed location!";
	}

	@Override
	public String getPermission() {
		return "bedwars.setup";
	}

	@Override
	public String getUsage() {
		return "/bw setTeamBed <Arena> <Team>";
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

		Block part1 = player.getLocation().getBlock();
		if (!BedUtils.isBed(part1)) {
			player.sendMessage(ChatUtils.info("You must be on top of a bed!"));
			return;
		}

		Team team = Team.getByName(args[2]);
		if (team == null) {
			player.sendMessage(ChatUtils.error("Invalid Team!"));
			player.sendMessage(ChatUtils.info("/bw teams"));
			return;
		}

		Block part2 = BedUtils.getOtherBedPart(part1);
		if (part2 == null) {
			if (BedUtils.isBedHead(part1))
				player.sendMessage(ChatUtils.error("Could not find bed foot!"));
			else
				player.sendMessage(ChatUtils.error("Cound not find bed head!"));

			return;
		}

		arena.setTeamBed(new BedwarsBed(team, part1, part2));

		player.sendMessage(ChatUtils.success(team.getColoredString() + " team §abed has been set!"));
	}

}