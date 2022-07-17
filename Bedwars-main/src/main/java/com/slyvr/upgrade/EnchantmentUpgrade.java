package com.slyvr.upgrade;

import java.util.Collection;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.slyvr.api.game.Game;
import com.slyvr.api.game.player.GamePlayer;
import com.slyvr.api.upgrade.Upgrade;

public class EnchantmentUpgrade extends AbstractUpgrade implements Upgrade {

	private Enchantment ench;
	private int level;

	private boolean unsafe;

	public EnchantmentUpgrade(String name, Enchantment ench, int level, boolean unsafe) {
		super(name);

		this.ench = ench;
		this.level = level;
		this.unsafe = unsafe;
	}

	@Override
	public boolean apply(GamePlayer gp) {
		if (gp == null)
			return false;

		Game game = gp.getGame();
		if (game == null)
			return false;

		Collection<GamePlayer> team_players = game.getTeamPlayers(gp.getTeam());
		if (team_players.isEmpty())
			return false;

		for (GamePlayer inGame : team_players) {
			Player player = inGame.getPlayer();

			for (ItemStack item : player.getInventory()) {
				if (item == null)
					continue;

				if (this.unsafe)
					item.addUnsafeEnchantment(this.ench, this.level);
				else if (this.ench.canEnchantItem(item))
					item.addEnchantment(this.ench, this.level);
			}

		}

		return true;
	}
}
