package com.farpost.logwatcher.web;

import static java.lang.Math.abs;

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

	public static String formatClassName(String className) {
		int index = className.lastIndexOf('.');
		if (index > 0 && index < className.length() - 1) {
			return className.substring(index + 1);
		} else {
			return className;
		}
	}
}
