package com.slyvr.prestige;

import java.io.File;

import org.bukkit.configuration.ConfigurationSection;

import com.slyvr.api.prestige.Prestige;
import com.slyvr.bedwars.Bedwars;
import com.slyvr.configuration.Configuration;
import com.slyvr.util.ChatUtils;

public class PrestigeConfig extends Configuration {

	private static PrestigeConfig instance;

	private Prestige default_prestige;

	private PrestigeConfig() {
		super(new File(Bedwars.getInstance().getDataFolder(), "Prestige.yml"));

		saveDefaultConfig();
	}

	public Prestige getDefaultPrestige() {
		if (this.default_prestige != null)
			return this.default_prestige;

		this.default_prestige = getPrestige(getConfig().getString("Default"));
		if (this.default_prestige == null)
			this.default_prestige = Prestige.DEFAULT;

		return this.default_prestige;
	}

	public Prestige getPrestige(String name) {
		if (name == null)
			return null;

		String displayName = getConfig().getString("Prestige." + name + ".display-name");
		if (displayName == null)
			return null;

		String format = this.config.getString("Prestige." + name + ".format.chat");
		if (format == null)
			return null;

		String sbFormat = this.config.getString("Prestige." + name + ".format.scoreboard");
		if (sbFormat == null)
			return null;

		int start = this.config.getInt("Prestige." + name + ".start");
		int end = this.config.getInt("Prestige." + name + ".end");

		if (start < 0 || end < 0 || end < start)
			return null;

		return new Prestige(name, ChatUtils.format(displayName), format, sbFormat, start, end);
	}

	public void loadPrestiges() {
		reloadConfig();

		ConfigurationSection section = this.config.getConfigurationSection("Prestige");
		if (section == null)
			return;

		for (String key : section.getKeys(false)) {
			Prestige prestige = getPrestige(key);

			if (prestige != null)
				Prestige.registerPrestige(prestige);
		}

	}

	@Override
	public void saveDefaultConfig() {
		if (!this.file.exists())
			Bedwars.getInstance().saveResource("Prestige.yml", false);
	}

	public static PrestigeConfig getInstance() {
		if (instance == null)
			PrestigeConfig.instance = new PrestigeConfig();

		return PrestigeConfig.instance;
	}

}