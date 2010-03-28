<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<div id="searchBox">
  <c:url value="/search" var="url" />
	<form method="get" action="${url}">
		<c:set var="query"><c:out value="${param['query']}" /></c:set>
		<input type="text" name="query" id="searchInput" accesskey="S" value="${query}" autocomplete="off"/>
	</form>
	<div id="legend">
		<ul>
			<li><a class="asynchronous">at</a> &mdash; search by application id (ex: <code>at: frontend</code>)</li>
			<li><a class="asynchronous">occurred</a> &mdash; search by date (ex: <code>occurred: last 2 days</code>,
				<code>occurred: 2010-01-12</code>)</li>
			<li><a class="asynchronous">severity</a> &mdash; search by severity (ex: <code>severity: warning</code>)</li>
			<li><a class="asynchronous">caused-by</a> &mdash; search exception type (ex: <code>caused-by: SoapError</code>)</li>
			<li><a class="asynchronous">@attribute</a> &mdash; search by attribute value (ex: <code>@url: /adding</code>)</li>
		</ul>
	</div>
</div>