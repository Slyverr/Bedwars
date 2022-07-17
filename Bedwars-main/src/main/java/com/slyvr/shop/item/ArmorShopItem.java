package com.slyvr.shop.item;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.cryptomorin.xseries.XSound;
import com.google.common.base.Preconditions;
import com.slyvr.api.event.player.GamePlayerItemBuyEvent;
import com.slyvr.api.game.player.ArmorType;
import com.slyvr.api.game.player.GamePlayer;
import com.slyvr.api.shop.item.ItemCost;
import com.slyvr.api.shop.item.ItemDescription;
import com.slyvr.manager.ItemManager;
import com.slyvr.util.ShopUtils;
import com.slyvr.util.TeamUtils;

public class ArmorShopItem extends AbstractShopItem {

	private ItemStack unbuyable;
	private ItemStack buyable;
	private ItemStack unlocked;
	private ItemStack high_tier;

	private ArmorType type;

	public ArmorShopItem(String name, ItemStack display, ArmorType type, ItemCost cost, ItemDescription desc) {
		super(name, cost, desc);

		Preconditions.checkNotNull(type, "Armor type cannot be null!");

		this.type = type;

		if (name == null)
			this.name = "Permanent " + type + " Armor";

		initDisplayItem(display);
	}

	private void initDisplayItem(ItemStack item) {
		ItemManager manager = new ItemManager(item != null ? item : new ItemStack(this.type.getBoots()));

		ItemStack[] display_items = ShopUtils.toShopDisplayItems(manager, this.name, this.cost, this.desc);

		this.unbuyable = display_items[0];
		this.buyable = display_items[1];

		List<String> lore = manager.getLore();

		lore.set(lore.size() - 1, "§aUNLOCKED!");
		this.unlocked = manager.setName(ChatColor.GREEN + this.name).getItem().clone();

		lore.set(lore.size() - 1, "§cYou already have a higher tier item.");
		this.high_tier = manager.setName(ChatColor.GREEN + this.name).getItem().clone();

	}

	@Override
	public ItemStack getRawItem(GamePlayer gp) {
		return null;
	}

	@Override
	public ItemStack getDisplayItem(GamePlayer gp) {
		if (gp == null)
			return null;

		ArmorType armor = gp.getArmorType();

		if (armor != null) {
			if (armor == this.type)
				return this.unlocked.clone();

			if (armor.ordinal() > this.type.ordinal())
				return this.high_tier.clone();
		}

		return ShopUtils.hasEnough(gp.getPlayer(), this.cost) ? this.buyable.clone() : this.unbuyable.clone();
	}

	public ShopItemType getShopItemType() {
		return ShopItemType.ARMOR;
	}

	public ArmorType getArmorType() {
		return this.type;
	}

	@Override
	public boolean onBuy(GamePlayer gp) {
		if (gp == null)
			return false;

		Player player = gp.getPlayer();

		ArmorType armor = gp.getArmorType();
		if (armor != null) {
			if (armor == this.type) {
				player.sendMessage("§cYou already have this armor equipped!");
				return false;
			}

			if (armor.ordinal() > this.type.ordinal()) {
				player.sendMessage("§cYou already have a higher tier armor");
				return false;
			}
		}

		int needed = ShopUtils.getAmountNeeded(player, this.cost);
		if (needed > 0) {
			player.sendMessage("§cYou don't have enough " + ShopUtils.formatResource(this.cost) + "! Need " + needed + " more!");
			return false;
		}

		GamePlayerItemBuyEvent event = new GamePlayerItemBuyEvent(gp, this, "§aYou purchased §6" + this.name);
		Bukkit.getPluginManager().callEvent(event);

		if (event.isCancelled() || !ShopUtils.removeCost(player, this.cost))
			return false;

		TeamUtils.setPlayerArmor(player, gp.getTeam(), type);
		gp.setArmorType(type);

		player.sendMessage(event.getBuyMessage());
		XSound.BLOCK_NOTE_BLOCK_PLING.play(player.getLocation(), 1F, 2F);
		return true;
	}

}