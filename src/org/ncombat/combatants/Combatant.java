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
	private static int nextId = 1;
	
	private static synchronized int getNextId() {
		return nextId++;
	}
	
	private Logger log = Logger.getLogger(Combatant.class);
	
	private int id;
	
	private long lastUpdateTime;
	
	private boolean alive = true;
	
	private List<String> messages = new ArrayList<String>();
	
	private GameServer gameServer;
	
	private Map<Class,Method> commandMethods = new HashMap<Class,Method>();
	
	private int shipNumber;
	
	private Vector position;
	
	private double damage;
	
	private double damageRepairRate;
	
	private double damageRepairStartTime;
	
	private ShieldArray shields;
	
	private double energy;
	
	public Combatant(Vector position) {
		this.id = getNextId();
		this.position = position;
	}
	
	public abstract void update(long updateTime);
	
	public void processCommands(CommandBatch commandBatch) {
		for (Command command : commandBatch.getCommands()) {
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

	public boolean isAlive() {
		return alive;
	}

	public void setAlive(boolean alive) {
		this.alive = alive;
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
	
	public int getShipNumber() {
		return shipNumber;
	}

	public void setShipNumber(int shipNumber) {
		this.shipNumber = shipNumber;
	}

	public ShieldArray getShields() {
		return shields;
	}

	public void setShields(ShieldArray shields) {
		this.shields = shields;
	}

	public Vector getPosition() {
		return position;
	}

	public void setPosition(Vector position) {
		this.position = position;
	}

	public double getDamage() {
		return damage;
	}

	public void setDamage(double damage) {
		this.damage = damage;
	}
}
