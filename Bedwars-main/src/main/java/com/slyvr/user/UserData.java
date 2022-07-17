package com.slyvr.user;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.util.NumberConversions;

import com.slyvr.api.game.GameMode;
import com.slyvr.api.level.BedwarsLevel;
import com.slyvr.api.prestige.Prestige;
import com.slyvr.api.shop.QuickBuy;
import com.slyvr.api.shop.item.Buyable;
import com.slyvr.api.user.Statistic;
import com.slyvr.api.user.UserStatistics;
import com.slyvr.bedwars.Bedwars;
import com.slyvr.configuration.Configuration;
import com.slyvr.shop.ShopConfig;

public class UserData extends Configuration {

	private Map<GameMode, UserStatistics> stats = new LinkedHashMap<>();
	private Map<GameMode, QuickBuy> quick_buys = new LinkedHashMap<>();

	private OfflinePlayer player;

	private BedwarsLevel level;
	private Prestige prestige;

	private int balance;

	public UserData(OfflinePlayer player) {
		super(new File(Bedwars.getInstance().getDataFolder() + "/Userdata", player.getUniqueId() + ".yml"));

		this.player = player;
		saveDefaultConfig();
	}

	public BedwarsLevel getLevel() {
		return this.level != null ? this.level.clone() : null;
	}

	public void setLevel(BedwarsLevel level) {
		if (level != null)
			this.level = level.clone();
	}

	public Prestige getPrestige() {
		return this.prestige;
	}

	public void setPrestige(Prestige prestige) {
		if (prestige != null)
			this.prestige = prestige;
	}

	public UserStatistics getOverallStats() {
		UserStatistics result = new UserStatistics();

		Statistic[] values = Statistic.values();
		for (UserStatistics user_stats : stats.values()) {
			for (Statistic stat : values)
				result.incrementStatistic(stat, user_stats.getStatistic(stat));
		}

		return result;
	}

	public UserStatistics getStats(GameMode mode) {
		UserStatistics result = this.stats.get(mode);
		return result != null ? result.clone() : null;
	}

	public void setStats(GameMode mode, UserStatistics stats) {
		if (mode != null && stats != null)
			this.stats.put(mode, stats.clone());
	}

	public QuickBuy getQuickBuy(GameMode mode) {
		return mode != null ? this.quick_buys.get(mode) : null;
	}

	public void setQuickBuy(GameMode mode, QuickBuy qb) {
		if (mode != null && qb != null)
			this.quick_buys.put(mode, qb);
	}

	public int getBalance() {
		return this.balance;
	}

	public void setBalance(int balance) {
		if (balance >= 0)
			this.balance = balance;
	}

	public void loadData() {
		reloadConfig();

		int level = this.config.getInt("Level.level", 1);
		if (level <= 0)
			level = 1;

		int exp = this.config.getInt("Level.exp", 0);
		if (exp < 0)
			exp = 0;

		this.level = new BedwarsLevel(level, exp, Bedwars.getInstance().getSettings().getLevelUpExpFor(level));

		this.balance = this.config.getInt("Coins", 0);
		if (this.balance < 0)
			this.balance = 0;

		this.prestige = Prestige.getByName(this.config.getString("Prestige"));
		if (this.prestige == null)
			this.prestige = Prestige.DEFAULT;

		initUserStats();
		initQuickBuy();
	}

	public void saveData() {
		FileConfiguration config = new YamlConfiguration();

		if (this.level != null) {
			config.set("Level.level", this.level.getLevel());
			config.set("Level.exp", this.level.getProgressExp());
		}

		if (this.prestige != null)
			config.set("Prestige", this.prestige.getName());

		config.set("Coins", this.balance);

		Statistic[] values = Statistic.values();
		for (Entry<GameMode, UserStatistics> entry : this.stats.entrySet()) {
			GameMode mode = entry.getKey();

			UserStatistics stats = entry.getValue();
			for (Statistic stat : values)
				config.set("Statistics." + mode.getName() + "." + stat, stats.getStatistic(stat));
		}

		for (Entry<GameMode, QuickBuy> entry : this.quick_buys.entrySet()) {
			GameMode mode = entry.getKey();

			QuickBuy qb = entry.getValue();

			for (Entry<Integer, Buyable> qbEntry : qb.getItems().entrySet()) {
				int slot = qbEntry.getKey();

				String path = ShopConfig.getItemPath(mode, qbEntry.getValue());
				if (path == null)
					continue;

				config.set("Quick-Buy." + mode.getName() + ".Slot-" + slot, path);
			}
		}

		save(config, this.file, "Could not save user data " + this.player.getUniqueId() + "!");
	}

	public void initUserStats() {
		ConfigurationSection statsSection = this.config.getConfigurationSection("Statistics");
		if (statsSection == null)
			return;

		for (String statsSectionKey : statsSection.getKeys(false)) {
			GameMode mode = GameMode.fromString(statsSectionKey);

			if (mode == null)
				continue;

			ConfigurationSection section = statsSection.getConfigurationSection(statsSectionKey);
			if (section == null)
				continue;

			UserStatistics stats = new UserStatistics();

			for (String statsKey : section.getKeys(false)) {
				Statistic stat = Statistic.getByName(statsKey);

				if (stat == null)
					continue;

				int value = Math.abs(section.getInt(statsKey, 0));

				stats.setStatistic(stat, value);
			}

			this.stats.put(mode, stats);
		}

	}

	public void initQuickBuy() {
		ConfigurationSection section = this.config.getConfigurationSection("Quick-Buy");
		if (section == null)
			return;

		for (GameMode mode : GameMode.values()) {
			QuickBuy qb = new com.slyvr.quickbuy.QuickBuy();

			for (String slotKey : section.getKeys(false)) {
				int slot = NumberConversions.toInt(slotKey.toLowerCase().replace("Slot-", ""));

				if (slot <= 0)
					continue;

				Buyable buyable = ShopConfig.getPathItem(mode, section.getString(slotKey));
				if (buyable == null)
					continue;

				qb.setItem(slot, buyable);
			}

			this.quick_buys.put(mode, qb);
		}

	}

	@Override
	public void saveDefaultConfig() {
		if (this.file.exists())
			return;

		InputStream stream = Bedwars.getInstance().getResource("DefaultUser.yml");

		FileConfiguration config = YamlConfiguration.loadConfiguration(new InputStreamReader(stream));

		save(config, this.file, "Could not save default user for " + this.player.getName());
	}

	private void save(FileConfiguration config, File file, String message) {
		try {
			config.save(file);
		} catch (IOException e) {
			Bukkit.getLogger().log(Level.SEVERE, message);
		}

	}

}