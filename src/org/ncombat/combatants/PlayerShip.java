package org.ncombat.combatants;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
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
	
	// If we have no contact from a player for NO_CONTACT_TIMEOUT milliseconds or more,
	// he is assumed to have left the game, i.e. "hung up the phone".
	public static final long NO_CONTACT_TIMEOUT = 120000;
	
	private Logger log = Logger.getLogger(PlayerShip.class);
	
	private boolean briefMode;
	
	private double sensorRange = DEFAULT_SENSOR_RANGE;
	
	private int trackedShip;
	
	private boolean regenDataReadout;
	
	public PlayerShip(String commander) {
		super(commander);
	}
	
	@Override
	public void processCommands(CommandBatch commandBatch) {
		super.processCommands(commandBatch);
		this.regenDataReadout = commandBatch.getRegenStatusReadout();
	}
	
	@Override
	public void update(long updateTime)
	{
		long noContactTime = updateTime - getLastContactTime();
		
		// no credit awarded if player exited with STOP command
		if ((noContactTime >= NO_CONTACT_TIMEOUT) && (super.getCombatantStatus() != "STOP")) {
			Combatant lastAttacker = getLastAttacker();
			if ((lastAttacker != null) && (lastAttacker.isAlive())) {
				lastAttacker.processKill(this);
			}
			markHungUp();
			return;
		}
		
		super.update(updateTime);
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
		case 3: generateShipRoster( cmd.getShip()); break;
		case 4: generatePlayerHistory(); break;
		case 5: generateMap(); break;
		case 6: generateGornReadout(); break;
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
		if (combatant instanceof Ship) {
			velocityToMatch = ((Ship)combatant).velocity;
		}
		
		Vector velocityDiff = velocityToMatch.subtract(velocity);
		
		double speed1 = velocityDiff.r();
		double rot1 = 0.0;
		if (Math.abs(speed1) > 0.0) {
			rot1 = NcombatMath.degreeAngle( velocityDiff.theta() - heading);
		}
		
		double speed2 = -speed1;
		double rot2 = Vector.stdAngleDegrees(rot1 + 180.0);
		
		addMessage("SP      ROT      SPD OR      ROT      SPD");
		addMessage( String.format("%2d %8.3f %8.3f    %8.3f %8.3f",
									shipNum, rot1, speed1, rot2, speed2));
	}
	
	private void computeCentralCourse()
	{
		double range = position.r();
		double azimuth = Vector.stdAngleDegrees( 
							Math.toDegrees( 
								heading - position.negate().theta()));
		
		addMessage( String.format("AZ: %6.1f RNG: %8.0f TO CENTER", azimuth, range));
	}

	public void processMessageCommand(MessageCommand cmd)
	{
		int shipNum = cmd.getDestination();
		
		String message = cmd.getMessage();
		if (message == null) return;
		message = message.trim();
		if (message.length() == 0) return;
		
		if (shipNum > 0) {
			message = "/" + getShipNumber() + " " + message;
			getGameServer().sendMessage(shipNum, message);
		}
		else {
			message = "#" + getShipNumber() + " " + message;
			getGameServer().sendMessage(message, getShipNumber());
		}
	}

	public void processNullCommand(NullCommand cmd) {
		// Do nothing.  Duh.
	}
	
	public void processSensorCommand(SensorCommand cmd) {
		sensorRange = cmd.getRange();
	}

	public void processStopCommand(StopCommand cmd) {
		markStopCommand();
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
	
	public void generateShipRoster() {
		generateShipRoster(0);
	}
	
	public void generateShipRoster(int shipNum)
	{
		addMessage("SHP  COMMANDERS NAME       KL");
		
		for (Combatant combatant : gameServer.getCombatants()) {
			if (combatant instanceof Ship) {
				if ((shipNum <= 0) || (shipNum == combatant.getShipNumber())) {
					String fmt = "%2d   %-20s %3d";
					addMessage( String.format(fmt,
									combatant.getShipNumber(),
									combatant.commander,
									combatant.numKills));
				}
			}
		}
	}
	
	public void generatePlayerHistory()
	{
		List<Combatant> players = gameServer.getCombatants();
		
		for (Iterator<Combatant> it = players.iterator() ; it.hasNext() ; ) {
			Combatant combatant = it.next();
			if (!(combatant instanceof Ship)) {
				it.remove();
			}
		}
		
		players.addAll( gameServer.getFormerPlayers());
		
		Comparator<Combatant> comp = new Comparator<Combatant>() {
			public int compare(Combatant o1, Combatant o2) {
				int result = 0;
				if (result == 0) {
					result = safeCompare( o1.timeOn, o2.timeOn);
				}
				if (result == 0) {
					result = safeCompare( o1.timeOff, o2.timeOff);
				}
				if (result == 0) {
					Integer s1 = o1.getShipNumber();
					Integer s2 = o2.getShipNumber();
					result = s1.compareTo(s2);
				}
				return result;
			}
		};
		
		Collections.sort(players, comp);
		
		int numItems = 0;
		
		if (!briefMode) {
			addMessage( String.format("SP %-20s STATUS KL   TIME ON    TIME OFF",
										"COMMANDER"));
		}
		
		SimpleDateFormat dateFmt = new SimpleDateFormat("MM/dd HH:mm");
		
		for (Combatant player : players) {
			if (numItems++ > 30) break;
			String timeOn = dateFmt.format(player.timeOn);
			String timeOff = ( player.timeOff == null ? "" : dateFmt.format(player.timeOff));
			addMessage( String.format("%2d %-20s %6s %2d %11s %11s", 
							player.getShipNumber(), player.commander, player.getStatus(), 
							player.numKills, timeOn, timeOff));
		}
	}
	
	private static int safeCompare(Date d1, Date d2)
	{
		if (d1 == d2) return 0;
		if ((d1 == null) && (d2 == null)) return 0;
		if (d1 == null) return 1;
		if (d2 == null) return -1;
		return d1.compareTo(d2);
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
			if ( range(combatant) > sensorRange) continue;
			
			int damage = (int) ship.damage;
			int p1 = (int) ship.shields.getEffectivePower(1);
			int p2 = (int) ship.shields.getEffectivePower(2);
			int speed = (int) ship.velocity.r();
			double course = course(ship);
			double azimuth = azimuth(ship);
			int range = (int) range(ship);
			double heading = ship.azimuth(this);
			
			buf.add( String.format("%2d%3d%% %2d %2d  %5d %6.1f %6.1f %6d  %6.1f",
						shipNum, damage, p1, p2, speed, course, azimuth, range, heading));
		}
		
		// Now data for our own ship.
		int shipNum = getShipNumber();
		int damage = (int) this.damage;
		int p1 = (int) this.shields.getEffectivePower(1);
		int p2 = (int) this.shields.getEffectivePower(2);
		int speed = (int) this.velocity.r();
		double course = course();
		
		buf.add( String.format("%2d%3d%% %2d %2d  %5d %6.1f", shipNum, damage, p1, p2, speed, course));
		
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
				
				buf.add( String.format("%3d/%6.1f)", shipNum, heading));
			}
		}
		
		// Finally, the really detailed stuff on ourselves.
		if (!briefMode) {
			buf.add("ENERGY DMG1 DMG2 T1/M1/T2 T3 ACEL/TIM  DEG/TIM HEAT");
		}
		
		int deg = (int)( NcombatMath.degreeAngle( getRotationRate() * getRotationTime()));
		int degTim = (int) getRotationTime();
		
		buf.add( String.format("%6d %4d %4d %2d/%2d/%2d %2d %4s/%3d %4s/%3d %4d",
						(int) energy, // ENERGY 
						(int) shields.getDamage(1), // DMG1
						(int) shields.getDamage(2), // DMG2
						(int) missileLoadTime[0], // T1
						numMissiles, // M1
						(int) missileLoadTime[1], // T2
						(int) laserCoolingTime, // T3
						(int) getAccelRate(), // ACEL
						(int) getAccelTime(), // TIM
						deg, // DEG
						degTim, // TIM
						(int) engineHeat)); // HEAT
		
		addMessages(buf);
		
		this.regenDataReadout = false;
	}
	
	private void generateMap()
	{
		int width = 15;
		int height = 15;
		double minx = position.x() - sensorRange;
		double miny = position.y() - sensorRange;
		double xBoxSize = (2 * sensorRange) / width;
		double yBoxSize = (2 * sensorRange) / height;
		
		String[][] map = new String[width][height];
		for (int x = 0 ; x < width ; x++) {
			for (int y = 0 ; y < height ; y++) {
				map[x][y] = "  ";
			}
		}
		
		String shipChars = "0123456789+-*/()";
		
		for (Combatant combatant : gameServer.getCombatants())
		{
			if ( range(combatant) > sensorRange) continue;
			
			Vector pos = combatant.position;
			int x = (int)(( pos.x() - minx) / xBoxSize);
			int y = (int)(( pos.y() - miny) / yBoxSize);
			x = (int) Math.max(0, Math.min(x, xBoxSize - 1));
			y = (int) Math.max(0, Math.min(y, yBoxSize - 1));
			
			int shipNum = combatant.getShipNumber();
			
			if ( map[x][y].equals("  ")) {
				// This is the only combatant in this box so far.
				
				String sym = null;
				if (combatant instanceof GornBase) {
					sym = "G" + shipChars.charAt(shipNum - 20);  
				}
				else {
					sym = "S" + shipChars.charAt(shipNum);
				}
				
				map[x][y] = sym;
				log.debug("Map: [" + sym + "] : " + pos);
			}
			else {
				// There is at least one combatant already in this box.
				map[x][y] = "**";
			}
		}
		
		StringBuilder buf = new StringBuilder();
		buf.append('|');
		for (int i = 0 ; i < width ; i++) buf.append("--");
		buf.append('|');
		String boundary = buf.toString();
		
		addMessage(boundary);
		for (int y = height - 1 ; y >= 0 ; y--) {
			buf.setLength(0);
			buf.append('|');
			for (int x = 0 ; x < width ; x++) {
				buf.append( map[x][y]);
			}
			buf.append('|');
			addMessage( buf.toString());
		}
		addMessage(boundary);
	}
	
	private void generateGornReadout()
	{
		if (!briefMode) {
			addMessage("GN DMG AZMUTH   RANGE");
		}
		
		for (Combatant combatant : gameServer.getCombatants()) {
			if (combatant instanceof GornBase) {
				if (combatant.isAlive()) {
					double range = range(combatant);
					double azimuth = azimuth(combatant);
					if ((range < sensorRange) || Math.abs(azimuth) < 15.0) {
						String fmt = "%2d %2d%% %6.1f %7d";
						addMessage( String.format( fmt,
										combatant.getShipNumber() - 20,
										(int) combatant.damage,
										azimuth,
										(int) range));
					}
				}
			}
		}
	}
}
