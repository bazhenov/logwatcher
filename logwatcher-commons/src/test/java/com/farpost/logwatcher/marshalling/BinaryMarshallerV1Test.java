package com.farpost.logwatcher.marshalling;

public class BinaryMarshallerV1Test extends AbstractLogEntryMarshallerTest {

	@Override
	protected Marshaller getMarshaller() throws Exception {
		return new BinaryMarshallerV1();
	}
}
