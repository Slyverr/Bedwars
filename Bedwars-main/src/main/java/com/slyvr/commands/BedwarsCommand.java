package com.slyvr.commands;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.NumberConversions;

import com.slyvr.bedwars.BedwarsHelp;
import com.slyvr.commands.subcommands.AddResourceGeneratorCommand;
import com.slyvr.commands.subcommands.CreateCommand;
import com.slyvr.commands.subcommands.DisableCommand;
import com.slyvr.commands.subcommands.EnableCommand;
import com.slyvr.commands.subcommands.ModesCommand;
import com.slyvr.commands.subcommands.ReloadCommand;
import com.slyvr.commands.subcommands.RemoveCommand;
import com.slyvr.commands.subcommands.RemoveResourceGeneratorCommand;
import com.slyvr.commands.subcommands.ResourcesCommand;
import com.slyvr.commands.subcommands.SaveCommand;
import com.slyvr.commands.subcommands.SetDragonSpawnPointCommand;
import com.slyvr.commands.subcommands.SetGenSpeedCommand;
import com.slyvr.commands.subcommands.SetLevelCommand;
import com.slyvr.commands.subcommands.SetLobbySpawnCommand;
import com.slyvr.commands.subcommands.SetMapNameCommand;
import com.slyvr.commands.subcommands.SetModeCommand;
import com.slyvr.commands.subcommands.SetPrestigeCommand;
import com.slyvr.commands.subcommands.SetRegionCommand;
import com.slyvr.commands.subcommands.SetSpectatorSpawnCommand;
import com.slyvr.commands.subcommands.SetTeamBedCommand;
import com.slyvr.commands.subcommands.SetTeamChestCommand;
import com.slyvr.commands.subcommands.SetTeamGeneratorCommand;
import com.slyvr.commands.subcommands.SetTeamShopCommand;
import com.slyvr.commands.subcommands.SetTeamSpawnCommand;
import com.slyvr.commands.subcommands.SetTeamUpgradeCommand;
import com.slyvr.commands.subcommands.SetTimeCommand;
import com.slyvr.commands.subcommands.SetWaitingRoomRegionCommand;
import com.slyvr.commands.subcommands.SetWaitingSpawnCommand;
import com.slyvr.commands.subcommands.TeamsCommand;
import com.slyvr.util.ChatUtils;

import net.md_5.bungee.api.ChatColor;

public class BedwarsCommand implements CommandExecutor {

	private static final Map<String, SubCommand> SUB_COMMANDS = new LinkedHashMap<>();
	private static final List<SubCommand> SUB_COMMANDS_LIST = new ArrayList<>();

