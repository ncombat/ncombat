package org.ncombat.combatants;

import java.util.ArrayList;
import java.util.List;

import org.ncombat.Movable;
import org.ncombat.command.BriefModeCommand;
import org.ncombat.command.CommandBatch;
import org.ncombat.command.HelpCommand;
import org.ncombat.command.IntelCommand;
import org.ncombat.command.MessageCommand;
import org.ncombat.command.NullCommand;
import org.ncombat.command.SensorCommand;
import org.ncombat.command.StopCommand;
import org.ncombat.command.TrackCommand;
import org.ncombat.utils.NcombatMath;
import org.ncombat.utils.Vector;

public class PlayerShip extends Ship
{
	public static final double DEFAULT_SENSOR_RANGE = 30000.0;
	
	private boolean briefMode;
	
	private double sensorRange = DEFAULT_SENSOR_RANGE;
	
	private int trackedShip;
	
	private boolean regenDataReadout;
	
	public PlayerShip(Vector position) {
		super(position);
	}
	
	@Override
	public void processCommands(CommandBatch commandBatch) {
		super.processCommands(commandBatch);
		this.regenDataReadout = commandBatch.getRegenStatusReadout();
	}

	@Override
	public void completeGameCycle()
	{
		if (regenDataReadout) {
			generateDataReadout();
			regenDataReadout = false;
		}
	}
	
	public void processBriefModeCommand(BriefModeCommand cmd) {
		briefMode = !briefMode;
	}

	public void processHelpCommand(HelpCommand cmd)
	{
		String[] helpText = new String[] {
				
		};
	}

	public void processIntelCommand(IntelCommand cmd)
	{
		switch (cmd.getSubcommand()) {
		case 1:	computeParallelCourse(cmd.getShip()); break;
		case 2: computeCentralCourse();	break;
		case 3:
		case 4:
		case 5:
		case 6:
		}
	}
	
	private void computeParallelCourse(int shipNum)
	{
		Combatant combatant = getGameServer().getCombatant(shipNum);
		
		if (combatant == null) {
			addMessage(shipNum + " is not a valid ship number.");
			return;
		}
		
		Vector velocityToMatch = Vector.ZERO;
		if (combatant instanceof Movable) {
			velocityToMatch = ((Movable) combatant).getVelocity();
		}
		
		Vector velocityDiff = velocityToMatch.subtract( getVelocity());
		
		double speed1 = velocityDiff.r();
		double rot1 = 0.0;
		if (Math.abs(speed1) > 0.0) {
			rot1 = NcombatMath.degreeAngle( velocityDiff.theta() - getHeading());
		}
		
		double speed2 = -speed1;
		double rot2 = Vector.stdAngleDegrees(rot1 + 180.0);
		
		addMessage("SP      ROT      SPD OR      ROT      SPD");
		addMessage( String.format("%2d %8.3f %8.3f    %8.3f %8.3f",
									shipNum, rot1, speed1, rot2, speed2));
	}
	
	private void computeCentralCourse()
	{
		Vector pos = getPosition();
		double range = pos.r();
		double azimuth = Vector.stdAngleDegrees( 
							Math.toDegrees( 
								getHeading() - pos.negate().theta()));
		
		addMessage( String.format("AZ: %6.1f RNG: %8.0f TO CENTER", azimuth, range));
	}

	public void processMessageCommand(MessageCommand cmd)
	{
		int shipNum = cmd.getDestination();
		String message = cmd.getMessage();
		
		if (shipNum > 0) {
			message = "/" + getShipNumber() + " " + message;
			getGameServer().sendMessage(shipNum, message);
		}
		else {
			message = "#" + getShipNumber() + " " + message;
			getGameServer().sendMessage(message);
		}
	}

	public void processNullCommand(NullCommand cmd) {
		// Do nothing.
	}
	
	public void processSensorCommand(SensorCommand cmd) {
		sensorRange = cmd.getRange();
	}

	public void processStopCommand(StopCommand cmd) {
		addMessage("Processing " + cmd);
	}

	public void processTrackCommand(TrackCommand cmd) {
		trackedShip = cmd.getTarget();
	}

	public static class ShipInfo
	{
		public Ship ship1;
		public Ship ship2;

		public double speed;
		public double course;
		public double azimuth;
		public double range;
		public double heading;
	}
	
	public void generateDataReadout()
	{
		List<String> buf = new ArrayList<String>();
		
		List<Combatant> combatants = getGameServer().getCombatants();
		
		if ((trackedShip != 0) && ( getGameServer().getCombatant(trackedShip) == null)) {
			trackedShip = 0;
		}
		
		if (!briefMode) {
			buf.add("SP DMG P1 P2  SPEED COURSE AZMUTH  RANGE HEADING");
		}
		
		// Full data for all the other ships, but this might only
		// be one ship if we are in tracking mode.
		for (Combatant combatant : combatants)
		{
			if (combatant == this) continue;
			if (!(combatant instanceof Ship)) continue;
			Ship ship = (Ship) combatant;
			int shipNum = combatant.getShipNumber();
			if ((trackedShip > 0) && (shipNum != trackedShip)) continue;
			
			int damage = (int) ship.getDamage();
			int p1 = (int) ship.getShields().getEffectivePower(1);
			int p2 = (int) ship.getShields().getEffectivePower(2);
			int speed = (int) ship.getVelocity().r();
			double course = course(ship);
			double azimuth = azimuth(ship);
			int range = (int) range(ship);
			double heading = ship.azimuth(this);
			
			buf.add( String.format("%2d %3d %2d %2d  %5d %6.1f %6.1f %6d  %6.1f",
						shipNum, damage, p1, p2, speed, course, azimuth, range, heading));
		}
		
		// Now data for our own ship.
		int shipNum = getShipNumber();
		int damage = (int) this.getDamage();
		int p1 = (int) this.getShields().getEffectivePower(1);
		int p2 = (int) this.getShields().getEffectivePower(2);
		int speed = (int) this.getVelocity().r();
		double course = course();
		
		buf.add( String.format("%2d %3d %2d %2d  %5d %6.1f", shipNum, damage, p1, p2, speed, course));
		
		// Now abbreviated data for all the other ships if we are in tracking mode.
		if (trackedShip != 0) {
			if (!briefMode) {
				buf.add("  S HEADNG");
			}
			for (Combatant combatant : combatants)
			{
				if (combatant == this) continue;
				if (!(combatant instanceof Ship)) continue;
				Ship ship = (Ship) combatant;
				shipNum = combatant.getShipNumber();
				if ((trackedShip > 0) && (shipNum == trackedShip)) continue;
				double heading = ship.azimuth(this);
				
				buf.add( String.format("%3d %6.1f", shipNum, heading));
			}
		}
		
		// Finally, the really detailed stuff on ourselves.
		if (!briefMode) {
			buf.add("ENERGY DMG1 DMG2 T1/M1/T2 T3 ACEL/TIM  DEG/TIM HEAT");
		}
		
		int acel = (int)( getAccelRate() * getAccelTime());
		int acelTim = (int) getAccelTime();
		int deg = (int)( Math.toDegrees( getRotationRate() * getRotationTime()));
		int degTim = (int) getRotationTime();
		
		buf.add( String.format("%6d %4d %4d %2d/%2d/%2d %2d %4d/%3d %4d/%3d %4d",
						0, 0, 0, 0, 0, 0, 0, acel, acelTim, deg, degTim, 0));
		
		addMessages(buf);
		
		this.regenDataReadout = false;
	}
}
