<%@tag import="org.bazhenov.logging.web.tags.EntryTag" %>
<%@tag import="java.util.Date" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="lf" uri="http://bazhenov.org/logging" %>
<%@taglib prefix="f" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="l" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@attribute name="entry" type="org.bazhenov.logging.AggregatedEntry" required="true" %>
<%@attribute name="currentApplicationId" type="java.lang.String" required="false" %>

<%
	boolean isExceptionNew = entry.getLastTime().plusMinute(30).isInFuture();

	Date date = entry.getLastTime().asDate();
	jspContext.setAttribute("lastDate", date);
	String lastOccurenceInfo = EntryTag.shortFormat.format(date);
	String fullDate = EntryTag.fullFormat.format(date);

	if (isExceptionNew) {
		lastOccurenceInfo = "<abbr class='warningMarker' title='" + fullDate + "'>" + lastOccurenceInfo + "</abbr>";
	} else {
		lastOccurenceInfo = "<abbr title='" + fullDate + "'>" + lastOccurenceInfo + "</abbr>";
	}
%>
<a name='${entry.checksum}'></a>

<fmt:formatDate value="${lastDate}" pattern="yyyy-MM-dd" var="lastOccurredDate"/>

<c:url value="/entries/${entry.checksum}" var="detailsLink">
	<c:param name="date" value="${lastOccurredDate}" />
</c:url>

<div class='entry ${entry.severity}' checksum='${entry.checksum}' lastOccurredDate='${lastOccurredDate}'>
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
		<div class='message'>
			<c:if test="${not empty(entry.sampleCause)}">
				<span class="causeType"><c:out value="${lf:rootCause(entry.sampleCause).type}"/>:</span>
			</c:if>
			<c:out value="${lf:trim(entry.message, 150, null)}"/>
		</div>
		<div class='messageOverlay'></div>
		<div class='times'>
			<c:if test="${currentApplicationId ne entry.applicationId}">
				<span class='applicationId'><c:out value="${entry.applicationId}"/></span> &mdash;
			</c:if>
			<%=lastOccurenceInfo%>
		</div>

		<div class="operations noBubble">
			<c:url value="/entries/${entry.checksum}" var="detailsLink">
				<c:param name="date"><fmt:formatDate value="${lastDate}" pattern="yyyy-MM-dd"/></c:param>
			</c:url>
			<a href="${detailsLink}">Details &rarr;</a>
		</div>
	</div>

	<div class="entryContainer" loaded="false">
		<div class="spinner"></div>
	</div>

</div>