	static {
		/** Setup */
		SubCommand create = new CreateCommand();
		SUB_COMMANDS.put("create", create);

		SubCommand remove = new RemoveCommand();
		SUB_COMMANDS.put("remove", remove);

		/** Info */
		SubCommand set_region = new SetRegionCommand();
		SUB_COMMANDS.put("setregion", set_region);

		SubCommand set_waiting_region = new SetWaitingRoomRegionCommand();
		SUB_COMMANDS.put("setwaitingroomregion", set_waiting_region);
		SUB_COMMANDS.put("setwaitingregion", set_waiting_region);

		SubCommand set_waiting_spawn = new SetWaitingSpawnCommand();
		SUB_COMMANDS.put("setwaitingroomspawn", set_waiting_spawn);
		SUB_COMMANDS.put("setwaitingspawn", set_waiting_spawn);

		SubCommand set_spectator_spawn = new SetSpectatorSpawnCommand();
		SUB_COMMANDS.put("setspectatorspawn", set_spectator_spawn);

		SubCommand set_lobby_spawn = new SetLobbySpawnCommand();
		SUB_COMMANDS.put("setlobbyspawn", set_lobby_spawn);

		SubCommand set_dragon_spawn = new SetDragonSpawnPointCommand();
		SUB_COMMANDS.put("setdragonspawn", set_dragon_spawn);
		SUB_COMMANDS.put("setdragon", set_dragon_spawn);

		/** Settings */
		SubCommand set_generator_speed = new SetGenSpeedCommand();
		SUB_COMMANDS.put("setgeneratorspeed", set_generator_speed);
		SUB_COMMANDS.put("setgenspeed", set_generator_speed);

		SubCommand set_map_name = new SetMapNameCommand();
		SUB_COMMANDS.put("setmapname", set_map_name);
		SUB_COMMANDS.put("setmap", set_map_name);

		SubCommand set_mode = new SetModeCommand();
		SUB_COMMANDS.put("setgamemode", set_mode);
		SUB_COMMANDS.put("setmode", set_mode);

		SubCommand set_time = new SetTimeCommand();
		SUB_COMMANDS.put("settime", set_time);

		SubCommand enable = new EnableCommand();
		SUB_COMMANDS.put("enable", enable);

		SubCommand disable = new DisableCommand();
		SUB_COMMANDS.put("disable", disable);

		/** Teams */
		SubCommand set_team_spawn = new SetTeamSpawnCommand();
		SUB_COMMANDS.put("setteamspawn", set_team_spawn);

		SubCommand set_team_shop = new SetTeamShopCommand();
		SUB_COMMANDS.put("setteamshop", set_team_shop);

		SubCommand set_team_upgrade = new SetTeamUpgradeCommand();
		SUB_COMMANDS.put("setteamupgrade", set_team_upgrade);

		SubCommand set_team_chest = new SetTeamChestCommand();
		SUB_COMMANDS.put("setteamchest", set_team_chest);
		SUB_COMMANDS.put("setchest", set_team_chest);

		SubCommand set_team_generator = new SetTeamGeneratorCommand();
		SUB_COMMANDS.put("setteamgenerator", set_team_generator);
		SUB_COMMANDS.put("setteamgen", set_team_generator);

		SubCommand set_team_bed = new SetTeamBedCommand();
		SUB_COMMANDS.put("setteambed", set_team_bed);
		SUB_COMMANDS.put("setbed", set_team_bed);

		/** Arena */
		SubCommand add_resource_generator = new AddResourceGeneratorCommand();
		SUB_COMMANDS.put("addresourcegenerator", add_resource_generator);
		SUB_COMMANDS.put("addresourcegen", add_resource_generator);

		SubCommand remove_resource_generator = new RemoveResourceGeneratorCommand();
		SUB_COMMANDS.put("removeresourcegenerator", remove_resource_generator);
		SUB_COMMANDS.put("removeresourcegen", remove_resource_generator);

		/** User */
		SubCommand set_prestige = new SetPrestigeCommand();
		SUB_COMMANDS.put("setprestige", set_prestige);

		SubCommand set_level = new SetLevelCommand();
		SUB_COMMANDS.put("setlevel", set_level);

		/** Utils */

		// SubCommand map_selector = new MapSelectorCommand();
		// SUB_COMMANDS.put("mapselector", map_selector);
		// SUB_COMMANDS.put("maps", map_selector);

		SubCommand resources = new ResourcesCommand();
		SUB_COMMANDS.put("resources", resources);

		SubCommand teams = new TeamsCommand();
		SUB_COMMANDS.put("teams", teams);

		SubCommand modes = new ModesCommand();
		SUB_COMMANDS.put("modes", modes);

		/** Config */
		SubCommand reload = new ReloadCommand();
		SUB_COMMANDS.put("reload", reload);

		SubCommand save = new SaveCommand();
		SUB_COMMANDS.put("save", save);

		SUB_COMMANDS_LIST.add(create);
		SUB_COMMANDS_LIST.add(remove);

		SUB_COMMANDS_LIST.add(set_region);
		SUB_COMMANDS_LIST.add(set_waiting_region);
		SUB_COMMANDS_LIST.add(set_waiting_spawn);
		SUB_COMMANDS_LIST.add(set_spectator_spawn);
		SUB_COMMANDS_LIST.add(set_lobby_spawn);
		SUB_COMMANDS_LIST.add(set_dragon_spawn);

		SUB_COMMANDS_LIST.add(set_generator_speed);
		SUB_COMMANDS_LIST.add(set_map_name);
		SUB_COMMANDS_LIST.add(set_mode);
		SUB_COMMANDS_LIST.add(set_time);
		SUB_COMMANDS_LIST.add(enable);
		SUB_COMMANDS_LIST.add(disable);

		SUB_COMMANDS_LIST.add(set_team_spawn);
		SUB_COMMANDS_LIST.add(set_team_shop);
		SUB_COMMANDS_LIST.add(set_team_upgrade);
		SUB_COMMANDS_LIST.add(set_team_chest);
		SUB_COMMANDS_LIST.add(set_team_generator);
		SUB_COMMANDS_LIST.add(set_team_bed);

		SUB_COMMANDS_LIST.add(add_resource_generator);
		SUB_COMMANDS_LIST.add(remove_resource_generator);

		SUB_COMMANDS_LIST.add(resources);
		SUB_COMMANDS_LIST.add(teams);
		SUB_COMMANDS_LIST.add(modes);

		SUB_COMMANDS_LIST.add(set_level);
		SUB_COMMANDS_LIST.add(set_prestige);
		SUB_COMMANDS_LIST.add(set_level);

		SUB_COMMANDS_LIST.add(reload);
		SUB_COMMANDS_LIST.add(save);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("§cOnly players who can execute this command!");
			return true;
		}

		Player player = (Player) sender;
		if (args.length == 0) {
			player.sendMessage(ChatUtils.info("/bw help/? <page>"));
			player.sendMessage(ChatUtils.info("/bw help/? <command>"));
			return true;
		}

		String first = args[0].toLowerCase();
		if (first.equals("help") || first.equals("?")) {
			sendHelp(player, args);
			return true;
		}

		SubCommand sub = BedwarsCommand.SUB_COMMANDS.get(first);
		if (sub == null)
			return true;

		if (!player.hasPermission("bw.admin") && !player.hasPermission(sub.getPermission())) {
			player.sendMessage(ChatColor.RED + "You don't have the permission to execute this command!");
			return true;
		}

		sub.perform(player, args);
		return true;
	}

	public static List<SubCommand> getSubCommandsList() {
		return BedwarsCommand.SUB_COMMANDS_LIST;
	}

	private void sendHelp(Player player, String[] args) {
		if (args.length == 1) {
			BedwarsHelp.send(player);
			return;
		}

		SubCommand sub = SUB_COMMANDS.get(args[1].toLowerCase());
		if (sub != null) {
			BedwarsHelp.send(player, sub);
			return;
		}

		int page = NumberConversions.toInt(args[1]);
		if (!BedwarsHelp.isValidPage(page)) {
			player.sendMessage(ChatUtils.error("Help page must be between 1 and " + BedwarsHelp.MAX_PAGE + "!"));
			return;
		}

		BedwarsHelp.send(player, page);
	}

}