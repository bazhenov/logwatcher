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
	<c:url value='/css/aggregated-entry.css' var="cssUrl"/>
	<link rel="stylesheet" href="${cssUrl}"/>
	<c:url value='/css/ui.all.css' var="cssUrl"/>
	<link rel="stylesheet" href="${cssUrl}"/>
</head>

<body>
<div id="header">
	<h1><span id="firstDot">&bull;</span><span id="secondDot">&bull;</span><span
		id="thirdDot">&bull;</span><span>&nbsp;</span></h1>

	<div id="menuBar">
		<a href="/" class="selected">Hidden page</a>
	</div>

	<div id="sliderContainer">
		<div id="sliderValue">&nbsp;</div>
		<div id="slider">&nbsp;</div>
	</div>
</div>

<div id="mainPanel">

	<div id="notificationPanel">
		I'm not perfect also...
	</div>

	<div class='entry error selectedEntry'>
		<div class='entryHeader'>
			<div class="count">&nbsp;</div>

			<div class='message'><c:out value="${cause}"/></div>
			<div class='messageOverlay'>&nbsp;</div>
			<div class='times'>
				<span class='applicationId'>self</span> &mdash; right now
			</div>
		</div>
		<div class='entryContainer'>
			<div class="entryContent">
				<pre class="stacktrace"><%
					StringWriter writer = new StringWriter();
					cause.printStackTrace(new PrintWriter(writer));
					out.write(writer.toString());
				%></pre>
			</div>
		</div>
	</div>

</div>
</body>
</html>
