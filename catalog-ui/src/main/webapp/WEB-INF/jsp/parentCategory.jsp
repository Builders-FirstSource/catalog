<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ include file="/WEB-INF/jsp/include.jsp" %>

<%!
	String toolsStrHover = "";
	String seasonStrHover = "";
	String plumbStrHover = "";
	String paintStrHover = "";
	String hsStrHover = "";
	String hwStrHover = "";
	String gardenStrHover = "";
	String elcStrHover = "";
	String bmStrHover = "";
%>

<%!

String ripStar(String str) {
    String s = str.replaceAll("@",",");
    //System.out.println("s ->"+s);
    return s;
}

String hoverString(String string) {
    String str = "";
    StringBuffer sb = new StringBuffer();

    //System.out.println("string -> "+string);

    if(!(string.equals("undefined"))) {
	    java.util.StringTokenizer st = new java.util.StringTokenizer(string, ",");
		while (st.hasMoreTokens()) {

		     sb.append(ripStar(st.nextToken())).append("%").append(ripStar(st.nextToken())).append("%");
		     try {
		     	str = sb.toString();
		        if(str !=null && str.length() > 1)
		        str = str.substring(0, str.length()-1);
		     } catch (Exception exp) { exp.printStackTrace(); }
		}
		//System.out.println("str -> "+str);
    }
	return str;
}
%>
<%

if(request.getParameter("toolsHover") != null) {
	//toolsStrHover = hoverString(request.getParameter("toolsHover"));
	session.setAttribute("toolsHover",hoverString(request.getParameter("toolsHover")));
}
toolsStrHover = (String)session.getAttribute("toolsHover");

if(request.getParameter("seasonalHover") != null) {
    //seasonStrHover = hoverString(request.getParameter("seasonalHover"));
    session.setAttribute("seasonalHover",hoverString(request.getParameter("seasonalHover")));
}
seasonStrHover = (String)session.getAttribute("seasonalHover");


if(request.getParameter("plumbHover") != null) {
    //plumbStrHover = hoverString(request.getParameter("plumbHover"));
    session.setAttribute("plumbHover",hoverString(request.getParameter("plumbHover")));
}
plumbStrHover = (String)session.getAttribute("plumbHover");


if(request.getParameter("paintHover") != null) {
    //paintStrHover = hoverString(request.getParameter("paintHover"));
    session.setAttribute("paintHover",hoverString(request.getParameter("paintHover")));
}
paintStrHover = (String)session.getAttribute("paintHover");


if(request.getParameter("hsHover") != null) {
    //hsStrHover = hoverString(request.getParameter("hsHover"));
    session.setAttribute("hsHover",hoverString(request.getParameter("hsHover")));
}
hsStrHover = (String)session.getAttribute("hsHover");


if(request.getParameter("hwHover") != null) {
	//hwStrHover = hoverString(request.getParameter("hwHover"));
    session.setAttribute("hwHover",hoverString(request.getParameter("hwHover")));
}
hwStrHover = (String)session.getAttribute("hwHover");


if(request.getParameter("gardenHover") != null) {
	//gardenStrHover = hoverString(request.getParameter("gardenHover"));
    session.setAttribute("gardenHover",hoverString(request.getParameter("gardenHover")));
}
gardenStrHover = (String)session.getAttribute("gardenHover");


if(request.getParameter("elcHover") != null) {
	//elcStrHover = hoverString(request.getParameter("elcHover"));
    session.setAttribute("elcHover",hoverString(request.getParameter("elcHover")));
}
elcStrHover = (String)session.getAttribute("elcHover");

if(request.getParameter("bmHover") != null) {
	//bmStrHover = hoverString(request.getParameter("bmHover"));
    session.setAttribute("bmHover",hoverString(request.getParameter("bmHover")));
}
bmStrHover = (String)session.getAttribute("bmHover");

%>

<script type="text/javascript">

function Create2DArray(rows) {
	var arr = [];
	for (var i=0;i<rows;i++) {
		arr[i] = [];
	}
	return arr;
}

function reCreateHover(Str) {
	var recreated_array = new Array();
	var array = new Array();
	array = Str.split("%");
	if(recreated_array.length==0) {
		recreated_array = Create2DArray(array.length/2);
		var j=0;
		for(var i=0;i<array.length;i++) {
			recreated_array[j][0] = array[i];
			recreated_array[j][1] = array[++i];
			j++;
		}
	}
	return recreated_array;
}
var tools_array;
if([${toolsmenutxt}].length != 0){
	var toolsarray = [${toolsmenutxt1}];
	tools_array = [${toolsmenutxt}];
} else  {
	tools_array = reCreateHover('<%=toolsStrHover%>');
}
//alert('tools_array '+ tools_array);

