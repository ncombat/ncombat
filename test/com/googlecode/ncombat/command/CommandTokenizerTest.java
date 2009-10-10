package com.googlecode.ncombat.command;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

public class CommandTokenizerTest
{
	private CommandTokenizer tok;
	private List<CommandText> out;
	private List<CommandText> expected;
	
	@Before
	public void setUp() {
		tok = new CommandTokenizer();
		out = null;
		expected = new ArrayList<CommandText>();
	}
	
	private void process(String line) {
		this.out = tok.parse(line);
		Assert.assertEquals(expected, out);
	}
	
	@Test
	public void testNullLine() {
		// Empty list is expected.
		process(null);
	}

	@Test
	public void testWhiteLine() {
		// Empty list is expected.
		process("   \t \n");
	}
	
	@Test
	public void testOneCommandNoArgs() {
		CommandText ct = new CommandText("A");
		expected.add(ct);
		process("A");
	}
	
	@Test
	public void testOneCommandOneArg() {
		CommandText ct = new CommandText("B");
		ct.getArgs().add("1");
		expected.add(ct);
		process(" B1");
	}
	
	@Test
	public void testTwoCommands() {
		CommandText ct = new CommandText("C");
		expected.add(ct);
		
		ct = new CommandText("D");
		ct.getArgs().add("2");
		expected.add(ct);
		
		process("CD2");
	}
	
	@Test
	public void testBigCommandLine() {
		CommandText ct = new CommandText("E");
		ct.getArgs().add("-22.0");
		expected.add(ct);
		
		ct = new CommandText("F");
		expected.add(ct);
		
		ct = new CommandText("G");
		ct.getArgs().add("3");
		expected.add(ct);
		
		ct = new CommandText("G");
		ct.getArgs().add("4");
		expected.add(ct);
		
		ct = new CommandText("H");
		ct.getArgs().add("5");
		ct.getArgs().add("6.");
		expected.add(ct);
		
		ct = new CommandText("I");
		expected.add(ct);
		
		process("E-22.0 F\tG3 G4H5,6.I  ");
	}
	
	@Test
	public void testLoneMessageCommandWithText() {
		CommandText ct = new CommandText("E");
		ct.getArgs().add("Jazzmaster MC");
		expected.add(ct);
		
		process("E|Jazzmaster MC ");
	}
	
	@Test
	public void testEmbeddedMessageCommandWithPrivateText() {
		CommandText ct = new CommandText("C");
		expected.add(ct);
		
		ct = new CommandText("E");
		ct.getArgs().add("7");
		ct.getArgs().add("HI, THERE. ");
		expected.add(ct);
		
		ct = new CommandText("D");
		ct.getArgs().add("2");
		expected.add(ct);
		
		process("CE7|HI, THERE. | D2");
	}
	
	@Test
	public void testStopCommand() {
		CommandText ct = new CommandText("STOP");
		expected.add(ct);
		
		process("STOP");
	}
	
	@Test
	public void testBadStartOfString() {
		CommandText ct = new CommandText(null);
		ct.getArgs().add("15%");
		expected.add(ct);
		
		process("15%");
	}
}
