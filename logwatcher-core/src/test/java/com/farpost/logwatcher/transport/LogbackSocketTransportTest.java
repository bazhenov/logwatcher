package com.farpost.logwatcher.transport;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.net.SocketAppender;
import com.farpost.logwatcher.LogEntry;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.net.InetAddress.getLocalHost;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;

public class LogbackSocketTransportTest {

	@Test
	public void shouldTransmitMessages() throws TransportException, InterruptedException, IOException {
		int port = 33450;
		Logger logger = (Logger) LoggerFactory.getLogger(LogbackSocketTransportTest.class);

		SocketAppender appender = new SocketAppender();
		appender.setPort(port);
		appender.setRemoteHost("localhost");
		appender.setContext(logger.getLoggerContext());
		appender.setReconnectionDelay(100);
		appender.setQueueSize(10);

		logger.addAppender(appender);
		appender.start();

		BlockingQueue<LogEntry> queue = new ArrayBlockingQueue<LogEntry>(1);
		LogbackSocketTransport socketTransport = new LogbackSocketTransport(port, new QueueLogEntryListener(queue));
		socketTransport.start();

		MDC.put("foo", "bar");
		String message = "This is message generated intentionally";
		logger.error(message);
		MDC.remove("foo");

		LogEntry entry = checkNotNull(queue.poll(10, SECONDS));

		assertThat(entry.getMessage(), is(message));
		assertThat(entry.getApplicationId(), is(logger.getLoggerContext().getName()));
		assertThat(entry.getAttributes(), hasEntry("foo", "bar"));
		assertThat(entry.getAttributes(), hasEntry("hostName", getLocalHost().getHostName()));

		socketTransport.stop();
	}
}
