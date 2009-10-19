package org.ncombat;

public abstract class ShieldArray
{
	private int numShields;
	
	private double[] power = new double[numShields];
	private double[] damage = new double[numShields];
	private double[] repairRate = new double[numShields];
	private double[] repairTime = new double[numShields];
	
	private double maxPower;
	
	public ShieldArray(int numShields, double maxPower)
	{
		this.numShields = numShields;
		
		this.power = new double[numShields];
		this.damage = new double[numShields];
		this.repairRate = new double[numShields];
		this.repairTime = new double[numShields];
		
		this.maxPower = maxPower;
	}
	
	public double getPower(int shieldNum) {
		return power[shieldNum-1];
	}
	
	public void setPower(int shieldNum, double power) {
		this.power[shieldNum-1] = power;
	}
	
	public double getEffectivePower(int shieldNum) {
		return power[shieldNum-1] * (100.0 - damage[shieldNum-1]) / 100.0;
	}
	
	public void dropShield(int shieldNum) {
		setPower(shieldNum-1, 0.0);
	}
	
	public void dropShields() {
		for (int i = 1 ; i <= numShields ; i++) {
			dropShield(i);
		}
	}
	
	public void fullShield(int shieldNum) {
		setPower(shieldNum-1, maxPower);
	}
	
	public void fullShields() {
		for (int i = 1 ; i <= numShields ; i++) {
			fullShield(i);
		}
	}
	
	/**
	 * Returns the number of the shield that will be hit by a weapon fired from
	 * the indicated azimuth, expressed in degrees, with positive clockwise.
	 */
	public abstract int coveringShield(double azimuth);
	
	public abstract double shieldStrength(int shieldNum);

	@Override
	public String toString() {
		return "[ShieldArray: numShields=" + numShields
					+ ", maxPower=" + maxPower
					+ ", power=" + power
					+ ", damage=" + damage
					+ ", repairRate=" + repairRate
					+ ", repairTime=" + repairTime
					+ "]";
	}
}
