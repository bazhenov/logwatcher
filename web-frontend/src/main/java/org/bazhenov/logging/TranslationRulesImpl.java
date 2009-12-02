package org.bazhenov.logging;

import org.bazhenov.logging.storage.LogEntryMatcher;
import org.bazhenov.logging.storage.SeverityMatcher;

public class TranslationRulesImpl {

	@Criteria("severity")
	public LogEntryMatcher severity(String severity) {
		return new SeverityMatcher(Severity.forName(severity));
	}
}
