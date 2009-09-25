package org.bazhenov.logging.frontend

import java.text.DateFormat
import java.text.FieldPosition
import java.text.ParsePosition

public class FrontendDateFormat extends DateFormat {

	private Calendar calendar = Calendar.instance

	private final monthNames = [
		"января", "февраля", "марта", "апреля", "мая", "июня", "июля", "августа", "сентября",
		"октября", "ноября", "декабря"
	]

	public synchronized StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition) {
		fieldPosition.beginIndex = fieldPosition.endIndex = 0
		calendar.time = date;
		formatGenericDate(calendar, toAppendTo, fieldPosition)
		toAppendTo
	}

	private def formatGenericDate(Calendar calendar, StringBuffer toAppendTo, FieldPosition fp) {
		def day = calendar.get(Calendar.DAY_OF_MONTH)
		def month = monthNames[calendar.get(Calendar.MONTH)]

		fp.beginIndex = toAppendTo.length()
		toAppendTo.
			append(day).append(" ").append(month)
		fp.endIndex = toAppendTo.length()
	}

	public Date parse(String source, ParsePosition pos) {
		throw new UnsupportedOperationException();
	}
}