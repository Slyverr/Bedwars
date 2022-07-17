package com.slyvr.manager;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.common.base.Preconditions;
import com.slyvr.api.util.Version;
import com.slyvr.bedwars.Bedwars;

public class ItemManager {

	private static final MethodHandle SET_UNBREAKABLE;
	private static final MethodHandle IS_UNBREAKABLE;
	private static final MethodHandle SPIGOT;

	static {
		MethodHandles.Lookup lookup = MethodHandles.lookup();

		MethodHandle spigot = null, method1 = null, method2 = null;

		if (!Version.getVersion().isNewAPI())
			try {
				Class<?> spigot_clazz = Class.forName("org.bukkit.inventory.meta.ItemMeta.Spigot");

				spigot = lookup.findVirtual(ItemMeta.class, "spigot", MethodType.methodType(spigot_clazz));

				method1 = lookup.findVirtual(spigot_clazz, "setUnbreakable", MethodType.methodType(void.class, boolean.class));

				method2 = lookup.findVirtual(spigot_clazz, "isUnbreakable", MethodType.methodType(boolean.class));

			} catch (Exception e) {

			}

		SPIGOT = spigot;
		SET_UNBREAKABLE = method1;
		IS_UNBREAKABLE = method2;
	}

	private final ItemStack item;
	private ItemMeta meta;

	private List<String> lore;

	/**
	 * */
	public ItemManager(ItemStack item) {
		Preconditions.checkNotNull(item, "ItemStack cannot be null!");
		Preconditions.checkArgument(item.getType() != Material.AIR, "ItemStack cannot be AIR!");

		this.item = item;
		this.meta = item.getItemMeta();
		this.lore = this.meta.hasLore() ? this.meta.getLore() : new ArrayList<>();
	}

	/**
	 * */
	public ItemManager(Material type, int amount, short durability) {
		this(new ItemStack(type, amount, durability));
	}

	/**
	 * */
	public ItemManager(Material type, int amount) {
		this(new ItemStack(type, amount));
	}

	/**
	 * */
	public ItemManager(Material type) {
		this(new ItemStack(type));
	}

	/**
	 * */
	public ItemStack getItem() {
		this.meta.setLore(this.lore);
		this.item.setItemMeta(this.meta);
		return this.item;
	}

	public int getAmount() {
		return this.item.getAmount();
	}

	/***/
	public ItemManager setAmount(int amount) {
		this.item.setAmount(amount);
		return this;
	}

	/***/
	public Material getType() {
		return this.item.getType();
	}

	/**
	 * */
	public ItemMeta getItemMeta() {
		return this.meta;
	}

	/**
	 * */
	public ItemManager setItemMeta(ItemMeta meta) {
		if (meta == null)
			this.lore = new ArrayList<>();

		this.meta = meta;
		return this;
	}

	/**
	 * */
	public List<String> getLore() {
		return this.lore != null ? this.lore : new ArrayList<>(0);
	}

	/**
	 * */
	public ItemManager addToLore(String line) {
		if (this.lore == null)
			this.lore = new ArrayList<>();

		this.lore.add(line);
		return this;
	}

	/**
	 * */
	public ItemManager setLore(List<String> lore) {
		this.lore = lore;
		return this;
	}

	/**
	 * */
	public String getName() {
		return this.meta.getDisplayName();
	}

	/**
	 * */
	public ItemManager setName(String name) {
		this.meta.setDisplayName(name);
		return this;
	}

	/**
	 * */
	public Set<ItemFlag> getItemFlags() {
		return this.meta.getItemFlags();
	}

	/**
	 * */
	public ItemManager addItemFlags(ItemFlag... flags) {
		this.meta.addItemFlags(flags);
		return this;
	}

	/**
	 * */
	public ItemManager addItemFlags(Collection<ItemFlag> flags) {
		for (ItemFlag flag : flags)
			this.meta.addItemFlags(flag);

		return this;
	}

	/**
	 * */
	public ItemManager removeItemFlags(ItemFlag... flags) {
		this.meta.removeItemFlags(flags);
		return this;
	}

	public ItemManager addEnchantment(Enchantment ench, int level, boolean unsafe) {
		this.meta.addEnchant(ench, level, unsafe);
		return this;
	}

	public ItemManager removeEnchantment(Enchantment ench) {
		this.meta.removeEnchant(ench);
		return this;
	}

	/**
	 * */
	public ItemManager setUnbreakable(boolean flag) {
		if (Bedwars.getInstance().getVersion().isNewAPI())
			this.meta.setUnbreakable(flag);
		else
			setUnbreakable(this.meta, flag);

		return this;
	}

	/**
	 * */
	public boolean isUnbreakable() {
		if (Bedwars.getInstance().getVersion().isNewAPI())
			return this.meta.isUnbreakable();

		return isUnbreakable(this.meta);
	}

	private boolean isUnbreakable(ItemMeta meta) {
		try {
			return (boolean) ItemManager.IS_UNBREAKABLE.invoke(spigot(meta));
		} catch (Throwable e) {
			return false;
		}

	}

	private void setUnbreakable(ItemMeta meta, boolean unbreakable) {
		try {
			ItemManager.SET_UNBREAKABLE.invoke(spigot(meta), unbreakable);
		} catch (Throwable e) {

		}
	}

	private Object spigot(ItemMeta meta) throws Throwable {
		return ItemManager.SPIGOT.invoke(meta);
	}

}