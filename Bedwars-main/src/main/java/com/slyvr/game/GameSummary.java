package com.slyvr.game;

import java.text.DecimalFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.slyvr.api.game.Game;
import com.slyvr.api.game.GameReward;
import com.slyvr.api.game.player.GamePlayer;
import com.slyvr.api.game.player.stats.GameStatistic;
import com.slyvr.api.game.player.stats.GameStatisticManager;
import com.slyvr.api.level.BedwarsLevel;
import com.slyvr.api.prestige.Prestige;
import com.slyvr.api.team.Team;
import com.slyvr.bedwars.Bedwars;
import com.slyvr.text.TextSection;
import com.slyvr.text.style.TextAlign;
import com.slyvr.util.LevelUtils;
import com.slyvr.util.ScoreboardUtils;
import com.slyvr.util.TextUtils;

// THE UGLY CLASS
public class GameSummary {

	private static final DecimalFormat FORMATTER = new DecimalFormat("###.##");
	private static final String EMPTY = "";

	private static final char CIRCLE = '\u30FB'; // ・

	private Map<Player, TextSection> rewards = new HashMap<>();

	private Game game;
	private Team winner;

	public GameSummary(Game game, Team winner) {

		this.game = game;
		this.winner = winner;
	}

	public Game getGame() {
		return this.game;
	}

	public Team getWinner() {
		return this.winner;
	}

	private String format(String first, Entry<Player, Integer> entry) {
		StringBuilder builder = new StringBuilder(first).append(" §7- ").append(entry.getKey().getDisplayName()).append("§7 - ").append(entry.getValue());

		return builder.toString();
	}

