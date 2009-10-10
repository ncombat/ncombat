package com.googlecode.ncombat.command;

public class RepairCommand implements Command
{
	private double shield1RepairPct;
	private double shield2RepairPct;
	
	public RepairCommand(double shield1RepairPct, double shield2RepairPct) {
		this.shield1RepairPct = shield1RepairPct;
		this.shield2RepairPct = shield2RepairPct;
	}

	public double getShieldRepair1Pct() {
		return shield1RepairPct;
	}

	public double getShieldRepair2Pct() {
		return shield2RepairPct;
	}
}
