package org.bazhenov.logging;

import org.bazhenov.logging.storage.LogEntryMatcher;

import java.util.*;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

/**
 * Данная имплементация интерфейса {@link QueryTranslator} выполняет трансляцию
 * запросов на основании объектов-правил.
 * <p/>
 * Объект-правило - это нетипизированный объект у которого есть методы с аннотациями
 * {@link Criteria}. Пример объекта-правила:
 * <pre>
 * class RulesImpl {
 *
 *   \@Criteria("severity")
 *   public LogEntryMatcher severity(String severity) {
 *     return SeverityMatcher(Severity.forName(severity))
 *   }
 *
 * }
 * </pre>
 * Такой метод будет реагировать на строчку запрос типа: "seveity: error".
 * <p/>
 * Данная имплементация использует {@link QueryParser} для парсинга входящих текстовых запросов.
 *
 * @see QueryParser
 * @see QueryTranslator
 * @see Criteria
 * @see TranslationRulesImpl
 */
public class AnnotationDrivenQueryTranslator implements QueryTranslator {

	private final Map<String, Method> methods;
	private final QueryParser queryParser = new QueryParser();
	private final Object rules;

	public AnnotationDrivenQueryTranslator(Object rules) {
		methods = traverse(rules);
		this.rules = rules;
	}

	public List<LogEntryMatcher> translate(String query) throws InvalidQueryException {
		Map<String, String> operands = queryParser.parse(query);
		List<LogEntryMatcher> matchers = new ArrayList<LogEntryMatcher>(operands.size());
		for ( Map.Entry<String, String> entry : operands.entrySet() ) {
			String operation = entry.getKey();
			String operand = entry.getValue();
			if ( !methods.containsKey(operation) ) {
				throw new InvalidQueryException("Translation rule for operation '"+ operation +"' is not set");
			}
			try {
				Method method = methods.get(operation);
				LogEntryMatcher matcher = (LogEntryMatcher) method.invoke(rules, operand);
				matchers.add(matcher);
			} catch ( IllegalAccessException e ) {
				throw new InvalidQueryException(e);
			} catch ( InvocationTargetException e ) {
				throw new InvalidQueryException(e);
			}
		}
		return matchers;
	}

	private static Map<String, Method> traverse(Object rules) {
		Map<String, Method> methods = new HashMap<String, Method>();
		for ( Method m : rules.getClass().getDeclaredMethods() ) {
			if ( m.isAnnotationPresent(Criteria.class) ) {
				Criteria c = m.getAnnotation(Criteria.class);
				methods.put(c.value(), m);
			}
		}
		return methods;
	}
}
