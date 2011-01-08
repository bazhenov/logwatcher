package com.farpost.logwatcher.marshalling;

import com.farpost.logwatcher.Cause;
import com.farpost.logwatcher.LogEntry;
import com.farpost.logwatcher.LogEntryImpl;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;

import static java.nio.charset.Charset.forName;

public class Jaxb2Marshaller implements Marshaller {

	private javax.xml.bind.Marshaller marshaller;
	private Unmarshaller unmarshaller;
	private static final Charset CHARSET = forName("utf8");

	public Jaxb2Marshaller() {
		try {
			JAXBContext context = JAXBContext.newInstance(LogEntryImpl.class, Cause.class);
			marshaller = context.createMarshaller();
			unmarshaller = context.createUnmarshaller();
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
	}

	public synchronized byte[] marshall(LogEntry entry) {
		StringWriter writer = new StringWriter();
		try {
			marshaller.marshal(entry, writer);
		} catch (JAXBException e) {
			throw new MarshallerException(e);
		}
		return writer.toString().getBytes(CHARSET);
	}

	public synchronized LogEntry unmarshall(byte[] data) {
		try {
			StreamSource source = new StreamSource(new ByteArrayInputStream(data));
			JAXBElement<LogEntryImpl> container = unmarshaller.unmarshal(source, LogEntryImpl.class);
			return container.getValue();
		} catch (JAXBException e) {
			throw new MarshallerException(e);
		}
	}
}
