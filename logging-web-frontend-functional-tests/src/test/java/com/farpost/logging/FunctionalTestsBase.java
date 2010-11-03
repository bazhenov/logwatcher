package com.farpost.logging;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;

import java.io.IOException;

import static java.lang.System.getProperty;

abstract class FunctionalTestsBase {

	protected String applicationUrl;
	protected WebClient browser = new WebClient(BrowserVersion.FIREFOX_3);

	public FunctionalTestsBase() throws IOException {
		applicationUrl = getProperty("it.location");
		if (applicationUrl == null || applicationUrl.equals("")) {
			throw new RuntimeException("it.location property should be given");
		}
		browser.setJavaScriptEnabled(false);
		browser.setCssEnabled(false);
		browser.setTimeout(5000);
	}
}
