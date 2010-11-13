package com.farpost.logwatcher.transport;

public interface TransportListener {

	void onMessage(String message) throws TransportException;
}
