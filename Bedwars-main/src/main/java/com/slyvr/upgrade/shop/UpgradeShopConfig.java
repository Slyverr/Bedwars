package com.slyvr.upgrade.shop;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.NumberConversions;

import com.cryptomorin.xseries.XMaterial;
import com.slyvr.api.game.GameMode;
import com.slyvr.api.shop.item.ItemCost;
import com.slyvr.api.shop.item.ItemDescription;
import com.slyvr.api.trap.Trap;
import com.slyvr.api.upgrade.TieredUpgrade;
import com.slyvr.api.upgrade.Upgrade;
import com.slyvr.api.upgrade.shop.UpgradeShop;
import com.slyvr.api.upgrade.shop.item.TieredUpgradeItem;
import com.slyvr.api.upgrade.shop.item.TieredUpgradeItemTier;
import com.slyvr.api.upgrade.shop.item.TrapItem;
import com.slyvr.api.upgrade.shop.item.UpgradeItem;
import com.slyvr.bedwars.Bedwars;
import com.slyvr.configuration.Configuration;
import com.slyvr.upgrade.shop.item.TieredUpgradeShopItem;
import com.slyvr.upgrade.shop.item.TrapShopItem;
import com.slyvr.upgrade.shop.item.UpgradeShopItem;
import com.slyvr.util.ConfigUtils;

public class UpgradeShopConfig extends Configuration {

	enum Type {
		TIERED_UPGRADE,
		UPGRADE,
		TRAP;

		private static final Map<String, Type> BY_NAME = new HashMap<>(3);

		static {
			for (Type type : values())
				BY_NAME.put(type.name().toLowerCase(), type);

			BY_NAME.put("tiered upgrade", TIERED_UPGRADE);
		}

		public static Type getByName(String name) {
			return name != null ? BY_NAME.get(name.toLowerCase()) : null;
		}

	}

	private static final Map<GameMode, UpgradeShop> SHOPS = new HashMap<>();

	private static UpgradeShopConfig instance;
	private ConfigUtils utils;

	private UpgradeShopConfig() {
		super(new File(Bedwars.getInstance().getDataFolder(), "UpgradeShop.yml"));

		this.utils = new ConfigUtils(getConfig());
		saveDefaultConfig();
	}

	public UpgradeShop getUpgradeShop(GameMode mode) {
		UpgradeShop shop = SHOPS.get(mode);
		if (shop != null)
			return shop;

		SHOPS.put(mode, shop = new BedwarsUpgradeShop());

		ConfigurationSection section = this.config.getConfigurationSection("Shop");
		if (section == null)
			return shop;

		for (String key : section.getKeys(false)) {
			int slot = NumberConversions.toInt(key.replace("Slot-", ""));
			if (slot <= 0)
				continue;

			Type type = getType("Shop." + key);
			if (type == null)
				continue;

			switch (type) {
				case TIERED_UPGRADE:
					shop.addItem(slot - 1, getTieredUpgradeItem("Shop." + key, mode));
					break;
				case UPGRADE:
					shop.addItem(slot - 1, getUpgradeItem("Shop." + key, mode));
					break;
				case TRAP:
					shop.addItem(slot - 1, getTrapItem("Shop." + key, mode));
					break;
				default:
					break;
			}

		}

		return shop;
	}

	private TieredUpgradeItem getTieredUpgradeItem(String path, GameMode mode) {
		Type type = getType(path);
		if (type != Type.TIERED_UPGRADE)
			return null;

		ConfigurationSection section = this.config.getConfigurationSection(path + ".tiers");
		if (section == null)
			return null;

		TieredUpgrade upgrade = Bedwars.getInstance().getUpgradesManager().getTieredUpgrade(this.config.getString(path + ".upgrade"));
		if (upgrade == null)
			return null;

		XMaterial itemType = this.utils.getXMaterial(path + ".type");
		if (itemType == null)
			return null;

		List<TieredUpgradeItemTier> tiers = new ArrayList<>();
		for (String key : section.getKeys(false)) {
			String name = section.getString(key + ".name");
			if (name == null)
				continue;

			ItemCost cost = this.utils.getCost(section.getCurrentPath() + "." + key + ".cost", mode);
			if (cost == null)
				continue;

			tiers.add(new TieredUpgradeItemTier(name, cost));
		}

		if (tiers.isEmpty())
			return null;

		ItemDescription desc = this.utils.getDescription(path + ".description");
		return new TieredUpgradeShopItem(getName(path), itemType.parseItem(), tiers, desc, upgrade);
	}

	private UpgradeItem getUpgradeItem(String path, GameMode mode) {
		Type type = getType(path);
		if (type != Type.UPGRADE)
			return null;

		Upgrade upgrade = Bedwars.getInstance().getUpgradesManager().getUpgrade(this.config.getString(path + ".upgrade"));
		if (upgrade == null)
			return null;

		XMaterial itemType = this.utils.getXMaterial(path + ".type");
		if (itemType == null)
			return null;

		ItemCost cost = this.utils.getCost(path + ".cost", mode);
		if (cost == null)
			return null;

		ItemDescription desc = this.utils.getDescription(path + ".description");
		return new UpgradeShopItem(getName(path), itemType.parseItem(), cost, desc, upgrade);
	}

	private TrapItem getTrapItem(String path, GameMode mode) {
		Type type = getType(path);
		if (type != Type.TRAP)
			return null;

		Trap upgrade = Bedwars.getInstance().getUpgradesManager().getTrapUpgrade(this.config.getString(path + ".trap"));
		if (upgrade == null)
			return null;

		XMaterial itemType = this.utils.getXMaterial(path + ".type");
		if (itemType == null)
			return null;

		ItemDescription desc = this.utils.getDescription(path + ".description");
		return new TrapShopItem(getName(path), itemType.parseItem(), desc, upgrade);
	}

	private String getName(String path) {
		return this.config.getString(path + ".name", "BedWars Trap");
	}

	private Type getType(String path) {
		return Type.getByName(this.config.getString(path + ".upgrade-type"));
	}

	@Override
	public void saveDefaultConfig() {
		if (!this.file.exists())
			Bedwars.getInstance().saveResource("UpgradeShop.yml", false);
	}

	public static UpgradeShopConfig getInstance() {
		if (instance == null)
			instance = new UpgradeShopConfig();

		return UpgradeShopConfig.instance;
	}

}