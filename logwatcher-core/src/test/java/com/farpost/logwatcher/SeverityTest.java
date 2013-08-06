package com.farpost.logwatcher;

import org.testng.annotations.Test;

import static com.farpost.logwatcher.Severity.*;
import static com.farpost.logwatcher.SeverityUtils.forName;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class SeverityTest {

	@Test
	public void seveityCanBeCastedToString() {
		assertThat(warning.toString(), equalTo("warning"));
	}

	@Test
	public void seveityCanBeParsedFromString() {
		assertThat(forName("warning").get(), equalTo(warning));
		assertThat(forName("Warning").get(), equalTo(warning));

		assertThat(forName("oops").isPresent(), is(false));
	}

	@Test
	public void seveityCanBeParsedFromInteger() {
		assertThat(forCode(1), equalTo(trace));
	}
}
