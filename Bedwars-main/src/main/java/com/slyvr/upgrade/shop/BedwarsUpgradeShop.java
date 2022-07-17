package com.slyvr.upgrade.shop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import com.cryptomorin.xseries.XMaterial;
import com.slyvr.api.game.player.GamePlayer;
import com.slyvr.api.shop.item.Buyable;
import com.slyvr.api.shop.item.ItemDescription;
import com.slyvr.api.team.GameTeam;
import com.slyvr.api.trap.Trap;
import com.slyvr.api.upgrade.shop.UpgradeShop;
import com.slyvr.api.upgrade.shop.item.TrapItem;
import com.slyvr.manager.ItemManager;

public class BedwarsUpgradeShop implements UpgradeShop {

	private static final ItemStack[] EMPTY_TRAPS = new ItemStack[3];
	private static final ItemStack SEPARATOR;

	static {

		ItemManager empty = new ItemManager(XMaterial.LIGHT_GRAY_STAINED_GLASS.parseItem());

		List<String> lore = new ArrayList<>();
		lore.add("§7The first enemy to walk into");
		lore.add("§7your base will trigger this");
		lore.add("§7trap!");
		lore.add("");
		lore.add("§7Purchasing a trap will queue it");
		lore.add("§7here. Its cost will scale based");
		lore.add("§7on the number of traps queued.");
		lore.add("");

		empty.setLore(lore);
		EMPTY_TRAPS[0] = empty.getItem().clone();

		lore.set(0, "§7The second enemy to walk into");
		EMPTY_TRAPS[1] = empty.setAmount(2).getItem().clone();

		lore.set(0, "§7The third enemy to walk into");
		EMPTY_TRAPS[2] = empty.setAmount(3).getItem().clone();

		ItemManager separator = new ItemManager(XMaterial.GRAY_STAINED_GLASS_PANE.parseItem());
		separator.setName("§8⬆ §7Purchasable");
		separator.addToLore("§8⬇ §7Traps Queue");

		SEPARATOR = separator.getItem();
	}

	private Map<Integer, Buyable> items = new HashMap<>();

	public BedwarsUpgradeShop() {
	}

	@Override
	public Map<Integer, Buyable> getItems() {
		return new HashMap<>(this.items);
	}

	@Override
	public Buyable getItem(int slot) {
		return this.items.get(slot);
	}

	@Override
	public boolean addItem(int slot, Buyable item) {
		if (item == null || slot >= 27 && slot <= 35)
			return false;

		this.items.put(slot, item);
		return true;
	}

	@Override
	public Buyable removeItem(int slot) {
		return this.items.remove(slot);
	}

	@Override
	public boolean openShop(GamePlayer gp) {
		if (gp == null)
			return false;

		GameTeam team = gp.getGame().getGameTeam(gp.getTeam());
		if (team == null)
			return false;

		return gp.getPlayer().openInventory(apply(gp, team, Bukkit.createInventory(null, 54, "Upgrades & Traps"))) != null;
	}

	@Override
	public boolean contains(Buyable buyable) {
		return this.items.containsValue(buyable);
	}

	private Inventory apply(GamePlayer gp, GameTeam team, Inventory inv) {

		List<Trap> traps = team.getTrapManager().getTraps();

		int trapCount = 0;
		for (Entry<Integer, Buyable> entry : this.items.entrySet()) {
			Buyable buyable = entry.getValue();

			ItemStack display = buyable.getDisplayItem(gp);
			inv.setItem(entry.getKey(), display);

			if (trapCount >= 3 || !(buyable instanceof TrapItem))
				continue;

			TrapItem trapItem = (TrapItem) buyable;

			Trap trap = trapItem.getTrap();

			for (int i = 0; i < traps.size() && i < 3; i++) {
				Trap trapsTrap = traps.get(i);

				if (!trapsTrap.equals(trap))
					continue;

				inv.setItem(39 + trapCount++, getTrapDisplayItem(display, trapItem.getDescription()));
			}
		}

		for (int i = 27; i <= 35; i++)
			inv.setItem(i, SEPARATOR);

		while (trapCount < 3)
			inv.setItem(39 + trapCount, getEmptyTrapDisplayItem(trapCount++));

		return inv;
	}

	private ItemStack getEmptyTrapDisplayItem(int index) {
		ItemManager manager = new ItemManager(EMPTY_TRAPS[index].clone());
		manager.setName("§cTrap #" + (index + 1) + ": No Trap!");

		manager.addToLore("§7Next trap: §b" + (int) Math.pow(2, index) + " Diamond");

		return manager.getItem();
	}

	private ItemStack getTrapDisplayItem(ItemStack item, ItemDescription desc) {
		ItemManager manager = new ItemManager(item);
		manager.addItemFlags(ItemFlag.values());
		manager.setName(ChatColor.GREEN + manager.getName());

		List<String> lore = new ArrayList<>(desc.getSize());
		desc.apply(lore);

		manager.setLore(lore);
		return manager.getItem();
	}

}