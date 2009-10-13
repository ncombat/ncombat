package org.ncombat.command;

public class SensorCommand implements Command
{
	private double range;

	public SensorCommand(double range) {
		this.range = range;
	}

	public double getRange() {
		return range;
	}
	
	@Override
	public String toString() {
		return "[SensorCommand: range=" + range + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(range);
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
		SensorCommand other = (SensorCommand) obj;
		if (Double.doubleToLongBits(range) != Double
				.doubleToLongBits(other.range))
			return false;
		return true;
	}
}
