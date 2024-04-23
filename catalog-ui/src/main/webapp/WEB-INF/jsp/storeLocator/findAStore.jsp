<%@ include file="/WEB-INF/jsp/include.jsp" %>
<tiles:insertDefinition name="baseNoSide">
	<tiles:putAttribute name="mainContent" type="string">
<div class="mainContentAreaFull" style="padding:8px 0 8px 8px;">



			<div class="columns">
				<div class="column span-15">
					<h3 class="pageTitle">Find a Store Near You</h3>
					<form:form method="post" action="findStores.htm" commandName="findAStoreForm">
						<table>
							<tr>
								<td>
								   <table class="basicTable">
									<thead>
									<tr>
										<th colspan="2">Address Information</th>
									</tr>
									</thead>
									<tr>
										<td style="text-align : right">Zip code</td>
										<td><form:input path="postalCode" size="5"/></td>
									</tr>
									<tr>
										<td style="text-align : right">Distance (miles)</td>
										<td><form:input path="distance" size="3" /></td>
									</tr>
									<tr>
										<td>&nbsp;</td>
										<td><input type="submit" name="Find" value="Find"/></td>
									</tr>
   									</table>
								</td>
								<td>
									<table class="basicTable">
										<tr>
											<td style="text-align : right"><u></>Store Hours :</u><br/>Mon - Fri: 7:00 am - 6:00 pm<br/>Sat: 8:00 am - 5:00 pm<br/>Sun: 9:00 am - 4:00 pm</td>
										</tr>
   									</table>
								</td>
							</tr>
						</table>
					</form:form>
				</div>

			<c:if test="${!empty findAStoreForm.storeDistanceMap || errorMessage != null}">
				<div class="leftRule column span-15" style="padding-bottom:18px;">
					<c:choose>
						<c:when test="${errorMessage != null}">
							<h3 class="pageTitle">Store Locations</h3>
							<span>${errorMessage}</span>
						</c:when>
						<c:when test="${!empty findAStoreForm.storeDistanceMap}">
							<h3 class="pageTitle">Store Locations</h3>
							<table class="basicTable">
								<thead>
									<tr>
										<th>Name</th>
										<th>Address</th>
										<th>Driving Distance</th>

									</tr>
								</thead>
								<c:forEach var="entry" items="${findAStoreForm.sortedStoreMap}" varStatus="status">
                                    <tr>
                                        <td><a href="/catalog/storeLocator/set.htm?locId=${entry.value.id}">${entry.value.name}</a></td>
                                        <td>
                                            ${entry.value.address1}
                                            <c:if test="${(entry.value.address2 != null) || !(empty entry.value.address2)}" >
                                                ${entry.value.address2 }<br/>
                                            </c:if>
                                            ${entry.value.city}, ${entry.value.state}, ${entry.value.zip}
                                        </td>
                                        <td><fmt:formatNumber value="${entry.key}" maxFractionDigits="2" /> miles</td>
                                    </tr>
                                </c:forEach>
<!--								<c:forEach var="entry" items="${findAStoreForm.storeDistanceMap}" varStatus="status">-->
<!--									<tr>-->
<!--										<td><a href="/catalog/storeLocator/set.htm?locId=${entry.key.id}">${entry.key.name}</a></td>-->
<!--										<td>-->
<!--											${entry.key.address1}-->
<!--											<c:if test="${(entry.key.address2 != null) || !(empty entry.key.address2)}" >-->
<!--												${entry.key.address2 }<br/>-->
<!--											</c:if>-->
<!--											${entry.key.city}, ${entry.key.state}, ${entry.key.zip}-->
<!--										</td>-->
<!--										<td><fmt:formatNumber value="${entry.value}" maxFractionDigits="2" /> miles</td>-->
<!--									</tr>-->
<!--								</c:forEach> -->
							</table>
						</c:when>
					</c:choose>
				</div>
			</c:if>

			</div>

</div>
	</tiles:putAttribute>
</tiles:insertDefinition>