package com.googlecode.ncombat;

import com.googlecode.ncombat.command.CommandBatch;

public class Combatant
{
	private static int nextId = 1;
	
	private static synchronized int getNextId() {
		return nextId++;
	}
	
	private int id;
	private long lastUpdateTime;
	private boolean alive;
	
	private GameServer gameServer;
	
	public Combatant() {
		this.id = getNextId();
	}
	
	public void update(long updateTime) {
	}
	
	public void processCommands(CommandBatch commandBatch) {
	}
	
	public void completeGameCycle() {
	}
	
	public int getId() {
		return id;
	}

	public GameServer getGameServer() {
		return gameServer;
	}

	public void setGameServer(GameServer gameServer) {
		this.gameServer = gameServer;
	}

	public long getLastUpdateTime() {
		return lastUpdateTime;
	}

	public void setLastUpdateTime(long lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
	}

	public boolean isAlive() {
		return alive;
	}

	public void setAlive(boolean alive) {
		this.alive = alive;
	}
}
