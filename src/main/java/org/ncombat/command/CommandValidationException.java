package org.ncombat.command;

public class CommandValidationException extends CommandException
{
	public CommandValidationException() {
	}

	public CommandValidationException(String message) {
		super(message);
	}

	public CommandValidationException(Throwable cause) {
		super(cause);
	}

	public CommandValidationException(String message, Throwable cause) {
		super(message, cause);
	}
}
