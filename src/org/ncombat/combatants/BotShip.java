package org.ncombat.combatants;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;
import org.ncombat.command.AccelerateCommand;
import org.ncombat.command.Command;
import org.ncombat.command.CommandBatch;
import org.ncombat.command.LaserCommand;
import org.ncombat.command.MessageCommand;
import org.ncombat.command.MissileCommand;
import org.ncombat.command.RotateCommand;
import org.ncombat.command.ShieldCommand;
import org.ncombat.utils.NcombatMath;
import org.ncombat.utils.Vector;


public class BotShip extends Ship
{
	private enum State {
		HEADING_IN,
		NORMAL,
		STARTING_TO_HEAD_IN,
		STARTING_TO_STOP,
		STOPPING
	}
	
	// Length of time between command generation cycles (seconds).
	private static final double CYCLE_LEN = 60.0;
	
	/*
	 * Bot ship will attack a vessel that comes within ENGAGEMENT_RANGE
	 * kilometers.
	 */
	private static final double ENGAGEMENT_RANGE = 15000.0;

	/*
	 * When the bot ship gets NORMAL_RADIUS kilometers or more from the center
	 * of the combat zone, it turns back the way it came.
	 */
	private static final double NORMAL_RADIUS = 30000.0;
	
	/*
	 * Shields will snap on if any combatants come within SHIELD_SNAP_RANGE
	 * kilometers.
	 */
	private static final double SHIELD_SNAP_RANGE = 20000.0;
	
	private LinkedList<Command> commandQueue = new LinkedList<Command>();
	
	private double cycleTimeLeft = CYCLE_LEN;
	
	private Logger log = Logger.getLogger(BotShip.class);
	
	private State state = State.NORMAL;
	
	public BotShip(String commander) {
		super(commander);
	}
	
	@Override
	public synchronized void completeGameCycle()
	{
		long now = System.currentTimeMillis();
		CommandBatch batch = new CommandBatch(now, this);
		
		double nearestRange = nearestRange();
		
		double shieldPower = 0.0;
		if ((damage > 0.0) || (nearestRange <= SHIELD_SNAP_RANGE)) {
			shieldPower = 25.0;
		}
		if ( shields.getPower(1) != shieldPower) {
			batch.addCommand( new ShieldCommand(1, shieldPower));
			batch.addCommand( new ShieldCommand(2, shieldPower));
		}
		
		double myRadius = position.r();
		double mySpeed = velocity.r();
		double myHeading = NcombatMath.degreeHeading(heading);		
		
		if ((state == State.NORMAL) && (myRadius >= NORMAL_RADIUS)) {
			state = State.STARTING_TO_STOP;
			commandQueue.clear();
		}
		
		switch (state) {
			case NORMAL:
				
				if ((cycleTimeLeft <= 0.0) && (nearestRange <= ENGAGEMENT_RANGE)) {
					Combatant nearest = nearest();
					double azimuth = azimuth(nearest);
					
					// Guess at the rotation required to keep us pointed at
					// our target.
					if ( azimuth != 0.0) {
						Vector pDiff = nearest.position.subtract(this.position);
						Vector vDiff = nearest.velocity.subtract(this.velocity);
						Vector pDiff2 = pDiff.add( vDiff.multiply(CYCLE_LEN));
						double hDiff = pDiff2.theta() - heading;
						double angle = NcombatMath.degreeAngle(hDiff);
						RotateCommand rotCmd = new RotateCommand(angle, 6.0);
						batch.addCommand(rotCmd);
					}
					
					if ((Math.abs(azimuth) <= 1.0) && laserReady()) {
						LaserCommand laserCmd = new LaserCommand(1000.0);
						batch.addCommand(laserCmd);
					}
					else if (( Math.abs(azimuth) <= 5.0) && missileReady()) {
						MissileCommand missileCmd = new MissileCommand(nearest.getShipNumber());
						batch.addCommand(missileCmd);
					}
					
					trashTalk(nearest);
					
					cycleTimeLeft = CYCLE_LEN;
				}
					
				break;
					
			case STARTING_TO_STOP:

				log.debug("[" + commander + "]: stopping from radius " + myRadius + ".");
				
				// First we rotate so we're pointing opposite to our direction of motion.
				double rotNeeded = Vector.stdAngleDegrees( 180.0 - course());					
				commandQueue.add( new RotateCommand(rotNeeded, 6.0));
				
				// Then we come to a full stop.
				commandQueue.add( new AccelerateCommand(5.0, mySpeed / 5.0));
				
				state = State.STOPPING;
				
			case STOPPING:
				
				if (quiet()) {
					if ( commandQueue.isEmpty()) {
						if ( Math.abs(mySpeed) <= 0.0001 ) {
							log.debug("[" + commander + "]: done stopping.");
							state = State.STARTING_TO_HEAD_IN;
						}
					}
					else {
						log.debug( String.format("[%s]: heading=%6.1f, speed=%4.1f, radius=%7.1f", commander, 
													myHeading, mySpeed, myRadius));
						
						Command cmd = commandQueue.removeFirst();
						batch.addCommand(cmd);
					}
				}
				
				break;
				
			case STARTING_TO_HEAD_IN:

				log.debug("[" + commander + "]: starting to head in from radius " + myRadius + ".");
				
				// First rotate towards the center of the combat zone.
				rotNeeded = NcombatMath.degreeAngle( position.negate().theta() - heading);					
				commandQueue.add( new RotateCommand(rotNeeded, 6.0));
				
				// Then accelerate to a new speed (randomly).
				double newSpeed = Math.floor( Math.random() * 10.0);
				commandQueue.add( new AccelerateCommand(5.0, newSpeed / 5.0));
				
				state = State.HEADING_IN;
				
			case HEADING_IN:
				
				if (quiet()) {
					if ( commandQueue.isEmpty()) {
						if (myRadius < NORMAL_RADIUS) {
							log.debug("[" + commander + "]: done heading in.");
							state = State.NORMAL;
						}
					}
					else {
						log.debug( String.format("[%s]: heading=%6.1f, speed=%4.1f, radius=%7.1f", commander, 
													myHeading, mySpeed, myRadius));
						
						Command cmd = commandQueue.removeFirst();
						batch.addCommand(cmd);
					}
				}
				
				break;
				
		}
		
		List<Command> cmds = batch.getCommands();
		
		if (! cmds.isEmpty()) {
			StringBuilder msg = new StringBuilder();
			msg.append("[" + commander + "]:");
			for (Command cmd : cmds) {
				msg.append(" ");
				msg.append(cmd.toString());
			}
			log.debug(msg);
			
			gameServer.addCommandBatch(batch);
		}
	}

