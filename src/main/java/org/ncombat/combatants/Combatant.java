package org.ncombat.combatants;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ncombat.GameServer;
import org.ncombat.ShieldArray;
import org.ncombat.command.Command;
import org.ncombat.command.CommandBatch;
import org.ncombat.utils.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unchecked")
public abstract class Combatant
{
	protected static class AttackResult
	{
		public int shieldHit;
		public double damage;
	}
	
	// Repairs begin REPAIR_WAIT_TIME seconds after damage is inflicted. 
	private static final double REPAIR_WAIT_TIME = 40.0;
	
	// Default damage repair rate (in % per second).
	private static final double DEFAULT_REPAIR_RATE = 0.50;
	
	private static int nextId = 1;
	
	private static synchronized int getNextId() {
		return nextId++;
	}
	
	private Logger log = LoggerFactory.getLogger(Combatant.class);
	
	private int id;
	
	protected String commander;
	
	protected int numKills;
	
	private long lastUpdateTime;
	
	protected boolean alive = true;
	
	private String status = "";
	
	private List<String> messages = new ArrayList<String>();
	
	protected GameServer gameServer;
	
	private Map<Class,Method> commandMethods = new HashMap<Class,Method>();
	
	private int shipNumber;
	
	protected Vector position = Vector.ZERO;
	
	protected Vector velocity = Vector.ZERO;
	
	protected double energy;
	protected double damage;
	protected double repairRate = DEFAULT_REPAIR_RATE;
	
	protected double repairWaitTime = REPAIR_WAIT_TIME;
	
	protected ShieldArray shields;
	
	private long lastContactTime;
	
	private Combatant lastAttacker;
	protected Date timeOn;
	
	protected Date timeOff;
	
	public Combatant(String commander)
	{
		this.id = getNextId();
		this.commander = commander;
		this.lastContactTime = System.currentTimeMillis();
		this.timeOn = new Date();
	}
	
	protected double addDamage(double damage)
	{
		double existingDamage = this.damage;
		double newDamage = Math.min( damage, 100.0 - existingDamage);
		
		this.damage += newDamage;
		
		if (this.damage >= 100.0) {
			alive = false;
		}
		else {
			this.repairWaitTime = REPAIR_WAIT_TIME;
		}
		
		return newDamage;
	}
	
	public void addMessage(String message) {
		synchronized (messages) {
			messages.add(message);
		}
	}
	
	public void addMessages(Collection<String> messages) {
		synchronized (this.messages) {
			this.messages.addAll(messages);
		}
	}

	public void clearMessages() {
		synchronized (messages) {
			messages.clear();
		}
	}

	public abstract void completeGameCycle();

	public List<String> drainMessages() {
		ArrayList<String> drainedMessages = null;
		synchronized (messages) {
			drainedMessages = new ArrayList<String>(messages);
			messages.clear();
		}
		return drainedMessages;
	}

	public String getCommander() {
		return commander;
	}
	
	public GameServer getGameServer() {
		return gameServer;
	}
	
	public int getId() {
		return id;
	}
	
	public Combatant getLastAttacker() {
		return lastAttacker;
	}
	
	public long getLastContactTime() {
		return lastContactTime;
	}

	public long getLastUpdateTime() {
		return lastUpdateTime;
	}
	
	public ShieldArray getShields() {
		return shields;
	}
	
	public int getShipNumber() {
		return shipNumber;
	}
	
	public String getStatus() {
		return status;
	}
	
	public boolean isAlive() {
		return alive;
	}
	
	private void markDead(String status)
	{
		this.status = status;
		this.alive = false;
		this.timeOff = new Date();
		
		if (gameServer != null) {
			gameServer.removeCombatant(this);
		}
	}
	
