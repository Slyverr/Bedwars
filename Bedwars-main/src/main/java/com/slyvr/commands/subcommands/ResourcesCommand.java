package com.slyvr.commands.subcommands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.slyvr.api.generator.Resource;
import com.slyvr.bedwars.Bedwars;
import com.slyvr.commands.SubCommand;

public class ResourcesCommand implements SubCommand {

	private static final String TEXT;

	static {
		StringBuilder builder = new StringBuilder(Bedwars.getInstance().getPluginPrefix());

		Resource[] values = Resource.values();
		for (int i = 0; i < values.length; i++) {
			Resource resource = values[i];

			if (resource == Resource.FREE)
				continue;

			builder.append(resource.getColoredName());

			if (i < values.length - 1)
				builder.append(ChatColor.GRAY).append(", ");
		}

		TEXT = builder.toString();
	}

	@Override
	public String getName() {
		return "resources";
	}

	@Override
	public String getDescription() {
		return "Shows all bedwars resources!";
	}

	@Override
	public String getPermission() {
		return "bedwars.setup";
	}

	@Override
	public String getUsage() {
		return "/bw resources";
	}

	@Override
	public void perform(Player player, String[] args) {
		player.sendMessage(TEXT);
	}

}