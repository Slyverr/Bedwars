package com.slyvr.util;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import com.cryptomorin.xseries.XMaterial;
import com.slyvr.api.game.player.ArmorType;
import com.slyvr.api.team.Team;
import com.slyvr.manager.ItemManager;

public class TeamUtils {

	public static XMaterial getTeamColoredWool(Team team) {
		switch (team) {
			case RED:
				return XMaterial.RED_WOOL;
			case BLUE:
				return XMaterial.BLUE_WOOL;
			case GREEN:
				return XMaterial.LIME_WOOL;
			case YELLOW:
				return XMaterial.YELLOW_WOOL;
			case AQUA:
				return XMaterial.CYAN_WOOL;
			case WHITE:
				return XMaterial.WHITE_WOOL;
			case PINK:
				return XMaterial.PINK_WOOL;
			case GRAY:
				return XMaterial.GRAY_WOOL;
		}

		return null;
	}

	public static XMaterial getTeamColoredGlass(Team team) {

		switch (team) {
			case RED:
				return XMaterial.RED_STAINED_GLASS;
			case BLUE:
				return XMaterial.BLUE_STAINED_GLASS;
			case GREEN:
				return XMaterial.LIME_STAINED_GLASS;
			case YELLOW:
				return XMaterial.YELLOW_STAINED_GLASS;
			case AQUA:
				return XMaterial.CYAN_STAINED_GLASS;
			case WHITE:
				return XMaterial.WHITE_STAINED_GLASS;
			case PINK:
				return XMaterial.PINK_STAINED_GLASS;
			case GRAY:
				return XMaterial.GRAY_STAINED_GLASS;
		}

		return null;
	}

	public static XMaterial getTeamColoredGlassPane(Team team) {

		switch (team) {
			case RED:
				return XMaterial.RED_STAINED_GLASS_PANE;
			case BLUE:
				return XMaterial.BLUE_STAINED_GLASS_PANE;
			case GREEN:
				return XMaterial.LIME_STAINED_GLASS_PANE;
			case YELLOW:
				return XMaterial.YELLOW_STAINED_GLASS_PANE;
			case AQUA:
				return XMaterial.CYAN_STAINED_GLASS_PANE;
			case WHITE:
				return XMaterial.WHITE_STAINED_GLASS_PANE;
			case PINK:
				return XMaterial.PINK_STAINED_GLASS_PANE;
			case GRAY:
				return XMaterial.GRAY_STAINED_GLASS_PANE;
		}

		return null;
	}

	public static XMaterial getTeamColoredTerracotta(Team team) {
		switch (team) {
			case RED:
				return XMaterial.RED_TERRACOTTA;
			case BLUE:
				return XMaterial.BLUE_TERRACOTTA;
			case GREEN:
				return XMaterial.LIME_TERRACOTTA;
			case YELLOW:
				return XMaterial.YELLOW_TERRACOTTA;
			case AQUA:
				return XMaterial.CYAN_TERRACOTTA;
			case WHITE:
				return XMaterial.WHITE_TERRACOTTA;
			case PINK:
				return XMaterial.PINK_TERRACOTTA;
			case GRAY:
				return XMaterial.GRAY_TERRACOTTA;
		}

		return null;
	}

	public static void setPlayerArmor(Player player, Team team, ArmorType type) {
		ItemStack helmet = new ItemStack(Material.LEATHER_HELMET);
		ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
		ItemStack leggings = new ItemStack(type.getLeggings());
		ItemStack boots = new ItemStack(type.getBoots());

		Color color = team.getDyeColor().getColor();

		helmet.addEnchantment(Enchantment.WATER_WORKER, 1);
		checkPiece(helmet, color);
		checkPiece(chestplate, color);
		checkPiece(leggings, color);
		checkPiece(boots, color);

		EntityEquipment equipment = player.getEquipment();

		equipment.setHelmet(helmet);
		equipment.setChestplate(chestplate);
		equipment.setLeggings(leggings);
		equipment.setBoots(boots);
	}

	private static void checkPiece(ItemStack item, Color color) {
		ItemManager manager = new ItemManager(item);
		manager.setUnbreakable(true);

		ItemMeta meta = manager.getItemMeta();
		if (meta instanceof LeatherArmorMeta) {
			LeatherArmorMeta leatherMeta = (LeatherArmorMeta) meta;
			leatherMeta.setColor(color);
		}

		item.setItemMeta(meta);
	}

}