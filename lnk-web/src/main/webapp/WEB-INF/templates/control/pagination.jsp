<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<div>
    <select id="pagination-ps" class="form-control pull-left" style="width: 115px" onchange="return changePs();">
        <option ${request.ps == 10 ? 'selected' : ''} value="10">每页10条</option>
        <option ${request.ps == 50 ? 'selected' : ''} value="50">每页50条</option>
        <option ${request.ps == 100 ? 'selected' : ''} value="100">每页100条</option>
        <option ${request.ps == 200 ? 'selected' : ''} value="200">每页200条</option>
        <option ${request.ps == 300 ? 'selected' : ''} value="300">每页300条</option>
        <option ${request.ps == 400 ? 'selected' : ''} value="400">每页400条</option>
        <option ${request.ps == 500 ? 'selected' : ''} value="500">每页500条</option>
        <option ${request.ps == 1000 ? 'selected' : ''} value="1000">每页1000条</option>
    </select>
    <ul id="pagination-pn" class="pagination pagination-sm no-margin pull-right">
    <c:choose>
      <c:when test="${request.curn <= 1}">
        <li class="disabled"><a href="javascript:void(0)">首页</a></li>
      </c:when>
      <c:otherwise>
        <li><a href="${url}?curn=1&ps=${request.ps}&${condition}">首页</a></li>
      </c:otherwise>
    </c:choose>
    <c:choose>
      <c:when test="${request.curn > 1}">
        <li><a href="${url}?curn=${request.curn-1}&ps=${request.ps}&${condition}">«上一页</a></li>
      </c:when>
      <c:otherwise>
        <li class="disabled"><a href="javascript:void(0)">«上一页</a></li>
      </c:otherwise>
    </c:choose>
    <c:forEach var="i" begin="${request.curn-5 <= 0 ? 1 : (request.curn+5 <= request.totaln ? request.curn-5 : (request.totaln-10 > 0 ? request.totaln-10 : 1))}" end="${request.curn<=6 ? (request.totaln-10>=0 ? 10 : request.totaln) : (request.totaln-request.curn>=5 ? request.curn+4 : request.totaln)}" step="1">
        <li ${request.curn == i ? "class='active'" : ""}><a href="${url}?curn=${i}&ps=${request.ps}&${condition}">${i}</a></li>
    </c:forEach>
    <c:choose>
      <c:when test="${request.curn < request.totaln}">
        <li><a href="${url}?curn=${request.curn+1}&ps=${request.ps}&${condition}">»下一页</a></li>
      </c:when>
      <c:otherwise>
        <li class="disabled"><a href="javascript:void(0)">»下一页</a></li>
      </c:otherwise>
    </c:choose>
    <c:choose>
      <c:when test="${request.curn == request.totaln || request.totaln == 0}">
        <li class="disabled"><a href="javascript:void(0)">末页</a></li>
      </c:when>
      <c:otherwise>
        <li><a href="${url}?curn=${request.totaln}&ps=${request.ps}&${condition}">末页</a></li>
      </c:otherwise>
    </c:choose>
    </ul>
    <span class="pull-right" style="margin:5px 10px">总数:${request.count},&nbsp;&nbsp;共${request.totaln}页</span>
</div>
<script>
function changePs() {
    var ps = document.getElementById("pagination-ps").value;
    location.href = "${url}?curn=1&ps=" + ps + "&${condition}";
    return;
}
</script>