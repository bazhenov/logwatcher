package org.bazhenov.logging.web.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;

public class ResponseHeadersFilter implements Filter {

	private FilterConfig filterConfig;

	public void init(FilterConfig filterConfig) throws ServletException {
		this.filterConfig = filterConfig;
	}

	public void doFilter(ServletRequest request, ServletResponse response,
	                     FilterChain chain) throws IOException, ServletException {
		chain.doFilter(request, response);
		if ( response instanceof HttpServletResponse ) {
			HttpServletResponse httpResponse = (HttpServletResponse) response;
			Enumeration names = filterConfig.getInitParameterNames();
			while ( names.hasMoreElements() ) {
				String headerName = (String)names.nextElement();
				String headerValue = filterConfig.getInitParameter(headerName);
				httpResponse.addHeader(headerName, headerValue);
			}
		}
	}

	public void destroy() {
		filterConfig = null;
	}
}
