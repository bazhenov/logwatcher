<%@attribute name="attribute" type="org.bazhenov.logging.AggregatedAttribute" required="true" %>
<%@attribute name="name" type="java.lang.String" required="true" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="l" uri="http://bazhenov.org/logging" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<label>${name}:</label>
<c:set var="valuesLimit" value="6" />
<c:forEach items="${attribute.values}" var="row" varStatus="counter">
	<c:if test="${counter.count le valuesLimit}">
		<c:choose>
			<c:when test="${fn:length(row.value) gt 20}">
				<abbr title="${row.value}"><code class="value">${fn:substring(row.value, 0, 17)}&hellip;<c:if test="${row.count gt 1}"><span>${row.count}</span></c:if></code></abbr>
			</c:when>
			<c:otherwise>
				<code class="value">${row.value}<c:if test="${row.count gt 1}"><span>${row.count}</span></c:if></code>
			</c:otherwise>
		</c:choose>
	</c:if>
</c:forEach>
<c:if test="${fn:length(attribute.values) gt valuesLimit}">
	<c:out value="${fn:length(attribute.values) - valuesLimit}" /> more&hellip;
</c:if>
