package com.slyvr.bedwars;

import org.bukkit.inventory.ItemStack;

import com.cryptomorin.xseries.XMaterial;
import com.slyvr.manager.ItemManager;

public class BedwarsItems {

	private static BedwarsItems instance;

	private ItemStack sword;

	private BedwarsItems() {
	}

	public ItemStack getSword() {
		if (this.sword != null)
			return this.sword;

		this.sword = new ItemManager(XMaterial.WOODEN_SWORD.parseItem()).setUnbreakable(true).getItem();
		return this.sword;
	}

	public static BedwarsItems getInstance() {
		if (instance == null)
			instance = new BedwarsItems();

		return instance;
	}

}