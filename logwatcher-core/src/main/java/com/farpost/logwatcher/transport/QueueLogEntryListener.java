package com.farpost.logwatcher.transport;

import com.farpost.logwatcher.LogEntry;

import java.util.Queue;

import static com.google.common.base.Preconditions.checkNotNull;

public class QueueLogEntryListener implements LogEntryListener {

	private final Queue<LogEntry> queue;

	public QueueLogEntryListener(Queue<LogEntry> queue) {
		this.queue = checkNotNull(queue);
	}

	@Override
	public void onEntry(LogEntry entry) {
		queue.add(entry);
	}
}
