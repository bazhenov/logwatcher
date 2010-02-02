<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="lf" uri="http://bazhenov.org/logging" %>
<%@taglib prefix="f" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="l" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@attribute name="entry" type="org.bazhenov.logging.LogEntry" required="true" %>

<c:set var="lengthLimit" value="20" />
<div class="entry">
	<div class="entryHeader">
		<div class="timestamp"><span class="hours"><fmt:formatDate
			value="${lf:date(entry.date)}"
			pattern="HH:mm"/></span><span class="seconds"><fmt:formatDate
			value="${lf:date(entry.date)}"
			pattern=".ss"/></span></div>
		<span class="message"><c:out value="${entry.message}"/></span>

		<c:if test="${not empty(entry.attributes)}">
			<span class="attributes">
				<c:forEach items="${entry.attributes}" var="row">
					<c:choose>
						<c:when test="${fn:length(row.value) > lengthLimit}">
							<code><c:out value="${fn:substring(row.value, 0, lengthLimit - 3)}"/>&hellip;</code>
						</c:when>
						<c:otherwise><code><c:out value="${row.value}"/></code></c:otherwise>
					</c:choose>
				</c:forEach>
			</span>
		</c:if>
	</div>
	<div class="entryContainer">
		<c:if test="${not empty(entry.attributes)}">
			<div class="attributes">
				<c:forEach items="${entry.attributes}" var="row">
				<span><label><c:out value="${row.key}"/>:</label>
					<code><c:out value="${row.value}"/></code></span>
				</c:forEach>
			</div>
		</c:if>
		<pre class="stacktrace"><c:choose
			><c:when test="${not empty(entry.cause)}"
				><c:out value="${lf:formatCause(entry.cause)}"
			/></c:when
			><c:otherwise
				><c:out value="${entry.message}"
			/></c:otherwise
			></c:choose></pre>
	</div>
</div>
