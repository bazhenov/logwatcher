package com.farpost.logwatcher.storage.spi;

import com.farpost.logwatcher.storage.LogEntryMatcher;

/**
 * Имплементации этого интерфейса конвертируют обьекты типа
 * {@link com.farpost.logwatcher.storage.LogEntryMatcher} в объекты представляющие собой критерии
 * отбора на уровне доступа к данным
 */
public interface MatcherMapper<T> {

	/**
	 * Выполняет конвертацию и возвращает объект-критерий отбора уровня доступа данных
	 *
	 * @param matcher критерий отбора записей
	 * @return критерий отбора записей уровня доступа к данным или {@code null}, если конвертационное правило
	 *         не может быть найдено
	 * @throws MatcherMapperException в случае ошибки конвертации
	 */
	public T handle(LogEntryMatcher matcher) throws MatcherMapperException;
}
