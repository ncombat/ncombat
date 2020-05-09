package org.ncombat.command;

import java.util.ArrayList;

import org.junit.Assert;

import org.junit.Before;
import org.junit.Test;
import org.ncombat.combatants.Combatant;
import org.ncombat.combatants.PlayerShip;

public class CommandParserTest
{
	private Combatant combatant = new PlayerShip("TEST");
	private CommandParser parser = new CommandParser(combatant);
	private CommandBatch expectedBatch;
	private Command expectedCommand;
	private ArrayList<Command> expectedCommands;
	
	@Before
	public void setUp() {
		combatant.clearMessages();
		expectedBatch = new CommandBatch(0, combatant);
		expectedCommand = null;
		expectedCommands = new ArrayList<Command>();
	}
	
	private void check(String line) {
		check(line, false);
	}
	
	private void checkBad(String line) {
		check(line, true);
	}

	private void check(String line, boolean expectingMessages)
	{
		if ((expectedCommands.size() == 0) && (expectedCommand != null)) {
			expectedCommands.add(expectedCommand);
		}
		
		CommandBatch actualBatch = parser.parse(line, 0);
		
		if (expectedBatch.getCommands().size() > 0) {
			Assert.assertEquals(expectedBatch, actualBatch);
		}
		else {
			Assert.assertEquals(expectedCommands, actualBatch.getCommands());
		}
		
		boolean gotMessages = ( combatant.numMessages() > 0 );
		
		if (gotMessages && (!expectingMessages)) {
			Assert.fail("Got unexpected message(s): " + combatant.drainMessages());
		}
		else if ((!gotMessages) && expectingMessages) {
			Assert.fail("Didn't receive expected message(s).");
		}
	}
	
	@Test
	public void testAccelerateCommand() {
		expectedCommand = new AccelerateCommand(1.0, 2.0);
		check("A1,2");
	}
	
	@Test
	public void testBriefCmd() {
		expectedCommand = new BriefModeCommand();
		check("B");
	}
	
	@Test
	public void testMessageCommand() {
		expectedCommands.add( new MessageCommand());
		expectedCommands.add( new MessageCommand("AB C"));
		expectedCommands.add( new MessageCommand(7, "DEF"));
		check("E E|AB C| E|/7 DEF");
	}
	
	@Test
	public void testFullShieldsCommand() {
		expectedCommands.add( new ShieldCommand(1, 25.0));
		expectedCommands.add( new ShieldCommand(2, 25.0));
		check("F");
	}
	
	@Test
	public void testSensorCommand() {
		expectedCommand = new SensorCommand(9999.0);
		check("G9999");
	}
	
	@Test
	public void testHelpCommand() {
		expectedCommand = new HelpCommand();
		check("H");
	}
	
	@Test
	public void testIntelCommand() {
		expectedCommands.add( new IntelCommand(1, 2));
		expectedCommands.add( new IntelCommand(2));
		expectedCommands.add( new IntelCommand(3));
		expectedCommands.add( new IntelCommand(3, 4));
		expectedCommands.add( new IntelCommand(4));
		expectedCommands.add( new IntelCommand(5));
		expectedCommands.add( new IntelCommand(6));
		check("I1,2 I2 I3 I3,4 I4 I5 I6");
	}
	
	@Test
	public void testLaserCommand() {
		expectedCommand = new LaserCommand(2000.0);
		check("L2000");
	}
	
	@Test
	public void testMissileCommand() {
		expectedCommands.add( new MissileCommand(1));
		expectedCommands.add( new MissileCommand(2));
		expectedCommands.add( new MissileCommand(3));
		check("M1 M2,3");
	}
	
	@Test
	public void testNullCommand() {
		expectedCommand = new NullCommand();
		check("N");
	}
	
	@Test
	public void testRepairCommand1() {
		expectedCommands.add( new RepairCommand(0.0, 0.0));
		expectedCommands.add( new RepairCommand(0.5, 0.0));
		expectedCommands.add( new RepairCommand(0.5, 0.6));
		check("P P0.5 P0.5,0.6");
	}
	
	@Test
	public void testRotateCommand() {
		expectedCommands.add( new RotateCommand(60.0, 6.0));
		expectedCommands.add( new RotateCommand(-45.0, 5.0));
		check("R60 R-45,5.0");
	}
	
	@Test
	public void testShieldCommand() {
		expectedCommands.add( new ShieldCommand(1, 0.0));
		expectedCommands.add( new ShieldCommand(2, 22.5));
		check("S1 S2,22.5");
	}
	
	@Test
	public void testStopCommand() {
		expectedCommand = new StopCommand();
		check("STOP");
	}
	
