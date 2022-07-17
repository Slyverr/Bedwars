package com.slyvr.commands.subcommands;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.NumberConversions;

import com.slyvr.api.arena.Region;
import com.slyvr.arena.BedwarsArena;
import com.slyvr.commands.SubCommand;
import com.slyvr.game.AbstractGame;
import com.slyvr.util.ChatUtils;

public class SetRegionCommand implements SubCommand {

	@Override
	public String getName() {
		return "setRegion";
	}

	@Override
	public String getDescription() {
		return "Sets arena's region!";
	}

	@Override
	public String getPermission() {
		return "bedwars.setup";
	}

	@Override
	public String getUsage() {
		return "/bw setRegion <Arena> <X-radius> <Z-radius> <Y-max> <Y-min>";
	}

	@Override
	public void perform(Player player, String[] args) {
		if (args.length < 6) {
			player.sendMessage(ChatUtils.usage(getUsage()));
			return;
		}

		BedwarsArena arena = BedwarsArena.getArena(args[1]);
		if (arena == null || !arena.exists()) {
			player.sendMessage(ChatUtils.error("Arena with name " + ChatColor.YELLOW + args[1] + ChatColor.RED + " doesn't exist!"));
			return;
		}

		if (AbstractGame.isArenaOccuped(arena)) {
			player.sendMessage(ChatUtils.error(ChatColor.YELLOW + args[1] + ChatColor.RED + " is already in use and cannot be edited!"));
			return;
		}

		int x_radius = NumberConversions.toInt(args[2]);
		if (!checkMinValue(x_radius, 20, "X radius must be atleast 20", player))
			return;

		int z_radius = NumberConversions.toInt(args[3]);
		if (!checkMinValue(z_radius, 20, "Z radius must be atleast 20", player))
			return;

		int y_max = NumberConversions.toInt(args[4]);
		int y_min = NumberConversions.toInt(args[5]);

		if (y_max < y_min) {
			player.sendMessage(ChatUtils.error("Y-max must be bigger than Y-min"));
			return;
		}

		Location loc = player.getLocation();
		loc.setY(0);

		Location pos1 = expand(loc, x_radius, y_max, z_radius);
		Location pos2 = expand(loc, -x_radius, y_min, -z_radius);

		arena.setArenaRegion(new Region(pos1, pos2));

		player.sendMessage(ChatUtils.success("Arena region has been set!"));
	}

	private Location expand(Location loc, double x, double y, double z) {

		return loc.clone().add(loc.getX() >= 0 ? x : -x, loc.getY() >= 0 ? y : -y, loc.getZ() >= 0 ? z : -z);
	}

	private boolean checkMinValue(int value, int min, String message, Player player) {
		if (value >= min)
			return true;

		player.sendMessage(ChatUtils.error(message));
		return false;
	}

}