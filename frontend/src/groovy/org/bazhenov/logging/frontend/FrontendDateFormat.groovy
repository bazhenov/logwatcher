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
		def now = Calendar.instance;

		def millsSinceNow = now.timeInMillis - date.time
		if ( millsSinceNow < 60000 && millsSinceNow > 0 ) {
			formatLessThanMinuteDate(calendar, toAppendTo)
			return toAppendTo
		}else{
			def today = now
			today.set(Calendar.HOUR_OF_DAY, 0)
			today.set(Calendar.MINUTE, 0)
			today.set(Calendar.SECOND, 0)
			today.set(Calendar.MILLISECOND, 0)

			calendar.setTime(date);
			if ( calendar.before(today) ) {
				formatGenericDate(calendar, toAppendTo)
			}else{
				formatTodaysDate(calendar, toAppendTo)
			}
		}
		toAppendTo
	}

	private def formatGenericDate(Calendar calendar, StringBuffer toAppendTo) {
		def day = calendar.get(Calendar.DAY_OF_MONTH)
		def month = monthNames[calendar.get(Calendar.MONTH)]
		def hours = calendar.get(Calendar.HOUR_OF_DAY) as String
		def minutes = calendar.get(Calendar.MINUTE) as String

		toAppendTo.
			append(day).append(" ").append(month).
			append(", ").
			append(hours.padLeft(2, "0")).append(":").append(minutes.padLeft(2, "0"))
	}

	private def formatTodaysDate(Calendar calendar, StringBuffer toAppendTo) {
		def hours = calendar.get(Calendar.HOUR_OF_DAY) as String
		def minutes = calendar.get(Calendar.MINUTE) as String

		toAppendTo.
			append(hours.padLeft(2, "0")).append(":").append(minutes.padLeft(2, "0"))
	}

	private def formatLessThanMinuteDate(Calendar calendar, StringBuffer toAppendTo) {
		toAppendTo.append("менее минуты назад")
	}

	public Date parse(String source, ParsePosition pos) {
		throw new UnsupportedOperationException();
	}
}