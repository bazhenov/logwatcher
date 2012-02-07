package com.farpost.logwatcher.transport;

import com.farpost.logwatcher.Cause;
import com.farpost.logwatcher.LogEntry;
import com.farpost.logwatcher.LogEntryImpl;
import com.farpost.logwatcher.marshalling.Jaxb2Marshaller;
import com.farpost.logwatcher.marshalling.Marshaller;
import org.springframework.integration.channel.QueueChannel;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static com.farpost.logwatcher.Severity.error;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.joda.time.DateTime.now;

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
    public void listenerShouldWriteEntryToDatabase() throws TransportException, InterruptedException {
        Cause cause = new Cause("type", "message", "stack");
        LogEntry entry = new LogEntryImpl(now(), "group", "message", error, "checksum", "default", null, cause);

        byte[] message = marshaller.marshall(entry);
        listener.onMessage(message);

        byte[] actualMessage = (byte[]) messageChannel.receive().getPayload();
        assertThat(actualMessage, equalTo(message));
    }

    @Test(expectedExceptions = TransportException.class)
    public void listenerShouldThrowExceptionOnQueueOverflow() throws TransportException, InterruptedException {
        Cause cause = new Cause("type", "message", "stack");
        LogEntry entry = new LogEntryImpl(now(), "group", "message", error, "checksum", "default", null, cause);

        byte[] message = marshaller.marshall(entry);
        listener.onMessage(message);
        listener.onMessage(message);
    }
}
