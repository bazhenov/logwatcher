package com.farpost.logwatcher;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Простая имплементация {@link ChecksumCalculator}, которая вычисляет контрольную сумму основываясь
 * на application id, severity и root cause записи лога.
 */
public class SimpleChecksumCalculator implements ChecksumCalculator {

	private MessageDigest digest;
	private static final String HEXES = "0123456789abcdef";

	public SimpleChecksumCalculator() {
		try {
			digest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	public String calculateChecksum(LogEntry entry) {
		String checksum = entry.getApplicationId() + ":" + entry.getSeverity();
		Cause cause = entry.getCause();
		if (cause != null) {
			checksum += ":" + cause.getRootCause().getType();
		} else if (entry.getChecksum() == null || entry.getChecksum().isEmpty()) {
			checksum += ":" + entry.getMessage();
		} else {
			checksum += ":" + entry.getChecksum();
		}
		digest.reset();
		return getHex(digest.digest(checksum.getBytes()));
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
