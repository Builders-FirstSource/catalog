/*
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.probuild.retail.web.catalog.ext.service;

import java.util.List;
import java.util.Map;

import org.broadleafcommerce.catalog.domain.Category;
import org.broadleafcommerce.catalog.domain.Product;
import org.broadleafcommerce.catalog.domain.Sku;
import org.broadleafcommerce.catalog.domain.SkuAttribute;
import org.broadleafcommerce.catalog.service.CatalogService;
import com.probuild.retail.web.catalog.domain.Item;
import com.probuild.retail.web.catalog.ext.dao.CatalogImagesDaoImpl;
import com.probuild.retail.web.catalog.ext.domain.SkuAvailabilityExtImpl;

public interface CatalogServiceExt extends CatalogService {

    public Product saveProduct(Product product);

    public Product findProductById(Long productId);

    public List<Product> findProductsByName(String searchName);

    public List<Product> findActiveProductsByCategory(Category category);

    public Category saveCategory(Category category);

    public void removeCategory(Category category);

    public Category findCategoryById(Long categoryId);

    public Category findCategoryByName(String categoryName);

    public List<Category> findAllCategories();

    public List<Product> findAllProducts();

    public List<Product> findProductsForCategory(Category category);

    public Sku saveSku(Sku sku);

    public List<Sku> findAllSkus();

    public List<Sku> findSkusByIds(List<Long> ids);

    public Sku findSkuById(Long skuId);

    public Map<String, List<Category>> getChildCategoryURLMapByCategoryId(Long categoryId);

    /** added for the demo **/
    public List<SkuAttribute> findAllSkuAttributes ( );
    public List<SkuAttribute> findSkuAttributes ( Sku sku );
    public SkuAttribute saveSkuAttribute ( SkuAttribute attrib );
    public void deleteSkuAttribute ( SkuAttribute attrib );
    public Product findProductBuSkuNum ( String skuNum );
    public void deleteProduct ( Product product );
    public void deleteSku ( Sku sku );

    public List<Product> findActiveProductsByWithAvailabilityCategory(
                                                Category category, Long locId );
    public Product findProductWithAvailabilityById ( Long id, Long locId );

    public List<Product> findActiveProductsByWithAvailabilityCategory (
                 Category category, Long locId, int pageNum, int displayCount );
    public List<Product> findActiveProductsByCategory(
                             Category category, int pageNum, int displayCount );

    public int findActiveProductsCountByCategory ( Category category );

    /*
     * @comment:Deletes the image files on the server
     */
    public void removeImages(Item item);

    /*
     * Save the image into the database
     */
	public void saveDBImage(long productId, String name, char type, String imageName, byte[] bytes, char newImage);

    /*
     * Read the image from the database
     */
	public byte[] readDBImage(String name, String imageName);
	/*
     * Read the image from the database based on Product ID
     */
	public List<CatalogImagesDaoImpl> readDBImageforProductId(Long ProductId);
	/*
	 * Adding public List<CatalogImagesDaoImpl> readDBImagesforProductIds(List<Long>ProductIds)
	 * to overcome performance issue on search
	 */
	public List<CatalogImagesDaoImpl> readDBImagesforProductIds(List<Long>ProductIds,char newImage);
	/*
     * Updates the newImage field to N in the database based on Product ID
     */
    public void updateNewImageToNforProductId(List<Long> ProductId, char newImage);
    /*
     * Adding public List<Product> findProductByIds(List<Long> productIds);
     * to overcome performance issue  on search
     */
    public List<Product> findProductByIds(List<Long> productIds);
    /*
     * Implementation to delete entries in BLC_SKU_AVAILABILITY table based on sku id
     */
    public void deleteSkuAvailBasedOnSkuId (List<SkuAvailabilityExtImpl> SkuId);
    /*
     * Implementation to read entries in BLC_SKU_AVAILABILITY table based on sku id
     */
    public List<SkuAvailabilityExtImpl>readSKUAvailabilityBasedOnSkuId(Long skuId);
}
