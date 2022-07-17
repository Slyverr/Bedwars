package com.slyvr.util;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.slyvr.bedwars.Bedwars;

public class ChatUtils {

	public static String usage(String usage) {
		return Bedwars.getInstance().getPluginPrefix() + "§eUsage: §c" + usage;
	}

	public static String info(String info) {
		return Bedwars.getInstance().getPluginPrefix() + ChatColor.YELLOW + info;
	}

	public static String success(String success) {
		return Bedwars.getInstance().getPluginPrefix() + ChatColor.GREEN + success;
	}

	public static String error(String error) {
		return Bedwars.getInstance().getPluginPrefix() + ChatColor.RED + error;
	}

	public static String bold(ChatColor color) {
		return color.toString() + ChatColor.BOLD;
	}

	public static String obfuscated(ChatColor color) {
		return color.toString() + ChatColor.MAGIC;
	}

	public static void sendMessage(Player player, String msg) {
		if (msg != null)
			player.sendMessage(format(msg));
	}

	public static String format(String msg) {
		return msg != null ? ChatColor.translateAlternateColorCodes('&', msg) : null;
	}

	public static String reset(String msg) {
		return ChatColor.RESET + ChatColor.stripColor(msg);
	}

}