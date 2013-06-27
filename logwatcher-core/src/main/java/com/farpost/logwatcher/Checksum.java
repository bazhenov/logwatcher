package com.farpost.logwatcher;

import java.util.Arrays;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Wrapper for byte array to enable by-value equality in collection library.
 */
final public class Checksum {

	private final byte[] checksum;

	public Checksum(byte[] checksum) {
		this.checksum = checkNotNull(checksum);
		checkArgument(checksum.length > 0, "Should be non empty array");
	}

	byte[] asByteArray() {
		return checksum;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Checksum)) return false;

		Checksum checksum1 = (Checksum) o;

		return Arrays.equals(checksum, checksum1.checksum);

	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(checksum);
	}
}
