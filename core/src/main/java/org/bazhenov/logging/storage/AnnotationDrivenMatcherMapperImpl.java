package org.bazhenov.logging.storage;

public class AnnotationDrivenMatcherMapperImpl implements SqlMatcherMapper {

	private final Object handler;
	private final Class<?> handlerClass;

	public AnnotationDrivenMatcherMapperImpl(Object handler) {
		this.handler = handler;
		this.handlerClass = handler.getClass();
	}

	public boolean handle(LogEntryMatcher matcher, WhereClause clause) {
		return false;
	}
}
