package com.slyvr.shop;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.cryptomorin.xseries.XMaterial;
import com.google.common.base.Preconditions;
import com.slyvr.api.event.quickbuy.QuickBuyOpenEvent;
import com.slyvr.api.event.shop.ShopCategoryOpenEvent;
import com.slyvr.api.game.player.GamePlayer;
import com.slyvr.api.shop.Category;
import com.slyvr.api.shop.Shop;
import com.slyvr.api.user.User;
import com.slyvr.bedwars.Bedwars;
import com.slyvr.manager.ItemManager;
import com.slyvr.quickbuy.QuickBuy;

public class BedwarsShop implements Shop {

	private static final ItemStack GREEN_SEPARATOR;
	private static final ItemStack GRAY_SEPARATOR;

	static {
		ItemManager separator = new ItemManager(XMaterial.GRAY_STAINED_GLASS_PANE.parseItem());
		separator.setName("§8⬆ §7Categories");
		separator.addToLore("§8⬇ §7Items");

		GRAY_SEPARATOR = separator.getItem();
		GREEN_SEPARATOR = XMaterial.GREEN_STAINED_GLASS_PANE.setType(GRAY_SEPARATOR.clone());
	}

	private List<Category> categories = new ArrayList<>();

	public BedwarsShop(List<Category> categories) {
		Preconditions.checkNotNull(categories, "Shop categories cannot be null!");

		for (int i = 0; i < categories.size(); i++) {
			Category category = categories.get(i);

			if (category == null)
				throw new IllegalArgumentException("Shop category cannot be null!");

			if (this.categories.contains(category))
				throw new IllegalStateException("Shop cannot have duplicate categories!");

			this.categories.add(category);
		}

	}

	public BedwarsShop() {
		this(new ArrayList<>(9));
	}

	@Override
	public boolean openShop(GamePlayer gp, Category category) {
		return openShop(gp, this.categories.indexOf(category));
	}

	@Override
	public boolean openShop(GamePlayer gp, int index) {
		if (gp == null || !isValidIndex(index))
			return false;

		Category category = this.categories.get(index);

		Player player = gp.getPlayer();
		Inventory inv = Bukkit.createInventory(null, 54, category.getName());

		if (category instanceof QuickBuy) {
			User user = Bedwars.getInstance().getUser(player);
			if (user == null)
				return false;

			com.slyvr.api.shop.QuickBuy userQB = user.getQuickBuy(gp.getGame().getMode());
			if (userQB == null)
				userQB = new QuickBuy();

			QuickBuyOpenEvent quickbuyevent = new QuickBuyOpenEvent(gp, userQB);
			Bukkit.getPluginManager().callEvent(quickbuyevent);

			if (quickbuyevent.isCancelled())
				return false;

			applyItems(gp, inv, userQB, index);

		} else {
			ShopCategoryOpenEvent shopevent = new ShopCategoryOpenEvent(gp, this, category);
			Bukkit.getPluginManager().callEvent(shopevent);

			if (shopevent.isCancelled())
				return false;

			applyItems(gp, inv, category, index);
		}

		player.openInventory(inv);
		return true;
	}

	@Override
	public boolean openShop(GamePlayer gp) {
		return openShop(gp, 0);
	}

	@Override
	public List<Category> getCategories() {
		return new ArrayList<>(this.categories);
	}

	@Override
	public void setCategories(List<Category> categories) {
		for (int i = 0; i < 9 && i < categories.size(); i++)
			categories.set(i, categories.get(i));
	}

	@Override
	public Category getCategory(int index) {
		return index >= 0 && index < this.categories.size() ? this.categories.get(index) : null;
	}

	@Override
	public boolean addCategory(Category category) {
		return category != null && this.categories.size() < 9 && this.categories.add(category);
	}

	@Override
	public boolean removeCategory(Category category) {
		return category != null && this.categories.remove(category);
	}

	@Override
	public Category removeCategory(int index) {
		Category category = isValidIndex(index) ? this.categories.get(index) : null;

		this.removeCategory(category);
		return category;
	}

	@Override
	public void setCategory(int index, Category category) {
		if (isValidIndex(index))
			this.categories.set(index, category);
	}

	@Override
	public boolean contains(Category category) {
		return category != null && this.categories.contains(category);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.categories);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (!(obj instanceof BedwarsShop))
			return false;

		BedwarsShop other = (BedwarsShop) obj;
		return Objects.equals(this.categories, other.categories);
	}

	private void applyItems(GamePlayer gp, Inventory inv, Category category, int index) {

		for (int i = 0; i < this.categories.size() && i < 9; i++) {
			Category current = this.categories.get(i);
			if (current == null)
				continue;

			if (i != index)
				inv.setItem(i + 9, GRAY_SEPARATOR);
			else {
				category.applyItems(inv, gp);

				inv.setItem(i + 9, GREEN_SEPARATOR);
			}

			inv.setItem(i, current.getDisplayItem());
		}

	}

	private boolean isValidIndex(int index) {
		return index >= 0 && index < this.categories.size();
	}

}