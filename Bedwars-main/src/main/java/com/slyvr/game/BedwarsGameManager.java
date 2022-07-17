package com.slyvr.game;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import com.google.common.base.Preconditions;
import com.slyvr.api.arena.Arena;
import com.slyvr.api.game.Game;
import com.slyvr.api.game.GameManager;
import com.slyvr.api.game.GamePhase;
import com.slyvr.api.game.player.GamePlayer;
import com.slyvr.api.scoreboard.Scoreboard.AnimatedTitle;
import com.slyvr.api.team.GameTeam;
import com.slyvr.api.team.Team;
import com.slyvr.api.trap.Trap;
import com.slyvr.api.trap.TrapManager;
import com.slyvr.api.user.Statistic;
import com.slyvr.api.user.User;
import com.slyvr.api.user.UserStatistics;
import com.slyvr.bedwars.Bedwars;
import com.slyvr.bedwars.settings.GameSettings;
import com.slyvr.scoreboard.game.GameScoreboard;
import com.slyvr.util.ScoreboardUtils;

public class BedwarsGameManager implements GameManager {

	private Set<UUID> TRAP_SAFE = new HashSet<>();

	private List<GamePhase> phases;

	private final Game game;
	private BukkitTask task;
	private BukkitTask titleTask;

	private GameScoreboard board;
	private GamePhase current;

	private long nextPhaseTime;
	private long currentTime;
	private long gameLength;

	private boolean isCancelled = true;

	public BedwarsGameManager(Game game) {
		Preconditions.checkNotNull(game, "Game cannot be null!");

		this.game = game;
		this.phases = Bedwars.getInstance().getGameSettings().getDefaultGamePhases();

		for (GamePhase phase : this.phases)
			this.gameLength += phase.getDuration();

		this.board = Bedwars.getInstance().getGameScoreboard(game.getMode());
	}

	@Override
	public Game getGame() {
		return this.game;
	}

	@Override
	public boolean start() {
		if (!this.isCancelled)
			return false;

		for (Player player : this.game.getPlayers()) {
			User user = Bedwars.getInstance().getUser(player);

			UserStatistics stats = user.getStatistics(this.game.getMode());
			if (stats != null)
				stats.incrementStatistic(Statistic.GAME_PLAYED, 1);
		}

		Iterator<GamePhase> iterator = this.phases.iterator();
		this.task = new BukkitRunnable() {

			@Override
			public void run() {
				if (BedwarsGameManager.this.nextPhaseTime == 0) {
					if (BedwarsGameManager.this.current != null)
						BedwarsGameManager.this.current.apply(BedwarsGameManager.this.game);

					if (!iterator.hasNext()) {
						BedwarsGameManager.this.game.stopGame();
						return;
					}

					BedwarsGameManager.this.current = iterator.next();
					BedwarsGameManager.this.nextPhaseTime = BedwarsGameManager.this.current.getDuration();
				}

				BedwarsGameManager.this.currentTime++;
				BedwarsGameManager.this.nextPhaseTime--;

				if (BedwarsGameManager.this.board != null)
					BedwarsGameManager.this.board
							.setPhaseText(BedwarsGameManager.this.current.getName() + " in " + ChatColor.GREEN
									+ ScoreboardUtils.formatCountdown(BedwarsGameManager.this.nextPhaseTime));

				GameSettings settings = Bedwars.getInstance().getGameSettings();

				Collection<GamePlayer> game_players = BedwarsGameManager.this.game.getGamePlayers();
				for (GamePlayer gp : game_players) {
					Player player = gp.getPlayer();

					if (BedwarsGameManager.this.board != null)
						Bukkit.getScheduler().runTask(Bedwars.getInstance(), () -> BedwarsGameManager.this.board.update(gp));

					if (!BedwarsGameManager.this.game.isSpectator(player)) {
						if (BedwarsGameManager.this.currentTime % settings.timePlayedForExpReward() == 0) {

							int exp = settings.getExpReward();

							gp.getStatisticManager().getExpReward().increment(exp);
							player.sendMessage("§b+" + exp + " Bed Wars Experience (Time Played)");
						}

						if (BedwarsGameManager.this.currentTime % settings.timePlayedForCoinsReward() == 0) {
							int coins = settings.getCoinsReward();

							gp.getStatisticManager().getCoinsReward().increment(coins);
							player.sendMessage("§6+" + coins + " coins! (Time Played)");
						}

					}

				}

				Arena arena = BedwarsGameManager.this.game.getArena();

				Collection<GameTeam> teams = BedwarsGameManager.this.game.getTeams();
				for (GameTeam team : teams) {
					Location spawn = arena.getTeamSpawnPoint(team.getTeam());
					if (spawn == null)
						continue;

					TrapManager manager = team.getTrapManager();

					List<Trap> traps = manager.getTraps();
					if (traps.isEmpty())
						continue;

					for (Player player : spawn.getWorld().getEntitiesByClass(Player.class)) {
						if ((player.getLocation().distanceSquared(spawn) > 400) || BedwarsGameManager.this.TRAP_SAFE.contains(player.getUniqueId())
								|| BedwarsGameManager.this.game.isSpectator(player))
							continue;

						GamePlayer gp = BedwarsGameManager.this.game.getGamePlayer(player);
						if (gp == null || gp.getTeam() == team.getTeam())
							continue;

						for (Trap trap : traps) {
							if (!onTrigger(trap, team.getTeam(), gp))
								continue;

							manager.removeTrap(trap);
							break;
						}

					}
				}

			}
		}.runTaskTimer(Bedwars.getInstance(), 0, 20);

		if (this.board != null) {
			AnimatedTitle title = this.board.getTitle();

			if (title.getUpdateTicks() > 0) {
				this.titleTask = new BukkitRunnable() {

					@Override
					public void run() {
						for (Player player : BedwarsGameManager.this.game.getPlayers()) {
							Scoreboard board = player.getScoreboard();
							if (board == null)
								continue;

							Objective obj = board.getObjective(DisplaySlot.SIDEBAR);
							if (obj != null)
								obj.setDisplayName(title.next());
						}
					}
				}.runTaskTimerAsynchronously(Bedwars.getInstance(), 0, title.getUpdateTicks());
			}

		}

		this.isCancelled = false;
		return true;
	}

	private boolean onTrigger(Trap trap, Team team, GamePlayer gp) {
		if (!trap.onTrigger(gp, team))
			return false;

		this.TRAP_SAFE.add(gp.getPlayer().getUniqueId());

		Bukkit.getScheduler().runTaskLaterAsynchronously(Bedwars.getInstance(), () -> {
			this.TRAP_SAFE.remove(gp.getPlayer().getUniqueId());
		}, 20 * trap.getDuration());

		return true;
	}

	@Override
	public boolean stop() {
		if (this.isCancelled || !this.game.hasStarted())
			return false;

		if (this.task != null)
			this.task.cancel();

		if (this.titleTask != null)
			this.titleTask.cancel();

		this.isCancelled = true;
		return true;
	}

	@Override
	public GamePhase getCurrentPhase() {
		return this.current;
	}

	@Override
	public long timeLeftForNextPhase() {
		return this.nextPhaseTime;
	}

	@Override
	public long gameLength() {
		return this.gameLength;
	}

	@Override
	public long currentTime() {
		return this.currentTime;
	}

	@Override
	public long timeLeft() {
		return this.gameLength - this.currentTime;
	}

}