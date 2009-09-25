<html>
<head>
	<meta content="text/html;charset=UTF8"/>
	<meta name="layout" content="main"/>
</head>
<body>

<h1>LogWatcher</h1>

<div id="menuBar">
	<g:each in="${linkDates}">
		<g:set var="href" value="${createLink(controller: 'index', action: 'index', params: [date: it.value])}" />
		<g:if test="${it.value == date}">
			<a href="${href}" class="selected">${it.key}</a>
		</g:if>
		<g:else>
			<a href="${href}">${it.key}</a>
		</g:else>
	</g:each>
</div>

<div id="mainPanel">
	<h2>Записи за <%= dateAsString %></h2>

	<g:if test="${entries.size() > 0}">
		<g:each in="${entries}" var="entry">
			<f:entry ref="${entry}"/>
		</g:each>
		<div id="footer">
			Всего <%= entries.collect { it.count }.sum().pluralize("сообщение сообщения сообщений") %>
			сгрупированных в <%= entries.size().pluralize("запись записи записей") %>
		</div>
	</g:if>
	<g:else>
		<div id="notificationPanel">
			Плохо дело. Нет записей.
		</div>
	</g:else>
</div>
</body>
</html>
