package org.ncombat.combatants;

import org.apache.log4j.Logger;
import org.ncombat.GornShieldArray;
import org.ncombat.MotionComputer;
import org.ncombat.combatants.Combatant.AttackResult;
import org.ncombat.command.AccelerateCommand;
import org.ncombat.command.CommandBatch;
import org.ncombat.command.LaserCommand;
import org.ncombat.command.MessageCommand;
import org.ncombat.command.RotateCommand;
import org.ncombat.command.ShieldCommand;
import org.ncombat.utils.NcombatMath;
import org.ncombat.utils.Vector;

public class GornBase extends Ship
{
	
	// Length of time between command generation cycles (seconds).
	private static final double CYCLE_LEN = 60.0;
	
	// increasing repair capacity and speed - Gorns are tough!
	// Repairs begin REPAIR_WAIT_TIME seconds after damage is inflicted. 
	private static final double GORN_REPAIR_WAIT_TIME = 3.0;
	
	// Default damage repair rate (in % per second).
	private static final double GORN_REPAIR_RATE = 0.20;
	
	// Gorn base
	
	/**
	 * engagement range is distance I will fire at a trespasser. 
	 * Gorns fire at all trespassers, including those not in their area of space. 
	 * Because of the position of the bases around the perimeter it's possible two 
	 * gorn bases could fire at the same ship simultaneously.
	*/
	private static final double ENGAGEMENT_RANGE = 100000.0;
	private static final double GORN_INITIAL_ENERGY = 20000.0;

	
	// ships and/or bots exceeding WARNING_RANGE will recieve WARNING_MESSAGE
	private static final double WARNING_RANGE = 50000.0;
	private static final String WARNING_MSG = "WARNING - YOU ARE APPROACHING GORN SPACE. TURN BACK OR I WILL OPEN FIRE.";
	
	// ships exceeding TRESPASS_RANGE will be fired upon and recieve TRESPASS_MESSAGE
	private static final double TRESPASS_RANGE = 70000.0;
	private static final String TRESPASS_MSG = "YOU ARE TRESPASSING IN GORN SPACE. TURN BACK OR I WILL DESTROY YOU.";
	
	
	
	private double cycleTimeLeft = CYCLE_LEN;
	protected double GornBlasterFiringPower=2000;
	
	// Gorns are armed with one laser.
	protected double laserCoolingTime;
	private Logger log = Logger.getLogger(GornBase.class);
	
	public GornBase(String commander) {
		super(commander);
		this.shields = new GornShieldArray();
		this.energy = GORN_INITIAL_ENERGY;
		this.velocity = Vector.ZERO;
		log.info(commander + " joins the fray.");
	}

	// I'm a Gorn. This is my boomstick. I don't wait for no cooldown time.
	private void attack(Combatant nearest) {
		energy =- GornBlasterFiringPower;		
		AttackResult result = nearest.onLaserHit(this, GornBlasterFiringPower);
		String fmt = "Gorn blaster hit on shield %d of ship %2d caused %3d damage.";
		addMessage( String.format(fmt, result.shieldHit, 
						nearest.getShipNumber(), (int) result.damage));
		
		if (!nearest.isAlive()) {
			processKill(nearest);
			nearest.markDestroyed(this);
		}
		
	}

	/**
	 * Gorns do not run out of energy, but they don't consume much unless a player is
	 *  in range. Therefore, we try to keep the Gorn energy level to a constant level
	 *  so they do are neither helpless nor an invaluable prize by constantly accumulating energy. 
	 *  If the base is under attack, it gets a smaller boost.
	 */
	
	protected void checkEnergy()
	{
			if ((damage < 0.1) || (energy > 30001)) {
				energy = 30000;
			}
			else {energy =+ 5000; }
	}

	@Override
	public void completeGameCycle() {
		
		int lastNearest =0;
		
		try {
			
			long now = System.currentTimeMillis();
			CommandBatch batch = new CommandBatch(now, this);
			
			Combatant nearest = nearest();
			
			double nearest_distance = nearest.position.r();
			
			double nearestRange = nearestRange();

			batch.addCommand( new ShieldCommand(1, 25));
			
			this.checkEnergy();
			
			// if it's approaching Gorn space, send a nastygram
			if  ((nearest_distance < WARNING_RANGE) && (nearest_distance > TRESPASS_RANGE) && (lastNearest!=nearest.getId()) && (nearest instanceof PlayerShip)) {
				log.debug( String.format("[%s] warning interloper [%s] at range %7.1f", commander, nearest.commander, nearestRange));
				getGameServer().sendMessage(nearest.getShipNumber(), "\nMessage from " + this.commander + " : " + WARNING_MSG);
			}
			
			// if it's trespassing on Gorn space and is in engagement range ( and isn't a Gorn :) ), kill it!
			// TODO: Should Gorn base refrain from attacking if ship is headed back toward core (say azimuth > +/- 90)?

			if ((nearest_distance <= TRESPASS_RANGE) && (nearestRange < ENGAGEMENT_RANGE) && !(nearest instanceof GornBase)){
				log.info( String.format("[%s] attacking interloper [%s] at range %7.1f", commander, nearest.commander, nearestRange));
				getGameServer().sendMessage(nearest.getShipNumber(), "\nMessage from " + this.commander + " : " + TRESPASS_MSG);
				attack(nearest);
			}
			
			// this is to prevent loop where it continually broadcasts warnings
			lastNearest = nearest.getId();
			
			gameServer.addCommandBatch(batch);
		}
		catch (Exception e) {
			log.error(e);
		}
	}

