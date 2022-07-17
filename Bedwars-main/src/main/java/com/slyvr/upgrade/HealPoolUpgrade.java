package com.slyvr.upgrade;

import java.util.Collection;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import com.slyvr.api.arena.Region;
import com.slyvr.api.game.Game;
import com.slyvr.api.game.player.GamePlayer;
import com.slyvr.api.team.GameTeam;
import com.slyvr.bedwars.Bedwars;

public class HealPoolUpgrade extends AbstractUpgrade {

	private static final PotionEffect EFFECT = new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 0);

	public HealPoolUpgrade() {
		super("Heal Pool");
	}

	@Override
	public boolean apply(GamePlayer gp) {
		if (gp == null)
			return false;

		Game game = gp.getGame();
		if (game.isEliminated(gp.getTeam()))
			return false;

		GameTeam team = game.getGameTeam(gp.getTeam());
		if (team == null)
			return false;

		Location teamSpawn = game.getArena().getTeamSpawnPoint(gp.getTeam());
		if (teamSpawn == null)
			return false;

		new BukkitRunnable() {
			private Collection<GamePlayer> players = game.getTeamPlayers(gp.getTeam());
			private Region region = getRegionByPoint(teamSpawn, 20);

			@Override
			public void run() {
				if (!game.hasStarted() || game.isEliminated(gp.getTeam())) {
					this.cancel();
					return;
				}

				for (GamePlayer gp : this.players) {
					Player p = gp.getPlayer();

					if (game.isDisconnected(p))
						continue;

					if (game.isSpectator(p))
						continue;

					if (!this.region.isInside(p))
						continue;

					p.addPotionEffect(EFFECT);
				}

			}
		}.runTaskTimer(Bedwars.getInstance(), 0, 20);

		return true;
	}

	private Region getRegionByPoint(Location point, double radius) {
		Location pos1 = point.clone();
		pos1.add(radius, radius, radius);

		Location pos2 = point.clone();
		pos2.subtract(radius, radius, radius);

		return new Region(pos1, pos2);
	}

}