package org.ncombat.command;

public class NullCommand implements Command
{
	public NullCommand() {
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (this == o) return true;
		if ( getClass().equals(o.getClass())) return true;
		return false;
	}

	@Override
	public int hashCode() {
		return 0;
	}
}
