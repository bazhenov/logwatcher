<html>
<head>
	<meta content="text/html;charset=UTF8"/>
	<meta name="layout" content="main"/>
</head>
<body>

<h1>LogWatcher</h1>

<div id="menuBar">
	<a href="${createLink(controller: 'index', action: 'index', params: [date: today])}">cегодня</a>
	<a href="${createLink(controller: 'index', action: 'index', params: [date: today.minusDay(1)])}">вчера</a>
	<a href="${createLink(controller: 'index', action: 'index', params: [date: today.minusDay(2)])}">позавчера</a>
</div>

<div id="mainPanel">
	<h3><%= date %></h3>

	<g:each in="${entries}" var="entry">
		<f:entry ref="${entry}"/>
	</g:each>
</div>
</body>
</html>
