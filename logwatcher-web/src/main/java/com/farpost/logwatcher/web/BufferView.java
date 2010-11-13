package com.farpost.logwatcher.web;

import org.springframework.web.servlet.View;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public class BufferView implements View {

	private final String contentType;
	private final String content;

	public BufferView(String content, String contentType) {
		this.contentType = contentType;
		this.content = content;
	}

	public BufferView() {
		this("", "text/html");
	}

	public BufferView(String content) {
		this(content, "text/html");
	}

	public String getContentType() {
		return contentType;
	}

	public void render(Map model, HttpServletRequest request, HttpServletResponse response)
		throws Exception {
		response.getWriter().write(content);
	}
}
