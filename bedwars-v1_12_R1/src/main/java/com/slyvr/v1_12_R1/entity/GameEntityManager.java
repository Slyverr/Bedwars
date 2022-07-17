package com.slyvr.v1_12_R1.entity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;

import com.slyvr.api.entity.BedBug;
import com.slyvr.api.entity.BodyGuard;
import com.slyvr.api.entity.Dragon;
import com.slyvr.api.entity.GameEntity;
import com.slyvr.api.game.Game;
import com.slyvr.api.game.player.GamePlayer;
import com.slyvr.api.team.Team;

import net.minecraft.server.v1_12_R1.EntityEnderDragon;
import net.minecraft.server.v1_12_R1.EntityInsentient;
import net.minecraft.server.v1_12_R1.EntityIronGolem;
import net.minecraft.server.v1_12_R1.EntitySilverfish;
import net.minecraft.server.v1_12_R1.EntityTypes;

public class GameEntityManager implements com.slyvr.api.entity.GameEntityManager {

	@Override
	public BedBug createBedBug(Game game, Team team, GamePlayer owner, Location loc) {
		return new com.slyvr.v1_12_R1.entity.BedBug(game, team, owner, loc);
	}

	@Override
	public BedBug getBedBug(Entity entity) {
		net.minecraft.server.v1_12_R1.Entity result = ((CraftEntity) entity).getHandle();
		return result instanceof BedBug ? (BedBug) result : null;
	}

	@Override
	public boolean isBedBug(Entity entity) {
		return entity != null ? ((CraftEntity) entity).getHandle() instanceof BedBug : false;
	}

	@Override
	public BodyGuard createBodyGuard(Game game, Team team, GamePlayer owner, Location loc) {
		return new com.slyvr.v1_12_R1.entity.BodyGuard(game, team, owner, loc);
	}

	@Override
	public BodyGuard getBodyGuard(Entity entity) {
		net.minecraft.server.v1_12_R1.Entity result = ((CraftEntity) entity).getHandle();
		return result instanceof BodyGuard ? (BodyGuard) result : null;
	}

	@Override
	public boolean isBodyGuard(Entity entity) {
		return entity != null ? ((CraftEntity) entity).getHandle() instanceof BodyGuard : false;
	}

	@Override
	public Dragon createDragon(Game game, Team team, GamePlayer owner, Location loc) {
		return new com.slyvr.v1_12_R1.entity.Dragon(game, team, owner, loc);
	}

	@Override
	public Dragon getDragon(Entity entity) {
		net.minecraft.server.v1_12_R1.Entity result = ((CraftEntity) entity).getHandle();
		return result instanceof Dragon ? (Dragon) result : null;
	}

	@Override
	public boolean isDragon(Entity entity) {
		return entity != null ? ((CraftEntity) entity).getHandle() instanceof Dragon : false;
	}

	@Override
	public GameEntity getGameEntity(Entity entity) {
		net.minecraft.server.v1_12_R1.Entity result = ((CraftEntity) entity).getHandle();
		return result instanceof GameEntity ? (GameEntity) result : null;
	}

	@Override
	public boolean isGameEntity(Entity entity) {
		return entity != null ? ((CraftEntity) entity).getHandle() instanceof GameEntity : false;
	}

	static {
		registerEntity("BedBug", 60, EntitySilverfish.class, com.slyvr.v1_12_R1.entity.BedBug.class);
		registerEntity("BodyGuard", 99, EntityIronGolem.class, com.slyvr.v1_12_R1.entity.BodyGuard.class);
		registerEntity("Dragon", 63, EntityEnderDragon.class, com.slyvr.v1_12_R1.entity.Dragon.class);
	}

	public static void registerEntity(String name, int id, Class<? extends EntityInsentient> nmsClass, Class<? extends EntityInsentient> customClass) {
		try {

			List<Map<?, ?>> dataMap = new ArrayList<>();
			for (Field f : EntityTypes.class.getDeclaredFields())
				if (f.getType().getSimpleName().equals(Map.class.getSimpleName())) {
					f.setAccessible(true);
					dataMap.add((Map<?, ?>) f.get(null));
				}

			if (dataMap.get(2).containsKey(id)) {
				dataMap.get(0).remove(name);
				dataMap.get(2).remove(id);
			}

			Method method = EntityTypes.class.getDeclaredMethod("a", Class.class, String.class, int.class);
			method.setAccessible(true);
			method.invoke(null, customClass, name, id);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}