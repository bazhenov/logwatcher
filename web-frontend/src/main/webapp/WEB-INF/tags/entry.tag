<%@ tag import="org.bazhenov.logging.LogEntry" %>
<%@ tag import="org.bazhenov.logging.web.tags.EntryTag" %>
<%@ tag import="java.util.*" %>
<%@ tag import="static org.bazhenov.logging.web.tags.EntryTag.formatCause" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="lf" uri="http://bazhenov.org/logging" %>
<%@taglib prefix="f" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="l" tagdir="/WEB-INF/tags" %>
<%@attribute name="entry" type="org.bazhenov.logging.AggregatedLogEntry" required="true" %>

<c:set var="sampleEntry" value="${entry.sampleEntry}"/>
<%
	LogEntry sampleEntry = entry.getSampleEntry();
	String title = sampleEntry.getMessage();

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

	String lastOccurenceInfo = EntryTag.shortFormat.format(entry.getLastTime().asDate());
	String fullDate = EntryTag.fullFormat.format(entry.getLastTime().asDate());

	if ( isExceptionNew ) {
		lastOccurenceInfo = "<abbr class='warningMarker' title='" + fullDate + "'>" + lastOccurenceInfo + "</abbr>";
	} else {
		lastOccurenceInfo = "<abbr title='" + fullDate + "'>" + lastOccurenceInfo + "</abbr>";
	}
%>
<a name='${sampleEntry.checksum}'></a>

<div class='entry ${sampleEntry.severity}' checksum='${sampleEntry.checksum}'>
	<div class='entryHeader'>
		<span class='marker'>&bull;</span>

		<div class='message'><c:out value="${sampleEntry.message}"/></div>
		<div class='messageOverlay'></div>
		<div class='times'>
			<span class='applicationId'><c:out value="${sampleEntry.applicationId}"/></span> &mdash;
			<c:set var="count" value="${entry.count}"/>
			<c:choose>
				<c:when test="${count gt 10000}">
					<abbr title="${count} times">more than 10 000 times</abbr>, last time
				</c:when>
				<c:when test="${count gt 5000}">
					<abbr title="${count} times">more than 5 000 times</abbr>, last time
				</c:when>
				<c:when test="${count gt 1000}">
					<abbr title="${count} times">more than 1 000 times</abbr>, last time
				</c:when>
				<c:when test="${count gt 1}">
					${count} times, last time
				</c:when>
			</c:choose>

			<%out.write(lastOccurenceInfo);%>
		</div>
		<div class='entryContent'>
			<ol class='attributes'>
				<c:forEach var="row" items="${entry.attributes}">
					<li><l:attribute set="${row.value}" name="${row.key}"/></li>
				</c:forEach>
			</ol>
			<%
				if ( hasMessage ) {
					out.write("<pre class='stacktrace'>" + message + "</pre>");
				}
			%>
		</div>
		<div class='operations'>
			<c:url value="http://jira.dev.loc/jira/secure/CreateIssueDetails.jspa"
			       var="jiraLink">
				<c:param name="pid">10000</c:param>
				<c:param name="issuetype">1</c:param>
				<c:param name="summary" value="${sampleEntry.message}"/>
				<c:param name="description" value="${lf:formatCause(sampleEntry.cause)}"/>
				<c:param name="priority">3</c:param>
			</c:url>
			<a href='${jiraLink}' target='_blank'>create task</a> or
			<a class='removeEntry asynchronous' href='#'>remove</a>
			<a
				href='/feed?date=${sampleEntry.date.date}&severity=${sampleEntry.severity}#${sampleEntry.checksum}'>
				<img src='/images/link-icon.png' alt="permanent link"/></a>
		</div>
	</div>
</div>
