package com.slyvr.player;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import org.bukkit.inventory.ItemStack;

import com.slyvr.api.game.player.GameInventory;
import com.slyvr.api.shop.item.TieredItemStack;

public class BedwarsInventory implements GameInventory {

	private Set<ItemStack> stacks = new HashSet<>();
	private Set<TieredItemStack> tiers = new HashSet<>();

	public BedwarsInventory() {
	}

	@Override
	public Set<ItemStack> getPermanentItems() {
		return this.stacks;
	}

	@Override
	public boolean addItem(ItemStack item) {
		return item != null ? this.stacks.add(item) : false;
	}

	@Override
	public boolean removeItem(ItemStack item) {
		return item != null ? this.stacks.remove(item) : false;
	}

	@Override
	public Set<TieredItemStack> getTieredItems() {
		return new HashSet<>(this.tiers);
	}

	@Override
	public boolean addTieredItem(TieredItemStack item) {
		return item != null ? this.tiers.add(item) : false;
	}

	@Override
	public boolean removeTieredItem(TieredItemStack item) {
		return item != null ? this.tiers.remove(item) : false;
	}

	@Override
	public boolean contains(ItemStack item) {
		if (item == null)
			return false;

		return this.stacks.contains(item) || tiersContain(item);
	}

	@Override
	public boolean contains(ItemStack item, Predicate<ItemStack> predicate) {
		if (item == null)
			return false;

		for (ItemStack itemstack : this.stacks)
			if (predicate.test(itemstack))
				return true;

		return tiersContain(item);
	}

	private boolean tiersContain(ItemStack item) {
		for (TieredItemStack tiered : this.tiers)
			if (tiered.contains(item))
				return true;

		return false;
	}

	@Override
	public boolean contains(TieredItemStack item) {
		return item != null ? this.tiers.contains(item) : false;
	}

	@Override
	public void clear() {
		this.stacks.clear();
		this.tiers.clear();
	}

}