package com.googlecode.ncombat.command;

public class MessageCommand implements Command
{
	private int destination;
	private String message;
	
	public MessageCommand(String message) {
		this(0, message);
	}

	public MessageCommand(int destination, String message) {
		this.destination = destination;
		this.message = message;
	}

	public int getDestination() {
		return destination;
	}

	public String getMessage() {
		return message;
	}
}
