package com.farpost.logging;

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.testng.annotations.Test;

import java.io.IOException;

import static com.farpost.htmlunit.Matchers.containsText;
import static org.hamcrest.MatcherAssert.assertThat;

public class WebAppIt extends FunctionalTestsBase {

	public WebAppIt() throws IOException {
	}

	@Test
	public void testCallIndexPage() throws Exception {
		HtmlPage page = browser.getPage(applicationUrl + "/dashboard");
		assertThat(page, containsText("frontend"));
		HtmlPage frontendPage = page.getAnchorByText("frontend").click();
		assertThat(frontendPage, containsText("frontend"));
	}
}
