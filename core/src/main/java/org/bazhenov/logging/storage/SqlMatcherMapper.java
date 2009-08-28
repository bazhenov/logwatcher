package org.bazhenov.logging.storage;

import java.util.List;

/**
 * Имплементации этого интерфейса занимаются тем, что превращают обьекты типа
 * {@link LogEntryMatcher} в sql WHERE выражения
 */
public interface SqlMatcherMapper {

	public boolean handle(LogEntryMatcher matcher, WhereClause clause);
}
