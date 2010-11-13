package com.farpost.logwatcher.transport;

import com.farpost.logging.marshalling.Marshaller;
import com.farpost.logging.marshalling.MarshallerException;
import com.farpost.logwatcher.storage.LogStorage;
import com.farpost.logwatcher.storage.LogStorageException;

public class WriteToStorageTransportListener implements TransportListener {

	private final LogStorage storage;
	private final Marshaller marshaller;

	public WriteToStorageTransportListener(LogStorage storage, Marshaller marshaller) {
		this.storage = storage;
		this.marshaller = marshaller;
	}

	public void onMessage(String message) throws TransportException {
		try {
			storage.writeEntry(marshaller.unmarshall(message));
		} catch ( MarshallerException e ) {
			throw new TransportException(e);
		} catch ( LogStorageException e ) {
			throw new TransportException(e);
		}
	}
}
