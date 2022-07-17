package com.slyvr.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;

import com.slyvr.hologram.BedwarsHologram;

public class HologramListener implements Listener {

	@EventHandler
	public void onArmorStandInteract(PlayerArmorStandManipulateEvent event) {
		if (BedwarsHologram.isHologram(event.getRightClicked()))
			event.setCancelled(true);
	}

	@EventHandler
	public void onArmorStandDamage(EntityDamageEvent event) {
		if (BedwarsHologram.isHologram(event.getEntity()))
			event.setCancelled(true);
	}

}