<%@ tag import="org.bazhenov.logging.web.tags.EntryTag" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="lf" uri="http://bazhenov.org/logging" %>
<%@taglib prefix="f" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="l" tagdir="/WEB-INF/tags" %>
<%@attribute name="entry" type="org.bazhenov.logging.AggregatedLogEntry" required="true" %>

<c:set var="sampleEntry" value="${entry.sampleEntry}"/>
<%
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
		<div class='entryContent noBubble'>
			<ol class='attributes'>
				<c:forEach var="row" items="${entry.attributes}">
					<li><l:attribute attribute="${row.value}" name="${row.key}"/></li>
				</c:forEach>
			</ol>
			<c:choose>
				<c:when test="${not empty sampleEntry.cause}">
					<pre class="stacktrace noBubble"><c:out
						value="${lf:formatCause(sampleEntry.cause)}"/></pre>
				</c:when>
				<c:otherwise>
					<pre class="stacktrace noBubble"><c:out value="${sampleEntry.message}"/></pre>
				</c:otherwise>
			</c:choose>
		</div>
		<div class='operations noBubble'>
			<c:url value="http://jira.dev.loc/jira/secure/CreateIssueDetails.jspa" var="jiraLink">
				<c:param name="pid">10000</c:param>
				<c:param name="issuetype">1</c:param>
				<c:param name="summary" value="${sampleEntry.message}"/>
				<c:param name="description" value="${lf:formatCause(sampleEntry.cause)}"/>
				<c:param name="priority">3</c:param>
			</c:url>
			<a href="<c:out value="${jiraLink}"/>" target='_blank'>create task</a> or
			<a class='removeEntry asynchronous' href='#'>remove</a>
			<a
				href='/feed?date=${sampleEntry.date.date}&severity=${sampleEntry.severity}#${sampleEntry.checksum}'>
				<img src='/images/link-icon.png' alt="permanent link"/></a>
		</div>
	</div>
</div>
