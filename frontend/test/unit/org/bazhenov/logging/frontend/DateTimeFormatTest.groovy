package org.bazhenov.logging.frontend

import java.text.DateFormat
import java.text.FieldPosition
import org.bazhenov.logging.frontend.DateTimeFormat
import static com.farpost.timepoint.Date.november
import static com.farpost.timepoint.Date.today

public class DateTimeFormatTest extends GroovyTestCase {

	DateFormat formatter = new DateTimeFormat();

	void testFormatterCanFormatGenericDates() {
		def date = new Date(99, 2, 1, 5, 01, 53)

		def fp = new FieldPosition(DateFormat.HOUR0_FIELD)
		def buffer = new StringBuffer()
		formatter.format(date, buffer, fp)
		assertEquals "1 марта, 05:01", buffer as String
		assertEquals 0, fp.beginIndex
		assertEquals 14, fp.endIndex
	}

	void testFormatterCanFormatDatesLessThanMinuteAgo() {
		def calendar = Calendar.instance
		calendar.add(Calendar.SECOND, -8)

		def fp = new FieldPosition(DateFormat.HOUR0_FIELD)
		def buffer = new StringBuffer()
		formatter.format(calendar.getTime(), buffer, fp)
		assertEquals "менее минуты назад", buffer as String
		assertEquals 0, fp.beginIndex
		assertEquals 18, fp.endIndex
	}

	void testFormatterCanProviderFieldPositionInformation() {
		def calendar = Calendar.instance
		calendar.set(Calendar.HOUR_OF_DAY, 15)
		calendar.set(Calendar.MINUTE, 32)

		def fp = new FieldPosition(DateFormat.HOUR0_FIELD)

		StringBuffer buffer = new StringBuffer()

		formatter.format(calendar.getTime(), buffer, fp)
		assertEquals "в 15:32", buffer as String
	}

	void testClientCanUseFieldPositionForAdditionalFormattingOfTodaysDates() {
		def timepoint = today().at(15, 32);

		def fp = new FieldPosition(DateFormat.HOUR0_FIELD)
		StringBuffer buffer = new StringBuffer()

		formatter.format(timepoint.asDate(), buffer, fp)

		wrapWithSpan fp, buffer

		assertEquals "в <span>15:32</span>", buffer as String
	}

	void testClientCanUseFieldPositionForAdditionalFormattingWithGeneralDates() {
		def timepoint = november(12, 2008).at(02, 03);

		def fp = new FieldPosition(DateFormat.HOUR0_FIELD)

		StringBuffer buffer = new StringBuffer()
		formatter.format(timepoint.asDate(), buffer, fp)
		wrapWithSpan fp, buffer
		assertEquals "<span>12 ноября, 02:03</span>", buffer as String

		buffer = new StringBuffer()
		buffer.append("Event occured at ")
		formatter.format(timepoint.asDate(), buffer, fp)
		wrapWithSpan fp, buffer
		assertEquals "Event occured at <span>12 ноября, 02:03</span>", buffer as String
	}

	private def wrapWithSpan(FieldPosition fp, StringBuffer buffer) {
		buffer.insert(fp.endIndex, "</span>");
		buffer.insert(fp.beginIndex, "<span>")
	}
}
