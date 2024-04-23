<%@ include file="/WEB-INF/jsp/include.jsp"%>
<tiles:insertDefinition name="baseNoSide">
<tiles:putAttribute name="mainContent" type="string">

    <c:url value="${currentCategory.generatedUrl}" var="backToUrl">
        <c:param name="productId" value="${currentProduct.id}"/>
    </c:url>
    <c:url value="/storeLocator/findAStore.htm" var="setStoreUrl">
        <c:param name="toUrl" value="${backToUrl}"/>
    </c:url>

	<div class="breadcrumb">
		<blc:breadcrumb categoryList="${breadcrumbCategories}" />
	</div>
	<div id="productContainer" class="mainContentArea">
	    <div style="position:relative;float:right;margin-right:5px;">
		    <c:choose>
	            <c:when test="${currentProduct.stockingLocationPresent}">

	                <div class="span-6 column" style="float:right; text-align:right;">
                        Viewing Store: <span class="storeAvailName"><a href="${setStoreUrl}" title="Click to select a new store">${currentProduct.store.name}</a></span>
                        <br/>
                        <span class="storeAvailPhone">${currentProduct.store.phone}</span>
                    </div>

	            </c:when>
	        </c:choose>
        </div>

		<h3 class="productName">${currentProduct.name}</h3>
		<jsp:include page="relatedProducts.jsp" />
		<div class="columns">
			<div class="column productImage span-6">
				<c:choose>
					<c:when test="${!empty currentProduct.productImages.large}">
						<a href="/catalog${currentProduct.productImages.large}" class="thickbox">
							<img src="/catalog${currentProduct.productImages.large}" style="max-width: 200px;" onerror="this.src='/catalog/images/nopic_large.jpg';"/>
							<p>View larger image</p>
						</a>
					</c:when>
					<c:otherwise>
					<img src="/catalog/images/nopic_large.jpg" width="200" />
