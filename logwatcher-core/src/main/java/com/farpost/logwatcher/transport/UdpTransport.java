package com.farpost.logwatcher.transport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;

import static java.lang.System.arraycopy;

public class UdpTransport implements Transport {

	private final SocketThread thread;
	private volatile int bufferSize = 100 * 1024;

	public UdpTransport(int port, TransportListener listener) throws SocketException {
		DatagramSocket socket = new DatagramSocket(port);
		thread = new SocketThread(socket, listener);
	}

	public void start() throws TransportException {
		new Thread(thread, "UDP Transport thread").start();
	}

	public void stop() throws TransportException {
		thread.stop();
	}

	public void setBufferSize(int bufferSize) {
		if (bufferSize <= 0) {
			throw new IllegalArgumentException();
		}
		this.bufferSize = bufferSize;
	}

	private class SocketThread implements Runnable {

		private DatagramSocket socket;
		private TransportListener listener;
		private final Logger log = LoggerFactory.getLogger(UdpTransport.class);
		private byte[] buffer;
		private DatagramPacket packet;

		public SocketThread(DatagramSocket socket, TransportListener listener) {
			this.socket = socket;
			this.listener = listener;
			buffer = new byte[bufferSize];
			packet = new DatagramPacket(buffer, buffer.length);
		}

		public void run() {
			while (true) {
				byte[] message;
				try {
					socket.receive(packet);
					int receivedLength = packet.getLength();
					message = new byte[receivedLength];
					arraycopy(buffer, 0, message, 0, receivedLength);
					if (log.isDebugEnabled()) {
						log.debug("Packet received: " + Arrays.toString(message));
					}
				} catch (IOException e) {
					if (socket.isClosed()) {
						log.info("Socket closed");
					} else {
						log.error("IO error occurred. Stopping transport thread", e);
					}
					break;
				}

				try {
					listener.onMessage(message);
				} catch (Exception e) {
					log.error("Listener failed at message: " + Arrays.toString(message), e);
				}
			}
		}

		public synchronized void stop() {
			socket.close();
		}
	}
}
