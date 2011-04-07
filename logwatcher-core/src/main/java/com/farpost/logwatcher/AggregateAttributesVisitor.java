package com.farpost.logwatcher;

import java.util.HashMap;
import java.util.Map;

public class AggregateAttributesVisitor implements Visitor<LogEntry, AggregationResult> {

	private Map<String, AggregatedAttribute> attributeMap = new HashMap<String, AggregatedAttribute>();
	private LogEntry firstEntry;
	private AggregationResult aggregationResult;

	@Override
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

		aggregationResult = new AggregationResult(attributeMap, firstEntry);
	}

	@Override
	public AggregationResult getResult() {
		return aggregationResult;
	}

}
