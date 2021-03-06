package com.farpost.logwatcher.log4j;

import com.farpost.logwatcher.Cause;
import com.farpost.logwatcher.LogEntry;
import com.farpost.logwatcher.LogEntryImpl;
import com.farpost.logwatcher.Severity;
import com.farpost.logwatcher.marshalling.Jaxb2Marshaller;
import com.farpost.logwatcher.marshalling.Marshaller;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;

import java.io.IOException;
import java.net.*;
import java.util.Date;

import static java.lang.Math.min;

/**
 * It is an implementation of Log4j appender that sends log message to the LogWatcher
 * <p/>
 * Configuration:
 * <pre>
 * &lt;appender name="remote" class="com.farpost.logwatcher.log4j.LogWatcherAppender">
 *   &lt;param name="address" value="aux2.srv.loc:6578" />
 *   &lt;param name="threshold" value="WARNING" />
 *   &lt;param name="applicationId" value="serviceName" />
 * &lt;/appender>
 * </pre>
 */
public class LogWatcherAppender extends AppenderSkeleton {

	private int port = 6578;
	private DatagramSocket socket;
	private InetAddress address;
	private String applicationId = "anonymous";
	private Marshaller marshaller = new Jaxb2Marshaller();

	@SuppressWarnings("UnusedDeclaration")
	public String getAddress() {
		return address.getHostName();
	}

	@SuppressWarnings("UnusedDeclaration")
	public String getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}

	public void setAddress(String address) {
		try {
			String parts[] = address.split(":");
			this.address = InetAddress.getByName(parts[0]);
			this.port = Integer.parseInt(parts[1]);
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
	}

	private void sendMessage(int port, byte[] data) throws IOException {
		DatagramPacket packet = new DatagramPacket(data, data.length);
		packet.setAddress(address);
		packet.setPort(port);
		socket.send(packet);
	}

	private Severity getSererity(Level level) {
		if (level == Level.OFF) {
			return Severity.error;
		} else if (level == Level.FATAL) {
			return Severity.error;
		} else if (level == Level.ERROR) {
			return Severity.error;
		} else if (level == Level.WARN) {
			return Severity.warning;
		} else if (level == Level.INFO) {
			return Severity.info;
		} else if (level == Level.DEBUG) {
			return Severity.debug;
		}
		return Severity.trace;
	}

	private String calculateChecksum(String message, String location) {
		String checksum = (message.replaceAll(" ", "") + location).replaceAll("\\.", "");
		return checksum.substring(0, min(32, checksum.length()));
	}

	public void activateOptions() {
		try {
			socket = new DatagramSocket();
		} catch (SocketException e) {
			throw new RuntimeException(e);
		}
	}

	protected void append(LoggingEvent event) {
		String message = event.getRenderedMessage();
		Level level = event.getLevel();
		String location = event.getLocationInformation().getClassName();
		Severity severity = getSererity(level);
		Date now = new Date();
		String checksum = calculateChecksum(message, location);

		LogEntry entry;
		if (event.getThrowableInformation() != null) {
			Throwable t = event.getThrowableInformation().getThrowable();
			entry = new LogEntryImpl(now, location, message, severity, checksum, applicationId, null, new Cause(t));
		} else {
			entry = new LogEntryImpl(now, location, message, severity, checksum, applicationId, null);
		}
		try {
			byte[] bytes = marshaller.marshall(entry);
			sendMessage(port, bytes);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void close() {
		socket.close();
	}

	public boolean requiresLayout() {
		return true;
	}
}
