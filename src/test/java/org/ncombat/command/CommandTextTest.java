package org.ncombat.command;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class CommandTextTest
{
	@Test
	public void testConstructor()
	{
		CommandText ct = new CommandText("A");
		Assert.assertNotNull(ct);
		
		Assert.assertEquals("A", ct.getAction());
		
		List<String> args = ct.getArgs();
		Assert.assertNotNull(args);
		Assert.assertEquals(0, args.size());
	}
	
	@Test
	public void testToStringZeroArgs() {
		CommandText ct = new CommandText("W");
		Assert.assertEquals("W", ct.toString());
	}
	
	@Test
	public void testToStringOneArg() {
		CommandText ct = new CommandText("X");
		ct.getArgs().add("22");
		Assert.assertEquals("X22", ct.toString());
	}
	
	@Test
	public void testToStringTwoArgs() {
		CommandText ct = new CommandText("Y");
		ct.getArgs().add("3.14");
		ct.getArgs().add("-6");
		Assert.assertEquals("Y3.14,-6", ct.toString());
	}
	
	@Test
	public void testToStringtThreeArgs() {
		CommandText ct = new CommandText("Z");
		ct.getArgs().add("+10.0");
		ct.getArgs().add("-6*(15%)");
		ct.getArgs().add("999");
		Assert.assertEquals("Z+10.0,-6*(15%),999", ct.toString());
	}
}
