<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags" %>

<%@attribute name="severity" type="java.lang.String" required="true" %>

<div id="severityContainer">
	<table>
		<tr>
			<td class="label">Severity:</td>
			<td>
				<ul id="severityMenu">
					<li class="selected"><span><c:out value="${severity}"/></span>
					<l:severity-position severity="error" currentSeverity="${severity}" />
					<l:severity-position severity="warning" currentSeverity="${severity}" />
					<l:severity-position severity="info" currentSeverity="${severity}" />
					<l:severity-position severity="debug" currentSeverity="${severity}" />
					<l:severity-position severity="trace" currentSeverity="${severity}" />
				</ul>
			</td>
		</tr>
	</table>
</div>