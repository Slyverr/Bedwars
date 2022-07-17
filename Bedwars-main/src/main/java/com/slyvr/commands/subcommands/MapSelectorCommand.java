package com.slyvr.commands.subcommands;

import org.bukkit.entity.Player;

import com.slyvr.commands.SubCommand;

public class MapSelectorCommand implements SubCommand {

	@Override
	public String getName() {
		return "maps";
	}

	@Override
	public String getDescription() {
		return "Shows all available maps!";
	}

	@Override
	public String getPermission() {
		return "bedwars.command.maps";
	}

	@Override
	public String getUsage() {
		return "/bw maps";
	}

	@Override
	public void perform(Player player, String[] args) {

	}

}