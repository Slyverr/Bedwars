package com.slyvr.commands.subcommands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.slyvr.api.game.GameMode;
import com.slyvr.bedwars.Bedwars;
import com.slyvr.commands.SubCommand;

public class ModesCommand implements SubCommand {

	private static final String TEXT;

	static {
		StringBuilder builder = new StringBuilder(Bedwars.getInstance().getPluginPrefix());

		GameMode[] values = GameMode.values();
		for (int i = 0; i < values.length; i++) {
			builder.append(ChatColor.GOLD).append(values[i].getName());

			if (i < values.length - 1)
				builder.append(ChatColor.GRAY).append(", ");
		}

		TEXT = builder.toString();
	}

	@Override
	public String getName() {
		return "Modes";
	}

	@Override
	public String getDescription() {
		return "Shows all bedwars gamemodes!";
	}

	@Override
	public String getPermission() {
		return "bedwars.setup";
	}

	@Override
	public String getUsage() {
		return "/bw modes";
	}

	@Override
	public void perform(Player player, String[] args) {
		player.sendMessage(TEXT);
	}

}