	/**
	 * It is truly and deeply annoying to go through 50 bot ships a day just
	 * because they keep running out of power, so this override will boost the
	 * energy of all undamaged bot ships whenever they run out. By making sure
	 * they're undamaged, we avoid giving them the advantage of extra staying
	 * power in a fight.
	 */
	@Override
	protected void onEnergyExhaustion()
	{
		if (damage < 0.1) {
			energy = 10000;
		}
		else {
			super.onEnergyExhaustion();
		}
	}
	
	/**
	 * Implementation required because callback from command seems
	 * to expect that every combatant needs to implement this.
	 * @param cmd
	 */
	public void processMessageCommand(MessageCommand cmd)
	{
		// thanks for the share but the bot does not care
		;
	}
	
	private boolean quiet()
	{
		boolean noRotation = ( getRotationTime() <= 0.0);
		boolean noAcceleration = ( getAccelTime() <= 0.0);
		boolean noHeat = (engineHeat <= 0.0);
		
		return noRotation && noAcceleration && noHeat;
	}

	/**
	 * Taunt the player
	 */
	private void trashTalk(Combatant opponent) {
		
		String[] taunts = {
				"Maybe you should try XBox Live.",
				"I find your lack of win disturbing.", 
				"Fear my wrath.",
				"Caesar si viveret, ad remum dareris."};
		
		if (opponent instanceof PlayerShip) {
			if (Math.random() > 0.75)  {
				getGameServer().sendMessage(opponent.getShipNumber(),"\nMessage from " + this.commander + " :  " + taunts[new Random().nextInt(taunts.length-1)]+ "\n");
			}
		}
		
	}

	@Override
	public void update(long updateTime) {
		super.update(updateTime);
		
		long intervalLong = updateTime - getLastUpdateTime();
		double intervalLen = (double)(intervalLong / 1000);
		cycleTimeLeft = Math.max(cycleTimeLeft - intervalLen, 0.0);
	}
}