	@Override
	public double course() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getAccelRate() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getAccelTime() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getHeading() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getRotationRate() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getRotationTime() {
		// TODO Auto-generated method stub
		return 0;
	}
	void moveTheShip(double intervalLen)
	{
		// no operation - Gorns don't move;
		// but we will cheat and reset the Gorn's energy back to 20000 so he does not run out
		this.energy = GORN_INITIAL_ENERGY;
	}

	@Override
	protected AttackResult onLaserHit(Combatant attacker, double power)
	{
		setLastAttacker(attacker);
		
		int shieldHit = shields.coveringShield(azimuth(attacker));
		double shieldPower = shields.getEffectivePower(shieldHit);
		double range = range(attacker);
		
		// reducing Gorn damage effect by decreasing constant from 33 to 28
		double coeffDamage = (power * 12.0 * (28.0 - shieldPower)) / range / 2.0;
		double shipDamage = addDamage(coeffDamage);
		shields.addDamage(shieldHit, coeffDamage);
		
		String exclamation = (shipDamage > 20.0 ? "**BLAM**" : ">>PWANG<<");
		String fmt = "%s Ship %d laser hit shield %d caused %d%% damage.";
		addMessage( String.format(fmt, exclamation, attacker.getShipNumber(), 
										shieldHit, (int) shipDamage));
		
		AttackResult results = new AttackResult();
		results.shieldHit = shieldHit;
		results.damage = shipDamage;
		
		return results;
	}

	
	@Override
	protected AttackResult onMissileHit(Combatant attacker)
	{
		setLastAttacker(attacker);
		
		int shieldHit = shields.coveringShield(azimuth(attacker));
		double shieldPower = shields.getEffectivePower(shieldHit);
		// Gorn's more powerful shields means coefficent needs to be increased, so less damage is done.
		double coeffDamage = 102.0 - (shieldPower * 4.5);
		double shipDamage = addDamage(coeffDamage);
		shields.addDamage(shieldHit, coeffDamage);
		
		String exclamation = (shipDamage > 20.0 ? "**BLAM**" : ">>PWANG<<");
		String fmt = "%s Ship %d missile hit shield %d caused %d%% damage.";
		addMessage( String.format(fmt, exclamation, attacker.getShipNumber(),
										shieldHit, (int) shipDamage));
		
		AttackResult results = new AttackResult();
		results.shieldHit = shieldHit;
		results.damage = shipDamage;
		
		return results;
	}

	@Override
	public void processAccelerateCommand(AccelerateCommand cmd) {
		// no operation;
	}
	
	public void processMessageCommand(MessageCommand cmd)
	{
		// thanks for the share but the gorn does not care
		;
	}
	
	@Override
	public void processRotateCommand(RotateCommand cmd) {
		// no operation;
	}

	@Override
	protected void repairShip(double intervalLen)
	{
		if (damage > 0.0) {
			double waitTime = Math.min(GORN_REPAIR_WAIT_TIME, intervalLen);
			double maxRepairTime = intervalLen - waitTime;
			double maxRepair = maxRepairTime * GORN_REPAIR_RATE;
			
			damage = Math.max(damage - maxRepair, 0.0);
			
			repairWaitTime -= waitTime;
		}
	}

	@Override
	public void setHeading(double heading) {
		// no operation;
	}
	
	@Override
	public void setVelocity(Vector velocity) {
		// ignore setting - gorns don't move
			velocity=Vector.ZERO;
	}
	
	@Override
	public double speed(Combatant ship) {
		return 0;
	}

	@Override
	public void update(long updateTime) {
		
		super.update(updateTime);
		long intervalLong = updateTime - getLastUpdateTime();
		double intervalLen = (double)(intervalLong / 1000);
		cycleTimeLeft = Math.max(cycleTimeLeft - intervalLen, 0.0);
		
		
	}
}
