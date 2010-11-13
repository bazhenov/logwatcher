package com.farpost.logging.marshalling;

import com.farpost.logwatcher.marshalling.Jaxb2Marshaller;
import com.farpost.logwatcher.marshalling.Marshaller;

public class Jaxb2MarshallerTest extends AbstractLogEntryMarshallerTest {

	@Override
	protected Marshaller getMarshaller() {
		return new Jaxb2Marshaller();
	}
}
