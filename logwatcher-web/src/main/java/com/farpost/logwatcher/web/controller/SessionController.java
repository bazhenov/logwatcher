package com.farpost.logwatcher.web.controller;

import com.farpost.logwatcher.web.BufferView;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.View;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@Controller
public class SessionController {

	@RequestMapping(value = "/session")
	public View handleFeed(@RequestParam(value = "severity", required = false) String severity,
	                       @RequestParam(value = "sortOrder", required = false) String sortOrder,
	                       HttpServletResponse response) {
		if ( severity != null ) {
			Cookie cookie = new Cookie("severity", severity);
			response.addCookie(cookie);
		}
		if ( sortOrder != null ) {
			Cookie cookie = new Cookie("sortOrder", sortOrder);
			response.addCookie(cookie);
		}
		return new BufferView();
	}
}