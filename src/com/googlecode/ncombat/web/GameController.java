package com.googlecode.ncombat.web;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

import com.googlecode.ncombat.command.CommandText;
import com.googlecode.ncombat.command.CommandTokenizer;

public class GameController extends MultiActionController
{
	private static final String NAME_PROMPT = "ENTER YOUR NAME?";
	private static final String COMMAND_PROPMPT = "CMDS?";
	
	private static final String LOGIN_URL = "gameLogin.json";
	private static final String COMMAND_URL = "gameCommands.json";
	
	private Logger log = Logger.getLogger(GameController.class);
	
	public ModelAndView game(HttpServletRequest request, HttpServletResponse response) {
		return new ModelAndView("ncombat");
	}
	
	public Map gameJoin(HttpServletRequest request, HttpServletResponse response)
	{
		GameStatusModel model = new GameStatusModel();
		
		Date now = new Date();
		SimpleDateFormat dateFmt = new SimpleDateFormat("EEE  MMM dd, yyyy hh:mm aa"); 
		String date = dateFmt.format(now).toUpperCase();
		
		model.addMessage(date + " SHIP-VS-SHIP TACTICAL SPACE COMBAT GAME IV");
		model.addMessage("STOP TO QUIT. H FOR HELP.");
		
		model.setPrompt(NAME_PROMPT);
		model.setUrl(LOGIN_URL);
		
		return model;
	}
	
	public Map gameLogin(HttpServletRequest request, HttpServletResponse response)
	{
		GameStatusModel model = new GameStatusModel();
		
		String text = request.getParameter("text");
		if ((text != null)) text = text.trim().toUpperCase();
		
		if ((text == null) || (text.length() == 0)) {
			model.addMessage("");
			model.addMessage("PLEASE ENTER YOUR NAME, DUMBASS.");
			model.setPrompt(NAME_PROMPT);
			model.setUrl(LOGIN_URL);
			return model;
		}
		
		model.addMessage("");
		model.addMessage( String.format("WELCOME, %s.", text));
		model.addMessage("");
		model.addMessage("SHP   COMMANDERS NAME   TER USERNUM KL");
		model.addMessage( String.format(" 1    %-17s 123 H7LT444  0", text));
		
		model.setPrompt(COMMAND_PROPMPT);
		model.setUrl(COMMAND_URL);
		
		return model;
	}
	
	public Map gameCommands(HttpServletRequest request, HttpServletResponse response)
	{
		GameStatusModel model = new GameStatusModel();
		
		String text = request.getParameter("text");
		if ((text != null)) text = text.trim().toUpperCase();
		
		if ((text != null) || (text.length() > 0)) {
			CommandTokenizer ctok = new CommandTokenizer();
			model.addMessage("CMDS? " + text);
			List<CommandText> commands = ctok.parse(text);
			int cmdNum = 1;
			for (CommandText cmd : ctok.parse(text)) {
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
		
		model.setPrompt(COMMAND_PROPMPT);
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
}
