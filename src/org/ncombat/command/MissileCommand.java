package org.ncombat.command;

public class MissileCommand implements Command
{
	private int target1;
	private int target2;

	public MissileCommand(int target1) {
		this(target1, 0);
	}

	public MissileCommand(int target1, int target2) {
		this.target1 = target1;
		this.target2 = target2;
	}

	public int getTarget1() {
		return target1;
	}

	public int getTarget2() {
		return target2;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + target1;
		result = prime * result + target2;
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
		MissileCommand other = (MissileCommand) obj;
		if (target1 != other.target1)
			return false;
		if (target2 != other.target2)
			return false;
		return true;
	}
}
