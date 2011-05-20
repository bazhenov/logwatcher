package com.farpost.logwatcher;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Простая имплементация {@link ChecksumCalculator}, которая вычисляет контрольную сумму основываясь
 * на application id, severity и root cause записи лога.
 */
public class SimpleChecksumCalculator implements ChecksumCalculator {

	private static final String HEXES = "0123456789abcdef";

	public String calculateChecksum(LogEntry entry) {
		String messageInfo = entry.getApplicationId() + ":" + entry.getSeverity();
		Cause cause = entry.getCause();
		if (cause != null) {
			messageInfo += ":" + cause.getRootCause().getType();
		} else {
			String messageChecksum = entry.getChecksum();
			if (messageChecksum != null && !messageChecksum.isEmpty()) {
				messageInfo += ":" + messageChecksum;
			} else {
				messageInfo += ":" + entry.getMessage();
			}
		}
		try {
			return getHex(MessageDigest.getInstance("MD5").digest(messageInfo.getBytes()));
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	public static String getHex(byte[] raw) {
		if (raw == null) {
			return null;
		}
		final StringBuilder hex = new StringBuilder(2 * raw.length);
		for (byte b : raw) {
			hex.
				append(HEXES.charAt((b & 0xF0) >> 4)).
				append(HEXES.charAt((b & 0x0F)));
		}
		return hex.toString();
	}
}
