package com.slyvr.generator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import com.google.common.base.Preconditions;
import com.slyvr.api.generator.DropItem;
import com.slyvr.api.generator.TeamGenerator;
import com.slyvr.bedwars.Bedwars;
import com.slyvr.listener.GamePlayerListener;

public class TeamResourceGenerator implements TeamGenerator {

	private static final Vector I_BELIEVE_I_CANT_FLY = new Vector(0, 0.25, 0);

	private Map<DropItem, BukkitTask[]> drop_task = new HashMap<>();

	private Location loc;

	private boolean isDropping;

	public TeamResourceGenerator(Location loc, Set<DropItem> drops) {
		Preconditions.checkNotNull(loc, "Drop location cannot be null!");
		Preconditions.checkNotNull(drops, "Drops cannot be null!");

		for (DropItem item : drops) {
			if (item == null)
				throw new IllegalArgumentException("Drop cannot be null!");

			this.drop_task.put(item, null);
		}

		if (this.drop_task.isEmpty())
			throw new IllegalArgumentException("Drops cannot be empty!");

		this.loc = loc.clone();
	}

	public TeamResourceGenerator(Location loc, DropItem drop) {
		Preconditions.checkNotNull(loc, "Drop location cannot be null!");
		Preconditions.checkNotNull(drop, "Drop cannot be null!");

		this.drop_task.put(drop, null);
		this.loc = loc;
	}

	@Override
	public Location getDropLocation() {
		return this.loc.clone();
	}

	@Override
	public void setDropLocation(Location loc) {
		if (loc != null)
			this.loc = loc.clone();
	}

	@Override
	public void start() {
		if (this.isDropping)
			return;

		for (Entry<DropItem, BukkitTask[]> entry : this.drop_task.entrySet())
			entry.setValue(startDrop(entry.getKey()));

		this.isDropping = true;
	}

	@Override
	public void stop() {
		if (!this.isDropping)
			return;

		for (BukkitTask[] tasks : this.drop_task.values()) {
			for (BukkitTask task : tasks)
				task.cancel();
		}

		this.isDropping = false;
	}

	@Override
	public Set<DropItem> getDrops() {
		return new HashSet<>(this.drop_task.keySet());
	}

	@Override
	public boolean addDrop(DropItem drop) {
		if (drop == null)
			return false;

		this.drop_task.put(drop, startDrop(drop));
		return true;
	}

	@Override
	public DropItem getDrop(Material material) {
		for (DropItem drop : this.drop_task.keySet()) {
			if (drop.getType() == material)
				return drop;
		}

		return null;
	}

	@Override
	public boolean removeDrop(DropItem item) {
		if (!contains(item))
			return false;

		BukkitTask[] tasks = this.drop_task.remove(item);
		if (tasks == null)
			return true;

		for (BukkitTask task : tasks)
			task.cancel();

		return true;
	}

	@Override
	public boolean contains(DropItem item) {
		return item != null && this.drop_task.containsKey(item);
	}

	@Override
	public boolean isDropping() {
		return this.isDropping;
	}

	private BukkitTask[] startDrop(DropItem drop) {
		int drops = drop.getDropsPerMinute();

		if (drops <= 60)
			return new BukkitTask[] { createTask(drop, 0, 1200 / drops) };

		int length = (int) Math.ceil(drops / 60.0);
		BukkitTask[] tasks = new BukkitTask[length];

		for (int i = 0; i < length; i++) {
			if (drops > 60)
				drops -= 60;

			tasks[i] = createTask(drop, i, 1200 / drops);
		}

		return tasks;
	}

	private BukkitTask createTask(DropItem drop, long delay, long ticks) {
		return new BukkitRunnable() {
			private ItemStack item = drop.getItem();
			private World world = TeamResourceGenerator.this.loc.getWorld();

			@Override
			public void run() {
				for (Entity entity : this.world.getNearbyEntities(TeamResourceGenerator.this.loc, 1.5, 1.5, 1.5)) {
					if (entity.getType() != EntityType.DROPPED_ITEM)
						continue;

					ItemStack dropped = ((Item) entity).getItemStack();
					if (dropped.getType() != this.item.getType())
						continue;

					if (dropped.getAmount() >= drop.getDropLimit())
						return;

					dropItem(this.item, TeamResourceGenerator.this.loc);
					return;
				}

				dropItem(this.item, TeamResourceGenerator.this.loc);

			}
		}.runTaskTimer(Bedwars.getInstance(), delay, ticks);
	}

	private void dropItem(ItemStack item, Location loc) {
		Item e = loc.getWorld().dropItem(loc, item);

		e.setMetadata("bedwars", GamePlayerListener.EMPTY);
		e.setVelocity(I_BELIEVE_I_CANT_FLY);
	}

}
