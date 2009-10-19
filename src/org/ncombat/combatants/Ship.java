package org.ncombat.combatants;

import org.apache.log4j.Logger;
import org.ncombat.MotionComputer;
import org.ncombat.Movable;
import org.ncombat.ShipShieldArray;
import org.ncombat.command.AccelerateCommand;
import org.ncombat.command.LaserCommand;
import org.ncombat.command.MissileCommand;
import org.ncombat.command.RepairCommand;
import org.ncombat.command.RotateCommand;
import org.ncombat.command.ShieldCommand;
import org.ncombat.utils.NcombatMath;
import org.ncombat.utils.Vector;

public abstract class Ship extends Combatant implements Movable
{
	private Logger log = Logger.getLogger(Ship.class);
	
	private Vector velocity = Vector.ZERO;
	
	private double heading;
	
	private double accelRate;
	private double accelTime;
	
	private double engineHeat;
	private boolean engineBlown;
	
	private double rotationRate;
	private double rotationTime;
	
	private double laserCoolingTime;
	
	private final int numMissileTubes = 2;
	private final double[] missileLoadTime = new double[numMissileTubes];
	private int numMissiles;
	
	public Ship(Vector position) {
		super(position);
		setShields( new ShipShieldArray());
	}
	
	@Override
	public void update(long updateTime)
	{
		double intervalLen = ((double)(updateTime - getLastUpdateTime())) / 1000.0;
		
		MotionComputer.Request motionRequest = new MotionComputer.Request();
		motionRequest.intervalLength = intervalLen;
		motionRequest.initialPosition = getPosition();
		motionRequest.initialVelocity = velocity;
		motionRequest.initialHeading = heading;
		motionRequest.accelRate = accelRate;
		motionRequest.accelTime = accelTime;
		motionRequest.rotationRate = rotationRate;
		motionRequest.rotationTime = rotationTime;
		
		MotionComputer.Response motionResponse = MotionComputer.compute(motionRequest);
		
		setPosition( motionResponse.finalPosition);
		velocity = motionResponse.finalVelocity;
		heading = motionResponse.finalHeading;
		accelTime = motionResponse.accelTimeLeft;
		if (accelTime == 0.0) accelRate = 0.0;
		rotationTime = motionResponse.rotationTimeLeft;
		if (rotationTime == 0.0) rotationRate = 0.0;
	}

	public void processAccelerateCommand(AccelerateCommand cmd)
	{
		if (engineBlown) {
			addMessage("Engine blown - cannot accelerate.");
			return;
		}
		accelRate = cmd.getRate();
		accelTime = cmd.getTime();
	}
	
	public void processLaserCommand(LaserCommand cmd) {
		addMessage("Processing " + cmd);
	}

	public void processMissileCommand(MissileCommand cmd) {
		addMessage("Processing " + cmd);
	}

	public void processRepairCommand(RepairCommand cmd) {
		addMessage("Processing " + cmd);
	}

	public void processRotateCommand(RotateCommand cmd)
	{
		rotationRate = - Math.toRadians(cmd.getRate()) * Math.signum(cmd.getAngle());
		rotationTime = Math.abs( cmd.getAngle()) / cmd.getRate();
	}

	public void processShieldCommand(ShieldCommand cmd) {
		getShields().setPower( cmd.getShieldNum(), cmd.getPower());
	}
	
	public double range(Combatant combatant) {
		return getPosition().subtract(combatant.getPosition()).r();
	}
	
	public double speed(Ship ship) {
		return ship.getVelocity().r();
	}
	
	public double azimuth(Combatant combatant)
	{
		Vector pos1 = getPosition();
		double heading1 = getHeading();
		Vector pos2 = combatant.getPosition();
		return NcombatMath.degreeAzimuth(pos1, heading1, pos2);
	}
	
	public double course()
	{
		Vector velocity = getVelocity();
		double heading = getHeading();
		return NcombatMath.degreeCourse(velocity, heading);
	}
	
	public double course(Ship ship)
	{
		Vector pos1 = getPosition();
		Vector pos2 = ship.getPosition();
		Vector velocity2 = ship.getVelocity();
		return NcombatMath.degreeCourse(pos1, pos2, velocity2);
	}

	public Vector getVelocity() {
		return velocity;
	}

	public void setVelocity(Vector velocity) {
		this.velocity = velocity;
	}

	public double getHeading() {
		return heading;
	}

	public void setHeading(double heading) {
		this.heading = heading;
	}

	public double getAccelRate() {
		return accelRate;
	}

	public double getAccelTime() {
		return accelTime;
	}

	public double getRotationRate() {
		return rotationRate;
	}

	public double getRotationTime() {
		return rotationTime;
	}
}
