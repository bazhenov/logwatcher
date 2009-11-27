package org.bazhenov.logging.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.View;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class SessionController {

	@RequestMapping(value = "/session")
	public View handleFeed(@RequestParam(value = "severity", required = false) String severity,
	                         HttpServletRequest request, HttpServletResponse response) {
		if ( severity != null ) {
			Cookie cookie = new Cookie("severity", severity);
			response.addCookie(cookie);
		}
		return new BufferView();
	}
}