	public void markDestroyed(Combatant killer)
	{
		String fmt = "Ship %d - %s commanding, was just destroyed";
		String msg = String.format(fmt, shipNumber, commander);
		gameServer.sendMessage(msg, shipNumber);
		
		fmt = "by ship %d - %s commanding.";
		msg = String.format(fmt, killer.shipNumber, killer.commander);
		gameServer.sendMessage(msg, shipNumber);
		
		addMessage("Your ship has been destroyed.");
		
		String status = null;
		
		if (killer instanceof GornBase) {
			status = "DG" + (killer.shipNumber - 20);
		}
		else {
			status = "DD" + killer.shipNumber;
		}
		
		markDead(status);
	}
	
	public void markDestroyedByEngineOverload() {
		addMessage("Your ship has been destroyed.");		
		markDead("DEO");
	}
	
	public void markExhausted()
	{
		String msg = "Ship " + shipNumber + " - " + commander
						+ " commanding, just lost all power.";
		gameServer.sendMessage(msg, shipNumber);
		addMessage("Your ship has lost all power.");
		markDead("LPR");
	}
	
	public void markHungUp()
	{
		String msg = "The crew of ship " + shipNumber + " - " + commander
						+ " commanding, just abandoned ship.";
		gameServer.sendMessage(msg);
		markDead("HNG");
	}
	
	// if player logs out normally, no one gets credit for a kill.
	public void markStopCommand()
	{
		String msg = "Commander " + commander + " quit the game.";
		gameServer.sendMessage(msg);
		markDead("DDS");
	}
	
	public Combatant nearest() {
		if (gameServer == null) return null;
		return gameServer.nearest(this);
	}
	
	public double nearestRange() {
		if (gameServer == null) return Double.NaN;
		return gameServer.nearestRange(this);
	}

	public int numMessages() {
		synchronized (messages) {
			return messages.size();
		}
	}

	protected abstract AttackResult onLaserHit(Combatant attacker, double power);

	protected abstract AttackResult onMissileHit(Combatant attacker);

	public void processCommands(CommandBatch commandBatch)
	{
		setLastContactTime( commandBatch.getTimestamp());
		
		for (Command command : commandBatch.getCommands()) {
			if (!alive) return;
			
			Class commandClass = command.getClass();
			
			Method commandMethod = commandMethods.get(commandClass);
			
			if (commandMethod == null) {
				String methodName = "process" + commandClass.getSimpleName();
				try {
					Class combatantClass = this.getClass();
					commandMethod = combatantClass.getMethod(methodName, commandClass);
				}
				catch (Exception e) {
					log.error("Caught exception looking up command method " + methodName + "().");
				}
				
				commandMethods.put(commandClass, commandMethod);
			}
			try {
				commandMethod.invoke(this, command);
			}
			catch (Exception e) {
				log.error("Caught exception invoking command method.", e);
			}
		}
	}
	
	public void processKill(Combatant killed) {
		this.numKills++;
	}
	
	public double range(Combatant combatant) {
		return position.subtract(combatant.position).r();
	}

	public void setGameServer(GameServer gameServer) {
		this.gameServer = gameServer;
	}
	
	public void setLastAttacker(Combatant lastAttacker) {
		this.lastAttacker = lastAttacker;
	}
	
	public void setLastContactTime(long lastContactTime) {
		this.lastContactTime = lastContactTime;
	}
	
	public void setLastUpdateTime(long lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
	}
	
	/**
	 * Allows the game server or other designated authorities to set the initial
	 * position of the combatant when it enters the combat zone.
	 */
	public void setPosition(Vector pos) {
		this.position = pos;
		log.debug("# " + this.id + " / " + this.commander + " position set to " + this.position.x() + " / " + this.position.y());
	}
	
	public void setShipNumber(int shipNumber) {
		this.shipNumber = shipNumber;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}
	
	/**
	 * Allows the game server or other designated authorities to set the initial
	 * velocity of the combatant when it enters the combat zone.
	 */
	public void setVelocity(Vector velocity) {
		this.velocity = velocity;
	}
	
	public double speed(Combatant ship) {
		return ship.velocity.r();
	}
	
	public abstract void update(long updateTime);
}
