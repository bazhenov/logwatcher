package com.farpost.logwatcher.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import com.farpost.logwatcher.Cause;
import com.farpost.logwatcher.LogEntry;
import com.farpost.logwatcher.LogEntryImpl;
import com.farpost.logwatcher.Severity;
import com.farpost.logwatcher.marshalling.Jaxb2Marshaller;
import com.farpost.logwatcher.marshalling.Marshaller;

import java.io.IOException;
import java.net.*;
import java.util.Date;
import java.util.Map;

import static java.lang.Integer.parseInt;
import static java.lang.Math.min;

public class LogWatcherAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {

	private static final int DEFAULT_PORT = 6578;
	private InetAddress address;
	private int port;
	private String applicationId;
	private DatagramSocket socket;
	private final Marshaller marshaller;
	private final Object socketLock = new Object();

	public LogWatcherAppender() {
		marshaller = new Jaxb2Marshaller();
	}

	@Override
	protected void append(ILoggingEvent event) {
		try {
			Date time = new Date(event.getTimeStamp());
			Severity severity = severity(event.getLevel());
			ThrowableProxy proxy = (ThrowableProxy) event.getThrowableProxy();
			Cause cause = proxy != null
				? new Cause(proxy.getThrowable())
				: null;

			Map<String, String> attributes = event.getMDCPropertyMap();
			LogEntry entry = new LogEntryImpl(time, event.getLoggerName(), event.getFormattedMessage(), severity,
				calculateChecksum(event.getMessage()),
				applicationId, attributes, cause);

			byte[] data = marshaller.marshall(entry);
			DatagramPacket packet = new DatagramPacket(data, data.length);

			synchronized (socketLock) {
				if (socket == null) {
					return;
				}
				socket.send(packet);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private String calculateChecksum(String message) {
		String checksum = message.replaceAll("[ |{|}]", "");
		return checksum.substring(0, min(32, checksum.length()));
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
	public void start() {
		synchronized (socketLock) {
			if (!isStarted()) {
				try {
					socket = new DatagramSocket();
					socket.connect(address, port);
				} catch (SocketException e) {
					throw new RuntimeException(e);
				}
				super.start();
			}
		}
	}

	@Override
	public void stop() {
		synchronized (socketLock) {
			if (isStarted()) {
				socket.close();
				socket = null;
				super.stop();
			}
		}
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
