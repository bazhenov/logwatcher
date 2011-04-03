package com.farpost.logwatcher.storage.spi;

import com.farpost.logwatcher.storage.LogEntryMatcher;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Выполняет маппинг {@link LogEntryMatcher}'ов, на осоновании объекта правил.
 * <p/>
 * Объект правил - это класс методы которого помечены аннотацией {@link Matcher} и выполняет трансляцию
 * матчеров в низкоуровневый объект представляющий собой критерий отбора.
 * <p/>
 * Методы помеченные аннотацией {@code Matcher}, должны иметь один аргумент типа наследника от
 * {@link LogEntryMatcher}.
 *
 * @param <T> тип объектов которые представляют собой критерии отбора на низком уровне
 * @see com.farpost.logwatcher.storage.LuceneMatcherMapperRules
 */
public class AnnotationDrivenMatcherMapperImpl<T> implements MatcherMapper<T> {

	private final Object handler;
	private final Class<?> handlerClass;

	public AnnotationDrivenMatcherMapperImpl(Object handler) {
		this.handler = handler;
		this.handlerClass = handler.getClass();
	}

	public T handle(LogEntryMatcher matcher) throws MatcherMapperException {
		for (Method m : handlerClass.getDeclaredMethods()) {
			if (m.getAnnotation(Matcher.class) != null) {
				Class<?>[] types = m.getParameterTypes();
				if (!matcher.getClass().isAssignableFrom(types[0])) {
					continue;
				}
				try {
					return (T) m.invoke(handler, matcher);
				} catch (IllegalAccessException e) {
					throw new MatcherMapperException(e);
				} catch (InvocationTargetException e) {
					throw new MatcherMapperException(e);
				}
			}
		}
		return null;
	}
}
