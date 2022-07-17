package com.slyvr.shop;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.NumberConversions;

import com.cryptomorin.xseries.XEnchantment;
import com.cryptomorin.xseries.XMaterial;
import com.slyvr.api.game.GameMode;
import com.slyvr.api.game.player.ArmorType;
import com.slyvr.api.shop.Category;
import com.slyvr.api.shop.Shop;
import com.slyvr.api.shop.item.Buyable;
import com.slyvr.api.shop.item.Item;
import com.slyvr.api.shop.item.ItemCost;
import com.slyvr.api.shop.item.ItemDescription;
import com.slyvr.api.shop.item.TieredItem;
import com.slyvr.bedwars.Bedwars;
import com.slyvr.configuration.Configuration;
import com.slyvr.manager.ItemManager;
import com.slyvr.quickbuy.QuickBuy;
import com.slyvr.shop.item.ArmorShopItem;
import com.slyvr.shop.item.PotionShopItem;
import com.slyvr.shop.item.ShopItem;
import com.slyvr.shop.item.ShopItemType;
import com.slyvr.shop.item.TieredShopItem;
import com.slyvr.util.ConfigUtils;

public class ShopConfig extends Configuration {

	private static final Map<GameMode, Shop> SHOPS = new HashMap<>();
	private static final Map<GameMode, Map<Buyable, String>> ITEMS_PATHS = new HashMap<>();
	private static final Map<GameMode, Map<String, Buyable>> PATHS_ITEMS = new HashMap<>();

	private static ShopConfig instance;
	private ConfigUtils utils;

	private ShopConfig() {
		super(new File(Bedwars.getInstance().getDataFolder(), "Shop.yml"));

		this.utils = new ConfigUtils(getConfig());
		saveDefaultConfig();
	}

	public Shop getShop(GameMode mode) {
		Shop shop = SHOPS.get(mode);
		if (shop != null)
			return shop;

		SHOPS.put(mode, shop = new BedwarsShop());

		ConfigurationSection shopSection = this.config.getConfigurationSection("Shop");
		if (shopSection == null)
			return shop;

		ConfigurationSection categorySection = this.config.getConfigurationSection("Categories");
		if (categorySection == null)
			return shop;

		for (String key : categorySection.getKeys(false))
			if (key.equalsIgnoreCase("Quick-Buy"))
				shop.addCategory(new QuickBuy());
			else {
				Category category = getShopCategory(key, mode);

				if (category != null)
					shop.addCategory(category);
			}

		return shop;
	}

	private void putInItems(GameMode mode, Buyable buyable, String path) {
		Map<Buyable, String> items = ITEMS_PATHS.get(mode);
		if (items == null)
			ITEMS_PATHS.put(mode, items = new HashMap<>());

		items.put(buyable, path);
	}

	private Category getShopCategory(String name, GameMode mode) {
		if (name == null)
			return null;

		XMaterial type = this.utils.getXMaterial("Categories." + name + ".type");
		if (type == null)
			return null;

		Map<Integer, Buyable> items = new HashMap<>();
		for (String key : this.config.getConfigurationSection("Shop." + name).getKeys(false)) {
			int slot = NumberConversions.toInt(key.replace("Slot-", ""));
			if (slot <= 0)
				continue;

			String path = "Shop." + name + "." + key; // Shop.Blocks.Slot-1

			ShopItemType itemType = ShopItemType.fromString(this.config.getString(path + ".item-type"));
			if (itemType == null || itemType == ShopItemType.CUSTOM)
				continue;

			Buyable buyable = null;
			if (itemType == ShopItemType.TIERED)
				buyable = getTieredShopItem(path, mode);
			else
				buyable = getShopItem(path, mode);

			if (buyable == null)
				continue;

			items.put(slot - 1, buyable);
			putInItems(mode, buyable, path);
		}

		return new ShopCategory(name, type.parseItem(), items);
	}

	private Item getShopItem(String path, GameMode mode) {
		if (path == null)
			return null;

		ShopItemType itemType = ShopItemType.fromString(this.config.getString(path + ".item-type"));
		if (itemType == null)
			return null;

		ItemCost cost = this.utils.getCost(path + ".cost", mode);
		if (cost == null)
			return null;

		String name = this.config.getString(path + ".name", "BedWars Item");

		ItemDescription desc = this.utils.getDescription(path + ".description");
		if (itemType == ShopItemType.ITEM)
			return getShopItem(path, name, cost, desc, this.config.getBoolean(path + ".permanent"));

		if (itemType == ShopItemType.ARMOR)
			return getArmorItem(path, name, cost, desc);

		if (itemType == ShopItemType.POTION)
			return getPotionItem(path, name, cost, desc);

		return null;
	}

