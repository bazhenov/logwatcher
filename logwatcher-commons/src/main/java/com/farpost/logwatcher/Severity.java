package com.farpost.logwatcher;

import com.google.common.base.Optional;

import static com.google.common.base.Optional.absent;

public enum Severity {

	trace(1), debug(2), info(3), warning(4), error(5);
	private final int code;

	Severity(int code) {
		this.code = code;
	}

	public static Optional<Severity> forName(String name) {
		for (Severity i : values()) {
			if (i.toString().equalsIgnoreCase(name)) {
				return Optional.of(i);
			}
		}
		return absent();
	}

	public static Severity forCode(int code) {
		for (Severity i : values()) {
			if (i.getCode() == code) {
				return i;
			}
		}
		return null;
	}

	public int getCode() {
		return code;
	}

	public boolean isEqualOrMoreImportantThan(Severity severity) {
		return getCode() >= severity.getCode();
	}
}
