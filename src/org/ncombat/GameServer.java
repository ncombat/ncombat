package org.ncombat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.ncombat.combatants.BotShip;
import org.ncombat.combatants.Combatant;
import org.ncombat.combatants.GornBase;
import org.ncombat.combatants.PlayerShip;
import org.ncombat.command.CommandBatch;
import org.ncombat.utils.Vector;
import org.springframework.beans.factory.DisposableBean;

public class GameServer implements DisposableBean
{
	public static final long DEFAULT_CYCLE_PERIOD = 1000; // milliseconds
	
	private static Vector randomPosition()
	{
		double maxCoord = 30000.0;
		double x0 = (2.0 * (Math.random() - 0.5)) * maxCoord;
		double y0 = (2.0 * (Math.random() - 0.5)) * maxCoord;
		Vector position = new Vector(x0, y0);
		return position;
	}
	
	private static Vector randomVelocity()
	{
		double maxSpeed = 20.0;
		double speed = Math.floor( Math.random() * maxSpeed);
		double heading = Math.random() * 360.0;
		Vector velocity = Vector.polarDegrees(speed, heading);
		return velocity;
	}
	
	private static final long PLAYER_SYNC_TIMEOUT = 5000; // milliseconds
	
	private static int nextServerNumber = 1;
	
	private Logger log = Logger.getLogger(GameServer.class);
	
	private int serverNumber;
	private Timer timer;
	private GameServerTimerTask timerTask;
	private long cyclePeriod = DEFAULT_CYCLE_PERIOD;
	private boolean started;
	
	//------------------------------------------------------------
	// The following variables are protected by the cycle monitor.
	//------------------------------------------------------------
	
	private Object cycleMonitor = new Object();
	private boolean paused;
	private Map<Integer,Combatant> combatants = new HashMap<Integer,Combatant>();
	private List<CommandBatch> commandBatches = new ArrayList<CommandBatch>();
	
	//-------------------------------------------------
	// End of variables protected by the cycle monitor.
	//-------------------------------------------------
	
	/*
	 * This object is used by the GameController to synchronize the game cycle
	 * with the incoming HTTP command submissions from the players. After
	 * submitting a command batch to the queue, the GameController waits on this
	 * monitor for the game cycle to complete so that they might return their
	 * messages and accept the next round of commands.
	 */
	private Object playerSyncMonitor = new Object();
	
	public GameServer()
	{
		synchronized (GameServer.class) {
			this.serverNumber = nextServerNumber++;
		}
		
		log.info("GameServer #" + serverNumber + " is starting.");
		
		// Create Gorn bases
		addCombatant(21, new GornBase(new Vector(100000.0,0.0)));
		addCombatant(22, new GornBase(new Vector(0.0,100000.0)));
		addCombatant(23, new GornBase(new Vector(-100000.0,0.0)));
		addCombatant(24, new GornBase(new Vector(0,-100000.0)));
		
		// TODO: Gorn base regeneration
		
		// Create a few bot ships just for fun.
		int numBots = 2;
		for (int i = 0 ; i < numBots ; i++) {
			Vector botPosition = randomPosition();
			Vector botVelocity = randomVelocity();
			addCombatant( new BotShip(botPosition, botVelocity));
		}

		timer = new Timer("GameServer" + serverNumber, true);
		timerTask = new GameServerTimerTask();
	}
	
	public void destroy() throws Exception {
		stop();
	}
	
	private void runGameCycle()
	{
		// log.debug("Entering game cycle.");
		
		synchronized (cycleMonitor) {
			long updateTime = System.currentTimeMillis();
			
			for (Combatant combatant : combatants.values()) {
				if (combatant.isAlive()) {
					if (!paused) {
						combatant.update(updateTime);
					}
					combatant.setLastUpdateTime(updateTime);
				}
			}
			
			List<CommandBatch> commandBatches = drainCommandBatches();
			
			if (!paused) {
				for (CommandBatch commandBatch : commandBatches) {
					Combatant combatant = commandBatch.getCombatant();
					if (combatant.isAlive()) {
						combatant.processCommands(commandBatch);
					}
				}
				
				for (Combatant combatant : combatants.values()) {
					if (combatant.isAlive()) {
						combatant.completeGameCycle();
					}
				}
			}
		}
		
		notifyPlayers();
		
		// log.debug("Exiting game cycle.");
	}
	
