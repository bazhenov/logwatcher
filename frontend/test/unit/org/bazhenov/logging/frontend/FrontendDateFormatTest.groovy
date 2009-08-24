package org.bazhenov.logging.frontend

import java.util.Date
import java.text.DateFormat
import org.bazhenov.logging.frontend.FrontendDateFormat
import java.text.FieldPosition

public class FrontendDateFormatTest extends GroovyTestCase {

	DateFormat formatter = new FrontendDateFormat();

	void testFormatterCanFormatGenericDates() {
		def date = new Date(99, 2, 1, 5, 01, 53)

		def fp = new FieldPosition(DateFormat.HOUR0_FIELD)
		def buffer = new StringBuffer()
		formatter.format(date, buffer, fp)
		assertEquals "1 марта, 05:01", buffer as String
		assertEquals 0, fp.beginIndex
		assertEquals 13, fp.endIndex
	}

	void testFormatterCanFormatDatesLessThanMinuteAgo() {
		def calendar = Calendar.instance
		calendar.add(Calendar.SECOND, -8)

		def fp = new FieldPosition(DateFormat.HOUR0_FIELD)
		def buffer = new StringBuffer()
		formatter.format(calendar.getTime(), buffer, fp)
		assertEquals "менее минуты назад", buffer as String
		assertEquals 0, fp.beginIndex
		assertEquals 17, fp.endIndex

	}

	void testFormatterCanProviderFieldPositionInformation() {
		def calendar = Calendar.instance
		calendar.set(Calendar.HOUR_OF_DAY, 15)
		calendar.set(Calendar.MINUTE, 32)

		def fp = new FieldPosition(DateFormat.HOUR0_FIELD)

		StringBuffer buffer = new StringBuffer()

		formatter.format(calendar.getTime(), buffer, fp)
		assertEquals "в 15:32", buffer as String
		assertEquals 2, fp.beginIndex
		assertEquals 6, fp.endIndex
	}
}