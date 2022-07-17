package com.slyvr.commands.subcommands;

import org.bukkit.entity.Player;

import com.slyvr.api.game.GameMode;
import com.slyvr.arena.BedwarsArena;
import com.slyvr.commands.SubCommand;
import com.slyvr.game.AbstractGame;
import com.slyvr.util.ChatUtils;

public class SetModeCommand implements SubCommand {

	@Override
	public String getName() {
		return "setMode";
	}

	@Override
	public String getDescription() {
		return "Sets arena game mode!";
	}

	@Override
	public String getPermission() {
		return "bedwars.setup";
	}

	@Override
	public String getUsage() {
		return "/bw setMode <Arena> <GameMode>";
	}

	@Override
	public void perform(Player player, String[] args) {
		if (args.length < 3) {
			player.sendMessage(ChatUtils.usage(getUsage()));
			return;
		}

		BedwarsArena arena = BedwarsArena.getArena(args[1]);
		if (arena == null || !arena.exists()) {
			player.sendMessage(ChatUtils.error("Arena with name §e" + args[1] + " §cdoesn't exist!"));
			return;
		}

		if (AbstractGame.isArenaOccuped(arena)) {
			player.sendMessage(ChatUtils.error("§e" + args[1] + " §is already in use and cannot be edited!"));
			return;
		}

		GameMode mode = GameMode.getByName(args[2]);
		if (mode == null) {
			player.sendMessage(ChatUtils.error("Invalid GameMode!"));
			player.sendMessage(ChatUtils.info("/bw modes"));
			return;
		}

		arena.setMode(mode);
		arena.saveConfig();

		player.sendMessage(ChatUtils.success("Arena gamemode has been set to §e" + mode.getName() + "§a!"));
	}

}
