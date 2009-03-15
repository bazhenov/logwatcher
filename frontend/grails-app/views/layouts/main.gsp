<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
<head>
	<title>LogViewer Application</title>
	<link rel="stylesheet" href="${createLinkTo(dir: 'css', file: 'main.css')}"/>
	<meta content="text/html;charset=UTF8"/>
	<script type="text/javascript" src="${createLinkTo(dir: 'js', file: 'application.js')}"></script>
</head>

<body>

<h1>LogWatcher</h1>

<table id="layout">
	<tr>
		<td id="linksBlock"/>
	</tr>
	<tr>
		<td id="content">
			<g:layoutBody/>
		</td>
	</tr>
</table>

</body>
</html>
