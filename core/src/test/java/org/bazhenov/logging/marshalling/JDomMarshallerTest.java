package org.bazhenov.logging.marshalling;

public class JDomMarshallerTest extends AbstractLogEntryMarshallerTest {

	protected Marshaller getMarshaller() {
		return new JDomMarshaller();
	}
}
