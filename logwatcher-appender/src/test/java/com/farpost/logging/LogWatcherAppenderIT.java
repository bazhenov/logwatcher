package com.farpost.logging;

import com.farpost.logging.marshalling.*;
import org.apache.log4j.Category;
import org.apache.log4j.Priority;
import org.apache.log4j.spi.LoggingEvent;
import org.bazhenov.logging.LogEntry;
import org.bazhenov.logging.transport.*;
import org.testng.annotations.Test;

import java.net.SocketException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class LogWatcherAppenderIT {

	@Test
	public void appenderShouldSendUdpMessages() throws SocketException, TransportException,
		InterruptedException, MarshallerException {

		Marshaller marshaller = new JDomMarshaller();
		BlockingQueue<String> queue = new LinkedBlockingQueue<String>();
		UdpTransport t = new UdpTransport(6589, new QueueAppendListener(queue));
		t.setBufferSize(100*1024);
		t.start();
		String applicationId = "foobar";
		LogWatcherAppender appender = createAppender("127.0.0.1:6589", applicationId);

		Throwable cause = new RuntimeException("Ooops");
		String message = "Сообщение";
		LoggingEvent e = new LoggingEvent("category", Category.getRoot(), Priority.DEBUG, message, cause);
		appender.doAppend(e);

		String packet = queue.take();
		LogEntry entry = marshaller.unmarshall(packet);

		assertThat(entry.getMessage(), equalTo(message));
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
