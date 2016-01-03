package org.ncombat.command;

public class ShieldCommand implements Command
{
	private int shieldNum;
	private double power;
	
	public ShieldCommand(int shieldNum, double power) {
		this.shieldNum = shieldNum;
		this.power = power;
	}

	public int getShieldNum()
	{
		return shieldNum;
	}

	public double getPower()
	{
		return power;
	}
	
	@Override
	public String toString() {
		return "[ShieldCommand: shieldNum=" + shieldNum
					+ ", power=" + power + "]";
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(power);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + shieldNum;
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ShieldCommand other = (ShieldCommand) obj;
		if (Double.doubleToLongBits(power) != Double
				.doubleToLongBits(other.power))
			return false;
		if (shieldNum != other.shieldNum)
			return false;
		return true;
	}
}
