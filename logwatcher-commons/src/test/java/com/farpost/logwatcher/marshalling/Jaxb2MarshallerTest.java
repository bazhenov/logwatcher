package com.farpost.logwatcher.marshalling;

public class Jaxb2MarshallerTest extends AbstractLogEntryMarshallerTest {

	@Override
	protected Marshaller getMarshaller() {
		return new Jaxb2Marshaller();
	}
}
