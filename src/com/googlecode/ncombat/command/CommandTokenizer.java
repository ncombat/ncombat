package com.googlecode.ncombat.command;

import java.util.ArrayList;
import java.util.List;

public class CommandTokenizer
{
	private CommandText command;
	private List<CommandText> commands;
	private StringBuilder argBuf = new StringBuilder();
	
	public CommandTokenizer() {
	}
	
	public List<CommandText> parse(String line)
	{
		command = null;
		commands = new ArrayList<CommandText>();
		argBuf.setLength(0);
		
		if (line == null) return commands;
		line = line.trim();
		if (line.length() == 0) return commands;
		
		if (line.toUpperCase().equals("STOP")) {
			command = new CommandText("STOP");
			commands.add(command);
			return commands;
		}
		
		boolean inLongArg = false;
		
		for (int i = 0 ; i < line.length() ; i++) {
			char ch = line.charAt(i);
			
			if (ch == '|') {
				completeCurrentArgument();
				inLongArg = !inLongArg;
			}
			else {
				if (inLongArg) {
					appendToCurrentArgument(ch);
				}
				else {
					if (Character.isWhitespace(ch)) {
						continue;
					}
					else if (Character.isLetter(ch)) {
						newCommand(ch);
					}
					else if (ch == ',') {
						completeCurrentArgument();
					}					
					else {
						appendToCurrentArgument(ch);
					}
				}
			}
		}
		
		completeCurrentCommand();
		
		return commands;
	}
	
	private void newCommand(char action) {
		completeCurrentCommand();
		command = new CommandText(String.valueOf(action));
	}
	
	private void completeCurrentCommand()
	{
		if (command == null) return;

		completeCurrentArgument();
		
		commands.add(command);
		command = null;
	}
	
	private void appendToCurrentArgument(char ch) {
		if (command == null) command = new CommandText(null);
		argBuf.append(ch);
	}
	
	private void completeCurrentArgument()
	{
		if (argBuf.length() > 0) {
			command.getArgs().add(argBuf.toString());
			argBuf.setLength(0);
		}
	}

	@Override
	public String toString()
	{
		StringBuilder buf = new StringBuilder();
		
		for (CommandText cmd : commands) {
			buf.append(cmd.toString());
		}
		
		return buf.toString();
	}
}
