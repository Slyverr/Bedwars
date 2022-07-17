package com.slyvr.upgrade;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.slyvr.api.game.Game;
import com.slyvr.api.game.player.GamePlayer;
import com.slyvr.api.generator.DropItem;
import com.slyvr.api.generator.Resource;
import com.slyvr.api.generator.TeamGenerator;
import com.slyvr.bedwars.Bedwars;

public final class ForgeUpgrade extends AbstractTieredUpgrade {

	public ForgeUpgrade() {
		super("Forge Upgrade", 0, 4);
	}

	private void increaseDropSpeed(TeamGenerator gen, Material material, int speed) {
		DropItem item = gen.getDrop(material);
		if (item == null)
			return;

		item.setDropsPerMinute(item.getDropsPerMinute() / speed);
	}

	private void addEmeraldDrop(Game game, TeamGenerator gen) {
		Resource resource = Resource.EMERALD;

		int limit = Bedwars.getInstance().getTeamForgeSettings().getDropLimit(game.getMode(), resource);
		if (limit <= 0)
			limit = 2;

		int speed = game.getArena().getGeneratorSpeed().getDropsPerMinute(resource);
		if (speed <= 0)
			speed = 60;

		gen.addDrop(new DropItem(new ItemStack(Material.EMERALD), speed, limit));
	}

	@Override
	public boolean apply(GamePlayer gp) {
		if (this.current == 0 || gp == null)
			return false;

		TeamGenerator gen = gp.getGame().getTeamGenerator(gp.getTeam());
		if (gen == null)
			return false;

		switch (this.current) {
			case 1:
				increaseDropSpeed(gen, Material.IRON_INGOT, 2);
				increaseDropSpeed(gen, Material.GOLD_INGOT, 2);
				break;
			case 2:
				increaseDropSpeed(gen, Material.IRON_INGOT, 2);
				increaseDropSpeed(gen, Material.GOLD_INGOT, 2);
				break;
			case 3:
				addEmeraldDrop(gp.getGame(), gen);
				break;
			case 4:
				increaseDropSpeed(gen, Material.EMERALD, 2);
				increaseDropSpeed(gen, Material.IRON_INGOT, 2);
				increaseDropSpeed(gen, Material.GOLD_INGOT, 2);
				break;
			default:
				return false;

		}

		return true;
	}

}