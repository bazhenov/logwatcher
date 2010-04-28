<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/functions" %>

<%@attribute name="applicationIds" type="java.util.Set" required="true" %>
<%@attribute name="currentApplicationId" type="java.lang.String" required="true" %>

<c:if test="${f:length(applicationIds) gt 1}">
	<div id="applicationBox">
		<c:forEach items="${applicationIds}" var="id">
			<c:url value="/feed/${id}" var="url"/>
			<c:choose>
				<c:when test="${id eq currentApplicationId}">
					<span class="active"><a href='${url}'><c:out value="${id}"/></a></span>
				</c:when>
				<c:otherwise>
					<span><a href='${url}'><c:out value="${id}"/></a></span>
				</c:otherwise>
			</c:choose>
		</c:forEach>
		<div style="clear: both;"></div>
	</div>
</c:if>