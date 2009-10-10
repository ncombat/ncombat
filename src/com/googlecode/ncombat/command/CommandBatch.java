package com.googlecode.ncombat.command;

import java.util.ArrayList;
import java.util.List;

import com.googlecode.ncombat.Combatant;

public class CommandBatch
{
	private long timestamp;
	private Combatant combatant;
	private List<Command> commands = new ArrayList<Command>();
	private boolean regenStatusReadout;
	
	public CommandBatch(long timestamp, Combatant combatant) {
		this.timestamp = timestamp;
		this.combatant = combatant;
	}
	
	public void addCommand(Command command) {
		commands.add(command);
	}

	public long getTimestamp() {
		return timestamp;
	}

	public Combatant getCombatant() {
		return combatant;
	}

	public List<Command> getCommands() {
		return commands;
	}

	public boolean getRegenStatusReadout() {
		return regenStatusReadout;
	}

	public void setRegenStatusReadout(boolean regenStatusReadout) {
		this.regenStatusReadout = regenStatusReadout;
	}
}
