package org.ncombat.command;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ncombat.combatants.Combatant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unchecked")
public class CommandParser
{
	public static final double MAX_SHIELD_POWER = 25.0;
	
	public static final double MAX_ROTATION_RATE = 6.0;
	public static final double DEFAULT_ROTATION_RATE = MAX_ROTATION_RATE;
	
	private static final Pattern PRIVATE_MSG_PATTERN = Pattern.compile("/(\\d{1,2}) (.+)");
	
	private static final Set NO_STATUS_REGEN_ACTIONS 
								= new HashSet<String>(	Arrays.asList( new String[] { "B", "E", "G", "I" }));
	
	public static final double MIN_DISCONNECT_RANGE = 30000.0;
	
	// Maps action (usually one letter, like 'A' for accelerate) to parse method.
	private static Map<String,Method> parseMethods;
	
	// Maps command class (like org.ncombat.command.AccelerateCommand) to validation method.
	private static Map<Class,Method> validateMethods;
	
	private Logger log = LoggerFactory.getLogger(CommandParser.class);
	
	private Combatant combatant;
	
	public CommandParser(Combatant combatant) {
		this.combatant = combatant;
		loadMethodCache();
	}
	
	protected synchronized void loadMethodCache() {
		if (parseMethods == null) { 
			parseMethods = new HashMap<String,Method>();
			
			loadParseMethod("A", "parseAccelerateAction");
			loadParseMethod("B", "parseBriefModeAction");
			loadParseMethod("E", "parseMessageAction");
			loadParseMethod("F", "parseFullShieldsAction");
			loadParseMethod("G", "parseSensorAction");
			loadParseMethod("H", "parseHelpAction");
			loadParseMethod("I", "parseIntelAction");
			loadParseMethod("L", "parseLaserAction");
			loadParseMethod("M", "parseMissileAction");
			loadParseMethod("N", "parseNullAction");
			loadParseMethod("P", "parseRepairAction");
			loadParseMethod("R", "parseRotateAction");
			loadParseMethod("S", "parseShieldAction");
			loadParseMethod("STOP", "parseStopAction");
			loadParseMethod("V", "parseTrackAction");
			loadParseMethod("Z", "parseZeroShieldsAction");
		}
		
		if (validateMethods == null) {
			validateMethods = new HashMap<Class,Method>();
			
			loadValidateMethod(AccelerateCommand.class);
			loadValidateMethod(IntelCommand.class);
			loadValidateMethod(LaserCommand.class);
			loadValidateMethod(MessageCommand.class);
			loadValidateMethod(MissileCommand.class);
			loadValidateMethod(RepairCommand.class);
			loadValidateMethod(RotateCommand.class);
			loadValidateMethod(SensorCommand.class);
			loadValidateMethod(ShieldCommand.class);
			loadValidateMethod(TrackCommand.class);
		}
	}
	
	protected void loadParseMethod(String action, String name) {
		try {
			Method method = getClass().getMethod(name, CommandText.class);
			parseMethods.put(action, method);
		}
		catch (Exception e) {
			throw new CommandException("Error locating parse method " + name + ".", e);
		}
	}
	
	protected void loadValidateMethod(Class commandClass) {
		String name = null;
		try {
			name = "validate" + commandClass.getSimpleName();
			Method method = getClass().getMethod(name, commandClass);
			validateMethods.put(commandClass, method);
		}
		catch (Exception e) {
			throw new CommandException("Error locating validate method " + name + ".", e);
		}
	}
	
