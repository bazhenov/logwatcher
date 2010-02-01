package org.bazhenov.logging.marshalling;

import org.bazhenov.logging.LogEntry;

/**
 * Имплементации данного интерфейса сериализуют и десериализуют обьекты типа {@link LogEntry}
 * в формат wire протокола.
 * <p />
 * Внимание, все имплементации этого интерфейса должны быть потокобезопасны.
 */
public interface Marshaller {

	String marshall(LogEntry entry) throws MarshallerException;

	LogEntry unmarshall(String data) throws MarshallerException;
}
