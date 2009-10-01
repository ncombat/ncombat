package com.googlecode.ncombat;

public class NcombatException extends RuntimeException
{
	public NcombatException() {
	}
	
	public NcombatException(String message) {
		super(message);
	}

	public NcombatException(Throwable cause) {
		super(cause);
	}

	public NcombatException(String message, Throwable cause) {
		super(message, cause);
	}
}
