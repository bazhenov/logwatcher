package com.farpost.logging;

import com.farpost.logging.marshalling.JDomMarshaller;
import com.farpost.logging.marshalling.Marshaller;
import com.farpost.logging.marshalling.MarshallerException;
import com.farpost.timepoint.DateTime;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.bazhenov.logging.Cause;
import org.bazhenov.logging.LogEntry;
import org.bazhenov.logging.Severity;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.*;

import static java.lang.Math.min;

/**
 * It is an implementation of Log4j appender that sends log message to the LogWatcher
 * <p/>
 * Configuration:
 * <pre>
 * &lt;appender name="remote" class="com.farpost.logging.LogWatcherAppender">
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
	private Marshaller marshaller = new JDomMarshaller();

	public String getAddress() {
		return address.getHostName();
	}

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

	private void sendMessage(int port, String message) throws IOException {
		byte[] data = message.getBytes("utf8");
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

	private Cause constructCause(Throwable t) {
		StringWriter buffer = new StringWriter();
		t.printStackTrace(new PrintWriter(buffer));
		Cause cause = t.getCause() == null
			? null
			: constructCause(t.getCause());
		return new Cause(t.getClass().getSimpleName(), t.getMessage(), buffer.toString(), cause);
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
		DateTime now = DateTime.now();
		String checksum = calculateChecksum(message, location);

		LogEntry entry;
		if (event.getThrowableInformation() != null) {
			Throwable t = event.getThrowableInformation().getThrowable();
			entry = new LogEntry(now, location, message, severity, checksum, applicationId, null, constructCause(t));
		} else {
			entry = new LogEntry(now, location, message, severity, checksum, applicationId, null);
		}
		try {
			String stringMessage = marshaller.marshall(entry);
			sendMessage(port, stringMessage);
		} catch (MarshallerException e) {
			throw new RuntimeException(e);
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
