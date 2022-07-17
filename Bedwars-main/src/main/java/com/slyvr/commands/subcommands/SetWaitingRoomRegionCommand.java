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

public class SetWaitingRoomRegionCommand implements SubCommand {

	@Override
	public String getName() {
		return "setWaitingRegion";
	}

	@Override
	public String getDescription() {
		return "Sets arena's waiting-room region!";
	}

	@Override
	public String getPermission() {
		return "bedwars.setup";
	}

	@Override
	public String getUsage() {
		return "/bw setWaitingRegion <Arena> <X-radius> <Y-radius> <Z-radius>";
	}

	@Override
	public void perform(Player player, String[] args) {
		if (args.length < 5) {
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
		if (!checkMinValue(x_radius, 0, "X radius must be greater than 0!", player))
			return;

		int y_radius = NumberConversions.toInt(args[3]);
		if (!checkMinValue(y_radius, 0, "Y radius must be atleast 0!", player))
			return;

		int z_radius = NumberConversions.toInt(args[4]);
		if (!checkMinValue(z_radius, 0, "Z radius must be atleast 0!", player))
			return;

		Location loc = player.getLocation();
		loc.setY(0);

		Location pos1 = loc.clone().add(x_radius, y_radius, z_radius);
		Location pos2 = loc.clone().subtract(x_radius, y_radius, z_radius);

		arena.setWaitingRoomRegion(new Region(pos1, pos2));

		player.sendMessage(ChatUtils.success("Waiting-room region has been set!"));
	}

	private boolean checkMinValue(int value, int min, String message, Player player) {
		if (value >= min)
			return true;

		player.sendMessage(ChatUtils.error(message));
		return false;
	}

}