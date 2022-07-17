package com.slyvr.util;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InventoryUtils {

	public static int getAmount(Inventory inv, Material type) {
		if (inv == null || type == null || type == Material.AIR)
			return 0;

		int result = 0;
		for (ItemStack item : inv.getContents())
			if (item != null && item.getType() == type)
				result += item.getAmount();

		return result;
	}

	/**
	 * Removes the amount given of the items containing the same type
	 *
	 * @param inv    inventory to remove from
	 * @param type   type to remove
	 * @param amount the amount to remove
	 *
	 * @return {@code true} if the item has been removed and the rest is 0
	 */
	public static boolean removeItem(Inventory inv, Material type, int amount) {
		if (inv == null || type == null)
			return false;

		if (type == Material.AIR || amount <= 0)
			return true;

		int rest = amount;

		ItemStack[] items = inv.getContents();
		for (int i = 0; i < items.length; i++) {
			ItemStack item = items[i];

			if (item != null && item.getType() == type)
				if (rest >= item.getAmount()) {
					rest -= item.getAmount();
					inv.setItem(i, null);
				} else {
					item.setAmount(item.getAmount() - rest);
					rest = 0;
				}

			if (rest == 0)
				return true;
		}

		return rest == 0;
	}

	public static boolean canAddItem(Inventory inv, ItemStack item) {
		return item != null && canAddItem(inv, item.getType(), item.getAmount());
	}

	public static boolean canAddItem(Inventory inv, Material type, int amount) {
		if (inv == null || type == null || amount <= 0)
			return false;

		ItemStack[] content = inv.getContents();
		for (ItemStack item : content)
			if (item == null || item.getType() == type && item.getAmount() + amount <= inv.getMaxStackSize())
				return true;

		return false;
	}

}