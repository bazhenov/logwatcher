package com.farpost.logwatcher;

import com.google.common.base.Joiner;
import com.google.common.hash.HashFunction;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.base.Strings.nullToEmpty;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.hash.Hashing.md5;
import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.compile;

/**
 * Простая имплементация {@link ChecksumCalculator}, которая вычисляет контрольную сумму основываясь
 * на application id, severity и root cause записи лога.
 */
public class SimpleChecksumCalculator implements ChecksumCalculator {

	private final HashFunction hash = md5();
	private final Map<Pattern, String> patterns = newHashMap();

	public SimpleChecksumCalculator() {
		registerPhpRelatedMessagePatterns();
	}

	private void registerPhpRelatedMessagePatterns() {
		patterns.put(compile("Maximum execution time of \\d+ seconds exceeded in (?:/[a-z\\.0-9-_]+)+(?::\\d+)?", CASE_INSENSITIVE),
			"Maximum execution time exceeded");
		patterns.put(compile("Maximum execution time of \\d+ seconds exceeded in (?:/[a-z\\.0-9-_]+)+(?:\\(\\d+\\))?\\s*:\\s*regexp code:\\d+", CASE_INSENSITIVE),
			"Maximum execution time exceeded");

		patterns.put(compile("Allowed memory size of \\d+ bytes exhausted \\(tried to allocate \\d+ bytes\\) in (?:/[a-z\\.0-9-_]+)+(?::\\d+)?", CASE_INSENSITIVE),
			"Allowed memory size exhausted");

		patterns.put(compile("Call to undefined method ([a-z0-9_]+::[a-z0-9_]+)\\(\\) in (?:/[a-z\\.0-9-_]+)+(?::\\d+)?", CASE_INSENSITIVE),
			"Call to undefined method");

		patterns.put(compile("Call to a member function ([a-z0-9_]+)\\(\\) on a non-object in (?:/[a-z\\.0-9-_]+)+(?::\\d+)?", CASE_INSENSITIVE),
			"Call to undefined function");
	}

	public String calculateChecksum(LogEntry entry) {
		CharSequence title = checkForRegisteredPatterns(entry);
		if (title != null) {
			return hash.hashString(title, UTF_8).toString();
		}
		StringBuilder checksum = new StringBuilder()
			.append(entry.getApplicationId())
			.append(':')
			.append(entry.getSeverity())
			.append(entry.getGroup());

		if (!nullToEmpty(entry.getChecksum()).isEmpty())
			checksum.append(':').append(entry.getChecksum());

		Cause cause = entry.getCause();
		if (cause != null) {
			checksum.append(':').append(cause.getType());
		} else if (entry.getChecksum() == null || entry.getChecksum().isEmpty()) {
			checksum.append(':').append(trimmedMessage(entry.getMessage()));
		}
		return hash.hashString(checksum, UTF_8).toString();
	}

	private static String trimmedMessage(String message) {
		String[] parts = message.split("\\s+", 4);
		if (parts.length == 4) {
			parts[parts.length - 1] = null;
			return Joiner.on(' ').skipNulls().join(parts);
		} else {
			return message;
		}
	}

	private CharSequence checkForRegisteredPatterns(LogEntry entry) {
		for (Map.Entry<Pattern, String> e : patterns.entrySet()) {
			Matcher matcher = e.getKey().matcher(entry.getMessage());
			if (matcher.matches()) {
				StringBuilder builder = new StringBuilder(e.getValue());
				for (int i = 1; i <= matcher.groupCount(); i++) {
					builder.append(':').append(matcher.group(i));
				}
				return builder;
			}
		}
		return null;
	}
}
