package org.bazhenov.logging.jsp.tags;

import javax.servlet.jsp.tagext.TagSupport;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import java.io.IOException;

public class GreetingsTag extends TagSupport {

	private String name;

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public int doEndTag() throws JspException {
		JspWriter out = pageContext.getOut();
		try {
			out.write("Hello, " + name);
		} catch ( IOException e ) {
			throw new JspException(e);
		}
		return EVAL_PAGE;
	}
}
