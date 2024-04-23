<%@ include file="/WEB-INF/jsp/include.jsp" %>

<c:choose>
	<c:when test="${!empty displayProducts}">
		
		<c:choose>
            <c:when test="${displayProducts[0].stockingLocationPresent}">
                <div class="span-12 columns" >
                    <c:url value="/storeLocator/findAStore.htm" var="setStoreUrl">
                       <c:param name="toUrl" value="${currentCategory.generatedUrl}"/>
                    </c:url>
                    <div class="span-6 column" style="float:right; text-align:right;">
                        Viewing Store: <span class="storeAvailName"><a class="storeAvailName" href="${setStoreUrl}" title="Click to select a new store">${displayProducts[0].store.name}</a></span>
                        <br/>
                        <span class="storeAvailPhone">${displayProducts[0].store.phone}</span>
                    </div>
                </div>
            </c:when>  
        </c:choose>
		
		<div class="span-12 columns" >
			<div class="span-5 column" style="float:right;">
				<b>Sort by:</b> 
                   <select id="catalogSort"  onchange="updateSearchFilterResults();">
	   		 		<option value="featured">Featured</option>
	   		 		<option value="priceL">Price - Low to High</option>
	   		 		<option value="priceH">Price - High to Low</option>
	   		 		<option value="manufacturerA">Manufacturer A-Z</option>
					<option value="manufacturerZ">Manufacturer Z-A</option>
				</select>
			</div>
		</div>

        <!-- paging controls -->
        <c:choose>
          <c:when test="${totalPages > 1}">
          
              <div class="span-12 columns" >
                 <div class="pagination" style="float:right;">

                    <c:forEach begin="1" var="x" end="${totalPages}">
                      <c:url value="/${currentCategory.generatedUrl}" var="nextPageLink">
                        <c:param name="page" value="${x-1}"/>
                      </c:url>
                      
                      <c:choose>
                          <c:when test="${selectedPage == (x-1)}">
                              <a class="current" href="${nextPageLink}">${x}</a>
                          </c:when>
                          <c:otherwise>
                              <a href="${nextPageLink}">${x}</a>
                          </c:otherwise>
                      </c:choose>
                      
                    
                    </c:forEach>
                    <br style="clear:both;" />
                 </div>
              </div>
          </c:when>
        </c:choose>
		
	 	<c:forEach var="product" items="${displayProducts}" varStatus="status">
			<c:choose>
				<c:when test="${!( empty displayProduct.promoMessage)}"> <div class="span-11 columns productResults featuredProduct"> 
				</c:when>
				<c:otherwise> <div class="span-11 columns productResults"> 
				</c:otherwise>
			</c:choose>
				<div class="span-3 column productResultsImage" align="center">
					<a href="/catalog/${currentCategory.generatedUrl}?productId=${product.id}">
						<img border="0" title="${product.name}" alt="${product.name}" src="/catalog${product.productImages.small}" onerror="this.src='/catalog/images/nopic_small.jpg';"/>
					</a>
				</div>
				<div class="span-7 column productResultsInfo">
					<blc:productLink product="${product}" />  <br/>
					<c:if test="${!(empty product.manufacturer) }" >
						<span> <b>Manufacturer:</b> ${product.manufacturer} </span> <br/>
					</c:if>
					<c:if test="${!(empty product.model) }" >
						<span> <b>Model:</b> ${product.model} </span> <br/>
					</c:if>
                    <c:if test="${!(empty product.skus[0].sku) }" >
                        <span> <b>SKU:</b> ${product.skus[0].sku} </span> <br/>
                    </c:if>
				</div>
				<div class="span-5 column productResultsRightCol" style="float:right;text-align:right;">
					<span class="productPrice">
						<c:choose>
						    <c:when test="${product.stockingLocationPresent}">
								<c:choose>
									<c:when test="${product.skus[0].salePrice != product.skus[0].retailPrice }" >
										<span class="salePrice"><b>Sale:</b> $<c:out value="${product.skus[0].salePrice}" /></span>&nbsp;
		                                <span class="uom" style=""><c:out value="${product.uom}" /></span>
										<br/><span class="originalPrice">$<c:out value="${product.skus[0].retailPrice}" /></span>
									</c:when>			
									<c:otherwise>
										<span class="productPrice">$<c:out value="${product.skus[0].retailPrice}" /></span>&nbsp;
										<span class="uom" style=""><c:out value="${product.uom}" /></span>
									</c:otherwise>
								</c:choose>
								
								<br/><span class="productAvailability"><c:out value="${product.availability.availabilityStatus}" /></span>
								<span style="display:none">onhand:<c:out value="${product.availability.quantityOnHand}" /></span>
							</c:when>
							<c:otherwise>
							    <c:url value="/storeLocator/findAStore.htm" var="setStoreUrl">
                                    <c:param name="toUrl" value="${currentCategory.generatedUrl}"/>
                                </c:url>
							    <span><a href="${setStoreUrl}">Choose</a> a store to see<br/> prices and availability</span>
							</c:otherwise>
						</c:choose>
					</span> <br/><br/>
				</div>
				<c:choose>
					<c:when test="${ !( empty displayProduct.promoMessage)}"> 
						<div class="span-13">
							<span class="featuredProductPromo"> <b>${displayProduct.promoMessage} </b></span>
						</div> 
					</c:when>
			</c:choose>
			</div>
		</c:forEach>
		
		<!-- paging controls -->
		<c:choose>
		  <c:when test="${totalPages > 1}">
		  
		      <div class="span-12 columns" >
                 <div class="pagination" style="float:right;">

		            <c:forEach begin="1" var="x" end="${totalPages}">
		              <c:url value="/${currentCategory.generatedUrl}" var="nextPageLink">
                        <c:param name="page" value="${x-1}"/>
                      </c:url>
		              
		              <c:choose>
		                  <c:when test="${selectedPage == (x-1)}">
		                      <a class="current" href="${nextPageLink}">${x}</a>
		                  </c:when>
		                  <c:otherwise>
		                      <a href="${nextPageLink}">${x}</a>
		                  </c:otherwise>
		              </c:choose>
		              
		            
		            </c:forEach>
		            <br style="clear:both;" />
		         </div>
		      </div>
		  </c:when>
		</c:choose>
		
	</c:when>
</c:choose>
