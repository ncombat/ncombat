package org.ncombat.combatants;

import java.util.List;

import org.apache.log4j.Logger;
import org.ncombat.command.Command;
import org.ncombat.command.CommandBatch;
import org.ncombat.command.LaserCommand;
import org.ncombat.command.MissileCommand;
import org.ncombat.command.RotateCommand;
import org.ncombat.command.ShieldCommand;
import org.ncombat.utils.NcombatMath;
import org.ncombat.utils.Vector;


public class BotShip extends Ship
{
	// Length of time between command generation cycles (seconds).
	private static final double CYCLE_LEN = 60.0;
	
	/*
	 * Shields will snap on if any combatants come within SHIELD_SNAP_RANGE
	 * kilometers.
	 */
	private static final double SHIELD_SNAP_RANGE = 20000.0;
	
	/*
	 * Bot ship will attack a vessel that comes within ENGAGEMENT_RANGE
	 * kilometers.
	 */
	private static final double ENGAGEMENT_RANGE = 15000.0;
	
	private Logger log = Logger.getLogger(BotShip.class);
	
	private double cycleTimeLeft = CYCLE_LEN;
	
	public BotShip(String commander) {
		super(commander);
	}
	
	@Override
	public void update(long updateTime) {
		super.update(updateTime);
		
		long intervalLong = updateTime - getLastUpdateTime();
		double intervalLen = (double)(intervalLong / 1000);
		cycleTimeLeft = Math.max(cycleTimeLeft - intervalLen, 0.0);
	}

	@Override
	public void completeGameCycle()
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
			
			cycleTimeLeft = CYCLE_LEN;
		}
		
		List<Command> cmds = batch.getCommands();
		
		if (! cmds.isEmpty()) {
			StringBuilder msg = new StringBuilder();
			msg.append("Bot commands: [" + commander + "]:");
			for (Command cmd : cmds) {
				msg.append(" ");
				msg.append(cmd.toString());
			}
			log.debug(msg);
			
			gameServer.addCommandBatch(batch);
		}
	}
}
