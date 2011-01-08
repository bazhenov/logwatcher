package com.farpost.logwatcher;

import com.farpost.logwatcher.transport.TransportException;
import com.farpost.logwatcher.transport.TransportListener;

import java.util.Queue;

public class QueueAppendListener implements TransportListener {

	private Queue<byte[]> queue;

	public QueueAppendListener(Queue<byte[]> queue) {
		this.queue = queue;
	}

	public void onMessage(byte[] message) throws TransportException {
		queue.add(message);
	}
}
