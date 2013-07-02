package com.farpost.logwatcher.statistics;

import org.joda.time.DateTime;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class MinuteVectorTest {

	@Test
	public void shouldBeAbleToIncrement() {
		MinuteVector v = new MinuteVector();
		v.increment(new DateTime().minusDays(1));
		assertThat(v.get(0), is(0L));

		v.increment(new DateTime());
		assertThat(v.get(0), is(1L));
	}

	@Test
	public void serializeDesirialize() {
		MinuteVector v = new MinuteVector();
		v.increment(new DateTime().minusSeconds(13));
		v.increment(new DateTime().minusSeconds(12));
		v.increment(new DateTime().minusSeconds(8));

		byte[] raw = v.toByteArray();
		MinuteVector copyOfV = new MinuteVector(raw);
		assertThat(copyOfV, equalTo(v));
	}
}
