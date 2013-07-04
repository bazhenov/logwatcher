package com.farpost.logwatcher.transport;

import com.farpost.logwatcher.marshalling.Marshaller;
import com.google.common.io.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.annotation.ServiceActivator;

import java.io.File;
import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;

public class DeserializeAndWriteToStorage {

	private final Marshaller marshaller;
	private final LogEntryListener listener;

	private static final Logger log = LoggerFactory.getLogger(DeserializeAndWriteToStorage.class);

	public DeserializeAndWriteToStorage(Marshaller marshaller, LogEntryListener listener) {
		this.marshaller = checkNotNull(marshaller);
		this.listener = checkNotNull(listener);
	}

	@ServiceActivator
	public void write(byte[] message) throws IOException {
		try {
			listener.onEntry(marshaller.unmarshall(message));
		} catch (RuntimeException e) {
			log.error(e.getMessage(), e);
			if (log.isDebugEnabled()) {
				File tempFile = File.createTempFile("logwatcher", "packet");
				Files.write(message, tempFile);
				log.debug("Packet processing failed. Packet dump at {}", tempFile.getAbsolutePath());
			}
		}
	}

}
