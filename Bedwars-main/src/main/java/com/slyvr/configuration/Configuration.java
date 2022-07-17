package com.slyvr.configuration;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.google.common.base.Preconditions;

public abstract class Configuration {

	protected FileConfiguration config;
	protected File file;

	public Configuration(File file) {
		Preconditions.checkNotNull(file, "File cannot be null");

		this.file = file;
	}

	public File getFile() {
		return this.file;
	}

	public void reloadConfig() {
		this.config = YamlConfiguration.loadConfiguration(this.file);
	}

	public void createFile() {
		try {
			this.file.createNewFile();
		} catch (IOException e) {
		}
	}

	public FileConfiguration getConfig() {
		if (this.config == null)
			reloadConfig();

		return this.config;
	}

	public void saveConfig() {
		if (this.config == null)
			return;

		try {
			this.config.save(this.file);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public abstract void saveDefaultConfig();

}