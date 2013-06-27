package com.farpost.logwatcher.web;

import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class FunctionsTest {

	@Test
	public static void testFormatExceptionType() {
		assertThat(Functions.formatClassName("java.lang.RuntimeException"), is("RuntimeException"));
	}
}
