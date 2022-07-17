package com.slyvr.shop.item;

import com.slyvr.api.generator.Resource;
import com.slyvr.api.shop.item.Item;
import com.slyvr.api.shop.item.ItemCost;
import com.slyvr.api.shop.item.ItemDescription;

public abstract class AbstractShopItem implements Item {

	protected String name;
	protected ItemCost cost;
	protected ItemDescription desc;

	protected AbstractShopItem(String name, ItemCost cost, ItemDescription desc) {

		this.name = name;
		this.cost = cost != null ? cost : new ItemCost(Resource.FREE, 0);
		this.desc = desc != null ? desc : new ItemDescription();
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public ItemDescription getDescription() {
		return this.desc.clone();
	}

	@Override
	public void setDescription(ItemDescription desc) {
		if (desc != null)
			this.desc = desc.clone();
	}

	@Override
	public ItemCost getCost() {
		return this.cost.clone();
	}

	@Override
	public void setCost(ItemCost cost) {
		if (cost != null)
			this.cost = cost.clone();
	}

}