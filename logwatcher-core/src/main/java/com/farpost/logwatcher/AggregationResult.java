package com.farpost.logwatcher;

import java.util.HashMap;
import java.util.Map;

/**
 * DTO. Результат агрегации логов. Содержит первую запись лога {@see LogEntry}
 * и агрегированные аттрибуты всех записей {@see AggregatedAttribute}
 */
public class AggregationResult {

	private Map<String, AggregatedAttribute> attributeMap = new HashMap<String, AggregatedAttribute>();
	private LogEntry firstEntry;

	public AggregationResult(Map<String, AggregatedAttribute> attributeMap, LogEntry firstEntry) {
		this.attributeMap = attributeMap;
		this.firstEntry = firstEntry;
	}

	public Map<String, AggregatedAttribute> getAttributeMap() {
		return attributeMap;
	}

	public LogEntry getFirstEntry() {
		return firstEntry;
	}

}
