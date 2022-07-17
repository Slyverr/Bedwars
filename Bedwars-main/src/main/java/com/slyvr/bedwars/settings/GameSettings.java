package com.slyvr.bedwars.settings;

import java.util.ArrayList;
import java.util.List;

import com.slyvr.api.game.GamePhase;

public class GameSettings {

	private static GameSettings instance = new GameSettings();

	private List<GamePhase> phases = new ArrayList<>();

	private boolean showFuseTick = false;

	private float tntPower = 4F;
	private int tntDelay = 60;
	private float tntKb = 2F;

	private float fireballPower = 4F;
	private float fireballSpeed = 2F;
	private float fireballKb = 2F;

	private long timePlayedForExp = 60;
	private int expReward = 25;

	private long timePlayedForCoins = 60;
	private int coinsReward = 10;

	private GameSettings() {
	}

	public List<GamePhase> getDefaultGamePhases() {
		return this.phases;
	}

	public void setDefaultGamePhases(List<GamePhase> phases) {
		if (phases != null)
			this.phases.addAll(phases);
	}

	public boolean showTNTFuseTicks() {
		return this.showFuseTick;
	}

	public void setShowTNTFuseTicks(boolean bool) {
		this.showFuseTick = bool;
	}

	public float getTNTExplosionPower() {
		return this.tntPower;
	}

	public void setTNTExplosionPower(float power) {
		if (power > 0 && power <= 50)
			this.tntPower = power;
	}

	public float getTNTExplosionKb() {
		return this.tntKb;
	}

	public void setTNTExplosionKb(float kb) {
		this.tntKb = kb;
	}

	public int getTNTFuseTicks() {
		return this.tntDelay;
	}

	public void setTNTFuseTicks(int ticks) {
		if (ticks > 0)
			this.tntDelay = ticks;
	}

	public float getFireballExplosionPower() {
		return this.fireballPower;
	}

	public void setFireballExplosionPower(float power) {
		if (power > 0 && power <= 50)
			this.fireballPower = power;
	}

	public float getFireballExplosionKb() {
		return this.fireballKb;
	}

	public void setFireballExplosionKb(float kb) {
		this.fireballKb = kb;
	}

	public float getFireballSpeed() {
		return this.fireballSpeed;
	}

	public void setFireballSpeed(float speed) {
		if (speed > 0)
			this.fireballSpeed = speed;
	}

	public long timePlayedForExpReward() {
		return this.timePlayedForExp;
	}

	public void setTimeForExpReward(long time) {
		if (time > 0)
			this.timePlayedForExp = time;
	}

	public int getExpReward() {
		return this.expReward;
	}

	public void setExpReward(int exp) {
		if (exp >= 0)
			this.expReward = exp;
	}

	public long timePlayedForCoinsReward() {
		return this.timePlayedForCoins;
	}

	public void setTimeForCoinsReward(long time) {
		if (time > 0)
			this.timePlayedForCoins = time;
	}

	public int getCoinsReward() {
		return this.coinsReward;
	}

	public void setCoinsReward(int coins) {
		if (coins >= 0)
			this.coinsReward = coins;
	}

	public static GameSettings getInstance() {
		return GameSettings.instance;
	}

}