package com.farpost.logwatcher;

public enum Severity {

	trace(1), debug(2), info(3), warning(4), error(5);
	private final int code;

	Severity(int code) {
		this.code = code;
	}

	public static Severity forName(String name) {
		for ( Severity i : values() ) {
			if ( i.toString().equalsIgnoreCase(name) ) {
				return i;
			}
		}
		return null;
	}

	public static Severity forCode(int code) {
		for ( Severity i : values() ) {
			if ( i.getCode() == code ) {
				return i;
			}
		}
		return null;
	}

	public int getCode() {
		return code;
	}

	public boolean isMoreImportantThan(Severity severity) {
		return getCode() >= severity.getCode();
	}
}
