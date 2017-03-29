package com.farpost.logwatcher;

import java.util.concurrent.atomic.AtomicLong;

public class MessageCounter {

	private static final AtomicLong received = new AtomicLong();
	private static final AtomicLong persisted = new AtomicLong();

	public static void incrementReceived() {
		received.incrementAndGet();
	}

	public static void incrementPersisted() {
		persisted.incrementAndGet();
	}

	public static long getReceivedPersistedDelta() {
		return received.get() - persisted.get();
	}
}
