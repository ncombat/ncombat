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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + target;
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
		TrackCommand other = (TrackCommand) obj;
		if (target != other.target)
			return false;
		return true;
	}
}
