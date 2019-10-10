package csce315.kalah.model;

import csce315.kalah.Main.APP_MODE;
import csce315.kalah.model.Player.PLAYER_TYPE;

public class GameOptions {
	private APP_MODE appMode = null;
	private PLAYER_TYPE player0Type = PLAYER_TYPE.Human;
	private PLAYER_TYPE player1Type = PLAYER_TYPE.Human;
	private int player0ThreadId = -1;
	private int player1ThreadId = -1;
	private int numPlayerPits = 6;
	private int numStones = 4;
	private int timeLimit = 0;
	private Boolean randomStones = false;
	
	public APP_MODE getAppMode() {
		return appMode;
	}
	public void setAppMode(APP_MODE appMode) {
		this.appMode = appMode;
	}
	public PLAYER_TYPE getPlayer0Type() {
		return player0Type;
	}
	public void setPlayer0Type(PLAYER_TYPE player0Type) {
		this.player0Type = player0Type;
	}
	public PLAYER_TYPE getPlayer1Type() {
		return player1Type;
	}
	public void setPlayer1Type(PLAYER_TYPE player1Type) {
		this.player1Type = player1Type;
	}
	public int getPlayer0ThreadId() {
		return player0ThreadId;
	}
	public void setPlayer0ThreadId(int player0ThreadId) {
		this.player0ThreadId = player0ThreadId;
	}
	public int getPlayer1ThreadId() {
		return player1ThreadId;
	}
	public void setPlayer1ThreadId(int player1ThreadId) {
		this.player1ThreadId = player1ThreadId;
	}
	public int getNumPlayerPits() {
		return numPlayerPits;
	}
	public void setNumPlayerPits(int numPlayerPits) {
		this.numPlayerPits = numPlayerPits;
	}
	public int getNumStones() {
		return numStones;
	}
	public void setNumStones(int numStones) {
		this.numStones = numStones;
	}
	public Boolean getRandomStones() {
		return randomStones;
	}
	public void setRandomStones(Boolean randomStones) {
		this.randomStones = randomStones;
	}
	public int getTimeLimit() {
		return timeLimit;
	}
	public void setTimeLimit(int timeLimit) {
		this.timeLimit = timeLimit;
	}
}