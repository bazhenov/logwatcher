package com.farpost.logwatcher.transport;

import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicLong;

import static com.google.common.base.Preconditions.checkNotNull;

public class WriteToChannelTransportListener implements TransportListener {

	public static AtomicLong rejectedByOverflowCount = new AtomicLong();

	public static final String SENDER_ADDRESS = "SenderAddress";
	private final MessageChannel messageChannel;

	public WriteToChannelTransportListener(MessageChannel messageChannel) {
		this.messageChannel = checkNotNull(messageChannel);
	}

	@Override
	public void onMessage(byte[] message, InetAddress sender) throws TransportException {
		Message<byte[]> enveloper = MessageBuilder
			.withPayload(message)
			.setHeader(SENDER_ADDRESS, sender)
			.build();
		if (!messageChannel.send(enveloper, 0)) {
			rejectedByOverflowCount.incrementAndGet();
			throw new TransportException("Queue overflowed");
		}
	}
}