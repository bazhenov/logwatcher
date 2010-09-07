package com.farpost.logging;

import org.testng.annotations.Test;

import java.net.HttpURLConnection;
import java.net.URL;

import static java.lang.Integer.parseInt;
import static java.lang.System.getProperty;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class WebAppIT {

	private String applicationUrl;

	public WebAppIT() {
		int port = parseInt(getProperty("it.port"));
		applicationUrl = "http://localhost:" + port + "/";
	}

	@Test
	public void testCallIndexPage() throws Exception {
		URL url = new URL(applicationUrl);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.connect();
		assertThat(connection.getResponseCode(), equalTo(200));
	}
}
