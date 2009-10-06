package com.googlecode.ncombat.web;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

public class AppController extends MultiActionController
{
	public Map appTest(HttpServletRequest request, HttpServletResponse response) {
		Map model = new HashMap();
		model.put("msg1", "Hello, world!");
		model.put("msg2", "AppController says hello.");
		return model;
	}
}
