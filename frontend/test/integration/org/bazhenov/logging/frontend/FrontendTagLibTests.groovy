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
		DateTime time = new DateTime(new Date(99, 0, 15, 15, 3, 28))
		def logEntry = new LogEntry(time, "SomeGroup", "OutOfMemoryException", Severity.error, "ae23",
			"frontend")
		def aggregatedEntry = new AggregatedLogEntryImpl(logEntry)
		def entry = new Entry(aggregatedEntry)

		tagLib.entry(ref: entry) {}
		String html = out as String
		assertContains "OutOfMemoryException", html
		assertContains "frontend", html
		assertContains "error", html
		assertContains "15 января, 15:03", html
	}
}