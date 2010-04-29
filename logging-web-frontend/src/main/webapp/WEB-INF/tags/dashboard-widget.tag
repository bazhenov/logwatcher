<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="l" uri="http://bazhenov.org/logging" %>

<%@attribute name="info" type="org.bazhenov.logging.web.ApplicationInfo" required="true" %>

<div class="dashboardWidget">
	<div class="title">
		<a href="/feed/${info.applicationId}"><c:out value="${info.applicationId}"/></a>
	</div>
	<ul>
		<c:forEach items="${info.entries}" var="entry" varStatus="status">
			<c:if test="${status.count lt 5}">
				<c:url var="url" value="/feed/${entry.applicationId}">
					<c:param name="severity" value="${entry.severity}"/>
				</c:url>

				<li><a href="${url}\#${entry.checksum}"><c:out value="${l:trim(entry.message, 33, '...')}"/></a></li>
			</c:if>
		</c:forEach>
	</ul>
</div>