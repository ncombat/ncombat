package org.ncombat.command;

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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(shield1RepairPct);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(shield2RepairPct);
		result = prime * result + (int) (temp ^ (temp >>> 32));
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
		RepairCommand other = (RepairCommand) obj;
		if (Double.doubleToLongBits(shield1RepairPct) != Double
				.doubleToLongBits(other.shield1RepairPct))
			return false;
		if (Double.doubleToLongBits(shield2RepairPct) != Double
				.doubleToLongBits(other.shield2RepairPct))
			return false;
		return true;
	}
}
