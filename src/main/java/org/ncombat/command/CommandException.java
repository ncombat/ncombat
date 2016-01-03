package org.ncombat.command;

import org.ncombat.NcombatException;

public class CommandException extends NcombatException
{
	public CommandException() {
	}

	public CommandException(String message) {
		super(message);
	}

	public CommandException(Throwable cause) {
		super(cause);
	}

	public CommandException(String message, Throwable cause) {
		super(message, cause);
	}
}
