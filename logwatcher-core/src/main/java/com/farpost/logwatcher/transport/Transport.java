package com.farpost.logwatcher.transport;

public interface Transport {

	void start() throws TransportException;

	void stop() throws TransportException;
}
