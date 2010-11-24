package com.farpost.logwatcher.web;

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.testng.annotations.Test;

import static com.farpost.htmlunit.Matchers.containsText;
import static org.hamcrest.MatcherAssert.assertThat;

public class DahsboardIT extends FunctionalTestsBase {

	@Test
	public void testCallIndexPage() throws Exception {
		HtmlPage page = goTo("/dashboard");
		assertThat(page, containsText("frontend"));
		HtmlPage frontendPage = page.getAnchorByText("frontend").click();
		assertThat(frontendPage, containsText("frontend"));
	}
}
