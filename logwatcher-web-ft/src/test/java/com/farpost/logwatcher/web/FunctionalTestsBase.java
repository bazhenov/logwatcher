package com.farpost.logwatcher.web;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;

import java.io.IOException;

import static java.lang.System.getProperty;

abstract class FunctionalTestsBase {

	protected final String applicationUrl;
	protected final WebClient browser = new WebClient(BrowserVersion.FIREFOX_3);

	public FunctionalTestsBase() {
		applicationUrl = getProperty("it.location");
		if (applicationUrl == null || applicationUrl.equals("")) {
			throw new RuntimeException("it.location property should be given");
		}
		browser.setJavaScriptEnabled(false);
		browser.setCssEnabled(false);
		browser.setTimeout(5000);
	}

	protected <P extends Page> P goTo(String url) throws IOException {
		return browser.getPage(applicationUrl + url);
	}
}
