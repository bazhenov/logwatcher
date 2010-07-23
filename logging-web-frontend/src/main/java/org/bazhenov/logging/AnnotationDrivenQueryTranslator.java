package org.bazhenov.logging;

import org.bazhenov.logging.storage.LogEntryMatcher;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

	private final Map<String, Method> methods = new HashMap<String, Method>();;
	private Method defaultMethod;
	private final QueryParser queryParser = new QueryParser();
	private final Object rules;

	public AnnotationDrivenQueryTranslator(Object rules) {
		traverse(rules);
		this.rules = rules;
	}

	public List<LogEntryMatcher> translate(String query) throws InvalidQueryException {
		Map<String, String> operands = queryParser.parse(query);
		List<LogEntryMatcher> matchers = new ArrayList<LogEntryMatcher>(operands.size());
		for ( Map.Entry<String, String> entry : operands.entrySet() ) {
			String operation = entry.getKey();
			String operand = entry.getValue();

			try {
				Method method = methods.get(operation);
				LogEntryMatcher matcher;
				if ( method != null ) {
					matcher = (LogEntryMatcher) method.invoke(rules, operand);
				}else if ( defaultMethod != null ) {
					matcher = (LogEntryMatcher) defaultMethod.invoke(rules, operation, operand);
				}else{
					throw new InvalidQueryException("Translation rule for operation '"+ operation +"' is not set");
				}
				matchers.add(matcher);
			} catch ( IllegalAccessException e ) {
				throw new InvalidQueryException(e);
			} catch ( IllegalArgumentException e ) {
				throw new InvalidQueryException(e);
			} catch ( InvocationTargetException e ) {
				throw new InvalidQueryException(e);
			}
		}
		return matchers;
	}

	private void traverse(Object rules) {
		for ( Method m : rules.getClass().getDeclaredMethods() ) {
			if ( m.isAnnotationPresent(Criteria.class) ) {
				Criteria c = m.getAnnotation(Criteria.class);
				methods.put(c.value(), m);
			}else if ( m.isAnnotationPresent(DefaultCriteria.class) ) {
				defaultMethod = m;
			}
		}
	}
}
