package org.ncombat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.ncombat.combatants.BotShip;
import org.ncombat.combatants.Combatant;
import org.ncombat.combatants.GornBase;
import org.ncombat.combatants.PlayerShip;
import org.ncombat.combatants.Ship;
import org.ncombat.command.CommandBatch;
import org.ncombat.utils.Vector;
import org.springframework.beans.factory.DisposableBean;

@SuppressWarnings("unchecked")
public class GameServer implements DisposableBean
{
	public class GameServerTimerTask extends TimerTask
	{
		@Override
		public void run() {
			runGameCycle();
		}
	}
	
	public static final int NUM_BOT_SHIPS = 2;
	
	public static final int NUM_GORN_BASES = 4;
	
	/** 
	 * Gorn bases are arranged at uniform intervals on a circle of
	 * GORN_BASE_RADIUS kilometers from the center of the combat
	 * zone. 
	 */
	public static final double GORN_BASE_RADIUS = 100000.0;
	
	private static final long DEFAULT_CYCLE_PERIOD = 1000; // milliseconds
	
	private static final long BOT_SHIP_REGEN_TIME = 60000; // milliseconds
	
	private static final long GORN_BASE_REGEN_TIME  = 180000; // milliseconds
	
	
	/*
	 * Shields snap on automatically when joining the game if there is another
	 * combatant within SHIELD_SNAP_RANGE kilometers.
	 */
	private static final double SHIELD_SNAP_RANGE = 20000; 
	
	private static int nextBotShipNum = 1;
	
	private static final long PLAYER_SYNC_TIMEOUT = 5000; // milliseconds
	
	private static int nextServerNumber = 1;
	
	private static Vector randomPosition()
	{
		double maxCoord = 25000.0;
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
	
	private Logger log = Logger.getLogger(GameServer.class);
	
	private GameManager gameManager;
	private int serverNumber;
	private Timer timer;
	private GameServerTimerTask timerTask;
	private long cyclePeriod = DEFAULT_CYCLE_PERIOD;
	
	//------------------------------------------------------------
	// The following variables are protected by the cycle monitor.
	//------------------------------------------------------------
	
	private boolean started;
	private Object cycleMonitor = new Object();
	private boolean paused;
	private Map<Integer,Combatant> combatants = new HashMap<Integer,Combatant>();
	private LinkedList<Combatant> formerPlayers = new LinkedList<Combatant>();
	
	//-------------------------------------------------
	// End of variables protected by the cycle monitor.
	//-------------------------------------------------
	
	private List<CommandBatch> commandBatches = new ArrayList<CommandBatch>();
	
	/*
	 * This object is used by the GameController to synchronize the game cycle
	 * with the incoming HTTP command submissions from the players. After
	 * submitting a command batch to the queue, the GameController waits on this
	 * monitor for the game cycle to complete so that they might return their
	 * messages and accept the next round of commands.
	 */
	private Object playerSyncMonitor = new Object();

	public GameServer(GameManager gameManager)
	{
		this.gameManager = gameManager;
		
		synchronized (GameServer.class) {
			this.serverNumber = nextServerNumber++;
		}
		
		log.info("GameServer #" + serverNumber + " is starting.");
		
		
		// TODO: Gorn base regeneration
		checkGornBases();
		
		// Create a few bot ships just for fun.
		for (int i = 1 ; i <= NUM_BOT_SHIPS ; i++) {
			createBotShip();
		}
		
		timer = new Timer("GameServer" + serverNumber, true);
		timerTask = new GameServerTimerTask();
	}
	
	/**
	 * Need to keep number of Gorn bases constant and because of way
	 * Gorns are positioned, make sure all positions in Gorn orbital are filled.
	 * Therefore, we check a list of gorn bases and fill each empty one.
	 */
	private void checkGornBases() {
		
		// Create Gorn bases
		for (int i = 1 ; i <= NUM_GORN_BASES ; i++) {
			createGornBase(i);
		}
		
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
			
			String fmt = "Ship %d - %s commanding, just appeared.";
			sendMessage( String.format(fmt, shipNumber, 
							combatant.getCommander()), shipNumber);
		}
	}

	public void addCommandBatch(CommandBatch commandBatch) {
		synchronized (cycleMonitor) {
			commandBatches.add(commandBatch);
		}
	}
	
	private void createBotShip()
	{
		Vector position = randomPosition();
		Vector velocity = randomVelocity();
		double heading = velocity.theta();
	
		BotShip botShip = new BotShip("Bot Ship #" + nextBotShipNum++);
		botShip.setPosition(position);
		botShip.setVelocity(velocity);
		botShip.setHeading(heading);
	
		addCombatant(botShip);
	}
	
	void createGornBase(int baseNum)
	{
		if ((baseNum <= 0) || (baseNum > NUM_GORN_BASES)) {
			log.error("Invalid Gorn base number: " + baseNum);
			return;
		}
		
		if (this.getCombatant(baseNum+20) != null) {
			log.debug("Gorn Base " + baseNum + " already exists.");
			return;
		}
		
		else {
		
			GornBase gorn = new GornBase("Gorn Base " + baseNum);
	
			// Determine position on the ring of Gorn bases.
			double theta = 90.0 - ((360.0 / NUM_GORN_BASES) * (baseNum - 1));
			Vector position = Vector.polarDegrees(GORN_BASE_RADIUS, theta);
			log.debug("Setting Gorn " + baseNum + " position with vector " + position);
			gorn.setPosition(position);
			
			addCombatant(20 + baseNum, gorn);
		}
	}

