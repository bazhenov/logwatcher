package org.bazhenov.logging.frontend

import org.bazhenov.logging.AggregatedLogEntryImpl
import org.bazhenov.logging.LogEntry
import org.bazhenov.logging.Severity
import com.farpost.timepoint.DateTime
import static org.bazhenov.logging.frontend.Asserts.*
import org.bazhenov.logging.Cause

public class FrontendTagLibTests extends GroovyTestCase {

	FrontendTagLib tagLib = new FrontendTagLib()
	StringWriter out = new StringWriter()

	void setUp() {
		tagLib.out = out
	}

	void testTagLibCanFormatNormalEntries() {
		def time = new DateTime(new Date(99, 0, 15, 15, 3, 28))
		def cause = new Cause("RuntimeException", "Ooops#1", "stacktrace#1",
			new Cause("NotRuntimeException", "Ooops#2", "stacktrace#2"));

		def logEntry = new LogEntry(time, "SomeGroup", "RuntimeFailure", Severity.error, "ae23",
			cause, "frontend")
		def aggregatedEntry = new AggregatedLogEntryImpl(logEntry)
		def entry = new Entry(aggregatedEntry)

		tagLib.entry(ref: entry) {}
		String html = out as String
		assertContains "RuntimeFailure", html
		assertContains "frontend", html
		assertContains "error", html
		assertContains(["15 января, 15:03", "15 Январь 1999, 15:03:28 VLAT"], html)

		assertContains "<pre class='stacktrace'>", html
		assertContains(["RuntimeException", "Ooops#1", "stacktrace#1"], html)
		assertContains(["NotRuntimeException", "Ooops#2", "stacktrace#2"], html)

	}

	void testTagLibCanFormatEmptyEntries() {
		DateTime time = new DateTime(new Date(99, 0, 15, 15, 3, 28))
		def logEntry = new LogEntry(time, "SomeGroup", "OutOfMemoryException", Severity.error, "ae23",
			"frontend")
		def aggregatedEntry = new AggregatedLogEntryImpl(logEntry)
		def entry = new Entry(aggregatedEntry)

		tagLib.entry(ref: entry) {}
		String html = out as String
		assertContains "OutOfMemoryException", html
		assertContains "∅", html
		assertNotContains "<pre", html
	}
}