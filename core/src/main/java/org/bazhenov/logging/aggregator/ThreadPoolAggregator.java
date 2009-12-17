package org.bazhenov.logging.aggregator;

import org.bazhenov.logging.AggregatedLogEntry;
import org.bazhenov.logging.LogEntry;
import org.bazhenov.logging.storage.LogEntryMatcher;
import static org.bazhenov.logging.storage.MatcherUtils.isMatching;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;

public class ThreadPoolAggregator implements Aggregator {

	private final int threadsCount = 2;
	private final List<Thread> threads = new ArrayList<Thread>(threadsCount);
	private final Queue<FilterTask> queue = new ConcurrentLinkedQueue<FilterTask>();

	public ThreadPoolAggregator() {
		Runnable worker = new Worker(queue);
		for ( int i = 0; i < threadsCount; i++ ) {
			Thread thread = new Thread(worker);
			threads.add(thread);
			thread.start();
		}
	}

	public Collection<AggregatedLogEntry> aggregate(Iterable<LogEntry> entries,
	                                          Collection<LogEntryMatcher> matchers) {
		FilterTask task = null;
		for ( LogEntry entry : entries ) {
			task = new FilterTask(entry, matchers);
			queue.add(task);
		}
		try {
			task.await();
		} catch ( InterruptedException e ) {
			throw new RuntimeException(e);
		}
		return null;
	}
}

class Worker implements Runnable {

	private final Queue<FilterTask> queue;

	public Worker(Queue<FilterTask> queue) {
		this.queue = queue;
	}

	public void run() {
		while ( !Thread.interrupted() ) {
			FilterTask task;
			do {
				task = queue.poll();
				if ( task == null ) {
					try {
						Thread.sleep(0);
					} catch ( InterruptedException e ) {
						Thread.interrupted();
					}
				}
			} while ( task == null );
			if ( isMatching(task.getEntry(), task.getMatchers()) ) {
				task.complete();
			}else{
				task.complete();
			}
		}
	}
}

class FilterTask {

	private final LogEntry entry;
	private final Collection<LogEntryMatcher> matchers;
	private final CountDownLatch latch = new CountDownLatch(1);

	FilterTask(LogEntry entry, Collection<LogEntryMatcher> matchers) {
		this.entry = entry;
		this.matchers = matchers;
	}

	public LogEntry getEntry() {
		return entry;
	}

	public Collection<LogEntryMatcher> getMatchers() {
		return matchers;
	}

	public void await() throws InterruptedException {
		latch.await();
	}

	public void complete() {
		latch.countDown();
	}
}
