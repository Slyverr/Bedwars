package com.slyvr.bedwars.settings;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.slyvr.api.game.GameMode;
import com.slyvr.api.generator.GeneratorSpeed;
import com.slyvr.api.generator.Resource;

public class TeamForgeSettings {

	private static TeamForgeSettings instance;

	private Map<GameMode, Map<Resource, Integer>> drop_limit = new HashMap<>();
	private Map<String, GeneratorSpeed> speeds = new HashMap<>();
	private Set<Resource> team_drops = new HashSet<>();

	private boolean resourceSplit = true;
	private double resourceSplitR = 1.5D;

	// DEFAULT VALUES
	private TeamForgeSettings() {
		GeneratorSpeed speed1 = new GeneratorSpeed("Slow");
		speed1.setDropsPerMinute(Resource.IRON, 34);
		speed1.setDropsPerMinute(Resource.GOLD, 11);

		GeneratorSpeed speed2 = new GeneratorSpeed("Fast");
		speed2.setDropsPerMinute(Resource.IRON, 48);
		speed2.setDropsPerMinute(Resource.GOLD, 75);

		this.speeds.put(speed1.getName(), speed1);
		this.speeds.put(speed2.getName(), speed2);

		for (GameMode mode : GameMode.values()) {
			int max = mode.getTeamMax();

			setDropLimit(mode, Resource.IRON, max >= 3 ? 64 : 48);
			setDropLimit(mode, Resource.GOLD, 12);

			setDropLimit(mode, Resource.DIAMOND, max >= 3 ? 8 : 4);
			setDropLimit(mode, Resource.DIAMOND, max >= 3 ? 4 : 2);
		}

		this.team_drops.add(Resource.IRON);
		this.team_drops.add(Resource.GOLD);
	}

	public boolean isResourceSplitting() {
		return this.resourceSplit;
	}

	public void setResourceSplitting(boolean split) {
		this.resourceSplit = split;
	}

	public double getSplitRadius() {
		return this.resourceSplitR;
	}

	public void setSplitRadius(double radius) {
		if (radius > 0)
			this.resourceSplitR = radius;
	}

	public int getDropLimit(GameMode mode, Resource rsc) {
		if (mode == null || rsc == null)
			return 0;

		Map<Resource, Integer> map = this.drop_limit.get(mode);
		return map != null && map.containsKey(rsc) ? map.get(rsc) : 0;
	}

	public void setDropLimit(GameMode mode, Resource rsc, int limit) {
		if (mode == null || rsc == null || limit < 0)
			return;

		Map<Resource, Integer> map = this.drop_limit.get(mode);
		if (map == null)
			this.drop_limit.put(mode, map = new HashMap<>());

		map.put(rsc, limit);
	}

	public Collection<GeneratorSpeed> getGeneratorsSpeed() {
		return new HashSet<>(this.speeds.values());
	}

	public GeneratorSpeed getGeneratorSpeed(String name) {
		return name != null ? this.speeds.get(name.toLowerCase()) : null;
	}

	public void setGeneratorSpeed(GeneratorSpeed speed) {
		if (speed != null)
			this.speeds.put(speed.getName().toLowerCase(), speed);
	}

	public Set<Resource> getTeamDrops() {
		return new HashSet<>(this.team_drops);
	}

	public void setTeamDrops(Set<Resource> drops) {
		if (drops == null || drops.isEmpty())
			return;

		Set<Resource> result = new HashSet<>(drops.size());
		for (Resource resource : drops) {
			if (resource == null)
				continue;

			this.team_drops.add(resource);
		}

		if (!result.isEmpty())
			this.team_drops = result;
	}

	public static TeamForgeSettings getInstance() {
		if (instance == null)
			instance = new TeamForgeSettings();

		return TeamForgeSettings.instance;
	}

}