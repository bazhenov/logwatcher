package com.farpost.logging;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.testng.annotations.Test;

import java.io.IOException;

import static com.farpost.htmlunit.Matchers.containsText;
import static java.lang.System.getProperty;
import static org.hamcrest.MatcherAssert.assertThat;

public class WebAppIT {

	private String applicationUrl;
	private WebClient browser = new WebClient(BrowserVersion.FIREFOX_3);

	public WebAppIT() throws IOException {
		applicationUrl = getProperty("it.location");
		if (applicationUrl == null || applicationUrl.equals("")) {
			throw new RuntimeException("it.location property should be given");
		}
		browser.setJavaScriptEnabled(false);
		browser.setCssEnabled(false);
		browser.setTimeout(5000);
	}

	@Test
	public void testCallIndexPage() throws Exception {
		HtmlPage page = browser.getPage(applicationUrl + "/dashboard");
		assertThat(page, containsText("frontend"));
		HtmlPage frontendPage = page.getAnchorByText("frontend").click();
		assertThat(frontendPage, containsText("frontend"));
	}
}
