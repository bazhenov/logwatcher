package com.farpost.logwatcher.transport;

import com.farpost.logwatcher.Cause;
import com.farpost.logwatcher.LogEntry;
import com.farpost.logwatcher.LogEntryImpl;
import com.farpost.logwatcher.marshalling.Jaxb2Marshaller;
import com.farpost.logwatcher.marshalling.Marshaller;
import org.springframework.integration.Message;
import org.springframework.integration.channel.QueueChannel;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static com.farpost.logwatcher.Severity.error;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.joda.time.DateTime.now;

public class WriteToChannelTransportListenerTest {

	private BlockingQueue<Message<byte[]>> queue;
	private QueueChannel messageChannel;

	@BeforeMethod
	public void setUp() {
		queue = new ArrayBlockingQueue<Message<byte[]>>(1);
		//noinspection unchecked
		messageChannel = new QueueChannel((BlockingQueue) queue);
	}

	@Test
	public void listenerShouldWriteEntryToDatabase() throws TransportException, InterruptedException {
		Cause cause = new Cause("type", "message", "stack");
		LogEntry entry = new LogEntryImpl(now(), "group", "message", error, "checksum", "default", null, cause);

		Marshaller marshaller = new Jaxb2Marshaller();
		TransportListener listener = new WriteToChannelTransportListener(messageChannel);

		byte[] message = marshaller.marshall(entry);
		listener.onMessage(message);

		byte[] actualMessage = queue.take().getPayload();
		assertThat(actualMessage, equalTo(message));

	}
}
