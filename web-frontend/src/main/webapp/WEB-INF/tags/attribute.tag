<%@attribute name="set" type="java.util.Map" required="true" %>
<%@attribute name="name" type="java.lang.String" required="true" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="l" uri="http://bazhenov.org/logging" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<label>${name}:</label>
<c:forEach items="${set}" var="row" varStatus="counter">
	<c:if test="${counter.count lt 5}">
		<c:choose><c:when
			test="${fn:length(row.key) gt 15}"><abbr title="${row.key}">${fn:substring(row.key, 0, 12)}...</abbr></c:when><c:otherwise
			>${row.key}</c:otherwise></c:choose><c:if
		test="${row.value gt 1}"> <span>(${row.value} times)</span></c:if><c:if
		test="${counter.count lt fn:length(set)}">, </c:if>
	</c:if>
</c:forEach>
