package com.slyvr.generator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import com.google.common.base.Preconditions;
import com.slyvr.api.generator.GeneratorTier;
import com.slyvr.api.generator.Resource;
import com.slyvr.api.generator.TieredGenerator;
import com.slyvr.bedwars.Bedwars;
import com.slyvr.hologram.BedwarsHologram;
import com.slyvr.hologram.Hologram;

public class MapResourceGenerator implements TieredGenerator {

	private static final Vector I_BELIEVE_I_CAN_FLY = new Vector(0, .25, 0);

	private List<GeneratorTier> tiers;

	private Hologram hologram;
	private ArmorStand animation;

	private BukkitTask dropTask;
	private BukkitTask hologramTask;
	private BukkitTask hologramAnimation;

	private GeneratorTier current;
	private Resource drop;
	private Location loc;
	private ItemStack item;

	private int tier = 1;

	private boolean isDropping;

	public MapResourceGenerator(Resource drop, Location loc, List<GeneratorTier> tiers) {
		Preconditions.checkNotNull(drop, "Resource cannot be null!");
		Preconditions.checkNotNull(loc, "Drop location cannot be null!");
		Preconditions.checkArgument(!tiers.isEmpty(), "Generator tiers cannot be empty");

		this.drop = drop;
		this.loc = loc;

		List<GeneratorTier> result = new ArrayList<>(tiers.size());
		for (GeneratorTier tier : tiers) {
			if (tier != null)
				result.add(tier);
		}

		this.tiers = result;
		this.item = new ItemStack(drop.getMaterial());
	}

	@Override
	public List<GeneratorTier> getTiers() {
		return new ArrayList<>(this.tiers);
	}

	@Override
	public Location getDropLocation() {
		return this.loc.clone();
	}

	@Override
	public void setDropLocation(Location loc) {
		if (loc == null)
			return;

		this.loc = loc.clone();
		if (this.hologram != null)
			this.hologram.teleport(loc);
	}

	@Override
	public GeneratorTier getCurrentTier() {
		return this.tiers.get(this.tier - 1);
	}

	@Override
	public void setCurrentTier(int tier) {
		if (tier >= 1 && tier <= this.tiers.size())
			upgrade(tier);
	}

	@Override
	public Resource getDrop() {
		return this.drop;
	}

	@Override
	public void start() {
		if (this.isDropping)
			return;

		upgrade(this.tier);

		this.hologram = new BedwarsHologram(this.loc.clone().add(0, 3, 0), .3);
		this.hologram.addLine("&eSpawns in &c" + this.current.getDropTime() + " &eseconds!");
		this.hologram.addLine(this.current.getDisplayName());
		this.hologram.addLine(this.drop.getColoredName());

		this.hologramTask = new BukkitRunnable() {
			int time = MapResourceGenerator.this.current.getDropTime();

			@Override
			public void run() {
				if (this.time == 0)
					this.time = MapResourceGenerator.this.current.getDropTime();

				MapResourceGenerator.this.hologram.setText(0, "&eSpawns in &c" + this.time-- + " &eseconds!");
			}
		}.runTaskTimerAsynchronously(Bedwars.getInstance(), this.tier, 20L);

		this.animation = (ArmorStand) this.loc.getWorld().spawnEntity(this.loc.clone().add(0, 2, 0), EntityType.ARMOR_STAND);
		this.animation.setHelmet(new ItemStack(this.drop.getBlock()));
		this.animation.setGravity(false);
		this.animation.setVisible(false);

		this.hologramAnimation = new BukkitRunnable() {
			private Location loc = MapResourceGenerator.this.animation.getLocation();

			private float toAdd = 5.95F; // 11.3636F;
			private float yaw = 0;

			private int count = 0;
			private boolean up = false;

			@Override
			public void run() {
				if (this.up) { // Bottom -> Top
					this.loc.setY(this.loc.getY() + 0.025);
					this.yaw += this.toAdd + this.count;

				} else { // Top -> Bottom
					this.loc.setY(this.loc.getY() - 0.025);
					this.yaw -= this.toAdd + this.count;
				}

				if (this.count++ > 20) {
					this.count = 0;
					this.up = !this.up;
				}

				this.loc.setYaw(this.yaw);
				MapResourceGenerator.this.animation.teleport(this.loc);
			}
		}.runTaskTimerAsynchronously(Bedwars.getInstance(), 0, 1L);

		this.isDropping = true;
	}

	private void upgrade(int tier) {
		if (tier == this.tiers.size())
			return;

		this.current = this.tiers.get(tier - 1);
		this.tier = tier;

		if (this.hologram != null)
			this.hologram.setText(1, this.current.getDisplayName());

		if (this.dropTask != null)
			this.dropTask.cancel();

		this.dropTask = new BukkitRunnable() {

			@Override
			public void run() {
				Collection<Entity> nearby = MapResourceGenerator.this.loc.getWorld().getNearbyEntities(MapResourceGenerator.this.loc, 2.5, 2.5, 2.5);

				for (Entity entity : nearby) {
					if (entity.getType() != EntityType.DROPPED_ITEM)
						continue;

					ItemStack dropped = ((Item) entity).getItemStack();
					if (dropped.getType() != MapResourceGenerator.this.drop.getMaterial())
						continue;

					if (dropped.getAmount() >= MapResourceGenerator.this.current.getDropLimit())
						return;

					dropItem(MapResourceGenerator.this.item);
					return;
				}

				dropItem(MapResourceGenerator.this.item);
			}
		}.runTaskTimer(Bedwars.getInstance(), 0, this.current.getDropTime() * 20);

	}

	private void dropItem(ItemStack drop) {
		Item item = this.loc.getWorld().dropItem(this.loc, drop);
		item.setVelocity(I_BELIEVE_I_CAN_FLY);
	}

	@Override
	public void stop() {
		if (!this.isDropping)
			return;

		this.dropTask.cancel();
		this.hologramTask.cancel();
		this.hologramAnimation.cancel();
		this.hologram.remove();
		this.animation.remove();

		this.isDropping = false;
	}

	@Override
	public boolean isDropping() {
		return this.isDropping;
	}

}