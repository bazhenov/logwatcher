package org.bazhenov.logging.frontend

import java.text.DateFormat
import java.text.FieldPosition
import java.text.ParsePosition

public class RussianDateFormat extends DateFormat {

	private final monthNames = [
		"января", "февраля", "марта", "апреля", "мая", "июня", "июля", "августа", "сентября",
		"октября", "ноября", "декабря"
	]

	public StringBuffer format(Date date, StringBuffer buffer, FieldPosition fp) {
		calendar = Calendar.instance
		calendar.time = date
		def day = calendar.get(Calendar.DAY_OF_MONTH)
		def month = monthNames[calendar.get(Calendar.MONTH)]

		fp.beginIndex = buffer.length()
		buffer.append(day).append(" ").append(month)
		fp.endIndex = buffer.length()
		return buffer
	}

	public Date parse(String s, ParsePosition parsePosition) {
		throw new UnsupportedOperationException()
	}
}
