package com.slyvr.upgrade.shop.item;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import com.cryptomorin.xseries.XSound;
import com.slyvr.api.event.trap.TrapBuyEvent;
import com.slyvr.api.game.player.GamePlayer;
import com.slyvr.api.generator.Resource;
import com.slyvr.api.shop.item.ItemDescription;
import com.slyvr.api.team.GameTeam;
import com.slyvr.api.trap.Trap;
import com.slyvr.api.trap.TrapManager;
import com.slyvr.api.upgrade.shop.item.TrapItem;
import com.slyvr.manager.ItemManager;
import com.slyvr.util.ShopUtils;

public class TrapShopItem implements TrapItem {

	private String name;
	private ItemStack display;
	private ItemDescription desc;

	private Trap trap;

	public TrapShopItem(String name, ItemStack display, ItemDescription desc, Trap trap) {

		this.name = name;
		this.desc = desc;
		this.trap = trap;

		initDisplayItem(display);
	}

	// copycopycopycopy
	private void initDisplayItem(ItemStack item) {
		ItemManager manager = new ItemManager(item.getType(), item.getAmount(), item.getDurability());
		manager.setName(ChatColor.RESET + this.name);

		List<String> lore = new ArrayList<>();

		if (!this.desc.isEmpty()) {
			this.desc.apply(lore);
			lore.add(null);
		}

		manager.addItemFlags(ItemFlag.values());
		manager.setLore(lore);

		this.display = manager.getItem();
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public Trap getTrap() {
		return this.trap;
	}

	@Override
	public ItemDescription getDescription() {
		return this.desc.clone();
	}

	@Override
	public ItemStack getDisplayItem(GamePlayer gp) {
		if (gp == null)
			return null;

		GameTeam team = gp.getGame().getGameTeam(gp.getTeam());
		if (team == null)
			return null;

		TrapManager trapManager = team.getTrapManager();
		List<Trap> traps = trapManager.getTraps();

		int price = (int) Math.pow(2, !traps.isEmpty() ? traps.size() - 1 : 0);

		ItemManager itemManager = new ItemManager(this.display.clone());
		itemManager.addToLore("§7Cost: " + ShopUtils.formatCost(Resource.DIAMOND, price));
		itemManager.addToLore(null);

		if (traps.contains(this.trap))
			itemManager.addEnchantment(Enchantment.WATER_WORKER, 1, true);

		if (traps.size() >= 3)
			itemManager.addToLore("§cYou reached traps limit!");
		else {
			int needed = ShopUtils.getAmountNeeded(gp.getPlayer(), Resource.DIAMOND, price);

			if (needed > 0) {
				itemManager.addToLore("§cYou don't have enough " + ShopUtils.formatResource(Resource.DIAMOND, price) + "!");
				itemManager.setName(ChatColor.RED + this.name);
			} else {
				itemManager.addToLore("§eClick to purchase!e");
				itemManager.setName(ChatColor.GREEN + this.name);
			}
		}

		return itemManager.getItem();
	}

	@Override
	public boolean onBuy(GamePlayer gp) {
		if (gp == null)
			return false;

		GameTeam team = gp.getGame().getGameTeam(gp.getTeam());
		if (team == null)
			return false;

		Player player = gp.getPlayer();

		TrapManager trapManager = team.getTrapManager();

		List<Trap> traps = trapManager.getTraps();
		if (traps.size() >= 3) {
			player.sendMessage("§cYou reached traps limit!");
			return false;
		}

		TrapBuyEvent event = new TrapBuyEvent(this, gp, "§aYou purchased §6" + this.name + "§a!");
		Bukkit.getPluginManager().callEvent(event);

		if (event.isCancelled())
			return false;

		int price = (int) Math.pow(2, !traps.isEmpty() ? traps.size() - 1 : 0);

		int needed = ShopUtils.getAmountNeeded(player, Resource.DIAMOND, price);
		if (needed > 0) {
			player.sendMessage("§cYou don't have enough Diamond! Need " + needed + " more!");
			return false;
		}

		if (!ShopUtils.removeCost(player, Resource.DIAMOND, price))
			return false;

		trapManager.addTrap(this.trap);
		player.sendMessage(event.getBuyMessage());
		XSound.BLOCK_NOTE_BLOCK_PLING.play(player.getLocation(), 1F, 2F);
		return true;
	}

}
