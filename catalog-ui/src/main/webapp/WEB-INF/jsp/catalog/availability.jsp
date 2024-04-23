<%@ include file="/WEB-INF/jsp/include.jsp" %>

<script>
    function updateAvailabilityResults() {
        //$('#mainContent').prepend("<div class='grayedOut'><img style='margin-top:25px' src='/catalog/images/ajaxLoading.gif'/></div>");
        var postData = $('#availForm').serializeArray();
        //postData.push({name:'ajax',value:'true'});
        $('#availabilityWindow').load('/catalog/${currentCategory.generatedUrl}', postData);
    }
</script>


<div id="availabilityWindow" style="width:435px; position:relative;float:right;text-align:left;margin-bottom:10px;">
    <span style="font-weight:bold;">Find Your Product at Another Store</span>
    <form method="post" action="" id="availForm" style="text-align:center;">
        <input type="hidden" name="ajaxItemAvail" id="ajaxItemAvail" value="true"/>
        <input type="hidden" name="productId" id="productId" value="${currentProduct.id}"/>
        Zipcode: <input type="text" name="zipcode" id="zipcode" size="5" value="${searchZip}"/>
        &nbsp;Distance: <input type="text" name="distance" id="distance" size="5" value="${searchDistance}"/>
        <input type="button" value="Find Nearest Stores" onclick="updateAvailabilityResults();" id="check"/><br/>
    </form>
    <div id="availList" style="font-size:8pt;">
        <div class="inventoryWarning">
            <table class="inventoryWarningTable"><tr><td style="text-align:center;">
            <img src="/catalog/images/dialog-warning.png"/>
            </td>
            <td style="text-align:left;">
                Stock levels fluctuate through out the day.
                You should call to verify stock at the desired store.
            </td></tr></table>
        </div>

        <c:choose>
            <c:when test="${! (empty otherStoreAvailability)}">
	            <c:forEach var="otherStoreAvail" items="${otherStoreAvailability}">
	                <c:url var="directionUrl" value="http://maps.google.com/">
	                   <c:param name="daddr">${otherStoreAvail.store.address1}, ${otherStoreAvail.store.city}, ${otherStoreAvail.store.state}, ${otherStoreAvail.store.zip}</c:param>
	                </c:url>
	                <div style="width:425px; margin-left:10px">
	                  <div class="storeAvailLevel" style="padding-right:5px;float:left;width:55px;text-align:right;height:30px;">${otherStoreAvail.availability.availabilityStatus}<span style="display:none">onhand:${otherStoreAvail.availability.quantityOnHand}</span></div>
	                  <div class="storeAvailName" style="float:left;width:360px;height:15px;">${otherStoreAvail.store.name} - ${otherStoreAvail.store.phone}</div>
	                  <div class="storeAvailAddress" style="float:left;width:360px;height:15px;">
	                     <a href="${directionUrl}" target="_blank" title="Directions">${otherStoreAvail.store.address1}, ${otherStoreAvail.store.city}, ${otherStoreAvail.store.state}, ${otherStoreAvail.store.zip}</a>
	                  </div>
	                  <div style="float:left;width:400px;height:10px;"></div>
	                </div>
	            </c:forEach>
            </c:when>
            <c:otherwise>
            	<br/>
				<h4>${message}</h4>
            </c:otherwise>
        </c:choose>
    </div>

</div>