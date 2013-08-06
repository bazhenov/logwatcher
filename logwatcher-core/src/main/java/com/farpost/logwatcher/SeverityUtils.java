package com.farpost.logwatcher;

import com.google.common.base.Optional;

import static com.google.common.base.Optional.absent;

public class SeverityUtils {

	public static Optional<Severity> forName(String name) {
		for (Severity i : Severity.values()) {
			if (i.toString().equalsIgnoreCase(name)) {
				return Optional.of(i);
			}
		}
		return absent();
	}
}
