package org.ncombat.web;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.View;

@SuppressWarnings("unchecked")
public class JsonView implements View
{
	public static final String DEFAULT_CONTENT_TYPE = "application/json";
	
	public static final String ARRAY_KEY = "__jsonArray";
	
	public static final String CONTENT_TYPE_KEY = "__jsonContentType";
	
	public String getContentType() {
		return null;
	}

	public void render(Map model, HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		if (model == null) {
			model = new HashMap();
		}
		
		String contentType = DEFAULT_CONTENT_TYPE;
		
		if (model.containsKey(CONTENT_TYPE_KEY)) {
			contentType = model.get(CONTENT_TYPE_KEY).toString();
			model.remove(CONTENT_TYPE_KEY);
		}
		
		Object jsonObject = null;
		
		if (model.containsKey(ARRAY_KEY)) {
			jsonObject = model.get(ARRAY_KEY);
		}
		if (jsonObject == null) {
			jsonObject = model;
		}
		
		String json = new JsonWriter().toString(jsonObject);
		
		response.setContentType(contentType);
		response.setContentLength(json.length());
		
		response.getWriter().print(json);
	}
}
