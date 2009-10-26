package org.ncombat;

public class ShipShieldArray extends ShieldArray
{
	public static final int NUM_SHIELDS = 2;
	
	public static final double MAX_SHIELD_POWER = 25.0;
	
	public static final double REPAIR_DELAY = 30.0;
	
	public ShipShieldArray() {
		super(NUM_SHIELDS, MAX_SHIELD_POWER);
	}

	@Override
	public int coveringShield(double azimuth) {
		return (Math.abs(azimuth) <= 30.0 ? 1 : 2);
	}

	@Override
	public double shieldStrength(int shieldNum) {
		return (shieldNum == 1 ? 1.0 : 0.5);
	}

	@Override
	public double repairDelay(int shieldNum) {
		return REPAIR_DELAY;
	}
}
