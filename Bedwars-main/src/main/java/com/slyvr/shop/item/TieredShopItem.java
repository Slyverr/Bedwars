package com.slyvr.shop.item;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.cryptomorin.xseries.XSound;
import com.google.common.base.Preconditions;
import com.slyvr.api.event.player.GamePlayerItemBuyEvent;
import com.slyvr.api.game.player.GameInventory;
import com.slyvr.api.game.player.GamePlayer;
import com.slyvr.api.shop.item.Item;
import com.slyvr.api.shop.item.TieredItem;
import com.slyvr.api.shop.item.TieredItemStack;
import com.slyvr.manager.ItemManager;
import com.slyvr.util.ShopUtils;

public class TieredShopItem implements TieredItem {

	private List<ShopItem> tiers;

	public TieredShopItem(List<ShopItem> items) {
		Preconditions.checkNotNull(items, "Tiers cannot be null");

		List<ShopItem> result = new ArrayList<>(items.size());
		for (ShopItem item : items) {
			if (item != null)
				result.add(item);
		}

		if (result.isEmpty())
			throw new IllegalArgumentException("Tiers cannot be empty!");

		this.tiers = result;
	}

	@Override
	public List<Item> getTiers() {
		return new ArrayList<>(this.tiers);
	}

	@Override
	public Item getTier(int tier) {
		return tier >= 1 && tier <= this.tiers.size() ? this.tiers.get(tier - 1) : null;
	}

	@Override
	public TieredItemStack getPlayerTier(GamePlayer gp) {
		if (gp == null)
			return null;

		TieredItemStack item = createTieredItemStack(gp);

		GameInventory inv = gp.getInventory();
		for (TieredItemStack tiered : inv.getTieredItems()) {
			if (tiered.equals(item))
				return tiered;
		}

		return item;
	}

	private TieredItemStack createTieredItemStack(GamePlayer gp) {
		List<ItemStack> items = new ArrayList<>(this.tiers.size());
		for (ShopItem shopItem : this.tiers) {
			ItemStack item = shopItem.getRawItem(gp);

			if (item != null)
				items.add(item);
		}

		return new TieredItemStack(items);
	}

	@Override
	public ItemStack getDisplayItem(GamePlayer gp) {
		if (gp == null)
			return null;

		TieredItemStack tier = getPlayerTier(gp);

		ItemManager manager = new ItemManager(this.tiers.get(tier.getNextTier() - 1).getDisplayItem(gp));

		List<String> lore = manager.getLore();

		lore.add(1, "§7Tier: §e" + tier.getNextTier());
		if (tier.getCurrentTier() == tier.getMaximumTier())
			lore.set(lore.size() - 1, "§cYou already have the maximum tier!");

		return manager.setLore(lore).getItem();
	}

	@Override
	public boolean onBuy(GamePlayer gp) {
		if (gp == null)
			return false;

		Player player = gp.getPlayer();

		TieredItemStack item = getPlayerTier(gp);
		if (item.getCurrentTier() == item.getMaximumTier()) {
			player.sendMessage("§cYou already have the highest tier!");
			return false;
		}

		ShopItem next = this.tiers.get(item.getNextTier() - 1);

		GamePlayerItemBuyEvent event = new GamePlayerItemBuyEvent(gp, this, "§aYou purchased §6" + next.getName());
		Bukkit.getPluginManager().callEvent(event);

		if (event.isCancelled() || !ShopUtils.buyItem(gp, next))
			return false;

		item.setCurrentTier(item.getNextTier());
		gp.getInventory().addTieredItem(item);

		if (item.hasPrevious())
			player.getInventory().removeItem(item.previous());

		player.sendMessage(event.getBuyMessage());
		XSound.BLOCK_NOTE_BLOCK_PLING.play(player.getLocation(), 100F, 2F);
		return true;
	}

	@Override
	public boolean contains(Item item) {
		return item != null && this.tiers.contains(item);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.tiers);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (!(obj instanceof TieredShopItem))
			return false;

		TieredShopItem other = (TieredShopItem) obj;
		return Objects.equals(this.tiers, other.tiers);
	}

}