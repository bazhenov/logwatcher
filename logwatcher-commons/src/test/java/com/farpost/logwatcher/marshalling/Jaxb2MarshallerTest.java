package com.farpost.logwatcher.marshalling;

import com.farpost.logwatcher.LogEntry;
import com.farpost.logwatcher.Severity;
import org.testng.annotations.Test;

import java.io.UnsupportedEncodingException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;

public class Jaxb2MarshallerTest extends AbstractLogEntryMarshallerTest {

	@Test
	public void marshallerShouldDemarshallMessages() throws UnsupportedEncodingException {
		String xml = "<?xml version='1.0' encoding='UTF-8'?>\n" +
			"<logEntry xmlns='http://logging.farpost.com/schema/v1.1' checksum='ff01f41d0e73d0da2129e1e49dbeb44b' date='2010-11-24T12:33:38+10:00'>\n" +
			"  <message>OutOfMemoryException</message>\n" +
			"  <application id='frontend' />\n" +
			"  <group name='' />\n" +
			"  <severity name='error' />\n" +
			"  <attributes>\n" +
			"    <attribute name='host' value='baza.farpost.ru' />\n" +
			"    <attribute name='machine' value='n22.baza.loc' />\n" +
			"    <attribute name='timeSpent' value='31,9s' />\n" +
			"    <attribute name='user' value='antonenko#210923' />\n" +
			"    <attribute name='url' value='/admin/statistica/29' />\n" +
			"    <attribute name='ip' value='172.16.7.5' />\n" +
			"  </attributes>\n" +
			"</logEntry>";
		LogEntry entry = marshaller.unmarshall(xml.getBytes("utf8"));
		assertThat(entry.getMessage(), equalTo("OutOfMemoryException"));
		assertThat(entry.getSeverity(), equalTo(Severity.error));
		assertThat(entry.getAttributes(), hasEntry("machine", "n22.baza.loc"));
		assertThat(entry.getApplicationId(), equalTo("frontend"));
		assertThat(entry.getChecksum(), equalTo("ff01f41d0e73d0da2129e1e49dbeb44b"));
	}

	@Override
	protected Marshaller getMarshaller() {
		return new Jaxb2Marshaller();
	}
}
