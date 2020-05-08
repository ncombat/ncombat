package org.ncombat.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class GameController
{
	private Logger log = LoggerFactory.getLogger(GameController.class);
	
	@GetMapping("/game.do")
	public ModelAndView game() {
		return new ModelAndView("ncombat");
	}
}
