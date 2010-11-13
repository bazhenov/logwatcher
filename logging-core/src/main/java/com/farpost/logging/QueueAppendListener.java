package com.farpost.logging;

import org.bazhenov.logging.transport.TransportException;
import org.bazhenov.logging.transport.TransportListener;

import java.util.Queue;

public class QueueAppendListener implements TransportListener {

	private Queue<String> queue;

	public QueueAppendListener(Queue<String> queue) {
		this.queue = queue;
	}

	public void onMessage(String message) throws TransportException {
		queue.add(message);
	}
}
