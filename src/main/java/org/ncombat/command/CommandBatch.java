package org.ncombat.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.ncombat.GameServer;
import org.ncombat.combatants.Combatant;

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
	
	public void addCommands(Collection<Command> commands) {
		this.commands.addAll(commands);
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((combatant == null) ? 0 : combatant.hashCode());
		result = prime * result
				+ ((commands == null) ? 0 : commands.hashCode());
		result = prime * result + (regenStatusReadout ? 1231 : 1237);
		result = prime * result + (int) (timestamp ^ (timestamp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CommandBatch other = (CommandBatch) obj;
		if (combatant == null) {
			if (other.combatant != null)
				return false;
		} else if (!combatant.equals(other.combatant))
			return false;
		if (commands == null) {
			if (other.commands != null)
				return false;
		} else if (!commands.equals(other.commands))
			return false;
		if (regenStatusReadout != other.regenStatusReadout)
			return false;
		if (timestamp != other.timestamp)
			return false;
		return true;
	}
}
