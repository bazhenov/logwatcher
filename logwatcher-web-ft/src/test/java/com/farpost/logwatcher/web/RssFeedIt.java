package com.farpost.logwatcher.web;

import com.gargoylesoftware.htmlunit.SgmlPage;
import org.testng.annotations.Test;

import java.io.IOException;

import static com.farpost.htmlunit.Matchers.xpathContainsText;
import static org.hamcrest.MatcherAssert.assertThat;

public class RssFeedIt extends FunctionalTestsBase {

	@Test
	public void FeedShouldContainsTitle() throws IOException {
		SgmlPage page = goTo("/feed/rss");
		assertThat(page, xpathContainsText("//title", "LogViewer feed"));
	}
}
