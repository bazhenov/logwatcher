package com.farpost.logging;

import com.gargoylesoftware.htmlunit.SgmlPage;
import org.testng.annotations.Test;

import java.io.IOException;

import static com.farpost.htmlunit.Matchers.containsElementWithText;
import static org.hamcrest.MatcherAssert.assertThat;

public class RssFeed extends FunctionalTestsBase {

	public RssFeed() throws IOException {
	}

	@Test
	public void FeedShouldContainsTitle() throws IOException {
		SgmlPage page = browser.getPage(applicationUrl + "/feed/rss");
		assertThat(page, containsElementWithText("//title", "LogViewer feed"));
	}

}