	public PlayerShip createPlayerShip(String commander)
	{
		PlayerShip ship = new PlayerShip(commander);
		ship.setPosition( randomPosition());
		
		if ( addCombatant(ship) == 0) {
			return null;
		}
		
		if ( ship.nearestRange() <= SHIELD_SNAP_RANGE) {
			ShipShieldArray shields = (ShipShieldArray) ship.getShields();
			shields.setPower(1, 25.0);
			shields.setPower(2, 25.0);
		}
		
		ship.generateShipRoster();
		ship.generateDataReadout();
		
		return ship;
	}
	
	public void destroy() throws Exception {
		stop();
	}

	private List<CommandBatch> drainCommandBatches() {
		synchronized (cycleMonitor) {
			List<CommandBatch> drainedBatches = new ArrayList(commandBatches);
			commandBatches.clear();
			return drainedBatches;
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
				
				// list Gorns last
				if ((o1 instanceof PlayerShip) && (o2 instanceof GornBase)) { return  -1; }
				
				else if ((o1 instanceof GornBase) && (o2 instanceof PlayerShip)) { return  1; }
				
				else { 
					return s1.compareTo(s2);
				}
			}
		});
		
		return results;
	}
	
	public long getCyclePeriod() {
		return cyclePeriod;
	}
	
	public List<Combatant> getFormerPlayers()
	{
		synchronized (cycleMonitor) {
			return new ArrayList<Combatant>(formerPlayers);
		}
	}
	
	public boolean isPaused() {
		synchronized (cycleMonitor) {
			return paused;
		}
	}
	
	public Combatant nearest(Combatant combatant)
	{
		Combatant nearest = null;
		double nearestRange = 0.0;
		
		for (Combatant aCombatant : getCombatants()) {
			if (aCombatant == combatant) continue;
			double range = combatant.range(aCombatant);
			if ((nearest == null) || (range < nearestRange)) {
				nearest = aCombatant;
				nearestRange = range;
			}
		}
		
		return nearest;
	}
	
	public double nearestRange(Combatant combatant)
	{
		Combatant nearest = nearest(combatant);
		if (nearest == null) return Double.NaN;
		return combatant.range(nearest);
	}
	
	private void notifyPlayers()
	{
		synchronized (playerSyncMonitor) {
			playerSyncMonitor.notifyAll();
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
	
	public void removeCombatant(Combatant combatant)
	{
		int regenTime=0;
		
		synchronized (cycleMonitor) {
			if ( combatant.getGameServer() == this) {
				int shipNumber = combatant.getShipNumber();
				
				if (combatants.containsKey(shipNumber)) {
					combatants.remove(shipNumber);
					combatant.setGameServer(null);
				}
				
				if (combatant instanceof Ship) {
					formerPlayers.addFirst(combatant);
					
					int maxNumFormerPlayers = 30;
					for ( int numFormerPlayers = formerPlayers.size() ;
						  numFormerPlayers > maxNumFormerPlayers ;
						  numFormerPlayers--)
					{
						formerPlayers.removeLast();
						gameManager.removeCombatant(combatant);
					}
				}
				
				if (combatant instanceof BotShip) {
					TimerTask regenTask = new TimerTask() {
						public void run() {
							synchronized (cycleMonitor) {
								createBotShip();
							}
						}
					};
					timer.schedule(regenTask, BOT_SHIP_REGEN_TIME);
				}
				
				if (combatant instanceof GornBase) {
					// Gorn Base 1
					
					final int gornBN = Integer.parseInt(combatant.getCommander().substring(10)); 
										
					TimerTask regenTask = new TimerTask() {
						public void run() {
							synchronized (cycleMonitor) {
								createGornBase(gornBN);
							}
						}
					};
					timer.schedule(regenTask, GORN_BASE_REGEN_TIME);
				}
				
			}
		}
	}
	
	private void runGameCycle()
	{
		// log.debug("Entering game cycle.");
		
		synchronized (cycleMonitor) {
			long updateTime = System.currentTimeMillis();
			
			// We must copy the list of combatants because sometimes a
			// combatant dies in the update.  In that case, the update
			// removes the combatant from the game server's store, but
			// this is not possible if we're iterating over it at the
			// time.  In that case, a ConcurrentModificationException
			// would be generated.
			
			List<Combatant> updateCombatants = getCombatants();
			
			for (Combatant combatant : updateCombatants) {
				if (combatant.isAlive()) {
					if (paused) {
						// While we're paused, we don't want all our combatants
						// to time out.
						combatant.setLastContactTime(updateTime);
					}
					else {
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
	
	public void sendMessage(int shipNum, String message) {
		synchronized (cycleMonitor) {
			Combatant combatant = combatants.get(shipNum);
			if ((combatant != null) && (combatant instanceof PlayerShip)) {
				combatant.addMessage(message);
			}
		}
	}
	
	public void sendMessage(String message) {
		sendMessage(message, 0);
	}
	
	public void sendMessage(String message, int excludedShip) {
		synchronized (cycleMonitor) {
			for (Combatant combatant : combatants.values()) {
				if (combatant.getShipNumber() != excludedShip) {
					combatant.addMessage(message);
				}
			}
		}
	}
	
	public void setCyclePeriod(long cyclePeriod) {
		this.cyclePeriod = cyclePeriod;
	}
	
	public void setPaused(boolean paused) {
		synchronized (cycleMonitor) {
			this.paused = paused;
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
}