	public CommandBatch parse(String line, long timestamp)
	{
		CommandBatch batch = new CommandBatch(timestamp, combatant);
		
		CommandTokenizer ctok = new CommandTokenizer();
		List<CommandText> commandTextList = ctok.parse(line);
		
		commandTextLoop:
		for (CommandText cmdText : commandTextList) {
			String action = cmdText.getAction();
			
			if (action == null) continue;
			action = action.trim().toUpperCase();
			if (action.length() == 0) continue;
			
			Method parseMethod = parseMethods.get(action);
			
			if (parseMethod == null) {
				combatant.addMessage("'" + action + "' is not a valid command.");
				continue;
			}
			
			List<Command> commandList = null;
			
			try {
				commandList = (List<Command>) parseMethod.invoke(this, cmdText);
			}
			catch (InvocationTargetException e) {
				// Parse failed.
				combatant.addMessage( e.getCause().getMessage());
				continue;
			}
			catch (Exception e) {
				log.error("Caught exception invoking command parse method.", e);
				combatant.addMessage(cmdText.toString() + ": INTERNAL ERROR - Unable to process.");
				continue;
			}
			
			for (Command cmd : commandList) {
				Method validateMethod = validateMethods.get( cmd.getClass());
				if (validateMethod != null) {
					try {
						validateMethod.invoke(this, cmd);
					}
					catch (InvocationTargetException e) {
						// Validation failed.
						combatant.addMessage( e.getCause().getMessage());
						continue commandTextLoop;
					}
					catch (Exception e) {
						log.error("Caught exception invoking command validation method.", e);
						combatant.addMessage(cmdText.toString() + ": INTERNAL ERROR - Unable to process.");
						continue commandTextLoop;
					}
				}
			}
			
			if (!NO_STATUS_REGEN_ACTIONS.contains(action)) {
				batch.setRegenStatusReadout(true);
			}
			
			batch.addCommands(commandList);
		}
		
		return batch;
	}
	
	public List<Command> parseAccelerateAction(CommandText cmdText)
	{
		checkNumArgs(cmdText, 2,2);
		double rate = doubleArg(cmdText, 0, "acceleration rate");
		double time = doubleArg(cmdText, 1, "acceleration time");
		Command cmd = new AccelerateCommand(rate, time);
		return quickList(cmd);
	}
	
	public List<Command> parseBriefModeAction(CommandText cmdText)
	{
		checkNumArgs(cmdText, 0,0);
		Command cmd = new BriefModeCommand();
		return quickList(cmd);
	}
	
	public List<Command> parseFullShieldsAction(CommandText cmdText)
	{
		checkNumArgs(cmdText, 0, 0);
		Command cmd1 = new ShieldCommand(1, MAX_SHIELD_POWER);
		Command cmd2 = new ShieldCommand(2, MAX_SHIELD_POWER);
		return quickList(cmd1, cmd2);
	}
	
	public List<Command> parseHelpAction(CommandText cmdText)
	{
		checkNumArgs(cmdText, 0,0);
		Command cmd = new HelpCommand();
		return quickList(cmd);
	}
	
	public List<Command> parseIntelAction(CommandText cmdText)
	{
		checkNumArgs(cmdText, 1, 2);
		int subCmd = intArg(cmdText, 0, "intelligence subcommand");
		
		Command cmd = null;
		
		switch (subCmd) {
		
		case 1: // Parallel course
			checkNumArgs(cmdText, 2, 2);
			int shipNum = intArg(cmdText, 1, "ship number");
			cmd = new IntelCommand(subCmd, shipNum);
			break;
			
		case 3: // Current player info
			checkNumArgs(cmdText, 1, 2);
			Integer shipNumObj = intArg(cmdText, 1, "ship number");
			if (shipNumObj == null) {
				cmd = new IntelCommand(subCmd);
			}
			else {
				cmd = new IntelCommand(subCmd, shipNumObj.intValue());
			}
			break;
			
		default:
			checkNumArgs(cmdText, 1, 1);
			cmd = new IntelCommand(subCmd);
		}
		
		return quickList(cmd);
	}
	
	public List<Command> parseLaserAction(CommandText cmdText)
	{
		checkNumArgs(cmdText, 1, 1);
		double power = doubleArg(cmdText, 0, "laser power");
		Command cmd = new LaserCommand(power);
		return quickList(cmd);
	}
	
	public List<Command> parseMessageAction(CommandText cmdText)
	{
		checkNumArgs(cmdText, 0, 1);
		
		Command cmd = null;
		
		if ( cmdText.numArgs() == 0) {
			cmd = new MessageCommand();
		}
		else {
			String text = cmdText.getArg(0);
			
			Matcher matcher = PRIVATE_MSG_PATTERN.matcher(text);
			if (matcher.matches()) {
				// This is a private message.
				int dest = Integer.parseInt( matcher.group(1));
				String msg = matcher.group(2);
				cmd = new MessageCommand(dest, msg);
			}
			else {
				// This is a public message.
				cmd = new MessageCommand(text);
			}
		}

		return quickList(cmd);
	}
	
