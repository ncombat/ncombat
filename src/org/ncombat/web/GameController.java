package org.ncombat.web;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.ncombat.GameManager;
import org.ncombat.GameServer;
import org.ncombat.combatants.Combatant;
import org.ncombat.combatants.Ship;
import org.ncombat.command.Command;
import org.ncombat.command.CommandBatch;
import org.ncombat.command.CommandParser;
import org.ncombat.command.MessageCommand;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

public class GameController extends MultiActionController
{
	public static final String LOGIN_URL = "gameLogin.json";
	public static final String COMMAND_URL = "gameCommands.json";
	public static final String MESSAGE_URL = "gameMessage.json";
	
	private Logger log = Logger.getLogger(GameController.class);
	
	private GameManager gameManager;
	
	public GameController() {
	}
	
	@Required
	public void setGameManager(GameManager gameManager) {
		this.gameManager = gameManager;
	}

	public ModelAndView game(HttpServletRequest request, HttpServletResponse response) {
		return new ModelAndView("ncombat");
	}
	
	public Map gameJoin(HttpServletRequest request, HttpServletResponse response)
	{
		GameStatusModel model = new GameStatusModel();
		
		/*
		 * This is meant to be the first Ajax request made by the player. We do
		 * not expect to find an HTTP session, in which case, we will create
		 * one. If we do find one unexpectedly, we will tolerate it, though if
		 * we also find a valid combatant ID, we will assume the user hit the
		 * refresh button in the middle of a game and put him back into the
		 * command cycle.
		 */
		HttpSession session = request.getSession(false);
		if (session == null) {
			session = request.getSession(true);
		}
		else {
			if (getCombatant(session) != null) {
				return model.promptForCommands(); 
			}
		}
		
		Date now = new Date();
		SimpleDateFormat dateFmt = new SimpleDateFormat("EEE  MMM dd, yyyy hh:mm aa"); 
		String date = dateFmt.format(now).toUpperCase();
		
		model.addMessage("            \nATDT9,6124102397                         " +
				"                                      \n" +
				"                                      \n" +
				"                                      \n" +
				"CONNECT 300\n\n\n");
		model.addMessage(date + " SHIP-VS-SHIP TACTICAL SPACE COMBAT GAME V");
		model.addMessage("STOP TO QUIT. H FOR HELP.");
		
		return model.promptForName();
	}
	
	public Map gameLogin(HttpServletRequest request, HttpServletResponse response)
	{
		GameStatusModel model = new GameStatusModel();
		
		/*
		 * Since we should have already received and processed the game-joining
		 * Ajax call if we are here, we expect to find a session already in
		 * place. If we do not find one, it is probably because the user's
		 * session timed out while the browser sat at the login screen. If so,
		 * we forgive the lack of a session and continue on. If the session is
		 * missing for any other reason, however, the cause will likely be
		 * either that the user's browser is rejecting our cookies, an internal
		 * error has occurred, or the user is hacking us. In all of these cases,
		 * we complain and shut the user down.
		 */
		HttpSession session = request.getSession(false);
		if (session == null) {
			if (request.getRequestedSessionId() == null) {
				return internalError(request, "Missing session at" + LOGIN_URL);
			}
			else {
				session = request.getSession(true);
			}
		}

		String playerName = request.getParameter("text");
		if ((playerName != null)) playerName = playerName.trim().toUpperCase();
		
		if ((playerName == null) || (playerName.length() == 0)) {
			model.addMessage("");
			model.addMessage("Please enter your name, dumbass.");
			return model.promptForName();
		}
		
		Ship ship = gameManager.createPlayerShip(playerName);
		setCombatant(session, ship);
		
		// Pass on the user's messages to the UI.
		for (String message : ship.drainMessages()) {
			model.addMessage(message);
		}
		
		return model.promptForCommands();
	}
	
	public Map gameCommands(HttpServletRequest request, HttpServletResponse response)
	{
		GameStatusModel model = new GameStatusModel();
		
		/*
		 * As we are in the middle of the game by the time we reach this point,
		 * it is definitely required that we have a session. Timed out sessions
		 * will receive an explanation, but we must shut down in any case if no
		 * session is present. Likewise if our combatant ID cannot be found in
		 * the session.
		 */
		HttpSession session = request.getSession(false);
		Combatant combatant = null;
		if (session == null) {
			if (request.getRequestedSessionId() != null) {
				model.addMessage("*** YOUR SESSION HAS TIMED OUT. ***");
				model.setAlive(false);
				return model;
			}
			else {
				return internalError(request, "Missing session in " + COMMAND_URL);
			}
		}
		else {
			combatant = getCombatant(session);
			if (combatant == null) {
				return internalError(request, "Missing combatant ID in " + COMMAND_URL);
			}
		}
		
		String cmdLine = request.getParameter("text");
		
		if (cmdLine == null) cmdLine = "";
		cmdLine = cmdLine.trim().toUpperCase();
		
		// Echo the command line back to the user for his records.
		model.addMessage("CMDS? " + cmdLine);
		
		// If the user gave us a blank line, we'll give him a null command.
		if (cmdLine.equals("")) cmdLine = "N";
			
		// Parse the player's text into a properly structured
		// and verified command batch.
		long now = System.currentTimeMillis();
		CommandParser parser = new CommandParser(combatant);
		CommandBatch batch = parser.parse(cmdLine, now);
		
		// If the user submitted message commands with no messages,
		// remember how many.  We need to prompt the user for the
		// messages to be sent.
		int numMessages = 0;
		for (Command cmd : batch.getCommands()) {
			if (cmd instanceof MessageCommand) {
				if (((MessageCommand) cmd).getMessage() == null) {
					numMessages++;
				}
			}
		}
		
		// Submit the commands for processing.  If a game server cannot
		// be found, we are no longer alive and cannot submit any more 
		// commands.
		
		GameServer gameServer = combatant.getGameServer();
		if (gameServer != null) {
			gameServer.addCommandBatch(batch);
		
			// If we need to get messages from the user, there's no
			// need to make the user wait for the next game cycle
			// update.
			if (numMessages > 0) {
				setNumMessages(session, numMessages);
				return model.promptForMessage();
			}
			
			// Otherwise, wait for the next game cycle to
			// complete.
			gameServer.playerSync();
		}
		
		// Pass on the user's messages to the UI.
		for (String message : combatant.drainMessages()) {
			model.addMessage(message);
		}
		
		if (combatant.isAlive()) {
			model.promptForCommands();
		}
		else {
			session.invalidate();
			model.markForDeath();
		}
		
		return model;
	}
	
