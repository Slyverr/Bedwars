package com.slyvr.shop;

import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import com.slyvr.api.game.player.GamePlayer;
import com.slyvr.api.shop.item.Buyable;
import com.slyvr.manager.ItemManager;
import com.slyvr.util.ShopUtils;

public class ShopCategory extends AbstractShopCategory {

	public ShopCategory(String name, ItemStack item, Map<Integer, Buyable> items) {
		super(name, item, items);

		initDisplayItem(item);
		setItems(items);
	}

	public ShopCategory(String name, ItemStack item) {
		this(name, item, null);
	}

	private void initDisplayItem(ItemStack item) {
		ItemManager manager = new ItemManager(item);
		manager.addItemFlags(ItemFlag.values());

		manager.setName(ChatColor.GREEN + this.name);
		manager.addToLore("§eClick for more!");

		this.display = manager.getItem();
	}

	@Override
	public void applyItems(Inventory inv, GamePlayer gp) {
		if (inv == null || gp == null)
			return;

		for (Entry<Integer, Buyable> entry : this.items.entrySet()) {
			Buyable buyable = entry.getValue();
			if (buyable == null)
				continue;

			int slot = entry.getKey();

			if (ShopUtils.isValidIndex(slot))
				inv.setItem(slot, buyable.getDisplayItem(gp));
			else
				inv.setItem(ShopUtils.getValidIndex(slot), buyable.getDisplayItem(gp));
		}

	}

	@Override
	public String toString() {
		return "ShopCategory [Name=" + getName() + ", Items=" + this.items + "]";
	}

}