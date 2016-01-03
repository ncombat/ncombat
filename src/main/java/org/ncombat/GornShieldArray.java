package org.ncombat;

public class GornShieldArray extends ShieldArray
{
	public static final int GORN_NUM_SHIELDS = 1;
	
	public static final double GORN_MAX_SHIELD_POWER = 50.0;
	
	public static final double GORN_REPAIR_DELAY = 30.0;

	public static final double GORN_DEFAULT_REPAIR_RATE = 2.0;
	
	public GornShieldArray()
	{
		super(GORN_NUM_SHIELDS, GORN_MAX_SHIELD_POWER);
		
		for (int i = 1 ; i <= GORN_NUM_SHIELDS ; i++) {
			setRepairRate(i, GORN_DEFAULT_REPAIR_RATE);
		}
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
		return GORN_REPAIR_DELAY;
	}

	@Override
	public double addDamage(int shieldNum, double damage) {
		// TODO Auto-generated method stub
		return super.addDamage(1, damage);
	}

	@Override
	public void dropShield(int shieldNum) {
		// TODO Auto-generated method stub
		super.dropShield(1);
	}

	@Override
	public void dropShields() {
		// TODO Auto-generated method stub
		super.dropShields();
	}

	@Override
	public void fullShield(int shieldNum) {
		// TODO Auto-generated method stub
		super.fullShield(1);
	}

	@Override
	public void fullShields() {
		// TODO Auto-generated method stub
		super.fullShields();
	}

	@Override
	public double getDamage(int shieldNum) {
		// TODO Auto-generated method stub
		return super.getDamage(1);
	}

	@Override
	public double getEffectivePower(int shieldNum) {
		// TODO Auto-generated method stub
		return super.getEffectivePower(1);
	}

	@Override
	public double getPower(int shieldNum) {
		// TODO Auto-generated method stub
		return super.getPower(shieldNum);
	}

	@Override
	public double getTotalPower() {
		// TODO Auto-generated method stub
		return super.getTotalPower();
	}

	@Override
	public void repair(double intervalLen) {
		// TODO Auto-generated method stub
		super.repair(intervalLen);
	}

	@Override
	public void setPower(int shieldNum, double power) {
		super.setPower(1, power);
	}

	@Override
	public void setRepairRate(int shieldNum, double repairRate) {
		super.setRepairRate(1,GORN_DEFAULT_REPAIR_RATE );
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return super.toString();
	}
}
