package org.bazhenov.logging.storage;

import org.bazhenov.logging.AggregatedLogEntry;
import org.bazhenov.logging.LogEntry;

import java.util.Map;

/**
 * Имплементация {@link LogEntryMatcher} тестирующая значение аттрибута записи.
 */
public class AttributeValueMatcher implements LogEntryMatcher {

	private final String name;
	private final String value;

	public AttributeValueMatcher(String name, String value) {
		if ( name == null || value == null ) {
			throw new NullPointerException("Name and value should not be null");
		}
		this.name = name;
		this.value = value;
	}

	public boolean isMatch(AggregatedLogEntry entry) {
		throw new UnsupportedOperationException();
	}

	public boolean isMatch(LogEntry entry) {
		Map<String,String> map = entry.getAttributes();
		return map.containsKey(name) && map.get(name).equals(value);
	}

	@Override
	public String toString() {
		return "@"+name+":"+value;
	}

	@Override
	public boolean equals(Object o) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		AttributeValueMatcher that = (AttributeValueMatcher) o;

		return name.equals(that.name) && value.equals(that.value);
	}

	@Override
	public int hashCode() {
		return 31 * name.hashCode() + value.hashCode();
	}
}
