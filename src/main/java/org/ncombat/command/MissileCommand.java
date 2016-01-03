package org.ncombat.command;

public class MissileCommand implements Command
{
	private int target;

	public MissileCommand(int target) {
		this.target = target;
	}

	public int getTarget() {
		return target;
	}
	
	@Override
	public String toString() {
		return "[MissileCommand: target=" + target + "]";
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + target;
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MissileCommand other = (MissileCommand) obj;
		if (target != other.target)
			return false;
		return true;
	}
}
