package com.googlecode.ncombat.command;

public class SensorCommand implements Command
{
	private double range;

	public SensorCommand(double range) {
		this.range = range;
	}

	public double getRange() {
		return range;
	}
}
