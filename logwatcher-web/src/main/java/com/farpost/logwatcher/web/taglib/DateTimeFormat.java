package com.farpost.logwatcher.web.taglib;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;

public class DateTimeFormat extends DateFormat {

	private Calendar calendar = Calendar.getInstance();

	private final String[] monthNames = new String[]{"january", "february", "march", "april", "may",
		"june", "july", "august", "september", "october", "november", "december"};

	public synchronized StringBuffer format(Date date, StringBuffer toAppendTo,
	                                        FieldPosition fieldPosition) {
		fieldPosition.setBeginIndex(0);
		fieldPosition.setEndIndex(0);
		Calendar now = Calendar.getInstance();

		long millsSinceNow = now.getTimeInMillis() - date.getTime();
		if ( millsSinceNow < 60000 ) {
			staticFormat(toAppendTo, fieldPosition, "less than a minute ago");
			return toAppendTo;
		} else if ( millsSinceNow < 60000*5 ) {
			staticFormat(toAppendTo, fieldPosition, "less than 5 minutes ago");
		} else if ( millsSinceNow < 60000*10 ) {
			staticFormat(toAppendTo, fieldPosition, "less than 10 minutes ago");
		} else if ( millsSinceNow < 60000*15 ) {
			staticFormat(toAppendTo, fieldPosition, "less than 15 minutes ago");
		} else if ( millsSinceNow < 60000*30 ) {
			staticFormat(toAppendTo, fieldPosition, "less than 30 minutes ago");
		} else {
			now.set(Calendar.HOUR_OF_DAY, 0);
			now.set(Calendar.MINUTE, 0);
			now.set(Calendar.SECOND, 0);
			now.set(Calendar.MILLISECOND, 0);

			calendar.setTime(date);
			if ( calendar.before(now) ) {
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

		toAppendTo.append("at ");
		fp.setBeginIndex(toAppendTo.length());
		toAppendTo.
			append(String.format("%02d", hours)).
			append(":").
			append(String.format("%02d", minutes));

		fp.setEndIndex(toAppendTo.length());
	}

	private void staticFormat(StringBuffer toAppendTo, FieldPosition fp, String str) {
		fp.setBeginIndex(toAppendTo.length());
		toAppendTo.append(str);
		fp.setEndIndex(toAppendTo.length());
	}

	public Date parse(String source, ParsePosition pos) {
		throw new UnsupportedOperationException();
	}
}
