package com.farpost.logwatcher;

import static com.google.common.base.Preconditions.checkArgument;

public final class TestUtils {

	public static Checksum checksum(int... input) {
		byte[] bytes = new byte[input.length];
		for (int i = 0; i < input.length; i++) {
			checkArgument(input[i] < 256);
			bytes[i] = (byte) input[i];
		}
		return new Checksum(bytes);
	}
}
