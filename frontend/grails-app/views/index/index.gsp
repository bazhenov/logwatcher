<html>
<head>
	<title>An Example Page</title>
	<meta name="layout" content="main"/>

</head>
<body>

<div id="header">
	<g:link controller="index" action="index" params="['date': today]">today</g:link> |
	<g:link controller="index" action="index" params="['date': today.minusDay(1)]">yesterday</g:link>
</div>

<g:each in="${entries}" var="entry">
	<g:entry ref="${entry}"/>
</g:each>

</body>
</html>