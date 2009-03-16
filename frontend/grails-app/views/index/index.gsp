<html>
<head>
	<title>An Example Page</title>
	<meta name="layout" content="main"/>

</head>
<body>

<g:each in="${entries}" var="entry">
	<g:entry ref="${entry}"/>
</g:each>

</body>
</html>