	@Test
	public void testTrackCommand() {
		expectedCommands.add( new TrackCommand());
		expectedCommands.add( new TrackCommand(1));
		check("V V1");
	}
	
	@Test
	public void testZeroShieldsCommand() {
		expectedCommands.add( new ShieldCommand(1, 0.0));
		expectedCommands.add( new ShieldCommand(2, 0.0));
		check("Z");
	}
	
	public void testNullLine() {
		check(null);
	}
	
	@Test
	public void testEmptyLine() {
		check("");
	}
	
	@Test
	public void testWhiteLine() {
		check("   ");
	}
	
	@Test
	public void testTooFewArgs() {
		checkBad("A");
		checkBad("A1");
		checkBad("I1");
		checkBad("G");
		checkBad("I");
		checkBad("I1");
		checkBad("L");
		checkBad("M");
		checkBad("R");
		checkBad("S");
	}
	
	@Test
	public void testTooManyArgs() {
		checkBad("A1,2,3");
		checkBad("B1");
		checkBad("E1,2");
		checkBad("F1");
		checkBad("G1,2");
		checkBad("H1");
		checkBad("I1,2,3");
		checkBad("I2,3");
		checkBad("I3,4,5");
		checkBad("I4,5");
		checkBad("I5,6");
		checkBad("I6,7");
		checkBad("L2000,2");
		checkBad("M1,2,3");
		checkBad("N1");
		checkBad("P1,2,3");
		checkBad("R1,2,3");
		checkBad("S1,2,3");
		checkBad("V1,2");
		checkBad("Z1");
	}
	
	@Test
	public void testBadIntFormat()
	{
		checkBad("I1.0,2");
		checkBad("I1,2.0");
		checkBad("I2.0");
		checkBad("I3.0");
		checkBad("I3,4.0");
		checkBad("I4.0");
		checkBad("I5.0");
		checkBad("I6.0");
		checkBad("M1.0");
		checkBad("M1,2.0");
		checkBad("S1.0,2");
		checkBad("V1.0");
	}
	
	@Test
	public void testBadDoubleFormat()
	{
		checkBad("A1%,2.0");
		checkBad("A1.0,2%");
		checkBad("G1%");
		checkBad("L2000%");
		checkBad("P1%");
		checkBad("P1%,0.5");
		checkBad("P1.0,2%");
		checkBad("R1.0%");
		checkBad("R1.0,2.0%");
		checkBad("S1,2%");
	}
	
	@Test
	public void testRangeViolations()
	{
		checkBad("A-5.01,1");
		checkBad("A5.01,1");
		checkBad("A1,-0.01");
		checkBad("A1,300.01");
		checkBad("G0");
		checkBad("I0");
		checkBad("I1,-1");
		checkBad("I1,100");
		checkBad("I3,-1");
		checkBad("I3,100");
		checkBad("I7");
		checkBad("L-1");
		checkBad("L2001");
		checkBad("M-1");
		checkBad("M100");
		checkBad("M1,-1");
		checkBad("M1,100");
		checkBad("P-0.01");
		checkBad("P1.51");
		checkBad("P0,-0.01");
		checkBad("P0,1.51");
		checkBad("R-360.01");
		checkBad("R360.01");
		checkBad("R1,-0.01");
		checkBad("R1,6.01");
		checkBad("S0");
		checkBad("S3");
		checkBad("S1,-0.01");
		checkBad("S1,25.01");
		checkBad("V100");
	}
	
	@Test
	public void testReadoutRegen1()
	{
		expectedBatch.addCommand( new BriefModeCommand());
		expectedBatch.addCommand( new MessageCommand());
		expectedBatch.addCommand( new SensorCommand(30000));
		expectedBatch.addCommand( new IntelCommand(2));
		expectedBatch.setRegenStatusReadout(false);
		
		CommandBatch actual = parser.parse("B E G30000 I2", 0);
		Assert.assertEquals(expectedBatch, actual);
		
		expectedBatch.addCommand( new NullCommand());
		expectedBatch.setRegenStatusReadout(true);
		
		actual = parser.parse("B E G30000 I2 N", 0);
		Assert.assertEquals(expectedBatch, actual);
	}
	
	@Test
	public void testReadoutRegen2()
	{
		expectedBatch.addCommand( new AccelerateCommand(1,1));
		expectedBatch.addCommand( new LaserCommand(2000));
		expectedBatch.addCommand( new MissileCommand(1));
		expectedBatch.addCommand( new MissileCommand(2));
		expectedBatch.addCommand( new RotateCommand(125.0, 6.0));
		expectedBatch.setRegenStatusReadout(true);
		
		CommandBatch actual = parser.parse("A1,1L2000M1,2R125", 0);
		Assert.assertEquals(expectedBatch, actual);
	}
}
