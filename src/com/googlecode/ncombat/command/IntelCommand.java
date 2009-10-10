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
}
