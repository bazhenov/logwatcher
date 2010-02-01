package org.bazhenov.logging.storage.sql;

import org.bazhenov.logging.storage.LogEntryMatcher;

/**
 * Имплементации этого интерфейса занимаются тем, что превращают обьекты типа
 * {@link LogEntryMatcher} в sql WHERE выражения
 */
public interface SqlMatcherMapper {

	public boolean handle(LogEntryMatcher matcher, WhereClause clause) throws MatcherMapperException;
}
