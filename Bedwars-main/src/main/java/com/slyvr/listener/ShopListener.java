package com.slyvr.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import com.slyvr.api.event.quickbuy.QuickBuyCloseEvent;
import com.slyvr.api.event.quickbuy.QuickBuyEditEvent;
import com.slyvr.api.event.quickbuy.QuickBuyEditEvent.QuickBuyAction;
import com.slyvr.api.event.shop.ShopCloseEvent;
import com.slyvr.api.event.shop.ShopItemClickEvent;
import com.slyvr.api.game.Game;
import com.slyvr.api.game.player.GamePlayer;
import com.slyvr.api.npc.NPC;
import com.slyvr.api.npc.Shopkeeper;
import com.slyvr.api.npc.Upgrader;
import com.slyvr.api.shop.Category;
import com.slyvr.api.shop.QuickBuy;
import com.slyvr.api.shop.Shop;
import com.slyvr.api.shop.item.Buyable;
import com.slyvr.api.upgrade.shop.UpgradeShop;
import com.slyvr.api.user.User;
import com.slyvr.bedwars.Bedwars;
import com.slyvr.game.AbstractGame;

public class ShopListener implements Listener {

	@EventHandler
	public void onGamePlayerDamageNPC(EntityDamageByEntityEvent event) {
		if (!(event.getDamager() instanceof Player))
			return;

		NPC npc = Bedwars.getInstance().getNPCManager().getNPC(event.getEntity());
		if (npc == null)
			return;

		openNPCShop((Player) event.getDamager(), npc);
		event.setCancelled(true);
	}

	@EventHandler
	public void onGamePlayerNPCClick(PlayerInteractEntityEvent event) {
		NPC npc = Bedwars.getInstance().getNPCManager().getNPC(event.getRightClicked());
		if (npc == null)
			return;

		openNPCShop(event.getPlayer(), npc);
		event.setCancelled(true);
	}

	@EventHandler
	public void onGamePlayerShopInteract(InventoryClickEvent event) {
		if (event.getClickedInventory() == null)
			return;

		Player player = (Player) event.getWhoClicked();

		Game game = AbstractGame.getPlayerGame(player);
		if (game == null || !game.hasStarted())
			return;

		GamePlayer gp = game.getGamePlayer(player);
		if (event.getView().getTitle().equalsIgnoreCase("Upgrades & Traps")) {

			UpgradeShop shop = Bedwars.getInstance().getTeamUpgradeShop(game.getMode());

			Buyable item = shop.getItem(event.getSlot());
			if (item != null) {
				item.onBuy(gp);
				shop.openShop(gp);

				GamePlayerListener.updateTeam(gp);
				GamePlayerListener.checkForSword(gp);
			}

			event.setCancelled(true);
			return;
		}

		Shop shop = Bedwars.getInstance().getTeamShop(game.getMode());

		Category current = getCurrentOpenedCategory(shop, event.getView());
		if (current == null)
			return;

		event.setCancelled(true);
		if (event.getRawSlot() > 53)
			return;

		ItemStack clicked = event.getCurrentItem();
		if (clicked == null)
			return;

		if (event.getRawSlot() <= 8)
			shop.openShop(gp, event.getRawSlot());
		else {
			Buyable buyable = current.getItem(event.getRawSlot());

			if (buyable == null)
				return;

			// Quick Buy Editing
			if (event.isShiftClick() && current instanceof QuickBuy) {
				User user = Bedwars.getInstance().getUser(player);

				if (user != null) {
					QuickBuy qb = user.getQuickBuy(game.getMode());
					if (qb == null)
						return;

					QuickBuyEditEvent qbEvent = new QuickBuyEditEvent(gp, qb, buyable, QuickBuyAction.REMOVE);
					Bukkit.getPluginManager().callEvent(qbEvent);

					if (qbEvent.isCancelled())
						return;

					qb.removeItem(event.getRawSlot());
					shop.openShop(gp, current);
					return;
				}

			}
			// Quick Buy Editing

			ShopItemClickEvent bwEvent = new ShopItemClickEvent(gp, shop, current, buyable);
			Bukkit.getPluginManager().callEvent(bwEvent);

			if (bwEvent.isCancelled())
				return;

			buyable.onBuy(gp);
			shop.openShop(gp, current);

			GamePlayerListener.updateTeam(gp);
			GamePlayerListener.checkForSword(gp);
		}

	}

	@EventHandler
	public void onGamePlayerShopClose(InventoryCloseEvent event) {
		Player player = (Player) event.getPlayer();

		Game game = AbstractGame.getPlayerGame(player);
		if (game == null || !game.hasStarted())
			return;

		GamePlayer gp = game.getGamePlayer(player);
		Shop shop = Bedwars.getInstance().getTeamShop(game.getMode());

		Category current = getCurrentOpenedCategory(shop, event.getView());
		if (current == null)
			return;

		if (current instanceof QuickBuy) {
			QuickBuyCloseEvent bwEvent = new QuickBuyCloseEvent(gp, (QuickBuy) current);
			Bukkit.getPluginManager().callEvent(bwEvent);

		} else {
			ShopCloseEvent bwEvent = new ShopCloseEvent(gp, shop, current);
			Bukkit.getPluginManager().callEvent(bwEvent);
		}

	}

	private Category getCurrentOpenedCategory(Shop shop, InventoryView view) {
		for (Category category : shop.getCategories())
			if (view.getTitle().equals(category.getName()))
				return category;

		return null;
	}

	private void openNPCShop(Player player, NPC npc) {
		Game game = AbstractGame.getPlayerGame(player);
		if (game == null || !game.hasStarted() || game.isSpectator(player))
			return;

		if (npc instanceof Shopkeeper)
			Bedwars.getInstance().getTeamShop(game.getMode()).openShop(game.getGamePlayer(player));
		else if (npc instanceof Upgrader)
			Bedwars.getInstance().getTeamUpgradeShop(game.getMode()).openShop(game.getGamePlayer(player));
	}

}