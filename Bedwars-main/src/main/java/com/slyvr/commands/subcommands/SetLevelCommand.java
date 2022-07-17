package com.slyvr.commands.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.util.NumberConversions;

import com.slyvr.api.level.BedwarsLevel;
import com.slyvr.api.prestige.Prestige;
import com.slyvr.api.user.User;
import com.slyvr.bedwars.Bedwars;
import com.slyvr.commands.SubCommand;
import com.slyvr.user.UserData;
import com.slyvr.util.ChatUtils;

public class SetLevelCommand implements SubCommand {

	@Override
	public String getName() {
		return "setLevel";
	}

	@Override
	public String getDescription() {
		return "Sets current player bedwars level!";
	}

	@Override
	public String getPermission() {
		return "bedwars.command.level";
	}

	@Override
	public String getUsage() {
		return "/Bw setLevel <Player> <Level> <Exp-Optional>";
	}

	@Override
	public void perform(Player player, String[] args) {
		if (args.length < 3) {
			player.sendMessage(ChatUtils.usage(getUsage()));
			return;
		}

		OfflinePlayer userPlayer = Bukkit.getOfflinePlayer(args[1]);
		if (!userPlayer.hasPlayedBefore()) {
			player.sendMessage(ChatUtils.error("Player not found!"));
			return;
		}

		int level = NumberConversions.toInt(args[2]);
		if (level < 1) {
			player.sendMessage(ChatUtils.error("Level must be between 1 and " + Integer.MAX_VALUE));
			return;
		}

		int exp = args.length >= 4 ? NumberConversions.toInt(args[3]) : 0;
		if (exp < 0) {
			player.sendMessage(ChatUtils.error("Exp must be positif!"));
			return;
		}

		boolean display = args.length >= 5 ? Boolean.parseBoolean(args[4]) : false;

		BedwarsLevel bwLvL = new BedwarsLevel(level, exp, Bedwars.getInstance().getSettings().getLevelUpExpFor(level));
		Prestige prestige = Prestige.getByLevel(level);

		if (userPlayer.isOnline()) {
			User user = Bedwars.getInstance().getUser(player);

			if (display) {
				user.setDisplayLevel(bwLvL);
				user.setDisplayPrestige(prestige);
			} else {
				user.setLevel(bwLvL);
				user.setPrestige(prestige);
			}

		} else {
			UserData config = new UserData(userPlayer);

			config.setLevel(bwLvL);
			config.setPrestige(prestige);
		}

	}

}