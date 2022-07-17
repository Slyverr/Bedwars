package com.slyvr.shop;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.bukkit.inventory.ItemStack;

import com.google.common.base.Preconditions;
import com.slyvr.api.shop.Category;
import com.slyvr.api.shop.item.Buyable;
import com.slyvr.util.ShopUtils;

public abstract class AbstractShopCategory implements Category {

	protected Map<Integer, Buyable> items = new HashMap<>();

	protected String name;
	protected ItemStack display;

	protected AbstractShopCategory(String name, ItemStack display, Map<Integer, Buyable> items) {
		Preconditions.checkNotNull(name, "Category name cannot be null");
		Preconditions.checkNotNull(display, "Category display item cannot be null");

		this.name = name;
		this.display = display;

		setItems(items);
	}

	public AbstractShopCategory(String name, ItemStack item) {
		this(name, item, null);
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public ItemStack getDisplayItem() {
		return this.display.clone();
	}

	@Override
	public Map<Integer, Buyable> getItems() {
		return new HashMap<>(this.items);
	}

	@Override
	public void setItems(Map<Integer, Buyable> items) {
		if (items == null)
			return;

		this.items.clear();

		for (Entry<Integer, Buyable> entry : items.entrySet()) {
			Buyable item = entry.getValue();
			int slot = entry.getKey();

			if (item != null)
				this.items.put(ShopUtils.isValidIndex(slot) ? slot : ShopUtils.getValidIndex(slot), item);
		}

	}

	@Override
	public void addItems(Buyable... items) {
		if (items == null)
			return;

		int nextIndex = 0;
		for (int i = 0; i < 21 && nextIndex < items.length; i++) {
			int slot = ShopUtils.getValidIndex(i);

			if (this.items.containsKey(slot))
				continue;

			Buyable item = items[nextIndex];
			if (item == null)
				continue;

			this.items.put(slot, item);
			nextIndex++;
		}

	}

	@Override
	public Buyable getItem(int slot) {
		return slot < 19 ? this.items.get(ShopUtils.getValidIndex(slot)) : this.items.get(slot);
	}

	@Override
	public void setItem(int slot, Buyable item) {
		this.items.put(slot < 21 ? ShopUtils.getValidIndex(slot) : slot, item);
	}

	@Override
	public boolean removeItem(Buyable item) {
		if (item == null)
			return false;

		for (Entry<Integer, Buyable> entry : this.items.entrySet()) {
			Buyable buyable = entry.getValue();

			if (!item.equals(buyable))
				continue;

			this.items.remove(entry.getKey());
			return true;
		}

		return false;
	}

	@Override
	public Buyable removeItem(int slot) {
		return ShopUtils.isValidIndex(slot) ? this.items.remove(slot) : this.items.remove(ShopUtils.getValidIndex(slot));
	}

	@Override
	public void clear() {
		this.items.clear();
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.name.toLowerCase(), this.display, this.items);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (!(obj instanceof AbstractShopCategory))
			return false;

		AbstractShopCategory other = (AbstractShopCategory) obj;
		if (!this.name.equalsIgnoreCase(other.name) || !this.display.equals(other.display) || !this.items.equals(other.items))
			return false;

		return true;
	}
}