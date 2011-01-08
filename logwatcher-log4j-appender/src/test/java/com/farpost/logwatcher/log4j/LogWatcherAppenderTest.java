package com.farpost.logwatcher.log4j;

import com.farpost.logwatcher.LogEntry;
import com.farpost.logwatcher.QueueAppendListener;
import com.farpost.logwatcher.Severity;
import com.farpost.logwatcher.marshalling.Jaxb2Marshaller;
import com.farpost.logwatcher.marshalling.Marshaller;
import com.farpost.logwatcher.transport.TransportException;
import com.farpost.logwatcher.transport.UdpTransport;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.SocketException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class LogWatcherAppenderTest {

	private BlockingQueue<byte[]> messages;
	private Marshaller marshaller = new Jaxb2Marshaller();
	private int port = 6590;
	private UdpTransport transport;
	private String applicationId = "foobar";
	private LogWatcherAppender appender;
	private Logger root = Logger.getRootLogger();
	private Level oldLevel;

	@BeforeMethod
	public void setUp() throws SocketException, TransportException {
		messages = new LinkedBlockingQueue<byte[]>();
		transport = new UdpTransport(port, new QueueAppendListener(messages));
		transport.setBufferSize(100 * 1024);
		transport.start();

		appender = createAppender("0.0.0.0:" + port, applicationId);

		root.addAppender(appender);
		oldLevel = root.getLevel();
		root.setLevel(Level.DEBUG);
	}

	@AfterMethod
	public void tearDown() throws TransportException {
		transport.stop();

		root.removeAppender(appender);
		root.setLevel(oldLevel);
	}

	@Test
	public void appenderShouldSendUdpMessages() throws InterruptedException {
		Throwable cause = new RuntimeException("Ooops");
		String message = "Сообщение";

		Logger.getLogger(LogWatcherAppender.class).debug(message, cause);

		byte[] lastMessage = messages.poll(1, TimeUnit.SECONDS);
		LogEntry entry = marshaller.unmarshall(lastMessage);

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
