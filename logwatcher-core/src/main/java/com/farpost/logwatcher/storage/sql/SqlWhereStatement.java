package com.farpost.logwatcher.storage.sql;

import java.util.Collection;

import static java.util.Arrays.asList;

class SqlWhereStatement {

	private final String statement;
	private final Collection<Object> arguments;

	public SqlWhereStatement(String statement, Object... arguments) {
		this.statement = statement;
		this.arguments = asList(arguments);
	}

	public String getStatement() {
		return statement;
	}

	public Collection<Object> getArguments() {
		return arguments;
	}
}
