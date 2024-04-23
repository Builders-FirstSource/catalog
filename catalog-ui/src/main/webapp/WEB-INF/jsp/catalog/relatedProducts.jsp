<%@ include file="/WEB-INF/jsp/include.jsp"%>
<c:if test="${!empty currentProduct.upSaleProducts || !empty currentProduct.crossSaleProducts}">
			<div class="relatedProducts">
				<div class="productUpSale">
					<c:if test="${!empty currentProduct.upSaleProducts}" >
						<h3 class="relatedProd">You might also like</h3>
						<c:forEach var="item" items="${currentProduct.upSaleProducts}" varStatus="status">
							<div class="relatedProd clearfix">
								<div class="relatedProdImage">
									<a href="${contextPath}${item.relatedProduct.productImages.small}" class="thickbox">
										<img src="/catalog${item.relatedProduct.productImages.small}" width="80" />
									</a>
								</div> 
								<div class="relatedProdText">
									<p>
										<a href="/catalog/${currentCategory.generatedUrl}?productId=${item.relatedProduct.id}">
											${item.relatedProduct.name}
										</a>
									</p>
									
									<c:choose>
                                        <c:when test="${currentProduct.stockingLocationPresent}">
											<p>Our Price: <br/>
												<c:choose>
													<c:when test="${item.relatedProduct.skus[0].salePrice != item.relatedProduct.skus[0].retailPrice }" >
														<span class="strikethrough"><c:out value="${item.relatedProduct.skus[0].retailPrice}" /></span>
														<c:out value="${item.relatedProduct.skus[0].salePrice}" />
													</c:when>			
													<c:otherwise>
														<c:out value="${item.relatedProduct.skus[0].retailPrice}" />
													</c:otherwise>
												</c:choose>
											</p>
									    </c:when>
									</c:choose>
								</div>
							</div>
						</c:forEach>
					</c:if>
				</div> 
				<div class="productCrossSale">
					<c:if test="${!empty currentProduct.crossSaleProducts}" >
						<h3 class="relatedProd">Related products</h3>
						<c:forEach var="item" items="${currentProduct.crossSaleProducts}" varStatus="status">
							<div class="relatedProd clearfix">
								<div class="relatedProdImage">
									<a href="${contextPath}${item.relatedProduct.productImages.small}" class="thickbox">
										<img src="/catalog${item.relatedProduct.productImages.small}" width="80" />
									</a>
								</div> 
								<div class="relatedProdText">
									<p>
<!--                                        <a href="/catalog/${currentCategory.generatedUrl}?productId=${item.relatedProduct.id}">-->
										<a href="/catalog/${item.relatedProduct.defaultCategory.generatedUrl}?productId=${item.relatedProduct.id}">
											${item.relatedProduct.name}
										</a>
									</p>
									
									<c:choose>
                                        <c:when test="${currentProduct.stockingLocationPresent}">
											<p>Our Price: <br/>
												<c:choose>
													<c:when test="${item.relatedProduct.skus[0].salePrice != item.relatedProduct.skus[0].retailPrice }" >
														<span class="strikethrough">${item.relatedProduct.skus[0].retailPrice}</span>
														${item.relatedProduct.skus[0].salePrice}
													</c:when>			
													<c:otherwise> ${item.relatedProduct.skus[0].retailPrice} </c:otherwise>
												</c:choose>
											</p>
									   </c:when>
									</c:choose>
									
								</div>
							</div>
						</c:forEach>
					</c:if>
				</div>
			</div>
</c:if>