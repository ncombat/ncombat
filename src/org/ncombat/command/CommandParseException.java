package org.ncombat.command;

public class CommandParseException extends CommandException
{
	public CommandParseException() {
	}

	public CommandParseException(String message) {
		super(message);
	}

	public CommandParseException(Throwable cause) {
		super(cause);
	}

	public CommandParseException(String message, Throwable cause) {
		super(message, cause);
	}
}
