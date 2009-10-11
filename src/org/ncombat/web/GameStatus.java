package com.googlecode.ncombat.web;

import java.util.ArrayList;
import java.util.List;

public class GameStatus
{
	private boolean alive = true;
	private List<String> messages = new ArrayList();
	private String prompt;
	private String url;
	
	public GameStatus() {
	}

	public boolean isAlive() {
		return alive;
	}

	public void setAlive(boolean alive) {
		this.alive = alive;
	}
	
	public void addMessage(String message) {
		messages.add(message);
	}

	public List<String> getMessages() {
		return messages;
	}

	public void setMessages(List<String> messages) {
		this.messages = messages;
	}

	public String getPrompt() {
		return prompt;
	}

	public void setPrompt(String prompt) {
		this.prompt = prompt;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}
