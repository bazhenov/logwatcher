<html>
<head>
	<title>An Example Page</title>
	<meta name="layout" content="main"/>

</head>
<body>

<div id="header">
	<p>
		<g:link controller="index" action="index" params="[date: today]">today</g:link> |
		<g:link controller="index" action="index" params="[date: today.minusDay(1)]">yesterday</g:link>
	</p>
	<p>
		Фильтр:
		<g:link controller="index" action="index" params="[date: date]">все</g:link>
		<g:each in="${allApps}" var="par">
			<g:link controller="index" action="index" params="[date: date, application: par]">${par}</g:link>
		</g:each>
	</p>
</div>

<g:each in="${entries}" var="entry">
	<g:entry ref="${entry}"/>
</g:each>

</body>
</html>