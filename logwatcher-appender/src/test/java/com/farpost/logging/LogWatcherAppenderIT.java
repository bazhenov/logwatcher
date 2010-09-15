package com.farpost.logging;

import com.farpost.logging.marshalling.JDomMarshaller;
import com.farpost.logging.marshalling.Marshaller;
import com.farpost.logging.marshalling.MarshallerException;
import org.apache.log4j.Category;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.apache.log4j.spi.LoggingEvent;
import org.bazhenov.logging.LogEntry;
import org.bazhenov.logging.Severity;
import org.bazhenov.logging.transport.TransportException;
import org.bazhenov.logging.transport.UdpTransport;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.SocketException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class LogWatcherAppenderIT {

	private BlockingQueue<String> messages;
	private Marshaller marshaller;
	private int port = 6590;
	private UdpTransport transport;

	@BeforeMethod
	public void setUp() throws SocketException, TransportException {
		messages = new LinkedBlockingQueue<String>();
		transport = new UdpTransport(port, new QueueAppendListener(messages));
		transport.setBufferSize(100 * 1024);
		transport.start();
		marshaller = new JDomMarshaller();
	}

	@AfterMethod
	public void tearDown() throws TransportException {
		transport.stop();
	}

	@Test
	public void appenderShouldSendUdpMessages() throws InterruptedException, MarshallerException {
		String applicationId = "foobar";
		LogWatcherAppender appender = createAppender("0.0.0.0:" + port, applicationId);

		Throwable cause = new RuntimeException("Ooops");
		String message = "Сообщение";

		Logger rootLogger = Logger.getRootLogger();
		rootLogger.addAppender(appender);

		Logger.getLogger(LogWatcherAppender.class).debug(message, cause);

		String lastMessage = messages.poll(1, TimeUnit.SECONDS);
		LogEntry entry = marshaller.unmarshall(lastMessage);

		rootLogger.removeAppender(appender);

		assertThat(entry.getMessage(), equalTo(message));
		assertThat(entry.getSeverity(), equalTo(Severity.debug));
		assertThat(entry.getApplicationId(), equalTo(applicationId));
		assertThat(entry.getCause().getMessage(), equalTo("Ooops"));
		assertThat(entry.getCause().getType(), equalTo(RuntimeException.class.getSimpleName()));
	}

	private static LogWatcherAppender createAppender(String address, String applicationId) {
		LogWatcherAppender appender = new LogWatcherAppender();
		appender.setAddress(address);
		appender.setApplicationId(applicationId);
		appender.activateOptions();
		return appender;
	}
}