	public List<Command> parseMissileAction(CommandText cmdText)
	{
		checkNumArgs(cmdText, 1, 2);
		List<Command> commands = new ArrayList<Command>(2);
		int target = intArg(cmdText, 0, "ship number");
		commands.add( new MissileCommand(target));
		if (cmdText.numArgs() == 2) {
			target = intArg(cmdText, 1, "ship number");
			commands.add( new MissileCommand(target));
		}
		return commands;
	}
	
	public List<Command> parseNullAction(CommandText cmdText)
	{
		checkNumArgs(cmdText, 0,0);
		Command cmd = new NullCommand();
		return quickList(cmd);
	}
	
	public List<Command> parseRepairAction(CommandText cmdText)
	{
		checkNumArgs(cmdText, 0, 2);
		
		double repair1 = 0.0;
		if (cmdText.numArgs() > 0) {
			repair1 = doubleArg(cmdText, 0, "repair percentage on shield 1");
		}
		
		double repair2 = 0.0;
		if (cmdText.numArgs() > 1) {
			repair2 = doubleArg(cmdText, 1, "repair percentage on shield 2");
		}
		
		Command cmd = new RepairCommand(repair1, repair2);
		return quickList(cmd);
	}
	
	public List<Command> parseRotateAction(CommandText cmdText)
	{
		checkNumArgs(cmdText, 1, 2);
		double angle = doubleArg(cmdText, 0, "rotation angle");
		double rate = DEFAULT_ROTATION_RATE;
		if (cmdText.numArgs() > 1) {
			rate = doubleArg(cmdText, 1, "rotation rate");
		}
		Command cmd = new RotateCommand(angle, rate);
		return quickList(cmd);
	}
	
	public List<Command> parseShieldAction(CommandText cmdText)
	{
		checkNumArgs(cmdText, 1, 2);
		int shieldNum = intArg(cmdText, 0, "shield number");
		double power = 0.0;
		if (cmdText.numArgs() > 1) {
			power = doubleArg(cmdText, 1, "shield power level");
		}
		Command cmd = new ShieldCommand(shieldNum, power);
		return quickList(cmd);
	}
	
	public List<Command> parseStopAction(CommandText cmdText)
	{
		checkNumArgs(cmdText, 0, 0);
		Command cmd = new StopCommand();
		return quickList(cmd);
	}
	
	public List<Command> parseSensorAction(CommandText cmdText) {
		checkNumArgs(cmdText, 1,1);
		double range = doubleArg(cmdText, 0, "sensor range");
		Command cmd = new SensorCommand(range);
		return quickList(cmd);
	}
	
	public List<Command> parseTrackAction(CommandText cmdText) {
		checkNumArgs(cmdText, 0, 1);
		int ship = 0;
		if (cmdText.numArgs() > 0) {
			ship = intArg(cmdText, 0, "ship number");
		}
		Command cmd = new TrackCommand(ship);
		return quickList(cmd);
	}
	
	public List<Command> parseZeroShieldsAction(CommandText cmdText)
	{
		checkNumArgs(cmdText, 0, 0);
		Command cmd1 = new ShieldCommand(1, 0.0);
		Command cmd2 = new ShieldCommand(2, 0.0);
		return quickList(cmd1, cmd2);
	}
	
	public void validateAccelerateCommand(AccelerateCommand cmd)
	{
		checkDoubleRange( cmd.getRate(), -5.0, true, 5.0, true, "acceleration rate");
		checkDoubleRange( cmd.getTime(), 0.0, false, 300.0, true, "acceleration time");
	}
	
	public void validateIntelCommand(IntelCommand cmd)
	{
		int subcmd = cmd.getSubcommand();
		checkIntRange(subcmd, 1, true, 6, true, "intelligence subcommand");
		int shipNum = cmd.getShip();
		if ((subcmd == 1) || ((subcmd == 3) && (shipNum != 0))) {
			checkShipNum(shipNum);
		}
	}
	
	public void validateLaserCommand(LaserCommand cmd) {
		checkDoubleRange( cmd.getPower(), 0.0, false, 2000.0, true, "laser power level");
	}
	
	public void validateMessageCommand(MessageCommand cmd) {
		int shipNum = cmd.getDestination();
		if (shipNum != 0) checkShipNum(shipNum);
	}
	
	public void validateMissileCommand(MissileCommand cmd) {
		int shipNum = cmd.getTarget();
		if (shipNum != 0) checkShipNum(shipNum);
	}
	
