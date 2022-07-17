package com.slyvr.commands.subcommands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.slyvr.api.team.Team;
import com.slyvr.bedwars.Bedwars;
import com.slyvr.commands.SubCommand;

public class TeamsCommand implements SubCommand {

	private static final String TEXT;

	static {
		StringBuilder builder = new StringBuilder(Bedwars.getInstance().getPluginPrefix());

		Team[] values = Team.values();
		for (int i = 0; i < values.length; i++) {
			builder.append(values[i].getColoredString());

			if (i < values.length - 1)
				builder.append(ChatColor.GRAY).append(", ");
		}

		TEXT = builder.toString();
	}

	@Override
	public String getName() {
		return "teams";
	}

	@Override
	public String getDescription() {
		return "Show all the available teams!";
	}

	@Override
	public String getPermission() {
		return "bedwars.setup";
	}

	@Override
	public String getUsage() {
		return "/bw teams";
	}

	@Override
	public void perform(Player player, String[] args) {
		player.sendMessage(TEXT);
	}

}