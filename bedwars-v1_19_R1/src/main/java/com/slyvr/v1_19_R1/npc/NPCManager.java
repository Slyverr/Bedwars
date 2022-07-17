package com.slyvr.v1_19_R1.npc;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;

import com.slyvr.api.game.Game;
import com.slyvr.api.npc.NPC;
import com.slyvr.api.npc.Shopkeeper;
import com.slyvr.api.npc.Upgrader;

public class NPCManager implements com.slyvr.api.npc.NPCManager {

	@Override
	public Shopkeeper createShopKeeper(Game game, Location loc) {
		return new com.slyvr.v1_19_R1.npc.Shopkeeper(game, loc);
	}

	@Override
	public Upgrader createUpgrader(Game game, Location loc) {
		return new com.slyvr.v1_19_R1.npc.Upgrader(game, loc);
	}

	@Override
	public Shopkeeper getShopKeeper(Entity entity) {
		net.minecraft.world.entity.Entity result = ((CraftEntity) entity).getHandle();
		return result instanceof Shopkeeper ? (Shopkeeper) result : null;
	}

	@Override
	public boolean isShopKeeper(Entity entity) {
		return ((CraftEntity) entity).getHandle() instanceof Shopkeeper;
	}

	@Override
	public Upgrader getUpgrader(Entity entity) {
		net.minecraft.world.entity.Entity result = ((CraftEntity) entity).getHandle();
		return result instanceof Upgrader ? (Upgrader) result : null;
	}

	@Override
	public boolean isUpgrader(Entity entity) {
		return ((CraftEntity) entity).getHandle() instanceof Upgrader;
	}

	@Override
	public NPC getNPC(Entity entity) {
		Shopkeeper shopkeeper = getShopKeeper(entity);

		return shopkeeper != null ? shopkeeper : getUpgrader(entity);
	}

	@Override
	public boolean isNPC(Entity entity) {
		return isShopKeeper(entity) || isUpgrader(entity);
	}
}