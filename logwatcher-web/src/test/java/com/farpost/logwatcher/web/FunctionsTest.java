package com.farpost.logwatcher.web;

import org.testng.annotations.Test;

import static com.farpost.logwatcher.web.Functions.extractExceptionClass;
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
	}
}
