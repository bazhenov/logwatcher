<%@ tag import="org.bazhenov.logging.LogEntry" %>
<%@ tag import="java.util.*" %>
<%@ tag import="java.text.FieldPosition" %>
<%@ tag import="java.text.DateFormat" %>
<%@ tag import="static org.apache.commons.lang.StringUtils.join" %>
<%@ tag import="static org.apache.commons.lang.StringEscapeUtils.escapeHtml" %>
<%@ tag import="static org.apache.commons.lang.StringEscapeUtils.escapeHtml" %>
<%@ tag import="static java.text.MessageFormat.format" %>
<%@ tag import="static org.apache.commons.lang.StringUtils.join" %>
<%@ tag import="static org.apache.commons.lang.StringUtils.*" %>
<%@ tag import="java.io.IOException" %>
<%@ tag import="org.bazhenov.logging.web.tags.EntryTag" %>
<%@ tag import="static org.bazhenov.logging.web.tags.EntryTag.pluralize" %>
<%@ tag import="static org.bazhenov.logging.web.tags.EntryTag.formatCause" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="l" tagdir="/WEB-INF/tags" %>
<%@attribute name="entry" type="org.bazhenov.logging.AggregatedLogEntry" required="true" %>

<%
	LogEntry sampleEntry = entry.getSampleEntry();
	String title = sampleEntry.getMessage();

	String applicationId = sampleEntry.getApplicationId();
	int count = entry.getCount();

	boolean withStacktrace = sampleEntry.getCause() != null;
	boolean isTitleTooLong = title.length() > EntryTag.MAX_LENGTH;
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

	Set<String> markerClasses = new TreeSet<String>();
	markerClasses.add("marker");

	if ( !hasMessage ) {
		markerClasses.add("emptyMarker");
	}

	String lastOccurenceInfo = EntryTag.shortFormat.format(entry.getLastTime().asDate());
	String fullDate = EntryTag.fullFormat.format(entry.getLastTime().asDate());

	if ( isExceptionNew ) {
		lastOccurenceInfo = "<abbr class='warningMarker' title='"+fullDate+"'>" + lastOccurenceInfo + "</abbr>";
	}else{
		lastOccurenceInfo = "<abbr title='"+fullDate+"'>" + lastOccurenceInfo + "</abbr>";
	}

	String timesInfo = pluralize(count, "- times times");
	if ( count > 10000 ) {
		timesInfo = "<abbr title='" + timesInfo + "'>more than 10 000 times</span>";
	} else if ( count > 5000 ) {
		timesInfo = "<abbr title='" + timesInfo + "'>more than 5 000 times</span>";
	} else if ( count > 1000 ) {
		timesInfo = "<abbr title='" + timesInfo + "'>more than 1 000 times</span>";
	}

	title = escapeHtml(title);
	message = escapeHtml(message);
	String jiraLink = format(EntryTag.JIRA_LINK_FORMAT, title, message);

	try {
		out.write("<a name='" + sampleEntry.getChecksum() + "'></a>");
		out.write(
			"<div class='" + join(classes, " ") + "' checksum='" + sampleEntry.getChecksum() + "'>");
		out.write("<div class='entryHeader'>");
		out.write("<span class='" + join(markerClasses, " ") + "'></span>");
		out.write("<div class='message'>" + title + "</div>");
		out.write("<div class='messageOverlay'></div>");
		out.write("<div class='times'>");
		out.write("<span class='applicationId'>" + applicationId + "</span> &mdash; ");
		out.write((count > 1
			? timesInfo + ", last time "
			: ""));
		out.write(lastOccurenceInfo);
		out.write("</div>");

		if ( hasMessage ) {
			out.write("<div class='entryContent'>");
			out.write("<ol class='attributes'>");
			%>
			<c:forEach var="row" items="${entry.attributes}">
				<li><l:attribute set="${row.value}" name="${row.key}" /></li>
			</c:forEach>
			<%
			out.write("</ol>");
			out.write("<pre class='stacktrace'>" + message + "</pre>");
			out.write("</div>");
		}
		out.write("<div class='operations'>");
		out.write("<a href='" + jiraLink + "' target='_blank'>create task</a>");
		out.write(" or ");
		out.write("<a class='removeEntry asynchronous' href='#'>remove</a> ");
		out.write(
			"<a href='/feed?date="+sampleEntry.getDate().getDate()+"&severity=" + sampleEntry.getSeverity() + "#" + sampleEntry.getChecksum() + "'>");
		out.write(" <img src='/images/link-icon.png' /></a>");
		out.write("</div>");
		out.write("</div>");
		out.write("</div>");
	} catch ( IOException e ) {
		throw new RuntimeException(e);
	}
%>