<!--						Image not available-->
					</c:otherwise>
				</c:choose>

				<div id="additionalPics">
		            <c:choose>
		                <c:when test="${!empty currentProduct.productImages.small1}">
		                    <div style="position:relative; float:left; margin: 5px;">
                                <a href="/catalog${currentProduct.productImages.large1}" class="thickbox">
                                   <img src="/catalog${currentProduct.productImages.small1}"/>
                                </a>
                            </div>
		                </c:when>
		                <c:otherwise>
		                </c:otherwise>
		            </c:choose>

				    <c:choose>
                        <c:when test="${!empty currentProduct.productImages.small2}">
                            <div style="position:relative; float:left; margin: 5px;">
                                <a href="/catalog${currentProduct.productImages.large2}" class="thickbox">
                                   <img src="/catalog${currentProduct.productImages.small2}"/>
                                </a>
                            </div>
                        </c:when>
                        <c:otherwise>
                        </c:otherwise>
                    </c:choose>

                    <c:choose>
                        <c:when test="${!empty currentProduct.productImages.small3}">
                            <div style="position:relative; float:left; margin: 5px;">
                                <a href="/catalog${currentProduct.productImages.large3}" class="thickbox">
                                   <img src="/catalog${currentProduct.productImages.small3}"/>
                                </a>
                            </div>
                        </c:when>
                        <c:otherwise>
                        </c:otherwise>
                    </c:choose>

                    <c:choose>
                        <c:when test="${!empty currentProduct.productImages.small4}">
                            <div style="position:relative; float:left; margin: 5px;">
                                <a href="/catalog${currentProduct.productImages.large4}" class="thickbox">
                                   <img src="/catalog${currentProduct.productImages.small4}"/>
                                </a>
                            </div>
                        </c:when>
                        <c:otherwise>
                        </c:otherwise>
                    </c:choose>
				</div>

			</div>
			<div class="column productSummary span-11">
				<div class="bottomRule">
					<c:if test="${currentProduct.isFeaturedProduct}">
						<img class="featuredSmall" src="/catalog/images/featuredSmallRed.gif" /><br/>
					</c:if>
					<c:if test="${!empty currentProduct.manufacturer}" >
						<b>Manufacturer:</b> ${currentProduct.manufacturer}<br/>
					</c:if>
					<c:if test="${!empty currentProduct.model}" >
						<b>Model:</b> ${currentProduct.model}<br/>
					</c:if>
					<b>SKU:</b> ${currentProduct.skus[0].sku}<br/>
                    <%-- <b>Dept:</b> ${currentProduct.department}<br/>--%>
                    <%--
                         @Comment: Dept is modified to display Parent Category name
                         @Date: 01-03-2012
                         @Author: Prathibha
                     --%>
                    <b>Dept:</b> ${breadcrumbCategories[1].name}<br/>
					<c:if test="${!empty currentProduct.longDescription}" >
						<b>Description:</b><pre style="white-space: pre-wrap; word-wrap: break-word;font-family:inherit;font-style:inherit;font-weight:inherit;">${currentProduct.longDescription}</pre><br/>
					</c:if>
					<%--
                         @Comment: Displays SKU Attributes
                         @Date: 01-17-2012
                         @Author: Prathibha
                     --%>
					<b>Specifications:</b><br/>
					<c:choose>
						<c:when test="${!empty skuAttributes}">
							<c:forEach var="attrib" items="${skuAttributes}" >
								<b><c:out value="${attrib.value}"/>:</b>     <c:out value="${attrib.key}"/>
								<br/>
							</c:forEach>
						</c:when>
						<c:otherwise>
                    		<span>Not Available</span>
                    	</c:otherwise>
					</c:choose>
				</div>
				<div class="columns">
					<div class="productLeftCol column span-7">
						<c:if test="${!(empty currentProduct.weight.weight)}" >
							<span> <b>Weight: </b> ${currentProduct.weight.weight} lb</span> <br/>
						</c:if>
						<c:if test="${!(empty currentProduct.dimension.width) and (currentProduct.dimension.width != '0.00' or currentProduct.dimension.depth != '0.00' or currentProduct.dimension.height != '0.00')}" >
							<span> <b>Dimensions (WDH): </b> ${currentProduct.dimension.width} X
							${currentProduct.dimension.depth} X ${currentProduct.dimension.height}  </span> <br/>
						</c:if>
					</div>
					<div class="productRightCol">
						<c:choose>
                            <c:when test="${currentProduct.stockingLocationPresent}">
                                <span class="productPrice">
                                    <b> Our Price: </b>
		                            <c:choose>
		                                <c:when test="${currentProduct.skus[0].salePrice != currentProduct.skus[0].retailPrice}" >
		                                    <span class="strikethrough"><c:out value="${currentProduct.skus[0].retailPrice}" /></span>
		                                    <span>$<c:out value="${currentProduct.skus[0].salePrice}" /></span>
		                                </c:when>
		                                <c:otherwise>
		                                    $<c:out value="${currentProduct.skus[0].retailPrice}" />
		                                </c:otherwise>
		                            </c:choose>
		                        </span>

                                <br/><span class="productAvailability">${currentProduct.availability.availabilityStatus}</span><br/>
                                <span style="display:none">onhand:${currentProduct.availability.quantityOnHand} /></span>
                                <span id="showMoreAvailLink"><a href="" onclick="$('#availabilityParentwindow').fadeIn();$('#showMoreAvailLink').css('display','none');$('#hideMoreAvailLink').css('display','block');return false;">check other stores</a></span>
                                <span id="hideMoreAvailLink" style="display:none;"><a href="" onclick="$('#availabilityParentwindow').css('display','none');$('#hideMoreAvailLink').css('display','none');$('#showMoreAvailLink').css('display','block');return false;">hide other stores</a></span>
                                <div id="availabilityParentwindow" style="display:none;">
                                   <%@ include file="/WEB-INF/jsp/catalog/availability.jsp"%>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <span><a href="${setStoreUrl}">Check Availability &amp; Price</a></span>
                            </c:otherwise>
                        </c:choose>

						<br/><br/>

					</div>
				</div>

			</div>

		</div>

	</div>
	</tiles:putAttribute>
</tiles:insertDefinition>