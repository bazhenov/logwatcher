package com.farpost.logwatcher;

import java.util.HashMap;
import java.util.Map;

public class AggregateAttributesVisitor implements Visitor<LogEntry, AggregationResult> {

	private Map<String, AggregatedAttribute> attributeMap = new HashMap<>();
	private LogEntry lastEntry;

	@Override
	public void visit(LogEntry entry) {
		for (Map.Entry<String, String> row : entry.getAttributes().entrySet()) {
			AggregatedAttribute aggregate = attributeMap.get(row.getKey());
			if (aggregate == null) {
				aggregate = new AggregatedAttribute(row.getKey());
				attributeMap.put(row.getKey(), aggregate);
			}
			aggregate.incrementCountFor(row.getValue());
		}
		if (lastEntry == null) {
			lastEntry = entry;
		}
	}

	@Override
	public AggregationResult getResult() {
		return new AggregationResult(attributeMap, lastEntry);
	}

}
