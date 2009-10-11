package org.ncombat.command;

public class LaserCommand implements Command
{
	private double power;

	public LaserCommand(double power) {
		this.power = power;
	}

	public double getPower() {
		return power;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(power);
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
		LaserCommand other = (LaserCommand) obj;
		if (Double.doubleToLongBits(power) != Double
				.doubleToLongBits(other.power))
			return false;
		return true;
	}
}
