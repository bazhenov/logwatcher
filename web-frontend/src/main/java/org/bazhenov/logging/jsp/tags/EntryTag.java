package org.bazhenov.logging.jsp.tags;

import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;
import static org.apache.commons.lang.StringUtils.join;
import org.bazhenov.logging.AggregatedLogEntry;
import org.bazhenov.logging.LogEntry;
import org.bazhenov.logging.Cause;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;
import static java.lang.Math.abs;
import java.text.DateFormat;
import java.text.FieldPosition;
import static java.text.MessageFormat.format;
import java.text.SimpleDateFormat;
import java.util.Set;
import java.util.TreeSet;

public class EntryTag extends TagSupport {

	private final int MAX_LENGTH = 80;
	private AggregatedLogEntry entry;
	DateFormat shortFormat = new DateTimeFormat();
	DateFormat fullFormat = new SimpleDateFormat("d MMMM yyyy, HH:mm:ss zz");
	private static final String JIRA_LINK_FORMAT = "http://jira.dev.loc/jira/secure/CreateIssueDetails.jspa?pid=10000&issuetype=1&summary={0}&description={1}&priority=3";

	public void setEntry(AggregatedLogEntry entry) {
		this.entry = entry;
	}

	@Override
	public int doEndTag() throws JspException {
		JspWriter out = pageContext.getOut();
		LogEntry sampleEntry = entry.getSampleEntry();
		String title = sampleEntry.getMessage();

		String applicationId = sampleEntry.getApplication();
		int count = entry.getCount();

		boolean withStacktrace = sampleEntry.getCause() != null;
		boolean isTitleTooLong = title.length() > MAX_LENGTH + "...".length();
		if ( isTitleTooLong ) {
			title = title.substring(0, MAX_LENGTH) + "...";
		}
		boolean hasMessage = withStacktrace || isTitleTooLong;
		String message = (!withStacktrace && isTitleTooLong)
			? sampleEntry.getMessage()
			: formatCause(sampleEntry.getCause());

		Set<String> classes = new TreeSet<String>();
		classes.add("entry");

		String severety = sampleEntry.getSeverity().toString();
		classes.add(severety);

		if ( hasMessage ) {
			classes.add("withStacktrace");
		}
		boolean isExceptionNew = entry.getLastTime().plusMinute(30).isInFuture();
		Set<String> additionalInfoClasses = new TreeSet<String>();
		additionalInfoClasses.add("additionalInfo");
		if ( isExceptionNew ) {
			additionalInfoClasses.add("warningMarker");
		}

		Set<String> markerClasses = new TreeSet<String>();
		markerClasses.add("marker");

		if ( !hasMessage ) {
			markerClasses.add("emptyMarker");
		}

		FieldPosition fieldPosition = new FieldPosition(DateFormat.HOUR0_FIELD);
		StringBuffer buffer = new StringBuffer();
		shortFormat.format(entry.getLastTime().asDate(), buffer, fieldPosition);
		String fullDate = fullFormat.format(entry.getLastTime().asDate());

		/**
		 * Не забываем, что теги надо вставлять в обратной последовательности, чтобы не допустить
		 * смещения индексов в FieldPosition
		 */
		buffer.insert(fieldPosition.getEndIndex(), "</span>");
		String startTag = "<span class='" + join(additionalInfoClasses,
			" ") + "' title='" + fullDate + "'>";
		buffer.insert(fieldPosition.getBeginIndex(), startTag);

		String lastOccurenceInfo = buffer.toString();

		String timesInfo = pluralize(count, "раз раза раз");
		if ( count > 10000 ) {
			timesInfo = "<span class='additionalInfo' title='" + timesInfo + "'>более 10 000 раз</span>";
		} else if ( count > 5000 ) {
			timesInfo = "<span class='additionalInfo' title='" + timesInfo + "'>более 5 000 раз</span>";
		} else if ( count > 1000 ) {
			timesInfo = "<span class='additionalInfo' title='" + timesInfo + "'>более 1 000 раз</span>";
		}

		title = escapeHtml(title);
		message = escapeHtml(message);
		String jiraLink = format(JIRA_LINK_FORMAT, title, message);

		try {
			out.write("<a name='" + sampleEntry.getChecksum() + "'></a>");
			out.write(
				"<div class='" + join(classes, " ") + "' checksum='" + sampleEntry.getChecksum() + "'>");
			out.write("<div class='entryHeader'>");
			out.write("<span class='" + join(markerClasses, " ") + "'>" + (hasMessage
				? "•"
				: "∅") + "</span>");
			out.write("<span class='message'>" + title + "</span>");
			out.write("<div class='times'>");
			out.write("<span class='applicationId'>" + applicationId + "</span> &mdash ");
			out.write((count > 1
				? timesInfo + ", последний раз "
				: ""));
			out.write(lastOccurenceInfo);
			out.write("</div>");

			if ( hasMessage ) {
				out.write("<div class='entryContent'>");
				out.write("<pre class='stacktrace'>" + message + "</pre>");
				out.write("</div>");
			}
			out.write("<div class='operations'>");
			out.write("<a href='" + jiraLink + "' target='_blank'>создать таск</a>");
			out.write(" или ");
			out.write("<a class='removeEntry asynchronous' href='#'>удалить</a> ");
			out.write("<a href='./" + entry.getLastTime()
				.getDate() + "?severity=" + sampleEntry.getSeverity() + "#" + sampleEntry.getChecksum() + "'>");
			out.write("<img src='./images/link-icon.png' /></a>");
			out.write("</div>");
			out.write("</div>");
			out.write("</div>");
		} catch ( IOException e ) {
			throw new RuntimeException(e);
		}

		return EVAL_PAGE;
	}

	private static String formatCause(Cause rootCause) {
		StringBuilder prefix = new StringBuilder();
		StringBuilder stackTrace = new StringBuilder();

		if ( rootCause != null ) {
			Cause cause = rootCause;
			while ( cause != null ) {
				if ( cause != rootCause ) {
					stackTrace.append("\n\n").append(prefix).append("Caused by ");
				}
				String iStack = cause.getStackTrace().replaceAll("\n", "\n" + prefix);
				stackTrace.append(cause.getType())
					.append(": ")
					.append(cause.getMessage())
					.append("\n")
					.append(prefix)
					.append(iStack);
				cause = cause.getCause();
				prefix.append("  ");
			}
		}
		return stackTrace.toString();
	}

	public static String pluralize(int number, String titles) {
		int abs = abs(number);
		int[] cases = new int[]{2, 0, 1, 1, 1, 2};
		String[] strings = titles.split(" ");
		String result = strings[(abs % 100 > 4 && abs % 100 < 20)
			? 2
			: cases[Math.min(abs % 10, 5)]];
		return number + " " + result;
	}
}
