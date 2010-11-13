package com.farpost.logwatcher.storage.sql;

import com.farpost.logwatcher.storage.LogEntryMatcher;

/**
 * Имплементации этого интерфейса занимаются тем, что превращают обьекты типа
 * {@link com.farpost.logwatcher.storage.LogEntryMatcher} в sql WHERE выражения
 */
public interface SqlMatcherMapper {

	public boolean handle(LogEntryMatcher matcher, WhereClause clause) throws MatcherMapperException;
}
