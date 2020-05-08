package org.ncombat.web;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

@Controller
public class AdminController
{
	@GetMapping("/admin.do")
	public ModelAndView admin() {
		return new ModelAndView("admin");
	}

	@GetMapping(path = "/adminTest.json", produces = MediaType.APPLICATION_JSON_VALUE)
	public Map adminTest() {
		Map model = new HashMap();
		model.put("msg1", "Hello, world!");
		model.put("msg2", "AdminController says hello.");
		return model;
	}
}
