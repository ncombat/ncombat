package org.ncombat;

public abstract class ShieldArray
{
	private int numShields;
	
	private double[] power = new double[numShields];
	private double[] damage = new double[numShields];
	private double[] repairRate = new double[numShields];
	private double[] repairWaitTime = new double[numShields];
	
	private double maxPower;
	
	public void repair(double intervalLen)
	{
		for (int i = 0 ; i < numShields ; i++) {
			if (damage[i] > 0.0) {
				double waitTime = Math.min( repairWaitTime[i], intervalLen);
				double maxRepairTime = intervalLen - waitTime;
				double maxRepair = maxRepairTime * repairRate[i];
				
				damage[i] = Math.max( damage[i] - maxRepair, 0.0);
				
				repairWaitTime[i] -= waitTime;
			}
		}
	}
	
	public ShieldArray(int numShields, double maxPower)
	{
		this.numShields = numShields;
		
		this.power = new double[numShields];
		this.damage = new double[numShields];
		this.repairRate = new double[numShields];
		this.repairWaitTime = new double[numShields];
		
		this.maxPower = maxPower;
	}
	
	public double getTotalPower()
	{
		double totalPower = 0.0;
		
		for (double shieldPower : this.power) {
			totalPower += shieldPower;
		}
		
		return totalPower;
	}
	
	public double getPower(int shieldNum) {
		return power[shieldNum-1];
	}
	
	public void setPower(int shieldNum, double power) {
		this.power[shieldNum-1] = power;
	}
	
	public double getDamage(int shieldNum) {
		return this.damage[shieldNum-1];
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
	
	public void setRepairRate(int shieldNum, double repairRate) {
		this.repairRate[shieldNum-1] = repairRate;
	}
	
	public double addDamage(int shieldNum, double damage)
	{
		int idx = shieldNum - 1;
		double existingDamage = this.damage[idx];
		double newDamage = Math.min( damage / shieldStrength(shieldNum),
									 100.0 - existingDamage );
		
		this.damage[idx] += newDamage;
		this.repairWaitTime[idx] = repairDelay(shieldNum);
		
		return newDamage;
	}
	
	/**
	 * Returns the number of the shield that will be hit by a weapon fired from
	 * the indicated azimuth, expressed in degrees, with positive clockwise.
	 */
	public abstract int coveringShield(double azimuth);
	
	public abstract double shieldStrength(int shieldNum);

	/**
	 * Returns the number of seconds before repair begins after damage to the
	 * indicated shield.
	 */
	public abstract double repairDelay(int shieldNum);

	@Override
	public String toString() {
		return "[ShieldArray: numShields=" + numShields
					+ ", maxPower=" + maxPower
					+ ", power=" + power
					+ ", damage=" + damage
					+ ", repairRate=" + repairRate
					+ ", repairWaitTime=" + repairWaitTime
					+ "]";
	}
}
