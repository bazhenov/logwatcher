package org.bazhenov.logging;

import org.bazhenov.logging.storage.LogEntryMatcher;

import java.util.*;

/**
 * Имплементации этого интерфейса преобразуют строковые запросы в набор соответствующих
 * объектов {@link LogEntryMatcher}. Пользователи формируют критерии в виде строчек поиска.
 * Хранилище же принимает условия отбора в виде объектов типа {@link LogEntryMatcher}.
 * Основная ответственность этой сущности осуществлять трансляцию из строкового запроса
 * в матчеры.
 * <p/>
 * @see AnnotationDrivenQueryTranslator
 */
public interface QueryTranslator {

	List<LogEntryMatcher> translate(String query) throws InvalidQueryException;
}