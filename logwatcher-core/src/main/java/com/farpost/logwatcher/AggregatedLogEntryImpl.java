package com.farpost.logwatcher;

import com.farpost.timepoint.DateTime;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class AggregatedLogEntryImpl implements AggregatedLogEntry {

	private volatile DateTime lastTime;
	private final LogEntry sampleEntry;
	private final AtomicInteger count;
	private final Map<String, AggregatedAttribute> attributes;

	public AggregatedLogEntryImpl(LogEntry sampleEntry) {
		this.sampleEntry = sampleEntry;
		this.lastTime = sampleEntry.getDate();
		count = new AtomicInteger(1);
		attributes = new HashMap<String, AggregatedAttribute>();
		merge(sampleEntry.getAttributes(), attributes);
	}

	public AggregatedLogEntryImpl(LogEntry sampleEntry, DateTime lastTime, int count,
																Map<String, AggregatedAttribute> attributes) {
		this.sampleEntry = sampleEntry;
		this.lastTime = lastTime;
		this.attributes = attributes;
		this.count = new AtomicInteger(count);
	}

	public DateTime getLastTime() {
		return lastTime;
	}

	public int getCount() {
		return count.get();
	}

	public String getGroup() {
		return sampleEntry.getCategory();
	}

	public LogEntry getSampleEntry() {
		return sampleEntry;
	}

	public void merge(AggregatedLogEntry entry) {
		count.addAndGet(entry.getCount());
		if (lastTime.lessThan(entry.getLastTime())) {
			lastTime = entry.getLastTime();
		}
	}

	public static void merge(Map<String, String> attributes, Map<String, AggregatedAttribute> map) {
		for (Map.Entry<String, String> row : attributes.entrySet()) {
			if (map.containsKey(row.getKey())) {
				map.get(row.getKey()).incrementCountFor(row.getValue());
			} else {
				AggregatedAttribute attribute = new AggregatedAttribute(row.getKey());
				attribute.add(new AttributeValue(row.getValue(), 1));
				map.put(row.getKey(), attribute);
			}
		}
	}
}
