package com.farpost.logwatcher.web.controller;

import com.farpost.logwatcher.Cause;
import com.farpost.logwatcher.LogEntry;
import com.farpost.logwatcher.LogEntryImpl;
import com.farpost.logwatcher.Severity;
import com.farpost.logwatcher.marshalling.Jaxb2Marshaller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.net.InetAddress.getLocalHost;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Controller
@RequestMapping("/system")
public class SystemController {

	@Autowired
	private Jaxb2Marshaller marshaller;

	private final DatagramSocket socket;

	private final int port;

	public SystemController(int port) throws SocketException {
		this.port = port;
		socket = new DatagramSocket();
	}

	@RequestMapping(value = "/generate-message", method = GET)
	public String form() {
		return "system/generate-message";
	}

	@RequestMapping(value = "/generate-message", method = POST)
	@ResponseBody
	public String generateMessage(@RequestParam(required = false, defaultValue = "") String applicationId,
																@RequestParam(required = false, defaultValue = "") String causeTitle,
																@RequestParam(required = false, defaultValue = "") String checksum,
																@RequestParam(required = false, defaultValue = "") String group,
																@RequestParam(required = false, defaultValue = "") String title)
		throws IOException {
		Cause cause = !isNullOrEmpty(causeTitle)
			? new Cause(new Exception(causeTitle))
			: null;
		Map<String, String> attributes = Collections.emptyMap();
		LogEntry entry = new LogEntryImpl(new Date(), group, title, Severity.error, checksum, applicationId, attributes,
			cause);
		byte[] bytes = marshaller.marshall(entry);
		DatagramPacket packet = new DatagramPacket(bytes, bytes.length, getLocalHost(), port);
		synchronized (socket) {
			socket.send(packet);
		}
		return "Ok";
	}
}