var toolsmenu={divclass:'anylinkmenu', inlinestyle:'', linktarget:''}
toolsmenu.items= tools_array;

if([${gardenmenutxt}].length != 0){
	var gardenarray = [${gardenmenutxt1}];
	var garden_array = [${gardenmenutxt}];
} else  {
	var garden_array = reCreateHover('<%=gardenStrHover%>');
}
var gardenmenu={divclass:'anylinkmenu', inlinestyle:'', linktarget:''} //First menu variable. Make sure "anylinkmenu1" is a unique name!
gardenmenu.items=garden_array;


if([${bmmenutxt}].length != 0){
	var bmarray = [${bmmenutxt1}];
	var bm_array = [${bmmenutxt}];
} else  {
	var bm_array = reCreateHover('<%=bmStrHover%>');
}
var bmmenu={divclass:'anylinkmenu', inlinestyle:'', linktarget:''}
bmmenu.items=bm_array;


if([${hardwaremenutxt}].length != 0){
	var hwarray = [${hardwaremenutxt1}];
	var hw_array = [${hardwaremenutxt}];
} else  {
	var hw_array = reCreateHover('<%=hwStrHover%>');
}
var hardwaremenu={divclass:'anylinkmenu', inlinestyle:'', linktarget:''}
hardwaremenu.items=hw_array;

if([${paintmenutxt}].length != 0){
	var paintarray = [${paintmenutxt1}];
	var paint_array = [${paintmenutxt}];
} else  {
	var paint_array = reCreateHover('<%=paintStrHover%>');
}
var paintmenu={divclass:'anylinkmenu', inlinestyle:'', linktarget:''}
paintmenu.items=paint_array;

if([${plumbmenutxt}].length != 0){
	var plumbarray = [${plumbmenutxt1}];
	var plumb_array = [${plumbmenutxt}];
} else  {
	var plumb_array = reCreateHover('<%=plumbStrHover%>');
}
var plumbmenu={divclass:'anylinkmenu', inlinestyle:'', linktarget:''}
plumbmenu.items=plumb_array;


if([${electricalmenutxt}].length != 0){
	var elcarray = [${electricalmenutxt1}];
	var elc_array = [${electricalmenutxt}];
} else  {
	var elc_array = reCreateHover('<%=elcStrHover%>');
}
var electricalmenu={divclass:'anylinkmenu', inlinestyle:'', linktarget:''}
electricalmenu.items=elc_array;


if([${homestoragemenutxt}].length != 0){
	var hsarray = [${homestoragemenutxt1}];
	var hs_array = [${homestoragemenutxt}];
} else  {
	var hs_array = reCreateHover('<%=hsStrHover%>');
}
var homestoragemenu={divclass:'anylinkmenu', inlinestyle:'', linktarget:''}
homestoragemenu.items=hs_array;


if([${seasonalmenutxt}].length != 0){
	var seasonarray = [${seasonalmenutxt1}];
	var season_array = [${seasonalmenutxt}];
} else  {
	var season_array = reCreateHover('<%=seasonStrHover%>');
}
var seasonalmenu={divclass:'anylinkmenu', inlinestyle:'', linktarget:''}
seasonalmenu.items=season_array;

</script>


<ul id="primaryNav" class="clearfix">
    <li><a href="/catalog/store">Search:</a></li>
    <li><a class="menuanchorclass" href="/catalog/store/Building_Materials"  rel="bmmenu">Building Materials</a></li>
    <li><a class="menuanchorclass" href="/catalog/store/Electrical"  rel="electricalmenu">Electrical</a></li>
    <li><a class="menuanchorclass" href="/catalog/store/Garden"  rel="gardenmenu">Garden</a></li>
    <li><a class="menuanchorclass" href="/catalog/store/Hardware"  rel="hardwaremenu">Hardware</a></li>
    <li><a class="menuanchorclass" href="/catalog/store/Home&Storage"  rel="homestoragemenu">Home &amp; Storage</a></li>
    <li><a class="menuanchorclass" href="/catalog/store/Paint"  rel="paintmenu">Paint</a></li>
    <li><a class="menuanchorclass" href="/catalog/store/Plumbing"  rel="plumbmenu">Plumbing</a></li>
    <li><a class="menuanchorclass" href="/catalog/store/Seasonal"  rel="seasonalmenu">Seasonal</a></li>
    <li><a class="menuanchorclass" href="/catalog/store/Tools"  rel="toolsmenu">Tools</a></li>
</ul>


