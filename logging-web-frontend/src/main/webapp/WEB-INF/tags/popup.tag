<%@attribute name="id" type="java.lang.String" required="true" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div class="popupContainer" id="${id}" style="display: none">
	<div style="position: relative">

		<img class="mark" src="/images/popup-mark.png" alt="" style="position: absolute; top: -10px; left: 30px"/>

		<div class="outerBlock">
			<div class="innerBlock">
				<jsp:doBody />
			</div>
		</div>
	</div>
</div>