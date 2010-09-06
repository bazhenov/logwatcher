package com.farpost.logging;

import org.testng.annotations.Test;

import java.net.URL;
import java.net.HttpURLConnection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class WebappIT {

	@Test
	public void testCallIndexPage() throws Exception {
		URL url = new URL("http://localhost:8080/");
		Thread.sleep(20000);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.connect();
		assertThat(connection.getResponseCode(), equalTo(200));
	}
}
