package org.bazhenov.logging.storage;

/**
 * ??????????????? ?????, ??????? ????????????? fluent ????????? ??? ??????
 * ??????? ? ????????? ??????? {@link LogStorage}.
 * <p/>
 * ?????? ?????????????:
 * <pre>
 * Collection&lt;AggregatedLogEntry&gt; enties = LogEntries.from(storage).
 *   date(november(12, 2008)).
 *   checksum("2fed4eade").
 *   find();
 * </pre>
 */
public class LogEntries {

	public static LogEntriesFinder entries() {
		return new LogEntriesFinder();
	}
}
