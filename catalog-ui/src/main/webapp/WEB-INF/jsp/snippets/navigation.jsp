<%@ page trimDirectiveWhitespaces="true"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="spring" uri="/spring"%>
<%@ taglib prefix="form" uri="/spring-form"%>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags"%>

<div id="header">
	<a href="http://www.dixieline.com/"><img class="logo" src="/catalog/images/dixie.jpg" /></a>

<!--<hr style="height:4px; background-color:#663300; color:#663300; width: 100%;"/>-->
<div id="primaryNavBox">
    <%@ include file="/WEB-INF/jsp/parentCategory.jsp"%>
</div>
<!--<hr style="height:4px; background-color:#663300; color:#663300; width: 100%;"/>-->

    <div align="RIGHT" style="position:absolute; top:3px; right:0px;">
        <a href="http://www.dixieline.com/aboutus.html" class="prodloginnav">About Us</a>
        <font color="#808080">&nbsp;·</font>&nbsp;
        <a class="prodloginnav" href="http://www.dixieline.com/news.html">News</a>
        <font color="#808080">&nbsp;·&nbsp; </font>
        <b><a href="http://www.dixieline.com/employment.html" class="prodloginnav">
            <font color="#000000">Careers</font>
        </a></b>&nbsp;
        <font color="#808080"> ·</font>&nbsp;
        <a href="http://www.dixieline.com/search.html" class="prodloginnav">Site Map</a>&nbsp;
        <font color="#808080"> ·</font>
        <font color="#666666"><b>&nbsp;</b></font>
        <b><a href="http://www.dixieline.com/locations.html" class="prodloginnav" id="dixietopnav">
        <font color="#666666">Find a Location</font></a>&nbsp;
        </b>
    </div>

	<div id="searchBar" style="width:400px;height:50px;background: url(/catalog/images/searchgradient.jpg) 0% 0% repeat-x;">
		<table style="width:100%;height:100%">
		  <tr>
		      <td valign="middle" style="width: 100%; text-align: center;">
		      <form id="search" method="post" action="/catalog/search/results.htm">
		         <span style="vertical-align:middle; font-size:12pt; font-weight:bold; color:white;">Item Search:</span>
			     <input class="searchField" type="text" style=font-size:8pt name="queryString" id="queryString" size="33" helpText="Search by name/manufacturer/model#..." />
			     <input type="hidden" name="toolsHover" id="toolsHover"/>
			     <input type="hidden" name="seasonalHover" id="seasonalHover"/>
			     <input type="hidden" name="plumbHover" id="plumbHover"/>
			     <input type="hidden" name="paintHover" id="paintHover"/>
			     <input type="hidden" name="hsHover" id="hsHover"/>
			     <input type="hidden" name="hwHover" id="hwHover"/>
			     <input type="hidden" name="gardenHover" id="gardenHover"/>
			     <input type="hidden" name="elcHover" id="elcHover"/>
			     <input type="hidden" name="bmHover" id="bmHover"/>
			     <input type="image" class="imageBtn" src="/catalog/images/searchBtn.png" alt="Search" />
		      </form>
		      </td>
		  </tr>
		</table>
	</div>
</div>

<%
	String toolsHoverval = request.getParameter("toolsHover");
	String seasonalHoverval = request.getParameter("seasonalHover");
	String plumbHoverval = request.getParameter("plumbHover");

	String paintHoverval = request.getParameter("paintHover");
	String hsHoverval = request.getParameter("hsHover");
	String hwHoverval = request.getParameter("hwHover");

	String gardenHoverval = request.getParameter("gardenHover");
	String elcHoverval = request.getParameter("elcHover");
	String bmHoverval = request.getParameter("bmHover");
%>

<script type="text/javascript">

var val = document.getElementById('toolsHover');
if(val != null && toolsarray != null) {
	document.getElementById('toolsHover').value = toolsarray;
} else {
	document.getElementById('toolsHover').value = '<%=toolsHoverval%>';
}

var val = document.getElementById('seasonalHover');
if(val != null && seasonarray != null ) {
	document.getElementById('seasonalHover').value = seasonarray;
} else {
	document.getElementById('seasonalHover').value = '<%=seasonalHoverval%>';
}

var val = document.getElementById('plumbHover');
if(val != null && plumbarray != null) {
	document.getElementById('plumbHover').value = plumbarray;
} else {
	document.getElementById('plumbHover').value = '<%=plumbHoverval%>';
}


var val = document.getElementById('paintHover');
if(val != null && paintarray != null) {
	document.getElementById('paintHover').value = paintarray;
} else {
	document.getElementById('paintHover').value = '<%=paintHoverval%>';
}


var val = document.getElementById('hsHover');
if(val != null && hsarray != null) {
	document.getElementById('hsHover').value = hsarray;
} else {
	document.getElementById('hsHover').value = '<%=hsHoverval%>';
}


var val = document.getElementById('hwHover');
if(val != null && hwarray != null) {
	document.getElementById('hwHover').value = hwarray;
} else {
	document.getElementById('hwHover').value = '<%=hwHoverval%>';
}



var val = document.getElementById('gardenHover');
if(val != null && gardenarray != null) {
	document.getElementById('gardenHover').value = gardenarray;
} else {
	document.getElementById('gardenHover').value = '<%=gardenHoverval%>';
}


var val = document.getElementById('elcHover');
if(val != null && elcarray != null) {
	document.getElementById('elcHover').value = elcarray;
} else {
	document.getElementById('elcHover').value = '<%=elcHoverval%>';
}


var val = document.getElementById('bmHover');
if(val != null && bmarray != null) {
	document.getElementById('bmHover').value = bmarray;
} else {
	document.getElementById('bmHover').value = '<%=bmHoverval%>';
}


</script>

