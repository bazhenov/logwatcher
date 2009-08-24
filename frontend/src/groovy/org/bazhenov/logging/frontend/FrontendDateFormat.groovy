package org.bazhenov.logging.frontend

import java.text.DateFormat
import java.text.FieldPosition
import java.text.ParsePosition
import static java.lang.Math.max

public class FrontendDateFormat extends DateFormat {

	private Calendar calendar = Calendar.instance

	private final monthNames = [
		"января", "февраля", "марта", "апреля", "мая", "июня", "июля", "августа", "сентября",
		"октября", "ноября", "декабря"
	]

	public synchronized StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition) {
		fieldPosition.beginIndex = fieldPosition.endIndex = 0
		def now = Calendar.instance;

		def millsSinceNow = now.timeInMillis - date.time
		if ( millsSinceNow < 60000 && millsSinceNow > 0 ) {
			formatLessThanMinuteDate(calendar, toAppendTo, fieldPosition)
			return toAppendTo
		}else{
			def today = now
			today.set(Calendar.HOUR_OF_DAY, 0)
			today.set(Calendar.MINUTE, 0)
			today.set(Calendar.SECOND, 0)
			today.set(Calendar.MILLISECOND, 0)

			calendar.setTime(date);
			if ( calendar.before(today) ) {
				formatGenericDate(calendar, toAppendTo, fieldPosition)
			}else{
				formatTodaysDate(calendar, toAppendTo, fieldPosition)
			}
		}
		toAppendTo
	}

	private def formatGenericDate(Calendar calendar, StringBuffer toAppendTo, FieldPosition fp) {
		def day = calendar.get(Calendar.DAY_OF_MONTH)
		def month = monthNames[calendar.get(Calendar.MONTH)]
		def hours = calendar.get(Calendar.HOUR_OF_DAY) as String
		def minutes = calendar.get(Calendar.MINUTE) as String

		fp.beginIndex = max(0, toAppendTo.length() - 1)
		toAppendTo.
			append(day).append(" ").append(month).
			append(", ").
			append(hours.padLeft(2, "0")).append(":").append(minutes.padLeft(2, "0"))
		fp.endIndex = toAppendTo.length() - 1
	}

	private def formatTodaysDate(Calendar calendar, StringBuffer toAppendTo, FieldPosition fp) {
		def hours = calendar.get(Calendar.HOUR_OF_DAY) as String
		def minutes = calendar.get(Calendar.MINUTE) as String

		def length = toAppendTo.length()
		fp.beginIndex = length + 2
		toAppendTo.
			append("в ").
			append(hours.padLeft(2, "0")).append(":").append(minutes.padLeft(2, "0"))

		fp.endIndex = toAppendTo.length() - 1
	}

	private def formatLessThanMinuteDate(Calendar calendar, StringBuffer toAppendTo, FieldPosition fp) {
		fp.beginIndex = max(0, toAppendTo.length() - 1)
		toAppendTo.append("менее минуты назад")
		fp.endIndex = toAppendTo.length() - 1
	}

	public Date parse(String source, ParsePosition pos) {
		throw new UnsupportedOperationException();
	}
}