package org.bazhenov.logging;

import org.bazhenov.logging.storage.*;

public class TranslationRulesImpl {

	@Criteria("severity")
	public LogEntryMatcher severity(String severity) {
		return new SeverityMatcher(Severity.forName(severity));
	}

	@Criteria("at")
	public LogEntryMatcher applicationId(String applicationId) {
		return new ApplicationIdMatcher(applicationId);
	}
}
