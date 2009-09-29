package org.bazhenov.logging;

import com.farpost.timepoint.DateTime;

import java.util.concurrent.atomic.AtomicInteger;

public class AggregatedLogEntryImpl implements AggregatedLogEntry {

	private volatile DateTime lastTime;
	private final LogEntry sampleEntry;
	private final AtomicInteger count;

	public AggregatedLogEntryImpl(LogEntry sampleEntry) {
		this.sampleEntry = sampleEntry;
		this.lastTime = sampleEntry.getDate();
		count = new AtomicInteger(1);
	}

	public AggregatedLogEntryImpl(LogEntry sampleEntry, DateTime lastTime, int count) {
		this.sampleEntry = sampleEntry;
		this.lastTime = lastTime;
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

	public synchronized void setLastTime(DateTime date) {
		if ( date.greaterThan(lastTime) ) {
			lastTime = date;
		}
	}

	public void incrementCount() {
		count.incrementAndGet();
	}

	public void incrementCount(int times) {
		count.addAndGet(times);
	}
}
