package com.farpost.logwatcher.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import com.farpost.logwatcher.QueueAppendListener;
import com.farpost.logwatcher.marshalling.Jaxb2Marshaller;
import com.farpost.logwatcher.marshalling.Marshaller;
import com.farpost.logwatcher.transport.TransportException;
import com.farpost.logwatcher.transport.UdpTransport;
import com.farpost.logwatcher.LogEntry;
import com.farpost.logwatcher.Severity;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.SocketException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.slf4j.LoggerFactory.getLogger;

public class LogWatcherAppenderTest {

	private BlockingQueue<String> messages;
	private Marshaller marshaller = new Jaxb2Marshaller();
	private int port = 6590;
	private UdpTransport transport;
	private String applicationId = "foobar";
	private Appender<ILoggingEvent> appender;
	private Logger root = (Logger) getLogger(LogWatcherAppender.class);
	private Level level;

	@BeforeMethod
	public void setUp() throws SocketException, TransportException {
		messages = new LinkedBlockingQueue<String>();
		transport = new UdpTransport(port, new QueueAppendListener(messages));
		transport.start();

		appender = createAppender("0.0.0.0:" + port, applicationId);
		appender.start();
		root.addAppender(appender);
		level = root.getLevel();
		root.setLevel(Level.DEBUG);
	}

	@AfterMethod
	public void tearDown() throws TransportException {
		root.detachAppender(appender);
		root.setLevel(level);
		appender.stop();
		transport.stop();
	}

	@Test
	public void appenderShouldSendUdpMessages() throws InterruptedException {
		Throwable cause = new RuntimeException("This exception is generated intentionally");
		String message = "Сообщение";

		getLogger(LogWatcherAppender.class).debug(message, cause);

		String lastMessage = messages.poll(1, TimeUnit.SECONDS);
		LogEntry entry = marshaller.unmarshall(lastMessage);

		assertThat(entry.getMessage(), equalTo(message));
		assertThat(entry.getSeverity(), equalTo(Severity.debug));
		assertThat(entry.getApplicationId(), equalTo(applicationId));
		assertThat(entry.getCause().getMessage(), equalTo("This exception is generated intentionally"));
		assertThat(entry.getCause().getType(), equalTo(RuntimeException.class.getSimpleName()));
	}

	private static Appender<ILoggingEvent> createAppender(String address, String applicationId) throws SocketException {
		LogWatcherAppender appender = new LogWatcherAppender();
		appender.setAddress(address);
		appender.setApplicationId(applicationId);
		return appender;
	}
}
