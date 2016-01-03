package org.ncombat.command;

import java.util.ArrayList;
import java.util.List;

public class CommandText
{
	private String action;
	
	private List<String> args = new ArrayList<String>(2);
	
	public CommandText(String action) {
		this.action = action;
	}
	
	public String getAction() {
		return action;
	}

	public List<String> getArgs() {
		return args;
	}
	
	public String getArg(int i) {
		return args.get(i);
	}
	
	public int numArgs() {
		return args.size();
	}
	

	@Override
	public String toString()
	{
		StringBuilder buf = new StringBuilder();
		
		buf.append(action);
		
		for (int i = 0 ; i < args.size() ; i++) {
			if (i > 0) buf.append(',');
			buf.append(args.get(i));
		}
		
		return buf.toString();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((action == null) ? 0 : action.hashCode());
		result = prime * result + ((args == null) ? 0 : args.hashCode());
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
		CommandText other = (CommandText) obj;
		if (action == null) {
			if (other.action != null)
				return false;
		} else if (!action.equals(other.action))
			return false;
		if (args == null) {
			if (other.args != null)
				return false;
		} else if (!args.equals(other.args))
			return false;
		return true;
	}
}
