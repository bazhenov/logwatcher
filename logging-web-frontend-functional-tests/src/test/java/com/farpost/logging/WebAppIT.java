package com.farpost.logging;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.testng.annotations.Test;

import static com.farpost.htmlunit.Matchers.containsText;
import static java.lang.Integer.parseInt;
import static java.lang.System.getProperty;
import static org.hamcrest.MatcherAssert.assertThat;

public class WebAppIT {

	private String applicationUrl;
	private WebClient browser = new WebClient();

	public WebAppIT() {
		int port = parseInt(getProperty("it.port"));
		applicationUrl = "http://localhost:" + port + "/";
	}

	@Test
	public void testCallIndexPage() throws Exception {
		HtmlPage page = browser.getPage(applicationUrl);
		assertThat(page, containsText("frontend"));
		HtmlPage frontendPage = page.getAnchorByText("frontend").click();
		assertThat(frontendPage, containsText("frontend"));
	}
}
