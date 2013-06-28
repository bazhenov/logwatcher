package com.farpost.logwatcher.transport;

import com.farpost.logwatcher.marshalling.Marshaller;

import static com.google.common.base.Preconditions.checkNotNull;

public class LogEntryListenerMarshallingAdapter implements TransportListener {

	private final Marshaller marshaller;
	private final LogEntryListener listener;

	public LogEntryListenerMarshallingAdapter(Marshaller marshaller, LogEntryListener listener) {
		this.listener = checkNotNull(listener);
		this.marshaller = checkNotNull(marshaller);
	}

	@Override
	public void onMessage(byte[] message) throws TransportException {
		listener.onEntry(marshaller.unmarshall(message));
	}
}
