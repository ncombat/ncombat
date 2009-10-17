package org.ncombat;

import org.ncombat.command.BriefModeCommand;
import org.ncombat.command.HelpCommand;
import org.ncombat.command.IntelCommand;
import org.ncombat.command.MessageCommand;
import org.ncombat.command.NullCommand;
import org.ncombat.command.SensorCommand;
import org.ncombat.command.StopCommand;
import org.ncombat.command.TrackCommand;
import org.ncombat.utils.Vector;

public class PlayerShip extends Ship
{
	public PlayerShip(Vector position) {
		super(position);
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

	public void processMessageCommand(MessageCommand cmd) {
		addMessage("Processing " + cmd);
	}

	public void processNullCommand(NullCommand cmd) {
		addMessage("Processing " + cmd);
	}
	
	public void processSensorCommand(SensorCommand cmd) {
		addMessage("Processing " + cmd);
	}

	public void processStopCommand(StopCommand cmd) {
		addMessage("Processing " + cmd);
	}

	public void processTrackCommand(TrackCommand cmd) {
		addMessage("Processing " + cmd);
	}
}
