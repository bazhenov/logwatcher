package org.bazhenov.logging.frontend

import java.text.DateFormat
import com.farpost.timepoint.Date

public class RussianDateFormatTest extends GroovyTestCase {

	DateFormat format = new RussianDateFormat();

	void testDateFormatCanFormatDates() {
		Date date = Date.november(12, 2009)
		assertEquals "12 ноября 2009", format.format(date.asDate())
	}
}
