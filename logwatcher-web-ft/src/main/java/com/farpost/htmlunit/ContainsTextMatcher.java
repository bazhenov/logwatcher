package com.farpost.htmlunit;

import com.gargoylesoftware.htmlunit.SgmlPage;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

public class ContainsTextMatcher extends BaseMatcher<SgmlPage> {

	private final String text;

	public ContainsTextMatcher(String text) {
		this.text = text;
		if (text == null) {
			throw new NullPointerException();
		}
	}

	@Override
	public boolean matches(Object page) {
		return ((SgmlPage)page).asText().contains(text);
	}

	@Override
	public void describeTo(Description description) {
		description.appendText("should contains text: " + text);
	}
}
