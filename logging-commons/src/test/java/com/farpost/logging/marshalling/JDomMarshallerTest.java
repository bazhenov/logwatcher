package com.farpost.logging.marshalling;

public class JDomMarshallerTest extends AbstractLogEntryMarshallerTest {

	protected Marshaller getMarshaller() {
		return new JDomMarshaller();
	}
}
