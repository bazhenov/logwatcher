package org.bazhenov.logging.transport;

public interface Transport {

	void start() throws TransportException;

	void stop() throws TransportException;
}
