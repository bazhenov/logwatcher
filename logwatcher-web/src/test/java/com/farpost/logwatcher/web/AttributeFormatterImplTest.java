package com.farpost.logwatcher.web;

import org.testng.annotations.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class AttributeFormatterImplTest {

	private AttributeFormatter formatter = new AttributeFormatterImpl("app/name-><a href='*'>*</a>");

	@Test
	public void simpleFormatSubstitution() {
		assertThat(formatter.format("app", "name", "John"), is("<a href='John'>John</a>"));
	}

	@Test
	public void htmlShouldBeEscaped() {
		assertThat(formatter.format("app", "name", "<i>John</i>"),
			is("<a href='&lt;i&gt;John&lt;/i&gt;'>&lt;i&gt;John&lt;/i&gt;</a>"));
	}

	@Test
	public void shouldPreserveNewLines() {
		assertThat(formatter.format("app", "name", "a\nb"), is("<a href='a\nb'>a\nb</a>"));
	}
}