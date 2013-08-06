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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

import static com.farpost.logwatcher.Severity.error;
import static com.farpost.logwatcher.transport.WriteToChannelTransportListener.SENDER_ADDRESS;
import static java.net.InetAddress.getLocalHost;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class WriteToChannelTransportListenerTest {

	private final Marshaller marshaller = new Jaxb2Marshaller();

	private QueueChannel messageChannel;
	private TransportListener listener;

	@BeforeMethod
	public void setUp() {
		messageChannel = new QueueChannel(1);
		listener = new WriteToChannelTransportListener(messageChannel);
	}

	@Test
	public void listenerShouldWriteEntryToChannel()
		throws TransportException, InterruptedException, UnknownHostException {
		Cause cause = new Cause("type", "message", "stack");
		LogEntry entry = new LogEntryImpl(new Date(), "group", "message", error, "checksum", "default", null, cause);

		byte[] message = marshaller.marshall(entry);
		InetAddress sender = getLocalHost();
		listener.onMessage(message, sender);

		Message<?> envelope = messageChannel.receive();
		byte[] actualMessage = (byte[]) envelope.getPayload();
		assertThat(actualMessage, equalTo(message));
		InetAddress senderCopy = envelope.getHeaders().get(SENDER_ADDRESS, InetAddress.class);
		assertThat(senderCopy, is(sender));
	}

	@Test(expectedExceptions = TransportException.class)
	public void listenerShouldThrowExceptionOnQueueOverflow()
		throws TransportException, InterruptedException, UnknownHostException {
		Cause cause = new Cause("type", "message", "stack");
		LogEntry entry = new LogEntryImpl(new Date(), "group", "message", error, "checksum", "default", null, cause);

		byte[] message = marshaller.marshall(entry);
		listener.onMessage(message, getLocalHost());
		listener.onMessage(message, getLocalHost());
	}
}
