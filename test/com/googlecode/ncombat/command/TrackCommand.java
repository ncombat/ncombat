package com.googlecode.ncombat.command;

public class TrackCommand implements Command
{
	private int target;
	
	public TrackCommand(int target) {
		this.target = target;
	}

	public int getTarget() {
		return target;
	}
}
