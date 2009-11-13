package org.bazhenov.logging.jsp.tags;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockPageContext;
import org.springframework.mock.web.MockServletContext;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import org.bazhenov.logging.*;
import static org.bazhenov.logging.jsp.tags.EntryTag.pluralize;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

import com.farpost.timepoint.DateTime;
import static com.farpost.timepoint.Date.january;

public class EntryTagTest {

	@BeforeMethod
	public void setUp() {
		Locale.setDefault(new Locale("RU"));
	}

	@Test
	public void tagCanFormatNormalEntries() throws JspException {
		DateTime time = january(15, 2008).at(15, 03);
		Cause cause = new Cause("RuntimeException", "Ooops#1", "stacktrace#1",
			new Cause("NotRuntimeException", "Ooops#2", "stacktrace#2"));

		LogEntry logEntry = new LogEntry(time, "SomeGroup", "RuntimeFailure", Severity.error, "ae23",
			cause, "frontend");
		AggregatedLogEntry aggregatedEntry = new AggregatedLogEntryImpl(logEntry, time, 5232);

		EntryTag tag = new EntryTag();
		tag.setEntry(aggregatedEntry);
		String result = renderTag(tag);
		assertThat(result, containsString("RuntimeFailure"));
		assertThat(result, containsString("frontend"));
		assertThat(result, containsString("error"));
		assertThat(result, containsString(
			"<span class='additionalInfo' title='15 Январь 2008, 15:03:00 VLAT'>15 января, 15:03</span>"));
		assertThat(result,
			containsString("<span class='additionalInfo' title='5232 раза'>более 5 000 раз</span>"));

		assertThat(result, containsString("<pre class='stacktrace'>"));

		assertThat(result, containsString("RuntimeException"));
		assertThat(result, containsString("Ooops#1"));
		assertThat(result, containsString("stacktrace#1"));

		assertThat(result, containsString("NotRuntimeException"));
		assertThat(result, containsString("Ooops#2"));
		assertThat(result, containsString("stacktrace#2"));
	}

	@Test
	void testTagLibShouldFormatLongEntries() throws JspException {
		String title = "very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very long text";
		DateTime time = january(2008, 12).at(12, 02);
		LogEntry logEntry = new LogEntry(time, "group", title, Severity.error, "as", null, "frontend");
		AggregatedLogEntry aggregatedEntry = new AggregatedLogEntryImpl(logEntry, time, 5232);

		EntryTag tag = new EntryTag();
		tag.setEntry(aggregatedEntry);
		String result = renderTag(tag);
		assertThat(result, containsString(title));
	}

	@Test
	void testTagLibCanFormatEmptyEntries() throws JspException {
		DateTime time = DateTime.now();
		LogEntry logEntry = new LogEntry(time, "SomeGroup", "OutOfMemoryException", Severity.error,
			"ae23", "frontend");
		AggregatedLogEntry aggregatedEntry = new AggregatedLogEntryImpl(logEntry);

		EntryTag tag = new EntryTag();
		tag.setEntry(aggregatedEntry);

		String result = renderTag(tag);
		assertThat(result, containsString("OutOfMemoryException"));
		assertThat(result, containsString("∅"));
		assertThat(result, not(containsString("<pre")));
	}

	@Test
	public void testPluralize() {
		assertThat(pluralize(1, "чемодан чемодана чемоданов"), equalTo("1 чемодан"));
		assertThat(pluralize(2, "чемодан чемодана чемоданов"), equalTo("2 чемодана"));
		assertThat(pluralize(5, "чемодан чемодана чемоданов"), equalTo("5 чемоданов"));
	}

	private static String renderTag(Tag tag) throws JspException {
		try {
			MockServletContext servletContext = new MockServletContext();
			MockPageContext pageContext = new MockPageContext(servletContext);

			MockHttpServletResponse response = (MockHttpServletResponse) pageContext.getResponse();
			response.setCharacterEncoding("UTF8");

			tag.setPageContext(pageContext);

			tag.doStartTag();
			tag.doEndTag();

			return response.getContentAsString();
		} catch ( UnsupportedEncodingException e ) {
			throw new RuntimeException(e);
		}
	}
}
