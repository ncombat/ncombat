package org.ncombat.web;

import org.ncombat.GameManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;

@RequestMapping("/admin.do")
@Controller
public class AdminController
{
	private final Logger logger = LoggerFactory.getLogger(AdminController.class);

	private final GameManager gameManager;

	public AdminController(GameManager gameManager) {
		this.gameManager = gameManager;
	}

	@GetMapping
	public ModelAndView admin() {
		HashMap<String, String> model = new HashMap<>();

		Instant startInstant = gameManager.getStartInstant();
		model.put("startTime", ZonedDateTime.ofInstant(startInstant, ZoneId.of("America/Chicago")).toString());
		model.put("upTime", Duration.between(startInstant, Instant.now()).toString());

		logger.debug("model={}", model);

		return new ModelAndView("admin", model);
	}
}
