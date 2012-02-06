package com.farpost.logwatcher.transport;

import org.springframework.integration.MessageChannel;
import org.springframework.integration.message.GenericMessage;

public class WriteToChannelTransportListener implements TransportListener {

	private final MessageChannel messageChannel;

	public WriteToChannelTransportListener(MessageChannel messageChannel) {
		this.messageChannel = messageChannel;
	}

	@Override
	public void onMessage(byte[] message) throws TransportException {
		messageChannel.send(new GenericMessage<byte[]>(message));
	}
}