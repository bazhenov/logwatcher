package org.bazhenov.logging.storage;

import org.bazhenov.logging.AggregatedLogEntry;
import org.bazhenov.logging.LogEntry;

import java.util.Map;

public class AttributeValueMatcher implements LogEntryMatcher {

	private final String name;
	private final String value;

	public AttributeValueMatcher(String name, String value) {
		this.name = name;
		this.value = value;
	}

	public boolean isMatch(AggregatedLogEntry entry) {
		return false;
	}

	public boolean isMatch(LogEntry entry) {
		Map<String,String> map = entry.getAttributes();
		return map.containsKey(name) && map.get(name).equals(value);
	}
}
