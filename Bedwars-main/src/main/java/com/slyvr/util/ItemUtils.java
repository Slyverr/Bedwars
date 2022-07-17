package com.slyvr.util;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemUtils {

	public static ItemStack getItem(Material type, int amount, short data, String name, String... desc) {
		ItemStack result = new ItemStack(type, amount, data);
		ItemMeta meta = result.getItemMeta();

		if (name != null)
			meta.setDisplayName(ChatUtils.format(name));

		if (desc != null) {
			List<String> lore = new ArrayList<>();

			for (String line : desc)
				lore.add(ChatUtils.format(line));

			meta.setLore(lore);
		}

		result.setItemMeta(meta);

		return result;
	}

	public static ItemStack getItem(Material type, int amount, short data, String name) {
		return getItem(type, amount, data, name, (String[]) null);
	}

	public static ItemStack getItem(Material type, int amount, short data) {
		return getItem(type, amount, data, null, (String[]) null);
	}

	public static ItemStack getItem(Material type, int amount) {
		return getItem(type, amount, (short) 0, null, (String[]) null);
	}

	public static ItemStack getItem(Material type) {
		return getItem(type, 1, (short) 0, null, (String[]) null);
	}

}