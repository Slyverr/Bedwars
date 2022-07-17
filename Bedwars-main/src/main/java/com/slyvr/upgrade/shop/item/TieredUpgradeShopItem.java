package com.slyvr.upgrade.shop.item;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import com.cryptomorin.xseries.XSound;
import com.slyvr.api.game.player.GamePlayer;
import com.slyvr.api.shop.item.ItemCost;
import com.slyvr.api.shop.item.ItemDescription;
import com.slyvr.api.team.GameTeam;
import com.slyvr.api.upgrade.TieredUpgrade;
import com.slyvr.api.upgrade.Upgrade;
import com.slyvr.api.upgrade.UpgradeManager;
import com.slyvr.api.upgrade.shop.item.TieredUpgradeItem;
import com.slyvr.api.upgrade.shop.item.TieredUpgradeItemTier;
import com.slyvr.manager.ItemManager;
import com.slyvr.util.ShopUtils;

public class TieredUpgradeShopItem implements TieredUpgradeItem {

	private List<TieredUpgradeItemTier> tiers;

	private String name;
	private ItemStack display;
	private ItemDescription desc;

	private TieredUpgrade upgrade;

	public TieredUpgradeShopItem(String name, ItemStack display, List<TieredUpgradeItemTier> tiers, ItemDescription desc, TieredUpgrade upgrade) {

		this.name = name;
		this.desc = desc;
		this.tiers = tiers;
		this.upgrade = upgrade;

		initDisplayItem(display);
	}

	private void initDisplayItem(ItemStack display) {
		ItemManager manager = new ItemManager(display);
		manager.setName(this.name);

		List<String> lore = new ArrayList<>();
		this.desc.apply(lore);

		manager.addItemFlags(ItemFlag.values());
		manager.setLore(lore);

		this.display = manager.getItem();
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public TieredUpgrade getUpgrade() {
		return this.upgrade;
	}

	@Override
	public List<TieredUpgradeItemTier> getTiers() {
		return new ArrayList<>(this.tiers);
	}

	@Override
	public TieredUpgradeItemTier getTier(int tier) {
		return tier >= 1 && tier <= this.tiers.size() ? this.tiers.get(tier - 1) : null;
	}

	private String formatCost(String name, int tier, ItemCost cost) {
		StringBuilder builder = new StringBuilder("§7Tier ").append(tier).append(": ").append(name).append(", ").append(ShopUtils.formatCost(cost));

		return builder.toString();
	}

	private String formatUnlocked(String name, int tier) {
		StringBuilder builder = new StringBuilder("§7Tier ").append(tier).append(": ").append(name).append(", ").append("§aUNLOCKED!");

		return builder.toString();
	}

	@Override
	public ItemStack getDisplayItem(GamePlayer gp) {
		if (gp == null)
			return null;

		GameTeam team = gp.getGame().getGameTeam(gp.getTeam());
		if (team == null)
			return null;

		Player player = gp.getPlayer();

		UpgradeManager upgradeManager = team.getUpgradeManager();
		TieredUpgrade upgrade = getTieredUpgrade(upgradeManager);

		int next = upgrade.getNextTier();

		ItemManager itemManager = new ItemManager(this.display.clone());
		itemManager.addToLore(null);

		for (int tier = 1; tier <= this.tiers.size(); tier++) {
			TieredUpgradeItemTier upgradeTier = this.tiers.get(tier - 1);

			if (tier <= upgrade.getCurrentTier())
				itemManager.addToLore(formatUnlocked(upgradeTier.getName(), tier));
			else
				itemManager.addToLore(formatCost(upgradeTier.getName(), tier, upgradeTier.getCost()));
		}

		itemManager.addToLore(null);

		if (upgrade.getCurrentTier() >= 1)
			itemManager.addEnchantment(Enchantment.WATER_WORKER, 1, true);

		if (upgrade.getCurrentTier() == upgrade.getMaximumTier())
			itemManager.addToLore("§cYou already unlocked the highest tier!");
		else
			ShopUtils.setBuyable(player, itemManager, this.tiers.get(next - 1).getCost());

		return itemManager.getItem();
	}

	private TieredUpgrade getTieredUpgrade(UpgradeManager mgr) {
		Upgrade existing = mgr.getUpgrade(this.upgrade.getName());

		if (existing instanceof TieredUpgrade)
			return (TieredUpgrade) existing;

		TieredUpgrade result = this.upgrade.clone();
		result.setCurrentTier(0);
		return result;
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
		TieredUpgrade upgrade = getTieredUpgrade(upgradeManager);

		if (upgrade.getCurrentTier() == upgrade.getMaximumTier()) {
			player.sendMessage("§cYou already unlocked the highest tier!");
			return false;
		}

		int next = upgrade.getNextTier();

		TieredUpgradeItemTier tier = this.tiers.get(next - 1);
		ItemCost cost = tier.getCost();

		int amount = ShopUtils.getAmountNeeded(player, cost);
		if (amount > 0) {
			player.sendMessage("§cYou don't have enough Diamond! Need " + amount + " more!");
			return false;
		}

		if (!ShopUtils.removeCost(player, cost))
			return false;

		upgrade.setCurrentTier(next);
		upgradeManager.add(upgrade);

		player.sendMessage("§aYou purchased §6" + this.name + "§a!");
		XSound.BLOCK_NOTE_BLOCK_PLING.play(player.getLocation(), 1F, 2F);
		return true;
	}

	@Override
	public ItemDescription getDescription() {
		return this.desc;
	}

}
