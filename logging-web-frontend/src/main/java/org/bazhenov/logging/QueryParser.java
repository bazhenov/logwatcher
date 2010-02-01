package org.bazhenov.logging;

import java.util.*;

/**
 * {@code QueryParser} парсит строчки типа "at: frontend severity: error" в <code>Map</code> где
 * ключом является имя операции ("at", "severity") а значением соответстенно операнд.
 * <p/>
 * Потокобезопасен, что обусловлено отсутствием разделяемого состояния.
 */
public class QueryParser {

	public Map<String, String> parse(String query) throws InvalidQueryException {
		String lastOperation = null;
		StringBuffer operand = new StringBuffer();
		Map<String, String> operations = new HashMap<String, String>();
		for ( String part : query.replace(":", ": ").split(" ") ) {
			if ( part.endsWith(":") ) {
				if ( lastOperation != null ) {
					if ( operand.length() <= 0 ) {
						throw new InvalidQueryException(
							"Operation '" + lastOperation + "' do not have operand");
					}
					operations.put(lastOperation, operand.toString().trim());
				}
				lastOperation = part.substring(0, part.length() - 1);
				operand = new StringBuffer();
			} else if ( part.length() > 0 ) {
				operand.append(part).append(' ');
			}
		}
		operations.put(lastOperation, operand.toString().trim());
		return operations;
	}
}
