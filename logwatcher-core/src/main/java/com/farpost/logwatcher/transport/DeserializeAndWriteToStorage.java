package com.farpost.logwatcher.transport;

import com.farpost.logwatcher.LogEntry;
import com.farpost.logwatcher.marshalling.Marshaller;
import com.google.common.io.Files;
import org.slf4j.Logger;
import org.springframework.messaging.Message;
import org.springframework.integration.annotation.ServiceActivator;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;

import static com.farpost.logwatcher.transport.WriteToChannelTransportListener.SENDER_ADDRESS;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Throwables.getRootCause;
import static java.io.File.createTempFile;
import static org.slf4j.LoggerFactory.getLogger;

public class DeserializeAndWriteToStorage {

	private final Marshaller marshaller;
	private final LogEntryListener listener;

	private static final Logger log = getLogger(DeserializeAndWriteToStorage.class);

	public DeserializeAndWriteToStorage(Marshaller marshaller, LogEntryListener listener) {
		this.marshaller = checkNotNull(marshaller);
		this.listener = checkNotNull(listener);
	}

	@ServiceActivator
	public void write(Message<byte[]> envelope) throws IOException {
		try {
			LogEntry entry = marshaller.unmarshall(envelope.getPayload());
			try {
				listener.onEntry(entry);
			} catch (RuntimeException e) {
				log.error("Unable to persist entry", e);
				makeADumpIfNeeded(envelope);
			}
		} catch (RuntimeException e) {
			log.error("Failed to read packet", getRootCause(e));
			makeADumpIfNeeded(envelope);
		}
	}

	private static void makeADumpIfNeeded(Message<byte[]> envelope) throws IOException {
		if (log.isDebugEnabled()) {
			InetAddress sender = envelope.getHeaders().get(SENDER_ADDRESS, InetAddress.class);
			File tempFile = createTempFile("logwatcher", "packet");
			Files.write(envelope.getPayload(), tempFile);
			log.debug("Packet processing failed. Sender is {}. Packet dump at {}", sender, tempFile.getAbsolutePath());
		}
	}
}
