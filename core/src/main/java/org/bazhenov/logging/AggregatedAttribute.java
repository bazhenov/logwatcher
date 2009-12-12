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
		values.add(new AttributeValue(value, 1));
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
}
