package org.bazhenov.logging.frontend

import org.bazhenov.logging.AggregatedLogEntryImpl
import org.bazhenov.logging.LogEntry
import org.bazhenov.logging.Severity
import com.farpost.timepoint.DateTime
import static org.bazhenov.logging.frontend.Asserts.assertContains

public class FrontendTagLibTests extends GroovyTestCase {

	FrontendTagLib tagLib = new FrontendTagLib()
	StringWriter out = new StringWriter()

	void setUp() {
		tagLib.out = out
	}

	void testTagLibCanFormatEntries() {
		DateTime time = DateTime.now().minusHour(1);
		def logEntry = new LogEntry(time, "SomeGroup", "OutOfMemoryException", Severity.error, "ae23",
			"frontend")
		def aggregatedEntry = new AggregatedLogEntryImpl(logEntry)
		def entry = new Entry(aggregatedEntry)

		tagLib.entry(ref: entry) {}
		assertContains "OutOfMemoryException", out as String
		assertContains "frontend", out as String
		assertContains "error", out as String
	}
}