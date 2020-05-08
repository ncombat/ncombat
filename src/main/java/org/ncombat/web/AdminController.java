package org.ncombat.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class AdminController
{
	@GetMapping("/admin.do")
	public ModelAndView admin() {
		return new ModelAndView("admin");
	}
}
