package com.farpost.logwatcher.transport;

import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.support.MessageBuilder;

import java.net.InetAddress;

import static com.google.common.base.Preconditions.checkNotNull;

public class WriteToChannelTransportListener implements TransportListener {

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
			throw new TransportException("Queue overflowed");
		}
	}
}