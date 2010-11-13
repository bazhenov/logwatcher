package com.farpost.logwatcher.marshalling;

/**
 * Имплементации данного интерфейса сериализуют и десериализуют обьекты типа {@link com.farpost.logwatcher.marshalling.LogEntryImpl}
 * в формат wire протокола.
 * <p />
 * Внимание, все имплементации этого интерфейса должны быть потокобезопасны.
 */
public interface Marshaller {

	String marshall(LogEntry entry) throws MarshallerException;

	LogEntry unmarshall(String data) throws MarshallerException;
}
