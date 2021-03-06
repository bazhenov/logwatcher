package com.farpost.logwatcher.transport;

import org.testng.annotations.Test;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class UdpTransportIT {

	@Test
	public void transportMustDeliverMessages()
		throws InterruptedException, TransportException, IOException {
		Listener listener = new Listener();
		Transport t = new UdpTransport(30054, listener);
		t.start();

		sendMessage(30054, "msg");
		//noinspection SynchronizationOnLocalVariableOrMethodParameter
		synchronized (listener) {
			listener.wait(1000);
			assertThat(listener.getData(), equalTo("msg".getBytes("utf8")));
		}
		t.stop();
	}

	private void sendMessage(int port, String message) throws IOException {
		DatagramSocket socket = new DatagramSocket();
		DatagramPacket packet = new DatagramPacket(message.getBytes(), message.length());
		InetAddress address;
		try {
			address = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
		packet.setAddress(address);
		packet.setPort(port);
		socket.send(packet);
	}

	static class Listener implements TransportListener {

		private byte[] data;

		public void onMessage(byte[] message, InetAddress sender) throws TransportException {
			data = message;
			synchronized (this) {
				notify();
			}
		}

		public byte[] getData() {
			return data;
		}
	}
}
