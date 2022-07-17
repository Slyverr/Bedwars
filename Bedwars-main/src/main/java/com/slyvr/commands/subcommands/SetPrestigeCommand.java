package com.slyvr.commands.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.slyvr.api.prestige.Prestige;
import com.slyvr.api.user.User;
import com.slyvr.bedwars.Bedwars;
import com.slyvr.commands.SubCommand;
import com.slyvr.user.UserData;
import com.slyvr.util.ChatUtils;

public class SetPrestigeCommand implements SubCommand {

	@Override
	public String getName() {
		return "setPrestige";
	}

	@Override
	public String getDescription() {
		return "Sets player prestige!";
	}

	@Override
	public String getPermission() {
		return "bedwars.command.prestige";
	}

	@Override
	public String getUsage() {
		return "/Bw setPrestige <Prestige> <Player-Optional>";
	}

	@Override
	public void perform(Player player, String[] args) {
		if (args.length < 2) {
			player.sendMessage(ChatUtils.usage(getUsage()));
			return;
		}

		Prestige prestige = Prestige.getByName(args[1]);
		if (prestige == null) {
			player.sendMessage(ChatUtils.error("§e" + args[1] + " §cdoesn't exist"));
			return;
		}

		if (args.length < 3) {
			editPrestige(Bedwars.getInstance().getUser(player), prestige);
			return;
		}

		OfflinePlayer userPlayer = Bukkit.getOfflinePlayer(args[2]);
		if (!userPlayer.hasPlayedBefore()) {
			player.sendMessage(ChatUtils.error("Player not found!"));
			return;
		}

		if (userPlayer.isOnline()) {
			editPrestige(Bedwars.getInstance().getUser(userPlayer.getPlayer()), prestige);
		} else {
			UserData data = new UserData(userPlayer);
			data.setPrestige(prestige);

			Bukkit.getScheduler().runTaskAsynchronously(Bedwars.getInstance(), () -> data.saveData());
		}

		player.sendMessage(ChatUtils.success(userPlayer.getName() + "'s prestige has been set to §e" + prestige.getName() + "§a!"));
	}

	private void editPrestige(User user, Prestige prestige) {
		user.setPrestige(prestige);
		user.getPlayer().sendMessage(ChatUtils.success("Your prestige has been changed to §e" + prestige.getName() + "§a!"));
	}

}