package com.slyvr.quickbuy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import com.cryptomorin.xseries.XMaterial;
import com.slyvr.api.game.player.GamePlayer;
import com.slyvr.api.shop.item.Buyable;
import com.slyvr.manager.ItemManager;
import com.slyvr.shop.AbstractShopCategory;
import com.slyvr.util.ShopUtils;

public class QuickBuy extends AbstractShopCategory implements com.slyvr.api.shop.QuickBuy {

	private static final ItemStack DEFAULT_DISPLAY_ITEM;
	private static final ItemStack EMPTY_SLOT;

	static {
		ItemManager display = new ItemManager(Material.NETHER_STAR);
		display.addItemFlags(ItemFlag.values());
		display.setName("§bQuick Buy");

		DEFAULT_DISPLAY_ITEM = display.getItem();

		ItemManager manager = new ItemManager(XMaterial.RED_STAINED_GLASS_PANE.parseItem());
		manager.addItemFlags(ItemFlag.values());
		manager.setName("§cEmpty Slot!");

		List<String> lore = new ArrayList<>(4);
		lore.add("§7This is a Quick Buy Slot!");
		lore.add("§bSneak Click §7any item in the");
		lore.add("§7shop to add it here.");

		manager.setLore(lore);

		EMPTY_SLOT = manager.getItem();
	}

	public QuickBuy(ItemStack display, Map<Integer, Buyable> items) {
		super("Quick Buy", display != null ? display : DEFAULT_DISPLAY_ITEM, items);

		initDisplayItem();
	}

	public QuickBuy(ItemStack display) {
		this(display, null);
	}

	public QuickBuy() {
		this(null);
	}

	private void initDisplayItem() {
		if (this.display == DEFAULT_DISPLAY_ITEM)
			return;

		ItemManager manager = new ItemManager(this.display);
		manager.addItemFlags(ItemFlag.values());
		manager.setName(ChatColor.AQUA + this.name);

		this.display = manager.getItem();
	}

	@Override
	public void applyItems(Inventory inv, GamePlayer gp) {
		if (inv == null || gp == null)
			return;

		for (int i = 0; i < 21; i++) {
			int slot = ShopUtils.getValidIndex(i);

			Buyable buyable = this.items.get(slot);
			if (buyable != null)
				inv.setItem(slot, buyable.getDisplayItem(gp));
			else
				inv.setItem(slot, EMPTY_SLOT);

		}

	}

	// @Override
	// public GamePlayer getOwner() {
	// // TODO Auto-generated method stub
	// return null;
	// }

	@Override
	public String toString() {
		return "QuickBuy [" + this.items + "]";
	}

}