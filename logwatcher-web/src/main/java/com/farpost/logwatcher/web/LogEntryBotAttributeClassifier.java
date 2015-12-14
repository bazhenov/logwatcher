package com.farpost.logwatcher.web;

import com.farpost.logwatcher.LogEntry;

import java.util.function.Predicate;
import java.util.regex.Pattern;

public class LogEntryBotAttributeClassifier implements LogEntryClassifier {

	private static final Predicate<String> TRUE_VALUE = Pattern.compile("true|y|1|yes", Pattern.CASE_INSENSITIVE).asPredicate();
	public static final String ATTRIBUTE_NAME = "isBot";

	@Override public String getEntryCssClass(LogEntry entry) {
		String botAttribute = entry.getAttributes().get(ATTRIBUTE_NAME);
		return botAttribute != null && TRUE_VALUE.test(botAttribute) ? "entry-bot" : "";
	}
}
