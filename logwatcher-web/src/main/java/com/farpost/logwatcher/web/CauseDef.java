package com.farpost.logwatcher.web;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

public class CauseDef {

	@Nullable
	private String simpleType;

	@Nullable
	private String type;
	private String message;

	public CauseDef(@Nullable String simpleType, @Nullable String type, String message) {
		this.simpleType = simpleType;
		this.type = type;
		this.message = checkNotNull(message);
	}

	@Nullable
	public String getSimpleType() {
		return simpleType;
	}

	@Nullable
	public String getType() {
		return type;
	}

	public String getMessage() {
		return message;
	}
}