	private TextSection getGameSummary(Collection<GamePlayer> players) {
		List<Player> winners = new ArrayList<>();

		// I guess this is faster than sorting if i'm wrong contact me please!

		Object[] top_players = new Object[players.size() < 3 ? players.size() : 3];

		int length = top_players.length;
		int size = 0;
		for (GamePlayer gp : players) {
			Player player = gp.getPlayer();

			if (gp.getTeam() == this.winner)
				winners.add(player);

			GameStatisticManager stats = gp.getStatisticManager();
			int total = stats.getStatistic(GameStatistic.KILLS) + stats.getStatistic(GameStatistic.FINAL_KILLS);

			Entry<Player, Integer> entry = createEntry(player, total);
			if (size < length)
				top_players[size++] = entry;

			for (int i = 0; i < length; i++) {
				Object obj = top_players[i];
				if (obj == null)
					continue;

				Entry<Player, Integer> other = (Entry<Player, Integer>) obj;
				if (total <= other.getValue() || other.getKey().equals(player))
					continue;

				for (int j = length - 1; j > i; j--)
					top_players[j] = top_players[j - 1];

				top_players[i] = entry;
				break;
			}

			this.rewards.put(player, getRewardSummary(gp));
		}

		TextSection summary = new TextSection(4 * 64);
		summary.append("§a§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
		summary.append("§lBed Wars", TextAlign.CENTER);
		summary.append(GameSummary.EMPTY);

		if (!winners.isEmpty()) {
			StringBuilder builder = new StringBuilder(this.winner.getColoredString()).append(" §7- ");

			int winnersSize = winners.size();
			for (int i = 0; i < winnersSize; i++) {
				Player player = winners.get(i);

				builder.append(player.getDisplayName());

				if (i != winners.size() - 1)
					builder.append("§7,");
			}

			summary.append(builder.toString(), TextAlign.CENTER);
			summary.append(GameSummary.EMPTY);
		}

		if (length > 0)
			summary.append(format("§e§l1st Killer", (Entry<Player, Integer>) top_players[0]), TextAlign.CENTER);

		if (length > 1)
			summary.append(format("§6§l2nd Killer", (Entry<Player, Integer>) top_players[1]), TextAlign.CENTER);

		if (length > 2)
			summary.append(format("§c§l3rd Killer", (Entry<Player, Integer>) top_players[2]), TextAlign.CENTER);

		summary.append(EMPTY);
		summary.append("§a§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");

		return summary;
	}

	private TextSection getRewardSummary(GamePlayer gp) {
		GameStatisticManager stats = gp.getStatisticManager();

		TextSection rewardSection = new TextSection(4 * 64);
		rewardSection.append("§a§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
		rewardSection.append("§lReward Summary", TextAlign.CENTER);
		rewardSection.append(GameSummary.EMPTY);

		rewardSection.append("§7You earned", 2);
		rewardSection.append(CIRCLE + "" + ChatColor.GOLD + stats.getCoinsReward().getAmount() + " Bed Wars Coins", 4);

		rewardSection.append(GameSummary.EMPTY);
		appendLevelSection(rewardSection, gp);
		rewardSection.append(GameSummary.EMPTY);

		rewardSection.append("§7You earned §b" + stats.getExpReward().getAmount() + " Bed Wars Experience");

		rewardSection.append(GameSummary.EMPTY);
		rewardSection.append("§a§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");

		return rewardSection;
	}

	private void appendLevelSection(TextSection section, GamePlayer gp) {
		GameStatisticManager stats = gp.getStatisticManager();
		GameReward reward = stats.getExpReward();

		section.append("§bBed Wars Experience", TextAlign.CENTER);

		BedwarsLevel level = Bedwars.getInstance().getUser(gp.getPlayer()).getLevel();
		if (!level.isLeveling(reward.getAmount())) {
			level.incrementProgressExp(reward.getAmount());

			section.append(formatLevelLine(level, formatLevel(level.getLevel() + 1)), TextAlign.CENTER);
			section.append(level.getProgressBar(34), TextAlign.CENTER);
			section.append(formatLevelProgress(level), TextAlign.CENTER);

			return;
		}

		BedwarsLevel leveled = level.clone();

		Prestige prestige = LevelUtils.levelUp(leveled, reward.getAmount());
		if (prestige != null) {
			section.append(formatLevelToPrestigeLine(level, prestige), TextAlign.CENTER);
			section.append(BedwarsLevel.getProgressBar(34, 1F), TextAlign.CENTER);
			section.append(formatLevelUpToPrestigeLine(prestige), TextAlign.CENTER);

		} else {
			section.append(formatLevelToLevelLine(level, leveled), TextAlign.CENTER);
			section.append(BedwarsLevel.getProgressBar(34, 1F), TextAlign.CENTER);
			section.append(formatLevelUpToLevelLine(leveled), TextAlign.CENTER);

		}

	}

	private String formatLevelProgress(BedwarsLevel level) {
		StringBuilder builder = new StringBuilder()
				.append(ChatColor.AQUA)
				.append(ScoreboardUtils.formatDecimal(level.getProgressExp()))
				.append(ChatColor.GRAY)
				.append(" / ")
				.append(ChatColor.GREEN)
				.append(ScoreboardUtils.formatDecimal(level.getExpToLevelUp()))
				.append(ChatColor.GRAY)
				.append(" (")
				.append(FORMATTER.format(level.getProgressPercentage() * 100))
				.append("%)");

		return builder.toString();
	}

	private String formatLevelToPrestigeLine(BedwarsLevel start, Prestige end) {
		return formatLevelLine(start, end.getDisplayName());
	}

	private String formatLevelToLevelLine(BedwarsLevel start, BedwarsLevel end) {
		return formatLevelLine(start, ChatColor.AQUA + "Level " + end.getLevel());
	}

	private String formatLevelLine(BedwarsLevel start, String end) {
		String first = formatLevel(start.getLevel());

		int width = 211 - (TextUtils.getTextWidth(first) + TextUtils.getTextWidth(end));
		return new StringBuilder(first).append(TextUtils.emptyLine(width)).append(end).toString();
	}

	private String formatLevel(int level) {
		return ChatColor.AQUA + "Level " + level;
	}

	private String formatLevelUpToPrestigeLine(Prestige prestige) {
		return formatLevelUpLine(prestige.getDisplayName());
	}

	private String formatLevelUpToLevelLine(BedwarsLevel level) {
		return formatLevelUpLine(ScoreboardUtils.formatDecimal(level.getLevel()));
	}

	private String formatLevelUpLine(String text) {
		StringBuilder builder = new StringBuilder()
				.append(ChatColor.GOLD)
				.append(ChatColor.MAGIC)
				.append("aa ")
				.append(ChatColor.AQUA)
				.append(ChatColor.BOLD)
				.append("LEVEL UP!")
				.append(ChatColor.AQUA)
				.append("You are now ")
				.append(text)
				.append(ChatColor.AQUA)
				.append("!")
				.append(ChatColor.GOLD)
				.append(ChatColor.MAGIC)
				.append("aa ");

		return builder.toString();
	}

	public void send() {
		Collection<GamePlayer> players = this.game.getGamePlayers();

		TextSection summary = getGameSummary(players);
		for (GamePlayer gp : players) {
			Player player = gp.getPlayer();

			summary.sendMessage(player);
			this.rewards.get(player).sendMessage(player);
		}

	}

	private Entry<Player, Integer> createEntry(Player player, int total) {
		return new AbstractMap.SimpleEntry<>(player, total);
	}

}