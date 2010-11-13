package com.farpost.logwatcher;

import com.farpost.logwatcher.transport.TransportException;
import com.farpost.logwatcher.transport.TransportListener;

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
