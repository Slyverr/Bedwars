package com.slyvr.hologram;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import com.google.common.base.Preconditions;
import com.slyvr.util.ChatUtils;

public final class BedwarsHologram implements Hologram {

	private static final Set<ArmorStand> holograms = new HashSet<>();

	private final List<ArmorStand> lines = new ArrayList<>();

	private final Location loc;
	private final double height;

	public BedwarsHologram(Location loc, double heightBetweenLines) {
		Preconditions.checkNotNull(loc, "Location cannot be null");

		this.loc = loc;
		this.height = heightBetweenLines;
	}

	public BedwarsHologram(Location loc) {
		this(loc, 0.4);
	}

	@Override
	public String getText(int line) {
		return isValidIndex(line) ? this.lines.get(line).getCustomName() : null;
	}

	@Override
	public void setText(int line, String text) {
		if (isValidIndex(line))
			this.lines.get(line).setCustomName(ChatUtils.format(text));

	}

	@Override
	public void addLine(String text) {
		createLine(this.loc.clone().add(0, this.lines.size() * this.height, 0), text);
	}

	@Override
	public void removeLine(int line) {
		if (!isValidIndex(line))
			return;

		Entity entity = this.lines.remove(line);
		BedwarsHologram.holograms.remove(entity);

		Location teleportTo = entity.getLocation().clone();
		for (int i = line; line < this.lines.size(); line++)
			this.lines.get(i).teleport(teleportTo.add(0, -this.height, 0));
	}

	@Override
	public List<String> getLines() {
		List<String> result = new ArrayList<>(this.lines.size());

		for (ArmorStand line : this.lines)
			result.add(line.getCustomName());

		return result;
	}

	@Override
	public void setLines(List<String> lines) {
		if (lines == null)
			return;

		Location teleportTo = this.loc.clone();
		for (int i = 0; i < lines.size(); i++)
			if (isValidIndex(i))
				this.lines.get(i).setCustomName(lines.get(i));
			else
				createLine(teleportTo.add(0, this.height, 0), lines.get(i));

	}

	@Override
	public void setVisible(int line, boolean visible) {
		if (isValidIndex(line))
			this.lines.get(line).setCustomNameVisible(visible);
	}

	@Override
	public boolean isVisible(int line) {
		return isValidIndex(line) && this.lines.get(line).isCustomNameVisible();
	}

	@Override
	public void remove() {
		for (ArmorStand line : this.lines) {
			BedwarsHologram.holograms.remove(line);
			line.remove();
		}

		this.lines.clear();
	}

	@Override
	public void teleport(Location loc) {
		if (loc == null)
			return;

		Location teleportTo = loc.clone();
		for (ArmorStand as : this.lines)
			as.teleport(teleportTo.add(0, this.height, 0));

	}

	@Override
	public int size() {
		return this.lines.size();
	}

	private void createLine(Location loc, String text) {
		ArmorStand as = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
		as.setCanPickupItems(false);
		as.setCustomNameVisible(true);
		as.setCustomName(ChatUtils.format(text));
		as.setVisible(false);
		as.setGravity(false);
		as.setSmall(true);

		this.lines.add(as);
		BedwarsHologram.holograms.add(as);
	}

	private boolean isValidIndex(int index) {
		return index >= 0 && index < this.lines.size();
	}

	public static boolean isHologram(Entity entity) {
		return BedwarsHologram.holograms.contains(entity);
	}

}