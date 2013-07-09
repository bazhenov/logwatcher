package com.farpost.logwatcher;

import com.google.common.hash.HashFunction;

import java.util.Map;
import java.util.regex.Pattern;

import static com.google.common.base.Charsets.UTF_8;
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
	private final Map<String, Pattern> patterns = newHashMap();

	public SimpleChecksumCalculator() {
		patterns.put("Maximum execution time exceeded",
			compile("Maximum execution time of \\d+ seconds exceeded in (/[a-z\\.0-9-]+)+(:\\d+)?", CASE_INSENSITIVE));
		patterns.put("Allowed memory size exhausted",
			compile("Allowed memory size of \\d+ bytes exhausted \\(tried to allocate \\d+ bytes\\) in (/[a-z\\.0-9-]+)+(:\\d+)?", CASE_INSENSITIVE));
	}

	public String calculateChecksum(LogEntry entry) {
		String title = checkForRegisteredPatterns(entry);
		if (title != null) {
			return hash.hashString(title, UTF_8).toString();
		}
		String checksum = entry.getApplicationId() + ":" + entry.getSeverity();
		Cause cause = entry.getCause();
		if (cause != null) {
			checksum += ":" + cause.getRootCause().getType();
		} else if (entry.getChecksum() == null || entry.getChecksum().isEmpty()) {
			checksum += ":" + entry.getMessage();
		} else {
			checksum += ":" + entry.getChecksum();
		}
		return hash.hashString(checksum, UTF_8).toString();
	}

	private String checkForRegisteredPatterns(LogEntry entry) {
		for (Map.Entry<String, Pattern> e : patterns.entrySet()) {
			if (e.getValue().matcher(entry.getMessage()).matches()) {
				return e.getKey();
			}
		}
		return null;
	}
}