	private TieredItem getTieredShopItem(String path, GameMode mode) {
		if (path == null || !this.config.isConfigurationSection(path + ".tiers"))
			return null;

		ShopItemType itemType = ShopItemType.fromString(this.config.getString(path + ".item-type"));
		if (itemType == null || itemType != ShopItemType.TIERED)
			return null;

		List<ShopItem> items = new ArrayList<>();
		for (String key : this.config.getConfigurationSection(path + ".tiers").getKeys(false)) {
			Item item = getShopItem(path + ".tiers." + key, mode);

			if (item instanceof ShopItem)
				items.add((ShopItem) item);
		}

		return new TieredShopItem(items);
	}

	private ShopItem getShopItem(String path, String name, ItemCost cost, ItemDescription desc, boolean permanent) {
		ItemStack raw = getItem(path);
		if (raw == null)
			return null;

		ItemStack display = getItem(path + ".Display-Item");
		for (Entry<Enchantment, Integer> entry : getEnchantments(path + ".enchants").entrySet()) {
			raw.addUnsafeEnchantment(entry.getKey(), entry.getValue());

			if (display != null)
				display.addUnsafeEnchantment(entry.getKey(), entry.getValue());
		}

		Material replace = this.utils.getMaterial(path + ".replace");
		return new ShopItem(name, raw, display, cost, desc, replace, permanent);
	}

	private ItemStack getItem(String path) {
		XMaterial material = this.utils.getXMaterial(path + ".type");
		if (material == null)
			return null;

		ItemManager manager = new ItemManager(material.parseItem());

		String name = this.config.getString(path + ".name");
		if (name != null)
			manager.setName(ChatColor.RESET + name);

		manager.setAmount(this.config.getInt(path + ".amount", 1));
		return manager.getItem();
	}

	private Map<Enchantment, Integer> getEnchantments(String path) {
		Map<Enchantment, Integer> result = new HashMap<>(4);

		List<String> enchantsList = this.config.getStringList(path);
		for (String string : enchantsList) {
			String[] values = string.split(":");
			if (values.length < 2)
				continue;

			Enchantment ench = getEnchantment(values[0]);
			if (ench == null)
				continue;

			int level = NumberConversions.toInt(values[1]);
			if (level <= 0)
				continue;

			result.put(ench, level);
		}

		return result;
	}

	private Enchantment getEnchantment(String name) {
		Optional<XEnchantment> optional = XEnchantment.matchXEnchantment(name);

		return optional.isPresent() ? optional.get().getEnchant() : null;
	}

	private ArmorShopItem getArmorItem(String path, String name, ItemCost cost, ItemDescription desc) {
		ArmorType type = ArmorType.getByName(this.config.getString(path + ".armor"));
		if (type == null)
			return null;

		ItemStack display = getItem(path + ".Display-Item");
		return new ArmorShopItem(name, display, type, cost, desc);
	}

	private PotionShopItem getPotionItem(String path, String name, ItemCost cost, ItemDescription desc) {
		PotionEffect effect = getEffect(path);
		if (effect == null)
			return null;

		ItemStack display = getItem(path + ".Display-Item");
		return new PotionShopItem(name, display, effect, cost, desc);
	}

	private PotionEffect getEffect(String path) {
		PotionEffectType type = PotionEffectType.getByName(this.config.getString(path + ".effect", ""));
		if (type == null)
			return null;

		int duration = this.config.getInt(path + ".duration", 0);
		if (duration <= 0)
			return null;

		int level = this.config.getInt(path + ".level", 0);
		if (level <= 0)
			return null;

		return new PotionEffect(type, duration * 20, level - 1);
	}

	@Override
	public void saveDefaultConfig() {
		if (!this.file.exists())
			Bedwars.getInstance().saveResource("Shop.yml", false);
	}

	public static String getItemPath(GameMode mode, Buyable buyable) {
		Map<Buyable, String> items = ITEMS_PATHS.get(mode);

		return items != null ? items.get(buyable) : null;
	}

	public static Buyable getPathItem(GameMode mode, String path) {
		Map<String, Buyable> items = PATHS_ITEMS.get(mode);

		return items != null ? items.get(path) : null;
	}

	public static ShopConfig getInstance() {
		if (instance == null)
			ShopConfig.instance = new ShopConfig();

		return ShopConfig.instance;
	}

}