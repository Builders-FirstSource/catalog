package com.probuild.retail.web.catalog.ext.service;

import java.util.List;
import java.util.Map;

import org.broadleafcommerce.catalog.domain.Product;

//import org.broadleafcommerce.catalog.domain.Category;
//import org.broadleafcommerce.catalog.domain.Product;
//import org.broadleafcommerce.catalog.domain.RelatedProduct;
//import org.broadleafcommerce.catalog.domain.Sku;

import com.probuild.retail.web.catalog.domain.Item;
import com.probuild.retail.web.catalog.domain.ItemFilter;
import com.probuild.retail.web.catalog.domain.ItemGroup;
import com.probuild.retail.web.catalog.domain.ItemInventory;
import com.probuild.retail.web.catalog.ext.domain.SkuAvailabilityExtImpl;
import com.probuild.retail.web.catalog.repository.ItemRepository;

public interface WebCatalogService {
//	public List<Category> findAllParentCategories( Long categoryId );
//	public Product saveProductImages(long productId, Map<String,String> images );
//	public Product saveProductShallow(Product product);
//	public Product saveProductRelatedProduct ( RelatedProduct item );

	/** categories management ***/
	public List<ItemGroup> findAllCategories();
	public List<ItemGroup> findAllChildCategories ( Long parentId );
	public List<Item> findProductsForCategory( Long groupId );
	public List<Product> findAllProducts();
	public List<Item> findAllItemProducts();
	public ItemGroup removeCategory ( Long groupId );
	public ItemGroup saveItemGroup( ItemGroup group );

	public void saveItemPrice( Item item );
	public Item saveItem( Item item );
	public void removeItem ( Item item );
	public int saveItems ( List<Item> items );
	public Item findItemBySkuNum ( String sku );
	public Item findItemByItemId ( Long id );

	public List<ItemFilter> findAllItemFilters ( );
	public List<ItemFilter> findItemFilters ( Long productId );
	public int saveItemFilters ( List<ItemFilter> filters );

	public boolean imageExists ( String type, String imageName );
	public byte[] readImage ( String type, String imageName );

	public long sendImage ( long productId, String name, char type, String imageName,
	        byte[] bytes, char newImage);

	public int saveItemInventory ( List<ItemInventory> inventories );
//	public void removeAllItemFilters ( Sku sku );
	public boolean saveSplashHtml ( String html );
	public String getSplashHtml ( );

	public boolean saveParentMenuHtml ( String html );
	public String getParentMenuHtml ( );

}
