package org.bazhenov.logging.frontend

import java.util.Date
import java.text.DateFormat
import org.bazhenov.logging.frontend.FrontendDateFormat

public class FrontendDateFormatTest extends GroovyTestCase {

	DateFormat formatter = new FrontendDateFormat();

	void testFormatterCanFormatGenericDates() {
		def date = new Date(99, 2, 1, 5, 01, 53);

		assertEquals("1 марта, 05:01", formatter.format(date));
	}

	void testFormatterCanFormatTodayDates() {
		def calendar = Calendar.instance
		calendar.set(Calendar.HOUR_OF_DAY, 15)
		calendar.set(Calendar.MINUTE, 32);

		assertEquals("15:32", formatter.format(calendar.getTime()));
	}

	void testFormatterCanFormatDatesLessThanMinuteAgo() {
		def calendar = Calendar.instance
		calendar.add(Calendar.SECOND, -8);

		assertEquals("менее минуты назад", formatter.format(calendar.getTime()));
	}
}