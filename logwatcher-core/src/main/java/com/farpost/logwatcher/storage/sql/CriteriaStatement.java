package com.farpost.logwatcher.storage.sql;

import com.farpost.logwatcher.storage.LogEntryMatcher;

import java.util.Collection;

class CriteriaStatement {

	private final String whereClause;
	private final Object[] arguments;
	private final Collection<LogEntryMatcher> lateBoundMatchers;

	CriteriaStatement(String whereClause, Object[] arguments,
	                  Collection<LogEntryMatcher> lateBoundMatchers) {
		this.whereClause = whereClause;
		this.arguments = arguments;
		this.lateBoundMatchers = lateBoundMatchers;
	}

	public String getWhereClause() {
		return whereClause;
	}

	public Object[] getArguments() {
		return arguments;
	}

	public Collection<LogEntryMatcher> getLateBoundMatchers() {
		return lateBoundMatchers;
	}

	public boolean haveLateBoundMatchers() {
		return lateBoundMatchers.size() > 0;
	}
}
