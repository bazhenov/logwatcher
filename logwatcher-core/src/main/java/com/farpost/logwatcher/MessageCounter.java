package com.farpost.logwatcher;

import java.util.concurrent.atomic.AtomicLong;

public class MessageCounter {

	private static final AtomicLong received = new AtomicLong();
	private static final AtomicLong persisted = new AtomicLong();
	private static final AtomicLong rejectedByChannelOverflow = new AtomicLong();

	public static void incrementReceived() {
		received.incrementAndGet();
	}

	public static void incrementRejectedByChannelOverflow() {
		rejectedByChannelOverflow.incrementAndGet();
	}

	public static void incrementPersisted() {
		persisted.incrementAndGet();
	}

	public static long getReceivedPersistedDelta() {
		return received.get() - persisted.get();
	}

	public static long getRejectedByChannelOverflow() {
		return rejectedByChannelOverflow.get();
	}
}
