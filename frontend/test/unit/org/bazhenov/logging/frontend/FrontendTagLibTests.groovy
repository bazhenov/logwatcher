package org.bazhenov.logging.frontend

import org.bazhenov.logging.AggregatedLogEntryImpl
import org.bazhenov.logging.LogEntry
import org.bazhenov.logging.Severity
import com.farpost.timepoint.DateTime
import static com.farpost.timepoint.Date.*
import static org.bazhenov.logging.frontend.Asserts.*
import org.bazhenov.logging.Cause

public class FrontendTagLibTests extends GroovyTestCase {

	FrontendTagLib tagLib = new FrontendTagLib()
	StringWriter out = new StringWriter()

	void setUp() {
		tagLib.out = out
	}

	void testTagLibCanFormatNormalEntries() {
		def time = january(15, 2008).at(15, 03)
		def cause = new Cause("RuntimeException", "Ooops#1", "stacktrace#1",
			new Cause("NotRuntimeException", "Ooops#2", "stacktrace#2"));

		def logEntry = new LogEntry(time, "SomeGroup", "RuntimeFailure", Severity.error, "ae23",
			cause, "frontend")
		def aggregatedEntry = new AggregatedLogEntryImpl(logEntry, time, 5232)

		tagLib.entry(ref: new Entry(aggregatedEntry)) {}
		String html = out as String
		assertContains "RuntimeFailure", html
		assertContains "frontend", html
		assertContains "error", html
		assertContains "<span class='additionalInfo' title='15 Январь 2008, 15:03:00 VLAT'>15 января, 15:03</span>", html
		assertContains "<span class='additionalInfo' title='5232 раза'>более 1000 раз</span>", html

		assertContains "<pre class='stacktrace'>", html
		assertContains(["RuntimeException", "Ooops#1", "stacktrace#1"], html)
		assertContains(["NotRuntimeException", "Ooops#2", "stacktrace#2"], html)
	}

	void testTagLibShouldFormatLongEntries() {
		def title = "very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very long text";
		def time = january(2008, 12).at("12:02")
		def logEntry = new LogEntry(time, "group", title, Severity.error, "as", null, "frontend");
		def aggregatedEntry = new AggregatedLogEntryImpl(logEntry, time, 5232)
		tagLib.entry(ref: new Entry(aggregatedEntry)) {}
		String html = out as String;
		assertContains title, html
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

	void testPluralize() {
		assertEquals "1 чемодан", 1.pluralize("чемодан чемодана чемоданов")
		assertEquals "2 чемодана", 2.pluralize("чемодан чемодана чемоданов")
		assertEquals "5 чемоданов", 5.pluralize(["чемодан", "чемодана", "чемоданов"])

	}
}