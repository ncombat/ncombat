package org.ncombat.command;

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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + destination;
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MessageCommand other = (MessageCommand) obj;
		if (destination != other.destination)
			return false;
		if (message == null) {
			if (other.message != null)
				return false;
		} else if (!message.equals(other.message))
			return false;
		return true;
	}
}
