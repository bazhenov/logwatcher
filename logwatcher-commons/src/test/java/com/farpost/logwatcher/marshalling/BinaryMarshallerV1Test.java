package com.farpost.logwatcher.marshalling;

public class BinaryMarshallerV1Test extends AbstractLogEntryMarshallerTest {

	@Override
	protected Marshaller getMarshaller() {
		return new BinaryMarshallerV1();
	}
}
