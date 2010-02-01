package org.bazhenov.logging.storage;

import org.bazhenov.logging.AggregatedLogEntry;
import org.bazhenov.logging.LogEntry;

import java.util.Collection;

public class MatcherUtils {

	public static boolean isMatching(AggregatedLogEntry entry,
	                                 Collection<LogEntryMatcher> criterias) {
		for ( LogEntryMatcher matcher : criterias ) {
			if ( !matcher.isMatch(entry) ) {
				return false;
			}
		}
		return true;
	}

	public static boolean isMatching(LogEntry entry, Collection<LogEntryMatcher> criterias) {
		for ( LogEntryMatcher matcher : criterias ) {
			if ( !matcher.isMatch(entry) ) {
				return false;
			}
		}
		return true;
	}
}
