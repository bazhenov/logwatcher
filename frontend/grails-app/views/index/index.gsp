<html>
<head>
	<title>An Example Page</title>
	<meta name="layout" content="main"/>

</head>
<body>

<g:each in="${entries}" var="entry">
	<g:entry ref="${entry}"/>
</g:each>

<g:entry title="subject" content="My text"/>

<g:entry title="Empty info" severity="info"/>
<g:entry title="Warning" severity="warning" content="Some text"/>
<g:entry title="Achtung" severity="error" content="Some text"/>
</body>
</html>