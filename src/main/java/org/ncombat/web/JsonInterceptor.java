package org.ncombat.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

public class JsonInterceptor extends HandlerInterceptorAdapter
{
	@Override
	public void postHandle( HttpServletRequest request,
							HttpServletResponse response, 
							Object handler,
							ModelAndView modelAndView) throws Exception
	{
		if (request.getRequestURI().endsWith(".json")) {
			modelAndView.setViewName("jsonView");
		}
	}
}
