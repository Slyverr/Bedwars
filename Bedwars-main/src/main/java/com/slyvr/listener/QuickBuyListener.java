package com.slyvr.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryView;

import com.slyvr.api.game.Game;
import com.slyvr.api.shop.Category;
import com.slyvr.api.shop.QuickBuy;
import com.slyvr.api.shop.Shop;
import com.slyvr.bedwars.Bedwars;
import com.slyvr.game.AbstractGame;

public class QuickBuyListener implements Listener {

	@EventHandler
	public void onQuickBuyEdit(InventoryClickEvent event) {
		if (event.getClickedInventory() == null || !event.isShiftClick())
			return;

		Player player = (Player) event.getWhoClicked();

		Game game = AbstractGame.getPlayerGame(player);
		if (game == null || game.isSpectator(player))
			return;

		Shop shop = Bedwars.getInstance().getShopConfig().getShop(game.getMode());
		if (shop == null)
			return;

		Category current = getCurrentOpenedCategory(shop, event.getView());
		if (!(current instanceof QuickBuy))
			return;

		event.setCancelled(true);
		if (event.getCurrentItem() == null)
			return;

	}

	private Category getCurrentOpenedCategory(Shop shop, InventoryView view) {
		for (Category category : shop.getCategories()) {
			if (view.getTitle().equals(category.getName()))
				return category;
		}

		return null;
	}

}