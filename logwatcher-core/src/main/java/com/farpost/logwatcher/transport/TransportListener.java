package com.farpost.logwatcher.transport;

public interface TransportListener {

	void onMessage(byte[] message) throws TransportException;
}
