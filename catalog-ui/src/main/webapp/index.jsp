<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%-- Redirected because we can't set the welcome page to a virtual URL. --%>


<SCRIPT>

var browserName=navigator.appName;

if (browserName == "Netscape") {
	window.location.assign("/catalog/store");
	//window.location.replace(url);
} else if (browserName=="Microsoft Internet Explorer") {
	window.location.reload("/catalog/store");
}

</SCRIPT>
