<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<div id="searchBox">
	<form method="get" action="">
		<input type="text" name="query" id="searchInput" accesskey="S" value="${param['query']}" autocomplete="off"/>
		<!--<div id="suggest">
			<ul>
				<li>First item</li>
				<li class="selectedItem">Second item</li>
			</ul>
		</div>-->
	</form>

	<span>Try: <a href="#" class="asynchronous"
	              onclick="$('#searchInput').val($(this).text()); $('#searchInput').focus();return false;"
		>at: frontend occured: last 2 days</a></span>
</div>