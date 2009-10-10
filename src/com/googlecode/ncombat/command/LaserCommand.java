package com.googlecode.ncombat.command;

public class LaserCommand implements Command
{
	private double power;

	public LaserCommand(double power) {
		this.power = power;
	}

	public double getPower() {
		return power;
	}
}
