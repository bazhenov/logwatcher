package com.farpost.logwatcher.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import com.farpost.logging.marshalling.Jaxb2Marshaller;
import com.farpost.logging.marshalling.Marshaller;
import com.farpost.timepoint.DateTime;
import org.bazhenov.logging.Cause;
import org.bazhenov.logging.LogEntry;
import org.bazhenov.logging.LogEntryImpl;
import org.bazhenov.logging.Severity;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.*;
import java.util.HashMap;

import static java.lang.Integer.parseInt;

public class LogWatcherAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {

	private static final int DEFAULT_PORT = 6578;
	private InetAddress address;
	private int port;
	private String applicationId;
	private DatagramSocket socket;
	private final Marshaller marshaller;

	public LogWatcherAppender() {
		marshaller = new Jaxb2Marshaller();
	}

	@Override
	protected void append(ILoggingEvent event) {
		try {
			DateTime time = new DateTime(event.getTimeStamp());
			Severity severity = severity(event.getLevel());
			ThrowableProxy throwableProxy = (ThrowableProxy) event.getThrowableProxy();
			Cause cause = constructCause(throwableProxy.getThrowable());

			LogEntry entry = new LogEntryImpl(time, event.getLoggerName(), event.getMessage(), severity, null, applicationId,
				new HashMap<String, String>(), cause);

			byte[] data = marshaller.marshall(entry).getBytes("utf8");
			DatagramPacket packet = new DatagramPacket(data, data.length);

			socket.send(packet);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private Cause constructCause(Throwable t) {
		StringWriter buffer = new StringWriter();
		t.printStackTrace(new PrintWriter(buffer));
		Cause cause = t.getCause() == null
			? null
			: constructCause(t.getCause());
		return new Cause(t.getClass().getSimpleName(), t.getMessage(), buffer.toString(), cause);
	}

	private Severity severity(Level level) {
		switch (level.levelInt) {
			case Level.ERROR_INT:
				return Severity.error;
			case Level.DEBUG_INT:
				return Severity.debug;
			case Level.INFO_INT:
				return Severity.info;
			case Level.WARN_INT:
				return Severity.warning;
			case Level.TRACE_INT:
				return Severity.trace;
			default:
				return Severity.error;
		}
	}

	@Override
	public synchronized void start() {
		if (!isStarted()) {
			try {
				socket = new DatagramSocket();
				socket.connect(address, port);
			} catch (SocketException e) {
				throw new RuntimeException(e);
			}
		}
		super.start();
	}

	@Override
	public synchronized void stop() {
		if (isStarted()) {
			socket.close();
			socket = null;
		}
		super.stop();
	}

	public void setAddress(String address) {
		try {
			String parts[] = address.split(":");

			this.address = InetAddress.getByName(parts[0]);
			this.port = parts.length > 1
				? parseInt(parts[1])
				: DEFAULT_PORT;
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
	}

	public void setApplicationId(String applicationId) {
		if (applicationId == null || applicationId.isEmpty()) {
			throw new IllegalArgumentException("Empty application id is given");
		}
		this.applicationId = applicationId;
	}
}
