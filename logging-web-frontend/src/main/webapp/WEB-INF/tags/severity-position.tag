<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/functions" %>

<%@attribute name="severity" type="java.lang.String" required="true" %>
<%@attribute name="currentSeverity" type="java.lang.String" required="true" %>

<c:if test="${severity ne currentSeverity}">
	<c:url value="/session" var="url">
		<c:param name="severity" value="${severity}" />
	</c:url>
	<li><a href="${url}"><c:out value="${severity}" /></a></li>
</c:if>
