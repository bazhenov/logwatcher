package com.farpost.logwatcher.transport;

public class TransportException extends Exception {

    public TransportException(String message) {
        super(message);
    }

    public TransportException(Throwable cause) {
		super(cause);
	}
}
