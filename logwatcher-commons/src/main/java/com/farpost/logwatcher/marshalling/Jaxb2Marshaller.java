package com.farpost.logwatcher.marshalling;

import com.farpost.logwatcher.Cause;
import com.farpost.logwatcher.LogEntry;
import com.farpost.logwatcher.LogEntryImpl;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.io.StringWriter;

public class Jaxb2Marshaller implements Marshaller {

	private javax.xml.bind.Marshaller marshaller;
	private Unmarshaller unmarshaller;

	public Jaxb2Marshaller() {
		try {
			JAXBContext context = JAXBContext.newInstance(LogEntryImpl.class, Cause.class);
			marshaller = context.createMarshaller();
			unmarshaller = context.createUnmarshaller();
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
	}

	public synchronized String marshall(LogEntry entry) {
		StringWriter writer = new StringWriter();
		try {
			marshaller.marshal(entry, writer);
		} catch (JAXBException e) {
			throw new MarshallerException(e);
		}
		return writer.toString();
	}

	public synchronized LogEntry unmarshall(String data) {
		try {
			JAXBElement<LogEntryImpl> container = unmarshaller.unmarshal(new StreamSource(new StringReader(data)), LogEntryImpl.class);
			return container.getValue();
		} catch (JAXBException e) {
			throw new MarshallerException(e);
		}
	}
}
