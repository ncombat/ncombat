package org.ncombat.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class AppRestController
{
	@GetMapping("/appTest.json")
	public Map appTest() {
		Map model = new HashMap();
		model.put("msg1", "Hello, world!");
		model.put("msg2", "AppController says hello.");
		return model;
	}
}
