package org.bazhenov.logging.transport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

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

		public SocketThread(DatagramSocket socket, TransportListener listener) {
			this.socket = socket;
			this.listener = listener;
		}

		public void run() {
			while (true) {
				String message;
				try {
					byte[] buffer = new byte[bufferSize];
					DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
					socket.receive(packet);
					byte[] data = packet.getData();
					message = new String(data, 0, packet.getLength(), "utf8");
					if (log.isDebugEnabled()) {
						log.debug("Packet received: " + message);
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
					log.error("Listener failed at message: " + message, e);
				}
			}
		}

		public synchronized void stop() {
			socket.close();
		}
	}
}
