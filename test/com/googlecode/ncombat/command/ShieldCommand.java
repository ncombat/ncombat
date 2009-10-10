package com.googlecode.ncombat.command;

public class ShieldCommand implements Command
{
	private double shieldPower1;
	private double shieldPower2;
	
	public ShieldCommand(double shieldPower1, double shieldPower2) {
		this.shieldPower1 = shieldPower1;
		this.shieldPower2 = shieldPower2;
	}

	public double getShieldPower1() {
		return shieldPower1;
	}

	public double getShieldPower2() {
		return shieldPower2;
	}
}
