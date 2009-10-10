package com.googlecode.ncombat.command;

public class RotateCommand implements Command
{
	private double angle;
	private double time;
	
	public RotateCommand(double angle, double time) {
		this.angle = angle;
		this.time = time;
	}

	public double getAngle() {
		return angle;
	}

	public double getTime() {
		return time;
	}
}
