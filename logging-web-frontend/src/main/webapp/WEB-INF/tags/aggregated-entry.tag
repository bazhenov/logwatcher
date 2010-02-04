<%@ tag import="org.bazhenov.logging.web.tags.EntryTag" %>
<%@ tag import="java.util.*" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="lf" uri="http://bazhenov.org/logging" %>
<%@taglib prefix="f" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="l" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@attribute name="entry" type="org.bazhenov.logging.AggregatedEntry" required="true" %>

<%
	boolean isExceptionNew = entry.getLastTime().plusMinute(30).isInFuture();

	Date date = entry.getLastTime().asDate();
	jspContext.setAttribute("lastDate", date);
	String lastOccurenceInfo = EntryTag.shortFormat.format(date);
	String fullDate = EntryTag.fullFormat.format(date);

	if ( isExceptionNew ) {
		lastOccurenceInfo = "<abbr class='warningMarker' title='" + fullDate + "'>" + lastOccurenceInfo + "</abbr>";
	} else {
		lastOccurenceInfo = "<abbr title='" + fullDate + "'>" + lastOccurenceInfo + "</abbr>";
	}
%>
<a name='${entry.checksum}'></a>

<c:url value="/entries/${entry.checksum}" var="detailsLink">
	<c:param name="date"><fmt:formatDate value="${lastDate}"
	                                     pattern="yyyy-MM-dd"/></c:param>
</c:url>

<div class='entry ${entry.severity}' checksum='${entry.checksum}'>
	<div class='entryHeader'>

		<div class="count">
			<c:set var="count" value="${entry.count}"/>
			<c:choose>
				<c:when test="${count gt 100000}">
					<abbr title="${count} times">10<sup>${lf:magnitude(count)}</sup></abbr>
				</c:when>
				<c:when test="${count gt 1000}">
					<abbr title="${count} times">${lf:thousands(count)}K</abbr>
				</c:when>
				<c:otherwise>
					${count}
				</c:otherwise>
			</c:choose>
		</div>
		<div class='message'><c:out value="${entry.message}"/></div>
		<div class='messageOverlay'></div>
		<div class='times'>
			<span class='applicationId'><c:out value="${entry.applicationId}"/></span>
			&mdash; <%out.write(lastOccurenceInfo);%>
		</div>

		<div class="operations noBubble">
			<a href="${detailsLink}">&rarr;</a>
		</div>

	</div>

	<div class="entryContainer">
		<div class='entryContent'>
			<ol class='attributes' loaded="false">
				<div class="spinner"></div>
			</ol>
			<c:choose>
				<c:when test="${not empty entry.sampleCause}">
						<pre class="stacktrace noBubble"><c:out
							value="${lf:formatCause(entry.sampleCause)}"/></pre>
				</c:when>
				<c:otherwise>
					<pre class="stacktrace noBubble"><c:out value="${entry.message}"/></pre>
				</c:otherwise>
			</c:choose>

		</div>
		<div class="entryFooter">
			<c:url value="http://jira.dev.loc/jira/secure/CreateIssueDetails.jspa" var="jiraLink">
				<c:param name="pid">10000</c:param>
				<c:param name="issuetype">1</c:param>
				<c:param name="summary" value="${entry.message}"/>
				<c:param name="description" value="${lf:formatCause(entry.sampleCause)}"/>
				<c:param name="priority">3</c:param>
			</c:url>
			<a href="<c:out value="${jiraLink}"/>" target='_blank'>Create task</a>
			<a href='/feed?date=${entry.lastTime.date}&severity=${entry.severity}#${entry.checksum}'>Permalink</a>

			<a href="${detailsLink}">Details</a>
			<a class='removeEntry asynchronous' href='#'>Remove</a>
		</div>
	</div>

</div>
