package org.ncombat;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.ncombat.command.AccelerateCommand;
import org.ncombat.command.BriefModeCommand;
import org.ncombat.command.Command;
import org.ncombat.command.CommandBatch;
import org.ncombat.command.HelpCommand;
import org.ncombat.command.IntelCommand;
import org.ncombat.command.LaserCommand;
import org.ncombat.command.MessageCommand;
import org.ncombat.command.MissileCommand;
import org.ncombat.command.NullCommand;
import org.ncombat.command.RepairCommand;
import org.ncombat.command.RotateCommand;
import org.ncombat.command.SensorCommand;
import org.ncombat.command.ShieldCommand;
import org.ncombat.command.StopCommand;
import org.ncombat.command.TrackCommand;

public class Combatant
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
	
	public Combatant() {
		this.id = getNextId();
	}
	
	public void update(long updateTime) {
	}
	
	private Map<Class,Method> commandMethods = new HashMap<Class,Method>();
	
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
	
	public void completeGameCycle() {
	}
	
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
	
	public void processAccelerateCommand(AccelerateCommand cmd) {
		addMessage("Processing " + cmd);
	}
	
	public void processBriefModeCommand(BriefModeCommand cmd) {
		addMessage("Processing " + cmd);
	}

	public void processHelpCommand(HelpCommand cmd) {
		addMessage("Processing " + cmd);
	}

	public void processIntelCommand(IntelCommand cmd) {
		addMessage("Processing " + cmd);
	}

	public void processLaserCommand(LaserCommand cmd) {
		addMessage("Processing " + cmd);
	}

	public void processMessageCommand(MessageCommand cmd) {
		addMessage("Processing " + cmd);
	}

	public void processMissileCommand(MissileCommand cmd) {
		addMessage("Processing " + cmd);
	}

	public void processNullCommand(NullCommand cmd) {
		addMessage("Processing " + cmd);
	}

	public void processRepairCommand(RepairCommand cmd) {
		addMessage("Processing " + cmd);
	}

	public void processRotateCommand(RotateCommand cmd) {
		addMessage("Processing " + cmd);
	}

	public void processSensorCommand(SensorCommand cmd) {
		addMessage("Processing " + cmd);
	}

	public void processShieldCommand(ShieldCommand cmd) {
		addMessage("Processing " + cmd);
	}
	
	public void processStopCommand(StopCommand cmd) {
		addMessage("Processing " + cmd);
	}

	public void processTrackCommand(TrackCommand cmd) {
		addMessage("Processing " + cmd);
	}
}
