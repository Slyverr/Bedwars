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
import com.google.common.base.Preconditions;
import com.slyvr.api.event.player.GamePlayerUpgradeBuyEvent;
import com.slyvr.api.game.player.GamePlayer;
import com.slyvr.api.shop.item.ItemCost;
import com.slyvr.api.shop.item.ItemDescription;
import com.slyvr.api.team.GameTeam;
import com.slyvr.api.upgrade.Upgrade;
import com.slyvr.api.upgrade.UpgradeManager;
import com.slyvr.api.upgrade.shop.item.UpgradeItem;
import com.slyvr.manager.ItemManager;
import com.slyvr.util.ShopUtils;

public class UpgradeShopItem implements UpgradeItem {

	private ItemStack unbuyable;
	private ItemStack buyable;
	private ItemStack unlocked;

	private String name;
	private ItemCost cost;
	private ItemDescription desc;

	private Upgrade upgrade;

	public UpgradeShopItem(String name, ItemStack display, ItemCost cost, ItemDescription desc, Upgrade upgrade) {
		Preconditions.checkNotNull(display, "Display item cannot be null!");

		this.name = name;
		this.cost = cost;
		this.desc = desc;
		this.upgrade = upgrade;

		initDisplayItem(display);
	}

	// Copy ItemShop ?
	private void initDisplayItem(ItemStack item) {
		ItemManager manager = new ItemManager(item.clone());
		manager.addItemFlags(ItemFlag.values());

		List<String> lore = new ArrayList<>();
		if (!this.desc.isEmpty()) {
			this.desc.apply(lore);
			lore.add(null);
		}

		lore.add("§7Cost: " + ShopUtils.formatCost(this.cost));
		lore.add(null);

		manager.setLore(lore);

		lore.add("§cYou don't have enough " + ShopUtils.formatResource(this.cost) + "!");
		this.unbuyable = manager.setName(ChatColor.RED + this.name).getItem().clone();

		lore.set(lore.size() - 1, "§eClick to purchase!");
		this.buyable = manager.setName(ChatColor.GREEN + this.name).getItem().clone();

		lore.set(lore.size() - 1, "§aUNLOCKED!");
		this.unlocked = manager.addEnchantment(Enchantment.WATER_WORKER, 1, false).getItem().clone();
	}

	@Override
	public ItemStack getDisplayItem(GamePlayer gp) {
		if (gp == null)
			return null;

		GameTeam team = gp.getGame().getGameTeam(gp.getTeam());
		if (team == null)
			return null;

		if (team.getUpgradeManager().contains(this.upgrade))
			return this.unlocked.clone();

		if (ShopUtils.hasEnough(gp.getPlayer(), this.cost))
			return this.buyable.clone();
		return this.unbuyable.clone();

	}

	@Override
	public boolean onBuy(GamePlayer gp) {
		if (gp == null)
			return false;

		GameTeam team = gp.getGame().getGameTeam(gp.getTeam());
		if (team == null)
			return false;

		Player player = gp.getPlayer();

		UpgradeManager upgradeManager = team.getUpgradeManager();

		if (upgradeManager.contains(this.upgrade)) {
			player.sendMessage("§cYou already purchased this upgrade!");
			return false;
		}

		GamePlayerUpgradeBuyEvent event = new GamePlayerUpgradeBuyEvent(gp, this, "§aYou purchased §6" + this.name + "§a!");
		Bukkit.getPluginManager().callEvent(event);

		if (event.isCancelled())
			return false;

		int amount = ShopUtils.getAmountNeeded(player, this.cost);
		if (amount != 0) {
			player.sendMessage("§cYou don't have enough " + this.cost.getResource() + "! Need " + amount + " more!");
			return false;
		}

		if (!ShopUtils.removeCost(player, this.cost))
			return false;

		upgradeManager.add(this.upgrade);

		player.sendMessage(event.getBuyMessage());
		XSound.BLOCK_NOTE_BLOCK_PLING.play(player.getLocation(), 1F, 2F);
		return true;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public Upgrade getUpgrade() {
		return this.upgrade;
	}

	@Override
	public ItemCost getCost() {
		return this.cost.clone();
	}

	@Override
	public ItemDescription getDescription() {
		return this.desc.clone();
	}

}
