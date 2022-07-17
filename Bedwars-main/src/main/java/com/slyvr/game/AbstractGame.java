package com.slyvr.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.google.common.base.Preconditions;
import com.slyvr.api.arena.Arena;
import com.slyvr.api.event.game.GameStateChangeEvent;
import com.slyvr.api.game.Game;
import com.slyvr.api.game.GameMode;
import com.slyvr.api.game.GameState;
import com.slyvr.arena.BedwarsArena;
import com.slyvr.exception.ArenaNotFoundException;
import com.slyvr.exception.ArenaNotReadyException;

public abstract class AbstractGame implements Game {

	protected static final Map<UUID, Game> PLAYERS_GAME = new HashMap<>();
	protected static final Map<UUID, Game> DISCONNECTED = new HashMap<>();

	private static final Map<Arena, Game> GAMES = new HashMap<>();

	private static final Random RANDOM = new Random();

	protected Arena arena;
	protected GameMode mode;
	protected GameState state;

	protected boolean hasStarted;

	public AbstractGame(Arena arena) {
		Preconditions.checkNotNull(arena, "Arena cannot be null!");

		if (!arena.exists())
			throw new ArenaNotFoundException("Arena doesn't exist!");

		if (!arena.isReady())
			throw new ArenaNotReadyException("Arena is not ready and cannot be used!");

		if (isArenaOccuped(arena))
			throw new IllegalStateException("Arena can only be used once per game!");

		this.arena = arena;
		this.mode = arena.getMode();
		this.state = GameState.WAITING;

		AbstractGame.GAMES.put(arena, this);
	}

	@Override
	public Arena getArena() {
		return this.arena;
	}

	@Override
	public boolean stopGame() {
		return AbstractGame.GAMES.remove(this.arena) != null;
	}

	@Override
	public GameState getGameState() {
		return this.state;
	}

	@Override
	public void setGameState(GameState state) {
		if (this.hasStarted || state == null)
			return;

		switch (state) {
			case COUNTDOWN:
			case WAITING:
				return;
			case RUNNING:
				startGame();
				break;
			case ENDED:
			case RESETTING:
				stopGame();
				break;
		}

		setState(state);
	}

	protected void setState(GameState state) {
		GameStateChangeEvent bwEvent = new GameStateChangeEvent(this, this.state, state);
		Bukkit.getPluginManager().callEvent(bwEvent);

		this.state = state;
	}

	@Override
	public GameMode getMode() {
		return this.mode;
	}

	@Override
	public boolean hasStarted() {
		return this.hasStarted;
	}

	/* static methods */
	public static Map<Arena, Game> getGames() {
		return new HashMap<>(AbstractGame.GAMES);
	}

	public static Game getPlayerGame(Player player) {
		return player != null ? AbstractGame.PLAYERS_GAME.get(player.getUniqueId()) : null;
	}

	public static Game getDisconnectedPlayerGame(Player player) {
		return player != null ? AbstractGame.DISCONNECTED.get(player.getUniqueId()) : null;
	}

	public static boolean inGame(Player player) {
		return getPlayerGame(player) != null;
	}

	public static boolean inRunningGame(Player player) {
		Game game = getPlayerGame(player);

		return game != null && game.hasStarted();
	}

	public static boolean isArenaOccuped(Arena arena) {
		return arena != null ? AbstractGame.GAMES.containsKey(arena) : false;
	}

	public static BedwarsArena randomArena(GameMode mode) {
		List<BedwarsArena> arenas = new ArrayList<>();
		for (BedwarsArena arena : BedwarsArena.getArenas()) {
			if (isArenaOccuped(arena))
				continue;

			GameMode arenaMode = arena.getMode();
			if ((arenaMode == null) || !arenaMode.equals(mode) || !arena.isReady())
				continue;

			arenas.add(arena);
		}

		return !arenas.isEmpty() ? arenas.get(AbstractGame.RANDOM.nextInt(arenas.size())) : null;
	}

}