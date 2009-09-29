package org.bazhenov.logging.storage.sql;

import org.bazhenov.logging.storage.LogEntryMatcher;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

public class AnnotationDrivenMatcherMapperImpl implements SqlMatcherMapper {

	private final Object handler;
	private final Class<?> handlerClass;

	public AnnotationDrivenMatcherMapperImpl(Object handler) {
		this.handler = handler;
		this.handlerClass = handler.getClass();
	}

	public boolean handle(LogEntryMatcher matcher, WhereClause clause) throws MatcherMapperException {
		for ( Method m : handlerClass.getDeclaredMethods() ) {
			if ( m.getAnnotation(Matcher.class) != null ) {
				Class<?>[] types = m.getParameterTypes();
				if ( !matcher.getClass().isAssignableFrom(types[0]) ) {
					continue;
				}
				if ( !types[1].equals(WhereClause.class) ) {
					continue;
				}
				try {
					m.invoke(handler, matcher, clause);
					return true;
				} catch ( IllegalAccessException e ) {
					throw new MatcherMapperException(e);
				} catch ( InvocationTargetException e ) {
					throw new MatcherMapperException(e);
				}
			}
		}
		return false;
	}
}
