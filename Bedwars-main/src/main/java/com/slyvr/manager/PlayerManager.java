package com.slyvr.manager;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

public class PlayerManager {

	public static void clearInventory(Player player) {
		player.getInventory().clear();
	}

	public static void clearEquipment(Player player) {
		player.getEquipment().setArmorContents(null);
	}

	public static void clearPotionEffects(Player player) {
		for (PotionEffect effect : player.getActivePotionEffects())
			player.removePotionEffect(effect.getType());
	}

	public static void clear(Player player) {
		clearPotionEffects(player);
		clearInventory(player);
		clearEquipment(player);
	}

	public static void resetScoreboard(Player player) {
		player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
	}

	public static void resetHealth(Player player) {
		player.setHealth(player.getMaxHealth());
	}

	public static void resetFoodLevel(Player player) {
		player.setFoodLevel(20);
	}

	public static void resetLevel(Player player) {
		player.setLevel(0);
		player.setExp(0);
	}

	public static void resetTime(Player player) {
		player.resetPlayerTime();
	}

	public static void setFlying(Player player, boolean fly) {
		GameMode mode = player.getGameMode();

		player.setAllowFlight(mode == GameMode.CREATIVE || mode == GameMode.SPECTATOR || fly);
		player.setFlying(fly);
	}

}