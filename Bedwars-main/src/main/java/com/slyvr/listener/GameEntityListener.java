package com.slyvr.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import com.slyvr.api.entity.GameEntity;
import com.slyvr.api.event.entity.GameEntityDamageByGamePlayerEvent;
import com.slyvr.api.event.entity.GameEntityDeathEvent;
import com.slyvr.api.game.Game;
import com.slyvr.api.game.player.GamePlayer;
import com.slyvr.bedwars.Bedwars;
import com.slyvr.game.AbstractGame;

public class GameEntityListener implements Listener {

	@EventHandler
	public void onGamePlayerDamageGameEntity(EntityDamageByEntityEvent event) {
		if (event.getEntity() instanceof Player || !(event.getDamager() instanceof Player))
			return;

		GameEntity damaged = Bedwars.getInstance().getEntityManager().getGameEntity(event.getEntity());
		if (damaged == null)
			return;

		Player damager = (Player) event.getDamager();

		Game game = AbstractGame.getPlayerGame(damager);
		if (game == null || !game.hasStarted())
			return;

		GamePlayer gp = game.getGamePlayer(damager);
		if (damaged.getGameTeam() == gp.getTeam()) {
			event.setCancelled(true);
			return;
		}

		GameEntityDamageByGamePlayerEvent bwEvent = new GameEntityDamageByGamePlayerEvent(damaged, gp);
		Bukkit.getPluginManager().callEvent(bwEvent);

		if (bwEvent.isCancelled()) {
			event.setCancelled(true);
			return;
		}

		if (((LivingEntity) event.getEntity()).getHealth() - event.getFinalDamage() > 0)
			return;

		GameEntityDeathEvent bwEvent2 = new GameEntityDeathEvent(damaged, gp);
		Bukkit.getPluginManager().callEvent(bwEvent2);

		damaged.remove();
		event.setCancelled(true);
	}

}