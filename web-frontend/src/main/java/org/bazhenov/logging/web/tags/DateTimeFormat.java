package org.bazhenov.logging.web.tags;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;

public class DateTimeFormat extends DateFormat {

	private Calendar calendar = Calendar.getInstance();

	private final String[] monthNames = new String[]{"января", "февраля", "марта", "апреля", "мая",
		"июня", "июля", "августа", "сентября", "октября", "ноября", "декабря"};

	public synchronized StringBuffer format(Date date, StringBuffer toAppendTo,
	                                        FieldPosition fieldPosition) {
		fieldPosition.setBeginIndex(0);
		fieldPosition.setEndIndex(0);
		Calendar now = Calendar.getInstance();

		long millsSinceNow = now.getTimeInMillis() - date.getTime();
		if ( millsSinceNow < 60000 && millsSinceNow > 0 ) {
			formatLessThanMinuteDate(calendar, toAppendTo, fieldPosition);
			return toAppendTo;
		} else {
			Calendar today = now;
			today.set(Calendar.HOUR_OF_DAY, 0);
			today.set(Calendar.MINUTE, 0);
			today.set(Calendar.SECOND, 0);
			today.set(Calendar.MILLISECOND, 0);

			calendar.setTime(date);
			if ( calendar.before(today) ) {
				formatGenericDate(calendar, toAppendTo, fieldPosition);
			} else {
				formatTodaysDate(calendar, toAppendTo, fieldPosition);
			}
		}
		return toAppendTo;
	}

	private void formatGenericDate(Calendar calendar, StringBuffer toAppendTo, FieldPosition fp) {
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		String month = monthNames[calendar.get(Calendar.MONTH)];
		int hours = calendar.get(Calendar.HOUR_OF_DAY);
		int minutes = calendar.get(Calendar.MINUTE);

		fp.setBeginIndex(toAppendTo.length());
		toAppendTo.
			append(day).append(" ").append(month).
			append(", ").
			append(String.format("%02d", hours)).append(":").append(String.format("%02d", minutes));
		fp.setEndIndex(toAppendTo.length());
	}

	private void formatTodaysDate(Calendar calendar, StringBuffer toAppendTo, FieldPosition fp) {
		int hours = calendar.get(Calendar.HOUR_OF_DAY);
		int minutes = calendar.get(Calendar.MINUTE);

		toAppendTo.append("в ");
		fp.setBeginIndex(toAppendTo.length());
		toAppendTo.
			append(String.format("%02d", hours)).
			append(":").
			append(String.format("%02d", minutes));

		fp.setEndIndex(toAppendTo.length());
	}

	private void formatLessThanMinuteDate(Calendar calendar, StringBuffer toAppendTo,
	                                      FieldPosition fp) {
		fp.setBeginIndex(toAppendTo.length());
		toAppendTo.append("менее минуты назад");
		fp.setEndIndex(toAppendTo.length());
	}

	public Date parse(String source, ParsePosition pos) {
		throw new UnsupportedOperationException();
	}
}
