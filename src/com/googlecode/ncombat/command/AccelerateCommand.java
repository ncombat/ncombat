package com.googlecode.ncombat.command;

public class AccelerateCommand implements Command
{
	private double rate;
	private double time;
	
	public AccelerateCommand(double rate, double time) {
		this.rate = rate;
		this.time = time;
	}

	public double getRate() {
		return rate;
	}

	public double getTime() {
		return time;
	}
}
