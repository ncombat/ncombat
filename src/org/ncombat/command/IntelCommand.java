package com.googlecode.ncombat.command;

public class IntelCommand implements Command
{
	private int subcommand;
	private int ship;
	
	public IntelCommand(int subcommand) {
		this(subcommand, 0);
	}

	public IntelCommand(int subcommand, int ship) {
		this.subcommand = subcommand;
		this.ship = ship;
	}

	public int getSubcommand() {
		return subcommand;
	}

	public int getShip() {
		return ship;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ship;
		result = prime * result + subcommand;
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
		IntelCommand other = (IntelCommand) obj;
		if (ship != other.ship)
			return false;
		if (subcommand != other.subcommand)
			return false;
		return true;
	}
}
