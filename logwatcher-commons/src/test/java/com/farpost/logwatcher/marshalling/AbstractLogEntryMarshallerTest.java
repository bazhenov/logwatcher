package com.farpost.logwatcher.marshalling;

import com.farpost.logwatcher.Cause;
import com.farpost.logwatcher.LogEntry;
import com.farpost.logwatcher.LogEntryImpl;
import com.farpost.logwatcher.Severity;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import static com.farpost.timepoint.DateTime.now;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;

public abstract class AbstractLogEntryMarshallerTest {

	private Marshaller marshaller;

	@BeforeMethod
	protected void setUp() throws Exception {
		marshaller = getMarshaller();
	}

	@Test
	public void marshalling() {
		Cause cause = new Cause("type", "msg", "stacktrace");
		LogEntry entry = new LogEntryImpl(now(), "group", "message", Severity.info, "2fe", "default", null,
			cause);
		byte[] data = marshaller.marshall(entry);
		LogEntry entryCopy = marshaller.unmarshall(data);

		assertThat(entryCopy, equalTo(entry));
	}

	@Test
	public void marshallingEntryWithAttributes() {
		Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("foo", "bar");
		attributes.put("bar", "foo");
		LogEntry entry = new LogEntryImpl(now(), "group", "message", Severity.info, null, "default", attributes, null);
		byte[] data = marshaller.marshall(entry);
		LogEntry entryCopy = marshaller.unmarshall(data);

		assertThat(entryCopy, equalTo(entry));
	}

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

	protected abstract Marshaller getMarshaller() throws Exception;
}
