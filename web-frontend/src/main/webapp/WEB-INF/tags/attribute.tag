<%@attribute name="attribute" type="org.bazhenov.logging.AggregatedAttribute" required="true" %>
<%@attribute name="name" type="java.lang.String" required="true" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="l" uri="http://bazhenov.org/logging" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<label>${name}:</label>
<c:forEach items="${attribute.values}" var="row" varStatus="counter">
	<c:if test="${counter.count lt 5}">
		<c:choose><c:when
			test="${fn:length(row.value) gt 25}"><abbr title="${row.value}">${fn:substring(row.value, 0, 22)}...</abbr></c:when><c:otherwise
			>${row.value}</c:otherwise></c:choose><c:if
		test="${row.count gt 1}"> <span>(${row.count} times)</span></c:if><c:if
		test="${counter.count lt fn:length(attribute.values)}">, </c:if>
	</c:if>
</c:forEach>
