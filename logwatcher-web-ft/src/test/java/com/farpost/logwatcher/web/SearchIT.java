package com.farpost.logwatcher.web;

import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.testng.annotations.Test;

import java.io.IOException;

import static com.farpost.htmlunit.Matchers.containsText;
import static org.hamcrest.MatcherAssert.assertThat;

public class SearchIT extends FunctionalTestsBase {

	@Test
	public void searchShouldDisplayResults() throws IOException {
		HtmlPage page = goTo("/search");
		HtmlForm form = page.getFormByName("searchForm");
		form.getInputByName("q").setValueAttribute("at: frontend");
		HtmlPage resultPage = form.getInputByName("submit").click();

		assertThat(resultPage, containsText("very 'very' very"));
	}
}
