package com.slyvr.upgrade;

import java.util.Collection;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.slyvr.api.game.player.GamePlayer;

public class TieredEnchantmentUpgrade extends AbstractTieredUpgrade {

	private Enchantment ench;
	private boolean unsafe;

	public TieredEnchantmentUpgrade(String name, Enchantment ench, int max, boolean unsafe) {
		super(name, 0, max);

		this.ench = ench;
		this.unsafe = unsafe;
	}

	@Override
	public boolean apply(GamePlayer gp) {
		if (this.current == 0 || gp == null)
			return false;

		Collection<GamePlayer> team_players = gp.getGame().getTeamPlayers(gp.getTeam());
		if (team_players.isEmpty())
			return false;

		for (GamePlayer inGame : team_players) {
			Player player = inGame.getPlayer();

			for (ItemStack item : player.getEquipment().getArmorContents()) {
				if (item == null)
					continue;

				if (this.unsafe)
					item.addUnsafeEnchantment(this.ench, this.current);
				else if (this.ench.canEnchantItem(item))
					item.addEnchantment(this.ench, this.current);
			}

			for (ItemStack item : player.getInventory()) {
				if (item == null)
					continue;

				if (this.unsafe)
					item.addUnsafeEnchantment(this.ench, this.current);
				else if (this.ench.canEnchantItem(item))
					item.addEnchantment(this.ench, this.current);
			}
		}

		return true;
	}

}