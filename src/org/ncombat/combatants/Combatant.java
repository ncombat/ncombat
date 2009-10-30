package org.ncombat.combatants;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.ncombat.GameServer;
import org.ncombat.ShieldArray;
import org.ncombat.command.Command;
import org.ncombat.command.CommandBatch;
import org.ncombat.utils.Vector;

public abstract class Combatant
{
	// Repairs begin REPAIR_WAIT_TIME seconds after damage is inflicted. 
	private static final double REPAIR_WAIT_TIME = 40.0;
	
	// Default damage repair rate (in % per second).
	private static final double DEFAULT_REPAIR_RATE = 0.50;
	
	private static int nextId = 1;
	
	private static synchronized int getNextId() {
		return nextId++;
	}
	
	private Logger log = Logger.getLogger(Combatant.class);
	
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
	
	protected Vector position;
	
	protected Vector velocity = Vector.ZERO;
	
	protected double energy;
	
	protected double damage;
	protected double repairRate = DEFAULT_REPAIR_RATE;
	protected double repairWaitTime = REPAIR_WAIT_TIME;
	
	protected ShieldArray shields;
	
	public Combatant(String commander) {
		this.id = getNextId();
		this.commander = commander;
	}
	
	public abstract void update(long updateTime);
	
	public void processCommands(CommandBatch commandBatch)
	{
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
	
	public abstract void completeGameCycle();
	
	public int getId() {
		return id;
	}

	public GameServer getGameServer() {
		return gameServer;
	}

	public void setGameServer(GameServer gameServer) {
		this.gameServer = gameServer;
	}

	public long getLastUpdateTime() {
		return lastUpdateTime;
	}

	public void setLastUpdateTime(long lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
	}

	/**
	 * Allows the game server or other designated authorities to set the initial
	 * position of the combatant when it enters the combat zone.
	 */
	public void setPosition(Vector position) {
		this.position = position;
	}
	
	/**
	 * Allows the game server or other designated authorities to set the initial
	 * velocity of the combatant when it enters the combat zone.
	 */
	public void setVelocity(Vector velocity) {
		this.velocity = velocity;
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
	
	public List<String> drainMessages() {
		ArrayList<String> drainedMessages = null;
		synchronized (messages) {
			drainedMessages = new ArrayList<String>(messages);
			messages.clear();
		}
		return drainedMessages;
	}
	
	public void clearMessages() {
		synchronized (messages) {
			messages.clear();
		}
	}
	
	public int numMessages() {
		synchronized (messages) {
			return messages.size();
		}
	}
	
	public void markExhausted() {
		addMessage("Your ship has lost all power.");
		markDead("LPR");
	}
	
	public void markDestroyedByEngineOverload() {
		addMessage("Your ship has been destroyed.");		
		markDead("DEO");
	}
	
	public void markDestroyed(Combatant killer)
	{
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
	
	private void markDead(String status)
	{
		this.status = status;
		this.alive = false;
		
		if (gameServer != null) {
			gameServer.removeCombatant(this);
		}
	}
	
	public int getShipNumber() {
		return shipNumber;
	}

	public void setShipNumber(int shipNumber) {
		this.shipNumber = shipNumber;
	}

	public boolean isAlive() {
		return alive;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	public String getCommander() {
		return commander;
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
	
	protected static class AttackResult
	{
		public int shieldHit;
		public double damage;
	}
	
	protected abstract AttackResult onLaserHit(Combatant attacker, double power);
	
	protected abstract AttackResult onMissileHit(Combatant attacker);
	
	public void processKill(Combatant killed)
	{
		String fmt = "Ship %d - %s commanding, was just destroyed";
		String msg = String.format(fmt, killed.shipNumber, killed.commander);
		gameServer.sendMessage(msg);
		
		fmt = "by ship %d - %s commanding.";
		msg = String.format(fmt, this.shipNumber, this.commander);
		gameServer.sendMessage(msg);
		
		this.numKills++;
		
		killed.markDestroyed(this);
	}
	
	public double range(Combatant combatant) {
		return position.subtract(combatant.position).r();
	}
	
	public double speed(Combatant ship) {
		return ship.velocity.r();
	}
}