	public Map gameMessage(HttpServletRequest request, HttpServletResponse response)
	{
		GameStatusModel model = new GameStatusModel();
		
		/*
		 * As we are in the middle of the game by the time we reach this point,
		 * it is definitely required that we have a session. Timed out sessions
		 * will receive an explanation, but we must shut down in any case if no
		 * session is present. Likewise if our combatant ID cannot be found in
		 * the session or the number of remaining messages to be prompted for
		 * is missing.
		 */
		HttpSession session = request.getSession(false);
		Combatant combatant = null;
		Integer numMessages = null;
		if (session == null) {
			if (request.getRequestedSessionId() != null) {
				model.addMessage("*** YOUR SESSION HAS TIMED OUT. ***");
				model.setAlive(false);
				return model;
			}
			else {
				return internalError(request, "Missing session in " + MESSAGE_URL);
			}
		}
		else {
			combatant = getCombatant(session);
			if (combatant == null) {
				return internalError(request, "Missing combatant ID in " + MESSAGE_URL);
			}
			numMessages = getNumMessages(session);
			if (numMessages == null) {
				return internalError(request, "Missing # of messages in " + MESSAGE_URL);
			}
		}
		
		String msgText = request.getParameter("text");
		
		if ((msgText != null)) msgText = msgText.trim().toUpperCase().replace('|', ':');
		
		if ((msgText != null) && (msgText.length() > 0)) {
			// Echo the message back to the user for his records.
			model.addMessage("MESSAGE: ");
			model.addMessage("? " + msgText);
			
			// Build a proper message command out of the users input and
			// stick it into a nicely formed command batch.
			String cmdText = "E|" + msgText + "|";
			long now = System.currentTimeMillis();
			CommandParser parser = new CommandParser(combatant);
			CommandBatch batch = parser.parse(cmdText, now);
			
			// Submit the message command for processing.
			GameServer gameServer = combatant.getGameServer();
			gameServer.addCommandBatch(batch);
			
			// If we need to get more messages from the user, there's no
			// need to make the user wait for the next game cycle
			// update.
			setNumMessages(session, --numMessages);
			if (numMessages > 0) {
				return model.promptForMessage();
			}
			
			// Otherwise, wait for the next game cycle to
			// complete.
			gameServer.playerSync();
			
			// Pass on the user's messages to the UI.
			for (String message : combatant.drainMessages()) {
				model.addMessage(message);
			}
			
			if (combatant.isAlive()) {
				model.promptForCommands();
			}
			else {
				model.markForDeath();
			}
		}
		
		return model;
	}
	
	public Map gamePing(HttpServletRequest request, HttpServletResponse response)
	{
		GameStatusModel model = new GameStatusModel();
		model.setSuccess(false);
		
		HttpSession session = request.getSession(false);
		if (session == null) return model;
		
		Combatant combatant = getCombatant(session);
		if (combatant == null) return model;
		if (!combatant.isAlive()) {
			model.markForDeath();
			return model;
		}
		
		long now = System.currentTimeMillis();
		combatant.setLastContactTime(now);
		
		model.setSuccess(true);
		return model;
	}
	
	private Combatant getCombatant(HttpSession session)
	{
		if (session == null) return null;
		Integer combatantId = (Integer) session.getAttribute("COMBATANT_ID");
		if (combatantId == null) return null;
		return gameManager.getCombatant(combatantId);
	}
	
	private void setCombatant(HttpSession session, Combatant combatant) {
		session.setAttribute("COMBATANT_ID", combatant.getId());
	}
	
	private Integer getNumMessages(HttpSession session)
	{
		if (session == null) return null;
		Integer numMessages = (Integer) session.getAttribute("NUM_MESSAGES");
		return numMessages;
	}
	
	private void setNumMessages(HttpSession session, int numMessages)
	{
		if (numMessages <= 0) {
			session.removeAttribute("NUM_MESSAGES");
			return;
		}
		session.setAttribute("NUM_MESSAGES", numMessages);
	}
	
	private GameStatusModel internalError(HttpServletRequest request, String msg)
	{
		HttpSession session = request.getSession(false);
		String sessionId = (session == null ? null : session.getId());
		
		msg = "IP=" + request.getRemoteAddr()
					+ ", requestedSession=" + request.getRequestedSessionId()
					+ ", session=" + sessionId
					+ ": INTERNAL ERROR: " + msg;
		
		log.warn(msg);
		
		GameStatusModel model = new GameStatusModel();
		model.addMessage("*** INTERNAL ERROR: SESSION IS ABORTED ***");
		model.setAlive(false);
		
		return model;
	}
}
