package com.farpost.logwatcher.web;

import com.farpost.logwatcher.Cause;
import com.farpost.logwatcher.statistics.MinuteVector;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Math.*;
import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.compile;

public class Functions {

	public static String pluralize(long number, String titles) {
		long abs = abs(number);
		int[] cases = new int[]{2, 0, 1, 1, 1, 2};
		String[] strings = titles.split(" ");
		String result = strings[(abs % 100 > 4 && abs % 100 < 20)
			? 2
			: cases[((int) Math.min(abs % 10, 5))]];
		return number + " " + result;
	}

	public static String shortNumberFormat(int n) {
		if (n < 1000)
			return Integer.toString(n);
		else if (n < 1000000)
			return (n / 1000) + "K";
		else
			return (n / 1000000) + "M";
	}


	public static String getSimpleType(String className) {
		int index = className.lastIndexOf('.');
		if (index < 0) index = className.lastIndexOf('\\');
		if (index > 0 && index < className.length() - 1) {
			return className.substring(index + 1);
		} else {
			return className;
		}
	}

	public static String formatCause(Cause rootCause) {
		if (rootCause == null) {
			return "";
		}
		StringBuilder prefix = new StringBuilder();
		StringBuilder stackTrace = new StringBuilder();

		Cause cause = rootCause;
		while (cause != null) {
			if (cause != rootCause) {
				stackTrace.append("\n\n").append(prefix).append("Caused by ");
			}
			String iStack = cause.getStackTrace().replaceAll("\n", "\n" + prefix);
			stackTrace.append(cause.getType())
				.append(": ")
				.append(cause.getMessage())
				.append("\n")
				.append(prefix)
				.append(iStack);
			cause = cause.getCause();
			prefix.append("  ");
		}
		return stackTrace.toString();
	}

	public static CauseDef extractExceptionClass(String title) {
		Pattern pattern = compile("\\b([a-z_0-9]+[\\.\\\\])*[a-z_0-9]*Exception\\b", CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(title);
		while (matcher.find()) {
			String fqnClassName = matcher.group();
			if (fqnClassName.equalsIgnoreCase("exception")) continue;
			String simpleClassName = getSimpleType(fqnClassName);
			if (matcher.start() == 0) {
				title = title.substring(fqnClassName.length()).replaceFirst("^[^a-zA-Z0-9]+", "");
			}
			return new CauseDef(simpleClassName, fqnClassName, title);
		}
		return new CauseDef(null, null, title);
	}

	public static double getIntensity(MinuteVector vector) {
		return ((double) vector.get(0) + vector.get(-1)) / 120d;
	}

	public static String formatIntensity(double intensity) {
		if (intensity < 1) {
			return round(intensity * 60) + "/minute";
		} else if (intensity <= 10) {
			return round(intensity) + "/second";
		} else {
			double orderOfMagnitude = log10(intensity);
			int factor = (int) pow(10, ceil(orderOfMagnitude - 2));
			return round(intensity / factor) * factor + "/second";
		}
	}
}
