package com.slyvr.commands.subcommands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.slyvr.api.team.Team;
import com.slyvr.arena.BedwarsArena;
import com.slyvr.commands.SubCommand;
import com.slyvr.game.AbstractGame;
import com.slyvr.util.ChatUtils;

public class SetTeamUpgradeCommand implements SubCommand {

	@Override
	public String getName() {
		return "setTeamUpgrade";
	}

	@Override
	public String getDescription() {
		return "Sets team's ugprade location!";
	}

	@Override
	public String getPermission() {
		return "bedwars.setup";
	}

	@Override
	public String getUsage() {
		return "/bw setTeamUpgrade <Arena> <Team>";
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

		Team team = Team.getByName(args[2]);
		if (team == null) {
			player.sendMessage(ChatUtils.error("Invalid Team!"));
			player.sendMessage(ChatUtils.info("/bw teams"));
			return;
		}

		arena.setTeamUpgrade(team, player.getLocation());

		player.sendMessage(ChatUtils.success(team.getColoredString() + " team §aupgrade has been set!"));
	}

}
