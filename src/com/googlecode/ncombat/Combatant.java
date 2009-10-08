package com.googlecode.ncombat;

public class Combatant
{
	private static int nextId = 1;
	
	private static synchronized int getNextId() {
		return nextId++;
	}
	
	private int id;
	
	private GameServer gameServer;
	
	public Combatant() {
		this.id = getNextId();
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
}
