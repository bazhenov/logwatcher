package com.farpost.logwatcher;

import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

public class CauseTest {

	@Test
	public void causeSHouldNotContainsMesssageOrType() {
		Cause cause = new Cause(new RuntimeException("foo"));
		assertThat(cause.getStackTrace(), not(containsString("foo")));
		assertThat(cause.getStackTrace(), not(containsString("RuntimeException")));
	}
}
