<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@attribute name="entry" type="org.bazhenov.logging.AggregatedLogEntry" required="true" %>

<c:set var="sample" value="${entry.sampleEntry}"/>
<c:url var="jiraUrl" value="http://jira.dev.loc/jira/secure/CreateIssueDetails.jspa">
	<c:param name="pid" value="10000"/>
	<c:param name="issuetype" value="1"/>
	<c:param name="summary" value="..."/>
	<c:param name="description" value="..."/>
	<c:param name="priority" value="3"/>
</c:url>

<a name="${sample.checksum}"></a>

<div checksum="${sample.checksum}" class="${classes}">
	<div class="entryHeader">
		<span class="marker"></span>

		<div class="message">${sample.message}</div>
		<div class="messageOverlay"></div>

		<div class="times">
			<span class="applicationId">${sample.applicationId}</span> &mdash;
			<c:choose>
				<c:when test="${entry.count} > 10000">
					<span class="additionalInfo" title="${times}">more then 10 000 times</span>, last time -
				</c:when>
				<c:when test="${entry.count} > 5000">
					<span class="additionalInfo" title="${times}">more then 4 000 times</span>, last time -
				</c:when>
				<c:when test="${entry.count} > 1000">
					<span class="additionalInfo" title="${times}">more then 1 000 times</span>, last time -
				</c:when>
				<c:otherwise>

				</c:otherwise>
			</c:choose>
		</div>
	</div>
</div>