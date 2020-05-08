package org.ncombat.web;

import java.util.HashMap;

@SuppressWarnings("unchecked")
public class GameStatusModel extends HashMap
{
	private static final String NAME_PROMPT = "ENTER YOUR NAME?";
	private static final String COMMAND_PROMPT = "CMDS?";
	private static final String MESSAGE_PROMPT = "MESSAGE?";
	
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
	
	public GameStatusModel promptForCommands() {
		setPrompt(COMMAND_PROMPT);
		setUrl(GameRestController.COMMAND_URL);
		return this;
	}
	
	public GameStatusModel promptForName() {
		setPrompt(NAME_PROMPT);
		setUrl(GameRestController.LOGIN_URL);
		return this;
	}
	
	public GameStatusModel promptForMessage() {
		setPrompt(MESSAGE_PROMPT);
		setUrl(GameRestController.MESSAGE_URL);
		return this;
	}
	
	public GameStatusModel markForDeath() {
		setPrompt(null);
		setAlive(false);
		return this;
	}
}
