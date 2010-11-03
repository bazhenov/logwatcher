package com.farpost.htmlunit;

import com.gargoylesoftware.htmlunit.SgmlPage;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.w3c.dom.Element;

public class ContainsElementWithTextMatcher extends BaseMatcher<SgmlPage> {

	private final String text;
	private final String xpath;

	public ContainsElementWithTextMatcher(String xpath, String text) {
		this.xpath = xpath;
		this.text = text;
	}

	@Override
	public boolean matches(Object page) {
		Element element = (Element) ((SgmlPage) page).getByXPath(xpath).get(0);
		return element.getTextContent().contains(text);
	}

	@Override
	public void describeTo(Description description) {
		description.appendText("element \"" + xpath +
			"\" with text: " + text);
	}
}