	public boolean isPaused() {
		synchronized (cycleMonitor) {
			return paused;
		}
	}

	public void setPaused(boolean paused) {
		synchronized (cycleMonitor) {
			this.paused = paused;
		}
	}
	
	public PlayerShip createPlayerShip()
	{
		PlayerShip ship = new PlayerShip( randomPosition());
		
		if ( addCombatant(ship) == 0) {
			return null;
		}
		
		ship.generateDataReadout();
		
		return ship;
	}
	
	public int addCombatant(Combatant combatant)
	{
		synchronized (cycleMonitor) {
			for (int shipNum = 1 ; shipNum <= 15 ; shipNum++){
				if (!combatants.containsKey(shipNum)) {
					addCombatant(shipNum, combatant);
					return shipNum;
				}
			}
		}
		
		return 0;
	}

	public void addCombatant(int shipNumber, Combatant combatant) {
		synchronized (cycleMonitor) {
			combatant.setShipNumber(shipNumber);
			combatant.setGameServer(this);
			combatant.setLastUpdateTime(System.currentTimeMillis());
			combatants.put(shipNumber, combatant);
		}
	}
	
	public void removeCombatant(Combatant combatant)
	{
		synchronized (cycleMonitor) {
			if ( combatant.getGameServer() == this) {
				int shipNumber = combatant.getShipNumber();
				if (combatants.containsKey(shipNumber)) {
					combatants.remove(shipNumber);
					combatant.setGameServer(null);
					combatant.setShipNumber(0);
				}
			}
		}
	}
	
	public Combatant getCombatant(int shipNum) {
		synchronized (cycleMonitor) {
			return combatants.get(shipNum);
		}
	}
	
	public List<Combatant> getCombatants()
	{
		List<Combatant> results = null;
		
		synchronized (cycleMonitor) {
			results = new ArrayList<Combatant>( combatants.values());
		}
		
		Collections.sort(results, new Comparator<Combatant>() {
			public int compare(Combatant o1, Combatant o2) {
				Integer s1 = o1.getShipNumber();
				Integer s2 = o2.getShipNumber();
				return s1.compareTo(s2);
			}
		});
		
		return results;
	}
	
	public void addCommandBatch(CommandBatch commandBatch) {
		synchronized (cycleMonitor) {
			commandBatches.add(commandBatch);
		}
	}
	
	private List<CommandBatch> drainCommandBatches() {
		synchronized (cycleMonitor) {
			List<CommandBatch> drainedBatches = new ArrayList(commandBatches);
			commandBatches.clear();
			return drainedBatches;
		}
	}
	
	public void sendMessage(int shipNum, String message) {
		synchronized (cycleMonitor) {
			Combatant combatant = combatants.get(shipNum);
			if (combatant != null) {
				combatant.addMessage(message);
			}
		}
	}
	
	public void sendMessage(String message) {
		synchronized (cycleMonitor) {
			for (Combatant combatant : combatants.values()) {
				combatant.addMessage(message);
			}
		}
	}
	
	public void playerSync()
	{
		synchronized (playerSyncMonitor) {
			try {
				playerSyncMonitor.wait(PLAYER_SYNC_TIMEOUT);
			}
			catch (InterruptedException e) {
				log.warn("playerSync() caught exception.", e);
			}
		}
	}
	
	private void notifyPlayers()
	{
		synchronized (playerSyncMonitor) {
			playerSyncMonitor.notifyAll();
		}
	}
	
	public synchronized void start()
	{
		if (started) return;
		timer.schedule(timerTask, cyclePeriod, cyclePeriod);
		started = true;
	}
	
	public synchronized void stop()
	{
		if (!started) return;
		timerTask.cancel();
		started = false;
	}
	
	public long getCyclePeriod() {
		return cyclePeriod;
	}

	public void setCyclePeriod(long cyclePeriod) {
		this.cyclePeriod = cyclePeriod;
	}
	
	public class GameServerTimerTask extends TimerTask
	{
		@Override
		public void run() {
			runGameCycle();
		}
	}
}
