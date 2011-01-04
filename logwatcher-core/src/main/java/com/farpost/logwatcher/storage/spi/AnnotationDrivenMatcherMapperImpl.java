package com.farpost.logwatcher.storage.spi;

import com.farpost.logwatcher.storage.LogEntryMatcher;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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
