package com.farpost.logwatcher.web;

import org.testng.annotations.Test;

import static com.farpost.logwatcher.web.Functions.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class FunctionsTest {

	@Test
	public static void testFormatExceptionType() {
		assertThat(Functions.getSimpleType("java.lang.RuntimeException"), is("RuntimeException"));
	}

	@Test
	public void extractExceptionName() {
		CauseDef def = extractExceptionClass("AdvertServiceException: Error Fetching http headers");
		assertThat(def.getSimpleType(), is("AdvertServiceException"));
		assertThat(def.getType(), is("AdvertServiceException"));
		assertThat(def.getMessage(), is("Error Fetching http headers"));

		def = extractExceptionClass("FarPost\\Search\\Client\\TimeoutException: Count error");
		assertThat(def.getSimpleType(), is("TimeoutException"));
		assertThat(def.getType(), is("FarPost\\Search\\Client\\TimeoutException"));
		assertThat(def.getMessage(), is("Count error"));

		String message = "search-admin-idxnode2.vfarm.loc:8080 failed to respond. Reason: I/O error: connect timed out; nested exception is java.net.SocketTimeoutException: connect timed out";
		def = extractExceptionClass(message);
		assertThat(def.getSimpleType(), is("SocketTimeoutException"));
		assertThat(def.getType(), is("java.net.SocketTimeoutException"));
		assertThat(def.getMessage(), is(message));

		def = extractExceptionClass("HTTP_Request2_MessageException: Can't search similar images");
		assertThat(def.getSimpleType(), is("HTTP_Request2_MessageException"));
		assertThat(def.getType(), is("HTTP_Request2_MessageException"));
		assertThat(def.getMessage(), is("Can't search similar images"));
	}

	@Test
	public void testFormatIntensity() {
		assertThat(formatIntensity(0.1d), is("6/minute"));

		assertThat(formatIntensity(2d), is("2/second"));
		assertThat(formatIntensity(2.5d), is("3/second"));

		assertThat(formatIntensity(11.9d), is("12/second"));
		assertThat(formatIntensity(128d), is("130/second"));
		assertThat(formatIntensity(1523d), is("1500/second"));
		assertThat(formatIntensity(15234d), is("15000/second"));
	}

	@Test
	public void testShortNumberFormat() {
		assertThat(shortNumberFormat(123), is("123"));
		assertThat(shortNumberFormat(1230), is("1K"));
		assertThat(shortNumberFormat(12300), is("12K"));
		assertThat(shortNumberFormat(12500), is("12K"));
		assertThat(shortNumberFormat(125000), is("125K"));
		assertThat(shortNumberFormat(1250000), is("1M"));
		assertThat(shortNumberFormat(12500000), is("12M"));
		assertThat(shortNumberFormat(125000000), is("125M"));
		assertThat(shortNumberFormat(1250000000), is("1250M"));
	}
}
