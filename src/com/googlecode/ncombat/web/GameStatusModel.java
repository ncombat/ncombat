package com.googlecode.ncombat.web;

import java.util.HashMap;

public class GameStatusModel extends HashMap
{
	private GameStatus gameStatus = new GameStatus();
	
	public GameStatusModel() {
		put("data", gameStatus);
		put("success", true);
	}
	
	public void addMessage(String message) {
		gameStatus.addMessage(message);
	}

	public GameStatus getGameStatus() {
		return gameStatus;
	}

	public void setGameStatus(GameStatus gameStatus) {
		this.gameStatus = gameStatus;
	}
	
	public boolean isAlive() {
		return gameStatus.isAlive();
	}

	public void setAlive(boolean alive) {
		gameStatus.setAlive(alive);
	}

	public boolean getSuccess() {
		return (Boolean) get("success");
	}

	public void setSuccess(boolean success) {
		put("success", success);
	}

	public String getPrompt() {
		return gameStatus.getPrompt();
	}

	public void setPrompt(String prompt) {
		gameStatus.setPrompt(prompt);
	}

	public String getUrl() {
		return gameStatus.getUrl();
	}

	public void setUrl(String url) {
		gameStatus.setUrl(url);
	}
}
