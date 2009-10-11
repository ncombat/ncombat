package org.ncombat.command;

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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(shieldPower1);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(shieldPower2);
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
		ShieldCommand other = (ShieldCommand) obj;
		if (Double.doubleToLongBits(shieldPower1) != Double
				.doubleToLongBits(other.shieldPower1))
			return false;
		if (Double.doubleToLongBits(shieldPower2) != Double
				.doubleToLongBits(other.shieldPower2))
			return false;
		return true;
	}
}
