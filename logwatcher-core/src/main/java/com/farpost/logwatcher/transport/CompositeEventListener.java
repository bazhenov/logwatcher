package com.farpost.logwatcher.transport;

import com.farpost.logwatcher.LogEntry;
import org.slf4j.Logger;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.slf4j.LoggerFactory.getLogger;

public class CompositeEventListener implements LogEntryListener {

	private final Iterable<? extends LogEntryListener> listeners;
	private Logger log = getLogger(CompositeEventListener.class);

	public CompositeEventListener(Iterable<? extends LogEntryListener> listeners) {
		this.listeners = checkNotNull(listeners);
	}

	@Override
	public void onEntry(LogEntry entry) {
		for (LogEntryListener listener : listeners) {
			try {
				listener.onEntry(entry);
			} catch (RuntimeException e) {
				log.warn("Failed on listener {}", listener.getClass(), e);
			}
		}
	}
}
