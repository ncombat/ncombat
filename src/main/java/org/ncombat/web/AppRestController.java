package org.ncombat.web;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.HashMap;
import java.util.Map;

@Controller
public class AppRestController
{
	@GetMapping(path = "/appTest.json", produces = MediaType.APPLICATION_JSON_VALUE)
	public Map appTest() {
		Map model = new HashMap();
		model.put("msg1", "Hello, world!");
		model.put("msg2", "AppController says hello.");
		return model;
	}
}
