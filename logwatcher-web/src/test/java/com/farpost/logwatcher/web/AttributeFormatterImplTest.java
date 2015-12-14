package com.farpost.logwatcher.web;

import com.google.common.collect.ImmutableMap;
import org.testng.annotations.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class AttributeFormatterImplTest {

	private AttributeFormatter formatter = new AttributeFormatterImpl(
			"app/name-><a href='*'>*</a>|anotherApp/anotherName-><a href='http://$[host]*'>*</a>");

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

	@Test
	public void shouldParseDollarPatterns() {
		assertThat(formatter.format("anotherApp", "anotherName", "/some/url?qwerty=uiop"), is("/some/url?qwerty=uiop"));
		assertThat(formatter.format("anotherApp", "anotherName", ImmutableMap.of("anotherName", "a\nb", "irrelevant", "qwerty")),
				is("a\nb"));
		assertThat(formatter.format("anotherApp", "anotherName", ImmutableMap.of("anotherName", "/some/url?qwerty=uiop", "host", "farpost.ru")),
				is("<a href='http://farpost.ru/some/url?qwerty=uiop'>/some/url?qwerty=uiop</a>"));
	}
}