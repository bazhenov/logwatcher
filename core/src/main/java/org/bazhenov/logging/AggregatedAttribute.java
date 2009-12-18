package org.bazhenov.logging;

import java.util.*;

public class AggregatedAttribute {

	private final String name;
	Map<String, AttributeValue> values = new HashMap<String, AttributeValue>();

	public AggregatedAttribute(String name, Map<String, Integer> counts) {
		this.name = name;
		for ( Map.Entry<String, Integer> row : counts.entrySet() ) {
			add(new AttributeValue(row.getKey(), row.getValue()));
		}
	}

	public AggregatedAttribute(String name) {
		this.name = name;
	}

	public int getCountFor(String value) {
		AttributeValue v = values.get(value);
		return v == null
			? 0
			: v.getCount();
	}

	public AttributeValue[] toArray() {
		return values.values().toArray(new AttributeValue[values.size()]);
	}

	public void incrementCountFor(String value) {
		AttributeValue v = values.get(value);
		if ( v == null ) {
			values.put(value, new AttributeValue(value, 1));
		}else{
			v.increment();
		}
	}

	public void add(AttributeValue value) {
		AttributeValue savedValue = values.get(value.getValue());
		if ( savedValue == null ) {
			values.put(value.getValue(), value);
		}else{
			savedValue.add(value);
		}
	}

	public void merge(AggregatedAttribute value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append('{');
		for ( AttributeValue value : values.values() ) {
			builder.append(value);
		}
		builder.append('}');
		return builder.toString();
	}
}
