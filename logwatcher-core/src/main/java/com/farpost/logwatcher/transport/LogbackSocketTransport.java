package com.farpost.logwatcher.transport;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggerContextVO;
import ch.qos.logback.classic.spi.ThrowableProxy;
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
import static com.google.common.hash.Hashing.md5;
import static java.lang.Thread.currentThread;

public class LogbackSocketTransport implements Transport {

	private final int port;
	private final LogEntryListener listener;
	private final HashFunction hashFunction = md5();
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
		ThrowableProxy proxy = (ThrowableProxy) event.getThrowableProxy();
		Cause cause = proxy != null
			? new Cause(proxy.getThrowable())
			: null;
		String checksum = hashFunction.hashString(event.getMessage()).toString();
		Map<String, String> attributes = event.getMDCPropertyMap();
		LoggerContextVO context = event.getLoggerContextVO();
		if (!attributes.containsKey("hostName") && context.getPropertyMap().containsKey("HOSTNAME")) {
			attributes.put("hostName", context.getPropertyMap().get("HOSTNAME"));
		}
		return new LogEntryImpl(dateTime, event.getLoggerName(), event.getFormattedMessage(),
			Severity.forName(event.getLevel().toString()),
			checksum, context.getName(), attributes, cause);
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
