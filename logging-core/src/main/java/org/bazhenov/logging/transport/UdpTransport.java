package org.bazhenov.logging.transport;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.*;

public class UdpTransport implements Transport {

	private final SocketThread thread;
	private volatile int bufferSize = 4096;

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
		if ( bufferSize <= 0 ) {
			throw new IllegalArgumentException();
		}
		this.bufferSize = bufferSize;
	}

	private class SocketThread implements Runnable {

		private DatagramSocket socket;
		private TransportListener listener;
		private final Logger log = Logger.getLogger(UdpTransport.class);

		public SocketThread(DatagramSocket socket, TransportListener listener) {
			this.socket = socket;
			this.listener = listener;
		}

		public void run() {
			while ( true ) {
				String message = null;
				try {
					byte[] buffer = new byte[bufferSize];
					DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
					socket.receive(packet);
					byte[] data = packet.getData();
					message = new String(data, 0, packet.getLength(), "utf8");
					if ( log.isDebugEnabled() ) {
						log.debug("Packet received: " + message);
					}
					listener.onMessage(message);
				} catch ( IOException e ) {
					log.warn("Stopping transport thread", e);
					break;
				} catch ( Exception e ) {
					log.error("Listener failed at message: " + message, e);
				}
			}
		}

		public synchronized void stop() {
			socket.close();
		}
	}
}
