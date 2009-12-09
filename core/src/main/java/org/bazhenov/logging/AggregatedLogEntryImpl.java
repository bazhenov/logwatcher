package org.bazhenov.logging;

import com.farpost.timepoint.DateTime;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.*;

public class AggregatedLogEntryImpl implements AggregatedLogEntry {

	private volatile DateTime lastTime;
	private final LogEntry sampleEntry;
	private final AtomicInteger count;
	private final Map<String, Map<String, Integer>> attributes;

	public AggregatedLogEntryImpl(LogEntry sampleEntry) {
		this.sampleEntry = sampleEntry;
		this.lastTime = sampleEntry.getDate();
		count = new AtomicInteger(1);
		attributes = aggregate(sampleEntry.getAttributes());
	}

	public AggregatedLogEntryImpl(LogEntry sampleEntry, DateTime lastTime, int count,
	                              Map<String, Map<String, Integer>> attributes) {
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

	public LogEntry getSampleEntry() {
		return sampleEntry;
	}

	public Map<String, Map<String, Integer>> getAttributes() {
		return attributes;
	}

	public void incrementCount(int times) {
		count.addAndGet(times);
	}

	public synchronized void happensAgain(LogEntry entry) {
		count.incrementAndGet();
		DateTime time = entry.getDate();
		if ( time.greaterThan(lastTime) ) {
			lastTime = time;
		}
		Map<String, String> attributes = entry.getAttributes();
		Map<String, Map<String, Integer>> map = this.attributes;
		merge(map, attributes);
	}

	public static void merge(Map<String, Map<String, Integer>> map, Map<String, String> attributes) {
		for ( Map.Entry<String, String> row : attributes.entrySet() ) {
			Map<String, Integer> counts;
			if ( map.containsKey(row.getKey()) ) {
				counts = map.get(row.getKey());
			}else{
				counts = new HashMap<String, Integer>();
				map.put(row.getKey(), counts);
			}
			int count = counts.containsKey(row.getValue())
				? counts.get(row.getValue())
				: 0;
			counts.put(row.getValue(), count + 1);
		}
	}

	public static Map<String, Map<String, Integer>> aggregate(Map<String, String> attributes) {
		HashMap<String, Map<String, Integer>> ret = new HashMap<String, Map<String, Integer>>();
		for ( Map.Entry<String, String> row : attributes.entrySet() ) {
			Map<String, Integer> counts = new HashMap<String, Integer>();
			counts.put(row.getValue(), 1);
			ret.put(row.getKey(), counts);
		}
		return ret;
	}
}