	public void validateRepairCommand(RepairCommand cmd)
	{
		double s1rate = cmd.getShieldRepair1Pct();
		checkDoubleRange(s1rate, 0.0, true, 1.5, true, "shield 1 repair rate");
		
		double s2rate = cmd.getShieldRepair2Pct();
		checkDoubleRange(s2rate, 0.0, true, 1.5, true, "shield 2 repair rate");
		
		if ((s1rate + s2rate) > 1.5) {
			throw new CommandValidationException("Repair rates exceed maximum.");
		}
	}
	
	public void validateRotateCommand(RotateCommand cmd) {
		checkDoubleRange( cmd.getAngle(), -360.0, true, 360.0, true, "rotation angle");
		checkDoubleRange( cmd.getRate(), 0.0, false, 6.0, true, "rotation rate");
	}
	
	public void validateSensorCommand(SensorCommand cmd) {
		double range = cmd.getRange();
		if (range <= 0.0) {
			throw new CommandValidationException(range + " is not a valid sensor range.");
		}
	}
	
	public void validateShieldCommand(ShieldCommand cmd) {
		checkIntRange( cmd.getShieldNum(), 1, true, 2, true, "shield number");
		checkDoubleRange( cmd.getPower(), 0.0, true, MAX_SHIELD_POWER, true, "shield power level");
	}
	
	public void validateStopCommand (StopCommand cmd) {
		if (combatant.nearestRange() < MIN_DISCONNECT_RANGE );
	}
	
	
	public void validateTrackCommand(TrackCommand cmd) {
		if (cmd.getTarget() != 0) checkShipNum( cmd.getTarget());
	}
	
	protected void checkNumArgs(CommandText cmdText, int minNumArgs, int maxNumArgs)
	{
		int numArgs = cmdText.numArgs();
		if ((numArgs < minNumArgs) || (numArgs > maxNumArgs)) {
			throw new CommandParseException(cmdText + ": wrong # of arguments.");
		}
	}
	
	protected Double doubleArg(CommandText cmdText, int argNum, String description)
	{
		Double result = null;
		
		int numArgs = cmdText.numArgs();
		String text = (argNum < numArgs ? cmdText.getArg(argNum) : null);
		if (text != null) {
			try {
				result = new Double(text);
			}
			catch (NumberFormatException e) {
				throw new CommandParseException(text + ": not a valid " + description + ".");
			}
		}
		
		return result;
	}
	
	protected Integer intArg(CommandText cmdText, int argNum, String description)
	{
		Integer result = null;
		
		int numArgs = cmdText.numArgs();
		String text = (argNum < numArgs ? cmdText.getArg(argNum) : null);
		if (text != null) {
			try {
				result = new Integer(text);
			}
			catch (NumberFormatException e) {
				throw new CommandParseException(text + ": not a valid " + description + ".");
			}
		}
		
		return result;
	}
	
	protected void checkDoubleRange(double value,
									double min, boolean minIncluded,
									double max, boolean maxIncluded,
									String description)
	{
		boolean tooSmall = false;
		boolean tooBig = false;
		if (value < min) tooSmall = true;
		else if ((value == min) && (!minIncluded)) tooSmall = true;
		else if (value > max) tooBig = true;
		else if ((value == max) && (!maxIncluded)) tooBig = true;
		
		if (tooSmall || tooBig) {
			throw new CommandValidationException(value + " is not a valid " + description + ".");
		}
	}
	
	protected void checkIntRange(int value,
								 int min, boolean minIncluded,
								 int max, boolean maxIncluded,
								 String description)
	{
		boolean tooSmall = false;
		boolean tooBig = false;
		if (value < min) tooSmall = true;
		else if ((value == min) && (!minIncluded)) tooSmall = true;
		else if (value > max) tooBig = true;
		else if ((value == max) && (!maxIncluded)) tooBig = true;

		if (tooSmall || tooBig) {
			throw new CommandValidationException(value + " is not a valid " + description + ".");
		}
	}
	
	protected void checkShipNum(int shipNum) {
		if ((shipNum >= 1) && (shipNum <= 15)) return;
		if ((shipNum >= 21) && (shipNum <= 24)) return;
		throw new CommandValidationException(shipNum + " is not a valid ship number.");
	}
	
	protected List<Command> quickList(Command ... cmds) {
		return Arrays.asList(cmds);
	}
}
