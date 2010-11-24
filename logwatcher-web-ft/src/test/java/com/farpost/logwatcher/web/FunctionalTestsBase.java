package com.farpost.logwatcher.web;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;

import java.io.IOException;

import static java.lang.System.getProperty;

abstract class FunctionalTestsBase {

	private final String applicationUrl;
	private final WebClient browser = new WebClient(BrowserVersion.FIREFOX_3);

	public FunctionalTestsBase() {
		applicationUrl = getProperty("it.location");
		browser.setJavaScriptEnabled(false);
		browser.setCssEnabled(false);
		browser.setTimeout(5000);
	}

	protected <P extends Page> P goTo(String url) throws IOException {
		if (applicationUrl == null || applicationUrl.equals("")) {
			throw new IllegalArgumentException("it.location property should be given");
		}
		if (url == null || url.isEmpty()) {
			throw new IllegalArgumentException("Not empty url should be given");
		}
		return (P)browser.getPage(applicationUrl + url);
	}
}
