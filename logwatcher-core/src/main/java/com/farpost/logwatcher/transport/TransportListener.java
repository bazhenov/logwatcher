package com.farpost.logwatcher.transport;

import java.net.InetAddress;

public interface TransportListener {

	void onMessage(byte[] message, InetAddress sender) throws TransportException;
}
