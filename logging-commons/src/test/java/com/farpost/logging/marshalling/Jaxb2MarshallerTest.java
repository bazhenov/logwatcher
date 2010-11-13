package com.farpost.logging.marshalling;

import javax.xml.bind.JAXBException;

public class Jaxb2MarshallerTest extends AbstractLogEntryMarshallerTest {

	@Override
	protected Marshaller getMarshaller() {
		return new Jaxb2Marshaller();
	}
}
