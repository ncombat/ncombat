package com.googlecode.ncombat.web;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

import com.googlecode.ncombat.Combatant;
import com.googlecode.ncombat.GameManager;
import com.googlecode.ncombat.command.CommandText;
import com.googlecode.ncombat.command.CommandTokenizer;
import com.googlecode.ncombat.components.Ship;

public class GameController extends MultiActionController
{
	private static final String NAME_PROMPT = "ENTER YOUR NAME?";
	private static final String COMMAND_PROMPT = "CMDS?";
	
	private static final String LOGIN_URL = "gameLogin.json";
	private static final String COMMAND_URL = "gameCommands.json";
	
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
				model.setPrompt(COMMAND_PROMPT);
				model.setUrl(COMMAND_URL);
				return model;
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
		
		model.setPrompt(NAME_PROMPT);
		model.setUrl(LOGIN_URL);
		
		return model;
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
			model.addMessage("PLEASE ENTER YOUR NAME, DUMBASS.");
			model.setPrompt(NAME_PROMPT);
			model.setUrl(LOGIN_URL);
			return model;
		}
		
		Ship ship = new Ship();
		setCombatant(session, ship);
		
		model.addMessage("");
		model.addMessage( String.format("WELCOME, %s.", playerName));
		model.addMessage("");
		model.addMessage("SHP   COMMANDERS NAME   TER USERNUM KL");
		model.addMessage( String.format(" 1    %-17s 123 H7LT444  0", playerName));
		
		model.setPrompt(COMMAND_PROMPT);
		model.setUrl(COMMAND_URL);
		
		return model;
	}
	
	public Map gameCommands(HttpServletRequest request, HttpServletResponse response)
	{
		GameStatusModel model = new GameStatusModel();
		
		/*
		 * As we are in the middle of the game by the time we reach this point,
		 * it is definitely required that we have a session. Timed out sessions
		 * will receive an explanation, but we must shut down in any case if no
		 * session is present. Likewise it our combatant ID cannot be found in
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
		if ((cmdLine != null)) cmdLine = cmdLine.trim().toUpperCase();
		
		if ((cmdLine != null) || (cmdLine.length() > 0)) {
			CommandTokenizer ctok = new CommandTokenizer();
			model.addMessage("CMDS? " + cmdLine);
			List<CommandText> commands = ctok.parse(cmdLine);
			int cmdNum = 1;
			for (CommandText cmd : ctok.parse(cmdLine)) {
				String msg = "  CMD#" + cmdNum + ": ACTION=" + cmd.getAction();
				int argNum = 1;
				for (String arg : cmd.getArgs()) {
					msg += String.format(" ARG #%d=%s", argNum, arg);
					argNum++;
				}
				cmdNum++;
				model.addMessage(msg);
			}
		}
		
		model.setPrompt(COMMAND_PROMPT);
		model.setUrl(COMMAND_URL);
		
		return model;
	}
	
	public Map gameTest(HttpServletRequest request, HttpServletResponse response)
	{
		GameStatusModel model = new GameStatusModel();
		model.addMessage("Hello, world!");
		model.addMessage("GameController says hello.");
		return model;
	}
	
	private Combatant getCombatant(HttpSession session)
	{
		if (session == null) return null;
		Integer combatantId = (Integer) session.getAttribute("COMBATANT_ID");
		if (combatantId == null) return null;
		return gameManager.getCombatant(combatantId);
	}
	
	private void setCombatant(HttpSession session, Combatant combatant)
	{
		gameManager.addCombatant(combatant);
		session.setAttribute("COMBATANT_ID", combatant.getId());
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
