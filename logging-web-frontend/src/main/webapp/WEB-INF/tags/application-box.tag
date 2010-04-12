<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@attribute name="applicationIds" type="java.util.Set" required="true" %>
<%@attribute name="currentApplicationId" type="java.lang.String" required="true" %>

<table width="100%">
  <tr>
    <td align="center">

      <table id="applicationBox">
        <tr>
          <td width="100%">
						<c:forEach items="${applicationIds}" var="id">
							<c:url value="/feed/${id}" var="url" />
							<c:choose>
								<c:when test="${id eq currentApplicationId}">
									<span class="active"><a href='${url}'><c:out value="${id}" /></a></span>
								</c:when>
								<c:otherwise>
									<span><a href='${url}'><c:out value="${id}" /></a></span>
								</c:otherwise>
							</c:choose>
						</c:forEach>
          </td>
        </tr>
      </table>

    </td>
  </tr>
</table>
