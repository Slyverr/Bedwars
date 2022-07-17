package com.slyvr.bedwars.settings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.slyvr.api.game.GameMode;
import com.slyvr.api.generator.GeneratorTier;
import com.slyvr.api.generator.Resource;

public class MapForgeSettings {

	private static final MapForgeSettings INSTANCE = new MapForgeSettings();

	private Map<GameMode, Map<Resource, List<GeneratorTier>>> tiers = new HashMap<>();

	private MapForgeSettings() {
		for (GameMode mode : GameMode.values()) {
			Map<Resource, List<GeneratorTier>> tiersMap = new HashMap<>(2);

			int limit1 = mode != GameMode.SOLO && mode != GameMode.DUO ? 8 : 4;

			List<GeneratorTier> tiers = new ArrayList<>();
			tiers.add(new GeneratorTier("&eTier &cI", 30, limit1));
			tiers.add(new GeneratorTier("&eTier &cII", 23, limit1));
			tiers.add(new GeneratorTier("&eTier &cIII", 12, limit1));

			int limit2 = mode != GameMode.SOLO && mode != GameMode.DUO ? 4 : 2;

			List<GeneratorTier> tiers2 = new ArrayList<>();
			tiers2.add(new GeneratorTier("&eTier &cI", 60, limit2));
			tiers2.add(new GeneratorTier("&eTier &cII", 45, limit2));
			tiers2.add(new GeneratorTier("&eTier &cIII", 30, limit2));

			tiersMap.put(Resource.DIAMOND, tiers);
			tiersMap.put(Resource.EMERALD, tiers2);

			this.tiers.put(mode, tiersMap);
		}

	}

	public Map<Resource, List<GeneratorTier>> getTiers(GameMode mode) {
		if (mode == null)
			return null;

		Map<Resource, List<GeneratorTier>> result = this.tiers.get(mode);
		return result != null ? new HashMap<>(result) : null;
	}

	public void setTiers(GameMode mode, Map<Resource, List<GeneratorTier>> tiers) {
		if (mode != null && tiers != null)
			this.tiers.put(mode, tiers);
	}

	public List<GeneratorTier> getGeneratorTiers(GameMode mode, Resource resource) {
		if (mode == null || resource == null)
			return null;

		Map<Resource, List<GeneratorTier>> result = this.tiers.get(mode);
		if (result == null)
			return null;

		List<GeneratorTier> tiers = result.get(resource);
		return tiers != null ? new ArrayList<>(tiers) : null;
	}

	public void setGeneratorTiers(GameMode mode, Resource resource, List<GeneratorTier> tiers) {
		if (mode == null || resource == null || tiers == null)
			return;

		Map<Resource, List<GeneratorTier>> result = this.tiers.get(mode);
		if (result == null)
			this.tiers.put(mode, result = new HashMap<>());

		result.put(resource, tiers);
	}

	public static MapForgeSettings getInstance() {
		return MapForgeSettings.INSTANCE;
	}

}