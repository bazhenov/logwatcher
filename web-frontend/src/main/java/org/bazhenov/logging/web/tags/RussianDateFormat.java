package org.bazhenov.logging.web.tags;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;

public class RussianDateFormat extends DateFormat {

	private Calendar calendar = Calendar.getInstance();

	private final String[] monthNames = new String[]{"января", "февраля", "марта", "апреля", "мая",
		"июня", "июля", "августа", "сентября", "октября", "ноября", "декабря"};

	public synchronized StringBuffer format(Date date, StringBuffer toAppendTo,
	                                        FieldPosition fieldPosition) {
		fieldPosition.setBeginIndex(0);
		fieldPosition.setEndIndex(0);
		calendar.setTime(date);
		formatGenericDate(calendar, toAppendTo, fieldPosition);
		return toAppendTo;
	}

	private void formatGenericDate(Calendar calendar, StringBuffer toAppendTo, FieldPosition fp) {
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		String month = monthNames[calendar.get(Calendar.MONTH)];

		fp.setBeginIndex(toAppendTo.length());
		toAppendTo.
			append(day).append(" ").append(month);
		fp.setEndIndex(toAppendTo.length());
	}

	public Date parse(String source, ParsePosition pos) {
		throw new UnsupportedOperationException();
	}
}
