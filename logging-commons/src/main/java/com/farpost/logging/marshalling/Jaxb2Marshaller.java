package com.farpost.logging.marshalling;

import org.bazhenov.logging.Cause;
import org.bazhenov.logging.LogEntry;
import org.bazhenov.logging.LogEntryImpl;

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

	public Jaxb2Marshaller() throws JAXBException {
		JAXBContext context = JAXBContext.newInstance(LogEntryImpl.class, Cause.class);
		marshaller = context.createMarshaller();
		unmarshaller = context.createUnmarshaller();
	}

	public synchronized String marshall(LogEntry entry) throws MarshallerException {
		StringWriter writer = new StringWriter();
		try {
			marshaller.marshal(entry, writer);
		} catch (JAXBException e) {
			throw new MarshallerException(e);
		}
		return writer.toString();
	}

	public synchronized LogEntry unmarshall(String data) throws MarshallerException {
		try {
			JAXBElement<LogEntryImpl> container = unmarshaller.unmarshal(new StreamSource(new StringReader(data)), LogEntryImpl.class);
			return container.getValue();
		} catch (JAXBException e) {
			throw new MarshallerException(e);
		}
	}
}
