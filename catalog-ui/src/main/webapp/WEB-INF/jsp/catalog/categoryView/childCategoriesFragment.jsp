<%@ include file="/WEB-INF/jsp/include.jsp" %>
<c:forEach var="childCategory" items="${currentCategory.childCategories}" varStatus="status">
      	<blc:productsForCategory var="products" categoryId="${childCategory.id}"/>
      	<c:if test="${fn:length(products) ge 0}">
       	<div class="span-4 <c:if test="${status.index != 0 && (status.index + 1) % 3 == 0}">last</c:if>" align="center">
			<a href="/catalog/${childCategory.generatedUrl}" style="text-decoration: none;">
				<img border="0" title="${childCategory.name}" alt="${childCategory.name}" src="/catalog${childCategory.categoryImages.small}" onerror="this.src='/catalog/images/nopic_small.jpg';"/>
			</a><br/>
			<a class="noTextUnderline" href="/catalog/${childCategory.generatedUrl}"><b>${childCategory.name}</b></a>
		</div>
		<c:if test="${status.index != 0 && (status.index + 1) % 3 == 0}">
			<div class="span-13">&nbsp;</div>
		</c:if>
	</c:if>
</c:forEach>