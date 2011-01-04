package com.farpost.logwatcher;

import java.util.HashMap;
import java.util.Map;

public class AggregateAttributesVisitor implements Visitor<LogEntry> {

	private Map<String, AggregatedAttribute> attributeMap = new HashMap<String, AggregatedAttribute>();
	private LogEntry firstEntry;

	public synchronized void visit(LogEntry entry) {
		for (Map.Entry<String, String> row : entry.getAttributes().entrySet()) {
			AggregatedAttribute aggregate = attributeMap.get(row.getKey());
			if (aggregate == null) {
				aggregate = new AggregatedAttribute(row.getKey());
				attributeMap.put(row.getKey(), aggregate);
			}
			aggregate.incrementCountFor(row.getValue());
		}
		if (firstEntry == null) {
			firstEntry = entry;
		}
	}

	public Map<String, AggregatedAttribute> getAttributeMap() {
		return attributeMap;
	}

	public LogEntry getFirstEntry() {
		return firstEntry;
	}
}
