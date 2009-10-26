package org.ncombat;

public class GornShieldArray extends ShieldArray
{
	public static final int NUM_SHIELDS = 1;
	
	public static final double MAX_SHIELD_POWER = 50.0;
	
	public static final double REPAIR_DELAY = 30.0;
	
	public GornShieldArray() {
		super(NUM_SHIELDS, MAX_SHIELD_POWER);
	}

	@Override
	public int coveringShield(double azimuth) {
		return 1;
	}

	@Override
	public double shieldStrength(int shieldNum) {
		return 1.0;
	}

	@Override
	public double repairDelay(int shieldNum) {
		return REPAIR_DELAY;
	}
}
