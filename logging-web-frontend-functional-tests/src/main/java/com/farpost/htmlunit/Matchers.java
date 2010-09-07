package com.farpost.htmlunit;

import com.gargoylesoftware.htmlunit.SgmlPage;
import org.hamcrest.Matcher;

public class Matchers {

	public static Matcher<SgmlPage> containsText(String text) {
		return new ContainsTextMatcher(text);
	}
}
