package org.bazhenov.logging.transport;

public interface TransportListener {

	void onMessage(String message) throws TransportException;
}
