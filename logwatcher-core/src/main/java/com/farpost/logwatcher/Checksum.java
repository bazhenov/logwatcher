package com.farpost.logwatcher;

import java.util.Arrays;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.Character.digit;

/**
 * Wrapper for byte array to enable by-value equality in collection library.
 */
final public class Checksum {

	private final static char[] hexArray = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

	private final byte[] checksum;

	public Checksum(byte[] checksum) {
		this.checksum = checkNotNull(checksum);
		checkArgument(checksum.length > 0, "Should be non empty array");
	}

	public byte[] asByteArray() {
		return checksum;
	}


	private static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		int v;
		for (int j = 0; j < bytes.length; j++) {
			v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}

	@Override
	public String toString() {
		return bytesToHex(checksum);
	}

	public static Checksum fromHexString(String s) {
		checkArgument(s.length() % 2 == 0);
		byte[] data = new byte[s.length() / 2];
		for (int i = 0; i < s.length(); i += 2) {
			data[i / 2] = (byte) ((digit(s.charAt(i), 16) << 4) + digit(s.charAt(i + 1), 16));
		}
		return new Checksum(data);
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(checksum);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Checksum)) return false;

		Checksum checksum1 = (Checksum) o;

		return Arrays.equals(checksum, checksum1.checksum);

	}
}
