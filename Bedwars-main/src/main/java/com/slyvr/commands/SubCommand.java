package com.slyvr.commands;

import org.bukkit.entity.Player;

public interface SubCommand {

	String getName();

	String getDescription();

	String getPermission();

	String getUsage();

	void perform(Player player, String[] args);

}