<%@ page import="java.io.PrintStream" %>
<%@ page import="java.io.StringWriter" %>
<%@ page import="java.io.PrintWriter" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page isErrorPage="true" %>

<c:set var="cause" value="${requestScope['javax.servlet.error.exception']}"/>
<%
	Throwable cause = (Throwable) pageContext.getRequest()
		.getAttribute("javax.servlet.error.exception");
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<title>Ooops...</title>
	<c:url value='/css/main.css' var="cssUrl"/>
	<link rel="stylesheet" href="${cssUrl}"/>
	<c:url value='/css/ui.all.css' var="cssUrl"/>
	<link rel="stylesheet" href="${cssUrl}"/>

	<c:url value="/js/jquery-1.3.2.js" var="jsUrl"/>
	<script type="text/javascript" src="${jsUrl}">/**/</script>

	<c:url value="/js/jquery-ui-1.7.2.js" var="jsUrl"/>
	<script type="text/javascript" src="${jsUrl}">/**/</script>
	<c:url value="/js/exception-list.js" var="jsUrl"/>
	<script type="text/javascript" src="${jsUrl}">/**/</script>
</head>

<body>
<div id="header">
	<h1><span id="firstDot">&bull;</span><span id="secondDot">&bull;</span><span
		id="thirdDot">&bull;</span><span>&nbsp;</span></h1>

	<div id="menuBar">
		<a href="/" class="selected">&infin;</a>
	</div>

	<div id="sliderContainer">
		<div id="sliderValue">&nbsp;</div>
		<div id="slider">&nbsp;</div>
	</div>
</div>

<div id="mainPanel">

	<jsp:include page="/widgets/searchBox.jsp"/>

	<h2>Invalid query</h2>

	<div id="notificationPanel">
		What did you mean?
	</div>

</div>
</body>
</html>
