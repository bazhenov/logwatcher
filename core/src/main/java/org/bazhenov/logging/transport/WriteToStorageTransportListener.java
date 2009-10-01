package org.bazhenov.logging.transport;

import org.bazhenov.logging.marshalling.Marshaller;
import org.bazhenov.logging.marshalling.MarshallerException;
import org.bazhenov.logging.storage.LogStorage;
import org.bazhenov.logging.storage.LogStorageException;
import org.apache.log4j.Logger;

public class WriteToStorageTransportListener implements TransportListener {

	private final LogStorage storage;
	private final Marshaller marshaller;
	private final Logger log = Logger.getLogger(WriteToStorageTransportListener.class);

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
