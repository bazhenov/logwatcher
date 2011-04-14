package com.farpost.logwatcher.storage;

import com.farpost.logwatcher.LogEntry;

import java.util.Map;

/**
 * Имплементация {@link LogEntryMatcher} тестирующая значение аттрибута записи.
 */
public class AttributeValueMatcher implements LogEntryMatcher {

	private final String name;
	private final String expectedValue;

	public AttributeValueMatcher(String name, String expectedValue) {
		if (name == null || expectedValue == null) {
			throw new NullPointerException("Name and expected value should not be null");
		}
		this.name = name;
		this.expectedValue = expectedValue;
	}

	public String getName() {
		return name;
	}

	public String getExpectedValue() {
		return expectedValue;
	}

	public boolean isMatch(LogEntry entry) {
		Map<String, String> map = entry.getAttributes();
		return map.containsKey(name) && map.get(name).equals(expectedValue);
	}

	@Override
	public String toString() {
		return "@" + name + ":" + expectedValue;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		AttributeValueMatcher that = (AttributeValueMatcher) o;

		return name.equals(that.name) && expectedValue.equals(that.expectedValue);
	}

	@Override
	public int hashCode() {
		return 31 * name.hashCode() + expectedValue.hashCode();
	}
}
