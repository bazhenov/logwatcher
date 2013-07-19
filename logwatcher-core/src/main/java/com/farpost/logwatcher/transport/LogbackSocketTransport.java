package com.farpost.logwatcher.transport;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.LoggerContextVO;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import com.farpost.logwatcher.Cause;
import com.farpost.logwatcher.LogEntry;
import com.farpost.logwatcher.LogEntryImpl;
import com.farpost.logwatcher.Severity;
import com.google.common.hash.HashFunction;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.hash.Hashing.md5;
import static java.lang.Thread.currentThread;
import static java.util.Collections.emptyMap;

public class LogbackSocketTransport implements Transport {

	private final int port;
	private final LogEntryListener listener;
	private final HashFunction hashFunction = md5();
	private final static Map<String, String> EMPTY = emptyMap();
	private Acceptor acceptor;
	private Logger log = LoggerFactory.getLogger(LogbackSocketTransport.class);

	public LogbackSocketTransport(int port, LogEntryListener listener) throws IOException {
		this.port = port;
		this.listener = checkNotNull(listener);
	}

	@Override
	public void start() throws TransportException {
		try {
			ServerSocket serverSocket = new ServerSocket(port);
			acceptor = new Acceptor(serverSocket);
			new Thread(acceptor).start();
		} catch (IOException e) {
			throw new TransportException(e);
		}

	}

	@Override
	public void stop() throws TransportException {
		try {
			acceptor.stop();
		} catch (IOException e) {
			throw new TransportException(e);
		}
	}

	private LogEntry createLogEntry(ILoggingEvent event) {
		DateTime dateTime = new DateTime(event.getTimeStamp());
		IThrowableProxy proxy = event.getThrowableProxy();
		Cause cause = proxy != null
			? new Cause(proxy.getClassName(), proxy.getMessage(), formatStackTrace(proxy))
			: null;
		String checksum = hashFunction.hashString(event.getMessage()).toString();

		Map<String, String> attributes = newHashMap();
		Map<String, String> mdc = event.getMDCPropertyMap();
		if (mdc != null)
			attributes.putAll(mdc);
		LoggerContextVO context = checkNotNull(event.getLoggerContextVO(), "Context is null");

		Map<String, String> propertyMap = context.getPropertyMap();
		if (!attributes.containsKey("hostName") && propertyMap.containsKey("HOSTNAME"))
			attributes.put("hostName", propertyMap.get("HOSTNAME"));

		return new LogEntryImpl(dateTime, event.getLoggerName(), event.getFormattedMessage(), forLevel(event.getLevel()),
			checksum, context.getName(), attributes, cause);
	}

	private static Severity forLevel(Level level) {
		if (level == Level.DEBUG)
			return Severity.debug;
		if (level == Level.WARN)
			return Severity.warning;
		if (level == Level.ERROR)
			return Severity.error;
		if (level == Level.TRACE)
			return Severity.trace;
		if (level == Level.INFO)
			return Severity.info;
		return Severity.error;
	}

	private static String formatStackTrace(IThrowableProxy proxy) {
		StringBuilder builder = new StringBuilder();
		for (StackTraceElementProxy i : proxy.getStackTraceElementProxyArray()) {
			builder.append(i.getSTEAsString()).append('\n');
		}
		return builder.toString().trim();
	}

	private class Acceptor implements Runnable {

		private final ServerSocket socket;

		private Acceptor(ServerSocket socket) {
			this.socket = checkNotNull(socket);
		}

		@Override
		public void run() {
			while (true) {
				try {
					Socket client = socket.accept();
					new Thread(new Reader(client, listener)).start();
				} catch (SocketException e) {
					if (!e.getMessage().equalsIgnoreCase("socket closed")) {
						// it's the only known way to check if accept failed because of socket was closed
						log.error("Exception whice accepting client", e);
					}
					break;
				} catch (IOException e) {
					log.error("Exception whice accepting client", e);
					break;
				}
			}
		}

		public void stop() throws IOException {
			socket.close();
		}
	}

	private class Reader implements Runnable {

		private final Socket client;
		private final LogEntryListener listener;

		public Reader(Socket client, LogEntryListener listener) {
			this.client = checkNotNull(client);
			this.listener = checkNotNull(listener);
		}

		@Override
		public void run() {
			try {
				ObjectInputStream is = new ObjectInputStream(client.getInputStream());
				while (!currentThread().isInterrupted()) {
					ILoggingEvent event = (ILoggingEvent) is.readObject();
					listener.onEntry(createLogEntry(event));
				}
			} catch (EOFException ignored) {
			} catch (IOException e) {
				log.error(e.toString());
			} catch (ClassNotFoundException e) {
				log.error(e.toString());
			}
		}
	}
}
