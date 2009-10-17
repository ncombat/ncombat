package org.ncombat;

import org.ncombat.command.AccelerateCommand;
import org.ncombat.command.LaserCommand;
import org.ncombat.command.MissileCommand;
import org.ncombat.command.RepairCommand;
import org.ncombat.command.RotateCommand;
import org.ncombat.command.ShieldCommand;
import org.ncombat.utils.Vector;

public class Ship extends Combatant
{
	protected Vector velocity;
	protected double heading;
	
	protected double accelRate;
	protected double accelTime;
	
	protected double rotationRate;
	protected double rotationTime;
	
	protected double laserCoolingTime;
	
	protected final int numMissileTubes = 2;
	protected final double[] missileLoadTime = new double[numMissileTubes];
	protected int numMissiles;
	
	public Ship(Vector position) {
		super(position);
	}
	
	@Override
	public int getNumShields() {
		return 2;
	}

	public void processAccelerateCommand(AccelerateCommand cmd) {
		addMessage("Processing " + cmd);
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

	public void processRotateCommand(RotateCommand cmd) {
		addMessage("Processing " + cmd);
	}

	public void processShieldCommand(ShieldCommand cmd) {
		addMessage("Processing " + cmd);
	}
}
