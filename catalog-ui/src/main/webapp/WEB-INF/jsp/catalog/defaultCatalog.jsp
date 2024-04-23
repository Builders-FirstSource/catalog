<%@ include file="/WEB-INF/jsp/include.jsp" %>
<tiles:insertDefinition name="baseNoSide">
	<tiles:putAttribute name="mainContent" type="string">

<!--	<script>-->
<!--		function updateSearchFilterResults() {-->
<!--			$('#mainContent').prepend("<div class='grayedOut'><img style='margin-top:25px' src='/catalog/images/ajaxLoading.gif'/></div>");-->
<!--			var postData = $('#refineSearch').serializeArray();-->
<!--			postData.push({name:'ajax',value:'true'});-->
<!--			postData.push({name:'catalogSort',value:$('#catalogSort option:selected').val()});-->
<!--			$('#mainContent').load($('#refineSearch').attr('action'), postData);-->
<!--		}-->
<!--	</script>-->

	<div class="breadcrumb">
		<blc:breadcrumb categoryList="${breadcrumbCategories}" />
	</div>

	<div class="catalogContainer columns mainContentAreaFull" style="padding:8px 0 8px 8px;">
    <div class="span-6">
  		<form:form method="post" id="refineSearch" commandName="doSearch">

  		<blc:searchFilter categories="${displayCategories}" queryString="">
			<blc:searchFilterItem property="name" displayTitle="Categories" />
  		</blc:searchFilter>

		<blc:searchFilter products="${completeProdListInCat}" queryString="">

      	Show results by:
				<blc:searchFilterItem property="manufacturer" displayTitle="Manufacturers"/>
				<br />
				 <blc:searchFilterItem property="skus[0].salePrice" displayTitle="Prices" displayType="sliderRange"/>
                 <blc:searchFilterAttribItem property="filter" displayTitle="Filters"/>
			<br />

			</blc:searchFilter>

			<blc:displayFeaturedProducts products="${displayProducts}" var="featuredProducts" maxFeatures="3">
				<br/><br/>

				<c:if test="${!(empty featuredProducts)}">
					<h3>Featured </h3>
					<c:forEach var="product" items="${featuredProducts}" >
						<div align="center">
							<a href="/catalog/${currentCategory.generatedUrl}?productId=${product.id}">
								<c:out value="${product.name}"/>
							</a><br>
							<a href="/catalog/${currentCategory.generatedUrl}?productId=${product.id}">
								<img border="0" src="/catalog${product.productImages.small}" width="75"/>
							</a>
						</div>
						<br/>
					</c:forEach>
				</c:if>
			</blc:displayFeaturedProducts>

		</form:form>
	</div>
    <div class="span-12" id="mainContent">
        <jsp:include page="/WEB-INF/jsp/catalog/categoryView/mainContentFragment.jsp" />
    </div>

    </div>

	</tiles:putAttribute>
</tiles:insertDefinition>
