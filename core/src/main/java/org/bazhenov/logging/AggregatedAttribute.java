package org.bazhenov.logging;

import java.util.*;

public class AggregatedAttribute {

	Set<AttributeValue> values = new TreeSet<AttributeValue>(new ByValueComparator());

	public AggregatedAttribute(Map<String, Integer> counts) {
		for ( Map.Entry<String, Integer> row : counts.entrySet() ) {
			values.add(new AttributeValue(row.getKey(), row.getValue()));
		}
	}

	public AggregatedAttribute(String value) {
		this(value, 1);
	}

	public AggregatedAttribute(String value, int count) {
		values.add(new AttributeValue(value, count));
	}

	public int getCountFor(String value) {
		AttributeValue v = findValue(value);
		return v == null
			? 0
			: v.getCount();
	}

	public AttributeValue[] toArray() {
		return values.toArray(new AttributeValue[values.size()]);
	}

	public void incrementCountFor(String value) {
		AttributeValue v = findValue(value);
		if ( v == null ) {
			values.add(new AttributeValue(value, 1));
		}else{
			v.increment();
		}
	}

	private AttributeValue findValue(String value) {
		for ( AttributeValue v : values ) {
			if ( v.getValue().equals(value) ) {
				return v;
			}
		}
		return null;
	}

	public Set<AttributeValue> getValues() {
		return values;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append('{');
		for ( AttributeValue value : values ) {
			builder.append(value);
		}
		builder.append('}');
		return builder.toString();
	}

	public void merge(AggregatedAttribute value) {
		throw new UnsupportedOperationException();
	}
}
