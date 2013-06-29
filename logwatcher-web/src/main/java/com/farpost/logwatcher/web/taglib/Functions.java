package com.farpost.logwatcher.web.taglib;

import com.farpost.logwatcher.AggregatedEntry;
import com.farpost.logwatcher.Cause;
import org.joda.time.ReadableDateTime;

import java.text.DateFormat;
import java.util.Date;

import static java.lang.Math.abs;

public class Functions {

	public static final ThreadLocal<DateFormat> shortFormat = new ThreadLocal<DateFormat>() {
		@Override
		protected DateFormat initialValue() {
			return new DateTimeFormat();
		}
	};

	public static String shortFormat(Date date) {
		return shortFormat.get().format(date);
	}

	public static boolean isNew(AggregatedEntry entry) {
		return entry.getLastTime().plusMinutes(30).isAfterNow();
	}

	public static Cause rootCause(Cause cause) {
		if (cause == null) {
			return null;
		}
		while (cause.getCause() != null) {
			cause = cause.getCause();
		}
		return cause;
	}

	public static String trim(String string, int limit, String append) {
		if (string == null) {
			return null;
		}
		if (string.length() > limit) {
			string = string.substring(0, limit);
			return append != null && append.length() > 0
				? string + append
				: string;
		} else {
			return string;
		}
	}

	public static String pluralize(int number, String titles) {
		int abs = abs(number);
		int[] cases = new int[]{2, 0, 1, 1, 1, 2};
		String[] strings = titles.split(" ");
		String result = strings[(abs % 100 > 4 && abs % 100 < 20)
			? 2
			: cases[Math.min(abs % 10, 5)]];
		return number + " " + result;
	}

	public static int thousands(int number) {
		return number / 1000;
	}

	public static int magnitude(int number) {
		return (int) Math.log10(number);
	}

	public static Date date(ReadableDateTime date) {
		return date.toDateTime().toDate();
	}
}
