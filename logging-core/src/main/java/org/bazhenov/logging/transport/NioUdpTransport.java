package org.bazhenov.logging.transport;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.DatagramChannel;

import static java.lang.Thread.currentThread;
import static java.lang.Thread.interrupted;

public class NioUdpTransport implements Transport {

	private TransportListener listener;
	private InetSocketAddress address;
	private NioUdpTransport.ReadThread thread;
	private Thread threadWrapper;

	public NioUdpTransport(InetAddress addr, int port, TransportListener listener) {
		this.listener = listener;
		address = new InetSocketAddress(addr, port);
	}

	public void start() throws TransportException {
		thread = new ReadThread();
		thread.init();
		threadWrapper = new Thread(thread);
		threadWrapper.start();
	}

	public void stop() throws TransportException {
		try {
			threadWrapper.interrupt();
			threadWrapper.join();
		} catch ( InterruptedException e ) {
			throw new TransportException(e);
		}
	}

	class ReadThread implements Runnable {

		private ByteBuffer buffer;
		private DatagramChannel channel;

		public void init() throws TransportException {
			try {
				buffer = ByteBuffer.allocate(1024);

				channel = DatagramChannel.open();
				channel.configureBlocking(true);
				channel.socket().bind(address);
			} catch ( IOException e ) {
				throw new TransportException(e);
			}
		}

		public void run() {
			try {
				while ( !interrupted() ) {
					buffer.clear();
					channel.receive(buffer);
					String message = new String(buffer.array(), 0, buffer.position());
					listener.onMessage(message);
				}
			} catch ( ClosedByInterruptException e ) {
				currentThread().interrupt();
			} catch ( IOException e ) {
				throw new RuntimeException(e);
			} catch ( TransportException e ) {
				throw new RuntimeException(e);
			}
		}
	}
}
