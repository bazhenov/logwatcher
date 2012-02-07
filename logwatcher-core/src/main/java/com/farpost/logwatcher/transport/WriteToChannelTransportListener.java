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
		if(!messageChannel.send(new GenericMessage<byte[]>(message), 0)) {
            throw new TransportException("Put message to queue failed");
        }
	}
}