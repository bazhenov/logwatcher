package com.farpost.logwatcher;

import com.farpost.logwatcher.LogEntry;

import java.util.HashMap;
import java.util.Map;

public class AggregateAttributesVisitor implements Visitor<LogEntry> {

	private Map<String, AggregatedAttribute> attributeMap = new HashMap<String, AggregatedAttribute>();

	public synchronized void visit(LogEntry entry) {
		for ( Map.Entry<String, String> row : entry.getAttributes().entrySet() ) {
			AggregatedAttribute aggregate = attributeMap.get(row.getKey());
			if ( aggregate == null ) {
				aggregate = new AggregatedAttribute(row.getKey());
				attributeMap.put(row.getKey(), aggregate);
			}
			aggregate.incrementCountFor(row.getValue());
		}
	}

	public Map<String, AggregatedAttribute> getAttributeMap() {
		return attributeMap;
	}
}
