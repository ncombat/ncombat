package org.ncombat.command;

public class RotateCommand implements Command
{
	private double angle;
	private double rate;
	
	public RotateCommand(double angle, double rate) {
		this.angle = angle;
		this.rate = rate;
	}

	public double getAngle() {
		return angle;
	}

	public double getRate() {
		return rate;
	}
	
	@Override
	public String toString() {
		return "[RotateCommand: angle=" + angle
					+ ", rate=" + rate + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(angle);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(rate);
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
		RotateCommand other = (RotateCommand) obj;
		if (Double.doubleToLongBits(angle) != Double
				.doubleToLongBits(other.angle))
			return false;
		if (Double.doubleToLongBits(rate) != Double
				.doubleToLongBits(other.rate))
			return false;
		return true;
	}
}
