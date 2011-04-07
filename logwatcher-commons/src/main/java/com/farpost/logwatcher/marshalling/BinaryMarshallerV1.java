package com.farpost.logwatcher.marshalling;

import com.farpost.logwatcher.Cause;
import com.farpost.logwatcher.LogEntry;
import com.farpost.logwatcher.LogEntryImpl;
import com.farpost.logwatcher.Severity;
import com.farpost.timepoint.DateTime;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * | Date | Severity | Checksum | Application id | Message | Category | Attributes | Cause presents flag | Cause?
 */
public class BinaryMarshallerV1 implements Marshaller {

	@Override
	public byte[] marshall(LogEntry entry) throws MarshallerException {
		try {
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
			ObjectOutputStream objectStream = new ObjectOutputStream(byteStream);

			objectStream.writeByte(1);
			objectStream.writeLong(entry.getDate().asTimestamp());
			objectStream.writeByte(entry.getSeverity().getCode());
			objectStream.writeUTF(entry.getChecksum());
			objectStream.writeUTF(entry.getApplicationId());
			objectStream.writeUTF(entry.getMessage());
			objectStream.writeUTF(entry.getCategory());

			writeAttributes(objectStream, entry.getAttributes());
			writeCause(objectStream, entry.getCause());

			objectStream.close();
			byteStream.close();
			return byteStream.toByteArray();
		} catch (IOException e) {
			throw new MarshallerException(e);
		}
	}

	private void writeAttributes(ObjectOutputStream objectStream, Map<String, String> attributes) throws IOException {
		objectStream.writeInt(attributes.size());
		for (Map.Entry<String, String> row : attributes.entrySet()) {
			objectStream.writeUTF(row.getKey());
			objectStream.writeUTF(row.getValue());
		}
	}

	@Override
	public LogEntry unmarshall(byte[] data) throws MarshallerException {
		try {
			ObjectInputStream objectStream = new ObjectInputStream(new ByteArrayInputStream(data));

			checkVersion(objectStream.readByte());
			DateTime date = new DateTime(objectStream.readLong());
			Severity severity = Severity.forCode(objectStream.readByte());
			String checksum = objectStream.readUTF();
			String applicationId = objectStream.readUTF();
			String message = objectStream.readUTF();
			String category = objectStream.readUTF();

			Map<String, String> attributes = readAttributes(objectStream);
			Cause cause = readCause(objectStream);

			return new LogEntryImpl(date, category, message, severity, checksum, applicationId, attributes, cause);
		} catch (IOException e) {
			throw new MarshallerException(e);
		}
	}

	private void checkVersion(int version) {
		if (version != 1) {
			throw new IllegalArgumentException("Invalid format version: " + version);
		}
	}

	private Map<String, String> readAttributes(ObjectInputStream objectStream) throws IOException {
		int attributesCount = objectStream.readInt();
		Map<String, String> attributes = new HashMap<String, String>();
		for (int i = 0; i < attributesCount; i++) {
			String key = objectStream.readUTF();
			String value = objectStream.readUTF();
			attributes.put(key, value);
		}
		return attributes;
	}

	private void writeCause(ObjectOutputStream objectStream, Cause cause) throws IOException {
		if (cause != null) {
			objectStream.writeUTF(cause.getType());
			objectStream.writeUTF(cause.getMessage());
			objectStream.writeUTF(cause.getStackTrace());
			writeCause(objectStream, cause.getCause());
		}
	}

	private Cause readCause(ObjectInputStream stream) throws IOException {
		if (stream.available() <= 0) {
			return null;
		}
		String type = stream.readUTF();
		String message = stream.readUTF();
		String stack = stream.readUTF();

		Cause cause = stream.available() > 0 ? readCause(stream) : null;
		return new Cause(type, message, stack, cause);
	}
}
