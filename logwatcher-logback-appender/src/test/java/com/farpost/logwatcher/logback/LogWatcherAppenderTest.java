package com.farpost.logwatcher.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import com.farpost.logwatcher.LogEntry;
import com.farpost.logwatcher.QueueAppendListener;
import com.farpost.logwatcher.Severity;
import com.farpost.logwatcher.marshalling.Jaxb2Marshaller;
import com.farpost.logwatcher.marshalling.Marshaller;
import com.farpost.logwatcher.transport.TransportException;
import com.farpost.logwatcher.transport.UdpTransport;
import org.slf4j.MDC;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.random;
import static java.lang.Thread.currentThread;
import static java.net.InetAddress.getLocalHost;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.notNullValue;
import static org.slf4j.LoggerFactory.getLogger;

public class LogWatcherAppenderTest {

	private BlockingQueue<byte[]> messages;
	private Marshaller marshaller = new Jaxb2Marshaller();
	private UdpTransport transport;
	private String applicationId = "foobar";
	private Appender<ILoggingEvent> appender;
	private Logger root = (Logger) getLogger(LogWatcherAppender.class);
	private Level level;

	@BeforeMethod
	public void setUp() throws IOException, TransportException {
		int port = findUnboundPort();
		try {
			messages = new LinkedBlockingQueue<byte[]>();
			transport = new UdpTransport(port, new QueueAppendListener(messages));
			transport.start();
		} catch (BindException e) {
			throw new RuntimeException("Unable bind UdpTransport to port " + port);
		}

		appender = createAppender("127.1:" + port, applicationId);
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

		root.debug(message, cause);

		LogEntry entry = getLastMessage();

		assertThat(entry.getMessage(), equalTo(message));
		assertThat(entry.getSeverity(), equalTo(Severity.debug));
		assertThat(entry.getApplicationId(), equalTo(applicationId));
		assertThat(entry.getCause().getMessage(), equalTo("This exception is generated intentionally"));
		assertThat(entry.getCause().getType(), equalTo(RuntimeException.class.getName()));
	}

	@Test
	public void appenderShouldProcessPlaceholders() throws InterruptedException {
		root.debug("Hi there, {}", "to you");

		LogEntry entry = getLastMessage();
		assertThat(entry.getMessage(), equalTo("Hi there, to you"));
	}

	@Test
	public void appenderShouldCreateChecksumFromUnformattedMessageOnly() throws InterruptedException {
		root.debug("I decided to write you {}", "foo");
		String firstCheckSum = getLastMessage().getChecksum();

		root.debug("I decided to write you {}", "bar");
		String secondCheckSum = getLastMessage().getChecksum();

		assertThat("Checksum should be same for identical unformatted messages",
			firstCheckSum, equalTo(secondCheckSum));

		root.debug("Another log");
		String thirdCheckSum = getLastMessage().getChecksum();
		assertThat("Checksum should be different for different unformated messages",
			thirdCheckSum, not(equalTo(secondCheckSum)));
	}

	@Test
	public void appenderShouldBeAbleToSendMessagesWithoutException() throws InterruptedException {
		String message = "Debug message";
		root.debug(message);

		LogEntry entry = getLastMessage();

		assertThat(entry.getMessage(), equalTo(message));
		assertThat(entry.getChecksum(), nullValue());
	}

	@Test
	public void appenderShouldSendMdcValuesAsWell() throws InterruptedException, UnknownHostException {
		MDC.put("url", "/foo?a=1");
		root.error("Request processing failed");

		LogEntry entry = getLastMessage();

		assertThat(entry.getAttributes(), hasEntry("url", "/foo?a=1"));
		assertThat(entry.getAttributes(), hasEntry("host", getLocalHost().getHostName()));
	}

	/**
	 * Возвращает последнее сообщение принятое от logback UDP appender'а. Если сообщение еще не принято, то
	 * данный метод будет ждать получения сообщения в течении одной секунды.
	 *
	 * @return последнее принятое сообщение
	 * @throws InterruptedException если истек timeout ожидания
	 */
	private LogEntry getLastMessage() throws InterruptedException {
		byte[] lastMessage = messages.poll(1, TimeUnit.SECONDS);
		assertThat("No message received", lastMessage, notNullValue());
		return marshaller.unmarshall(lastMessage);
	}

	private static Appender<ILoggingEvent> createAppender(String address, String applicationId)
		throws SocketException, UnknownHostException {
		LogWatcherAppender appender = new LogWatcherAppender();
		appender.setAddress(address);
		appender.setApplicationId(applicationId);
		return appender;
	}

	/**
	 * @return случайный свободный порт в диапазоне от 1025 до 65535
	 * @throws java.io.IOException в случае ошибки при закрытии временного сокета
	 */
	public static int findUnboundPort() throws IOException {
		int port;
		while (!currentThread().isInterrupted()) {
			// pick a random port in 1025-65535 range
			port = (int) ((random() * (65535 - 1025)) + 1025);
			Socket socket = null;
			try {
				socket = new Socket();
				socket.bind(new InetSocketAddress(port));
			} catch (IOException e) {
				continue;
			} finally {
				if (socket != null) {
					socket.close();
				}
			}
			return port;
		}
		throw new IllegalStateException("Someone interrupts me. So I give up.");
	}
}
