package com.slyvr.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.util.NumberConversions;

import com.cryptomorin.xseries.XMaterial;
import com.google.common.base.Preconditions;
import com.slyvr.api.arena.Region;
import com.slyvr.api.game.GameMode;
import com.slyvr.api.generator.Resource;
import com.slyvr.api.shop.item.ItemCost;
import com.slyvr.api.shop.item.ItemDescription;
import com.slyvr.api.util.LocationUtils;

public class ConfigUtils {

	private final FileConfiguration config;

	public ConfigUtils(FileConfiguration config) {
		Preconditions.checkNotNull(config, "Config cannot be null!");

		this.config = config;
	}

	public XMaterial getXMaterial(String path, XMaterial def) {
		String type = this.config.getString(path);
		if (type == null)
			return def;

		Optional<XMaterial> result = XMaterial.matchXMaterial(type);
		return result.isPresent() ? result.get() : def;
	}

	public XMaterial getXMaterial(String path) {
		return getXMaterial(path, null);
	}

	public Material getMaterial(String path, Material def) {
		XMaterial material = getXMaterial(path);

		return material != null ? material.parseMaterial() : def;
	}

	public Material getMaterial(String path) {
		return getMaterial(path, null);
	}

	public Location getLocation(String path, Location def) {
		String location = this.config.getString(path);

		if (location != null) {
			Location result = LocationUtils.deserialize(location);

			return result != null ? result : def;
		}

		return null;
	}

	public Location getLocation(String path) {
		return getLocation(path, null);
	}

	public List<Location> getLocationList(String path) {
		List<Location> result = new ArrayList<>();

		for (String location : this.config.getStringList(path)) {
			Location loc = LocationUtils.deserialize(location);

			if (loc != null)
				result.add(loc);
		}

		return result;
	}

	public Region getRegion(String path, Region def) {
		Location pos1 = getLocation(path + ".pos-1");
		Location pos2 = getLocation(path + ".pos-2");

		return pos1 != null && pos2 != null ? new Region(pos1, pos2) : def;
	}

	public Region getRegion(String path) {
		return getRegion(path, null);
	}

	public ItemCost getCost(String path, GameMode mode) {
		if (path == null)
			return null;

		ConfigurationSection section = this.config.getConfigurationSection(path);

		if (mode == null || section == null)
			return getCostByString(this.config.getString(path));

		for (String key : section.getKeys(false)) {
			if (!key.equalsIgnoreCase(mode.getName()))
				continue;

			return getCostByString(this.config.getString(path + "." + key));
		}

		return null;
	}

	public ItemCost getCost(String path) {
		return getCost(path, null);
	}

	private ItemCost getCostByString(String string) {
		if (string == null)
			return null;

		String[] values = string.split(" ");
		if (values.length < 2)
			return null;

		return new ItemCost(Resource.getByName(values[1]), NumberConversions.toInt(values[0]));
	}

	public List<ItemCost> getItemCostList(String path) {
		List<ItemCost> result = new ArrayList<>();

		if (!this.config.isList(path))
			return result;

		for (String line : this.config.getStringList(path)) {
			ItemCost cost = getCostByString(line);

			if (cost != null)
				result.add(cost);
		}

		return result;
	}

	public ItemDescription getDescription(String path) {
		return path != null ? new ItemDescription(this.config.getStringList(path)) : null;
	}

	public FileConfiguration getConfig() {
		return this.config;
	}

}