package com.farpost.logwatcher.transport;

import org.slf4j.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;

import static java.lang.System.arraycopy;
import static java.lang.Thread.currentThread;
import static org.slf4j.LoggerFactory.getLogger;

public class UdpTransport implements Transport {

	private final SocketRunnable runnable;
	private volatile int bufferSize = 100 * 1024;
	private final Logger log = getLogger(UdpTransport.class);

	public UdpTransport(int port, TransportListener listener) throws SocketException {
		DatagramSocket socket = new DatagramSocket(port);
		runnable = new SocketRunnable(socket, listener);
	}

	public void start() throws TransportException {
		Thread thread = new Thread(runnable, "UDP Transport thread");
		thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				log.error("Abnormal thread shutdown", e);
			}
		});
		thread.start();
	}

	public void stop() throws TransportException {
		runnable.stop();
	}

	public void setBufferSize(int bufferSize) {
		if (bufferSize <= 0) {
			throw new IllegalArgumentException();
		}
		this.bufferSize = bufferSize;
	}

	private class SocketRunnable implements Runnable {

		private DatagramSocket socket;
		private TransportListener listener;
		private byte[] buffer;
		private DatagramPacket packet;

		public SocketRunnable(DatagramSocket socket, TransportListener listener) {
			this.socket = socket;
			this.listener = listener;
			buffer = new byte[bufferSize];
			packet = new DatagramPacket(buffer, buffer.length);
		}

		public void run() {
			while (!currentThread().isInterrupted()) {
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
			log.info("UDP thread stopped");
		}

		public synchronized void stop() {
			socket.close();
		}
	}
}
