package com.slyvr.shop.item;

import java.util.HashMap;
import java.util.Map;

public enum ShopItemType {

	ITEM,
	ARMOR,
	POTION,
	TIERED,
	CUSTOM;

	private static final Map<String, ShopItemType> BY_NAME = new HashMap<>(5);

	static {
		for (ShopItemType type : values())
			ShopItemType.BY_NAME.put(type.name().toLowerCase(), type);

	}

	public static ShopItemType fromString(String string) {
		return string != null ? ShopItemType.BY_NAME.get(string.toLowerCase()) : null;
	}

}