package org.bazhenov.logging.marshalling;

import org.bazhenov.logging.LogEntry;

public interface Marshaller {

	String marshall(LogEntry entry) throws MarshallerException;

	LogEntry unmarshall(String data) throws MarshallerException;
}
