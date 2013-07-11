package com.farpost.logwatcher.web.controller;

import com.farpost.logwatcher.statistics.MinuteVector;
import org.joda.time.DateTime;
import org.testng.annotations.Test;

import static com.farpost.logwatcher.web.controller.BackController.calculateRelativeWindow;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class BackControllerTest {

	@Test
	public void testCalculateRelativeWindow() {
		MinuteVector v;
		v = new MinuteVector();
		v.increment(DateTime.now());
		assertThat(calculateRelativeWindow(v, 5), is(-4));

		v = new MinuteVector();
		v.increment(DateTime.now().minusMinutes(3));
		assertThat(calculateRelativeWindow(v, 6), is(-8));

		v = new MinuteVector();
		v.increment(DateTime.now().minusMinutes(1435));
		assertThat(calculateRelativeWindow(v, 10), is(-1439));
	}
}
