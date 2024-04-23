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

import java.io.File;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.broadleafcommerce.catalog.dao.CategoryDao;
import org.broadleafcommerce.catalog.dao.ProductDao;
import org.broadleafcommerce.catalog.domain.Category;
import org.broadleafcommerce.catalog.domain.Product;
import org.broadleafcommerce.catalog.domain.Sku;
import org.broadleafcommerce.catalog.domain.SkuAttribute;
import org.broadleafcommerce.inventory.domain.SkuAvailability;
import org.broadleafcommerce.inventory.domain.SkuAvailabilityImpl;
import org.broadleafcommerce.inventory.service.AvailabilityService;
import org.broadleafcommerce.store.domain.Store;
import org.springframework.stereotype.Service;

import com.probuild.retail.web.catalog.domain.Item;
import com.probuild.retail.web.catalog.ext.dao.CatalogImagesDaoImpl;
import com.probuild.retail.web.catalog.ext.dao.SkuDaoExt;
import com.probuild.retail.web.catalog.ext.domain.ProductExtImpl;
import com.probuild.retail.web.catalog.ext.domain.SkuExtImpl;
import com.probuild.retail.web.catalog.ext.domain.SkuAvailabilityExtImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service("blCatalogServiceExt")
public class CatalogServiceExtImpl implements CatalogServiceExt {

	public static final Logger logger = LoggerFactory.getLogger(CatalogServiceExtImpl.class);

	private static String PATH;
	private static final String SERVER_IMG_PATH_SMALL = "/images/products/small/";
    private static final String SERVER_IMG_PATH_LARGE = "/images/products/large/";
    private Map<String, List<Category>> childCategoryURLMap;
    private long childCategoryURLMapTime = System.currentTimeMillis();
    //Category Hashmap is re-created every 5 mins(5*60)
    private long refreshinterval = 300; //Time in seconds

    @Resource(name="blCategoryDaoExt")
    protected CategoryDao categoryDao;

    @Resource(name="blProductDaoExt")
    protected ProductDao productDao;

    @Resource(name="blSkuDaoExt")
    protected SkuDaoExt skuDao;

    @Resource(name="blAvailabilityService")
    protected AvailabilityService availabilityService;

    /*@Resource(name="blSkuAvailabilityDaoExt")
    protected SkuAvailabilityDaoImpl skuAvailService;
*/
    //public Product findProductById(Long productId) {
    //    return productDao.readProductById(productId);
    //}

    public CatalogServiceExtImpl ( ) {
	    // set the application path
	    String envImageFolder = System.getenv( "CATALOG_IMAGE_DIR" );
	    if ( envImageFolder == null ) {
	        PATH = "";
	    } else {
	        System.out.println ( "using image folder at " + envImageFolder );
	        PATH = envImageFolder;
	    }
	}



    public List<Product> findProductsByName(String searchName) {
        return productDao.readProductsByName(searchName);
    }

    public List<Product> findActiveProductsByCategory(Category category) {

//    	List<Product> products =
//    				productDao.readActiveProductsByCategory(category.getId());
        List<Product> products = skuDao.readActiveProductsByCategory(category.getId());
        //System.out.println("Performance : category.getId() -> "+category.getId());
        //System.out.println("Performance : products.size() -> "+products.size());
        // TODO: check the performance of this loop
    	// get the sku attributes for each product sku
        /*long lDateTime = new Date().getTime();
        System.out.println("Performance : forLoop on Products Start Date() - Time in milliseconds: " + lDateTime);*/

        List<Long> ids = new ArrayList<Long>(1);

        if(products.size() != 0) {
            for ( Product p : products ) {
                ids.add( p.getSkus().get(0).getId() );
                /*SkuExtImpl skuExt = (SkuExtImpl)p.getSkus().get(0);
      		    skuExt.setAttributes( skuDao.readSkuAttributesBySkuId( ids ) );
        		if(!(skuAttrib.isEmpty())) {
                skuExt.setAttributes( skuAttrib);
        		}*/
            }
            /*
             * @Comment: Implementation altered to overcome performance issue
             */
            List<SkuAttribute> skuAttrib = skuDao.readSkuAttributesBySkuId(ids);
            for(Product p : products) {
                Long skuId = p.getSkus().get(0).getId();
                List<SkuAttribute> skuAttribTmp = new ArrayList<SkuAttribute>();
                for (SkuAttribute sk : skuAttrib) {
                    Long skuAId = sk.getSku().getId();
                    if (skuAId.equals(skuId)){
                        skuAttribTmp.add(sk);
                    }
                }
                SkuExtImpl skuExt = (SkuExtImpl)p.getSkus().get(0);
                if(!(skuAttribTmp.isEmpty())) {
                    skuExt.setAttributes(skuAttribTmp);
                }
            }
        }
        /*long lDateTime1 = new Date().getTime();
        System.out.println("Performance : forLoop on Products End Date() - Time in milliseconds: " + lDateTime1);*/
        return products;
    }


    public List<Product> findActiveProductsByWithAvailabilityCategory(
                                               Category category, Long locId ) {

        List<Product> products = findActiveProductsByCategory( category );

        // get the location name
        Store store = skuDao.readStoreById( locId );

        // TODO: check the performance of this loop
        // get the sku attributes for each product sku
        /*for ( Product p : products ) {
            if ( p.getAllSkus() == null || p.getAllSkus().size() == 0 )
                continue;

            ProductExtImpl product = (ProductExtImpl)p;

            Long skuId = p.getSkus().get(0).getId();
            SkuAvailability skuAvail = availabilityService.lookupSKUAvailabilityForLocation( skuId, locId, true );

            // check for null before setting...
            if ( skuAvail == null ) { // no record found
                skuAvail = new SkuAvailabilityImpl();
                product.setAvailability( skuAvail );
                skuAvail.setAvailabilityStatus( "Call" );
                skuAvail.setQuantityOnHand( 0 );
                skuAvail.setReserveQuantity( 0 );
            }

            product.setAvailability( skuAvail );
            product.setStockingLocationPresent( true );
            product.setPriceInventoryHidden( false );
            product.setStockingLocationName( store.getName() );
            product.setStore( store );

        }*/
        /*
         * @Comment: Implementation altered to overcome performance issue
         */
        List<Long> skuIds = new ArrayList<Long>();

        if(products != null) {
            for(Product p: products) {
                if( p.getAllSkus() == null || p.getAllSkus().size() == 0) {
                    continue;
                }
                else {
                    skuIds.add(p.getSkus().get(0).getId());
                }
            }
        }

        List<SkuAvailability> skuAvailList = new ArrayList<SkuAvailability>();
        if( !(skuIds.isEmpty()) && (skuIds != null)) {
            skuAvailList = availabilityService.lookupSKUAvailabilityForLocation(skuIds, locId, true);
        }
        if(products != null) {
            for(Product p : products) {
                if ( p.getAllSkus() == null || p.getAllSkus().size() == 0 ) {
                    continue;
                }
                ProductExtImpl product = (ProductExtImpl)p;
                Long skuId = p.getSkus().get(0).getId();
                for(SkuAvailability skuAvail :skuAvailList) {
                    if((skuAvail != null) && (skuId.equals(skuAvail.getSkuId()))) {
                        product.setAvailability( skuAvail );
                        product.setStockingLocationPresent( true );
                        product.setPriceInventoryHidden( false );
                        product.setStockingLocationName( store.getName() );
                        product.setStore( store );
                    }
                    else if(skuAvail == null) {
                        skuAvail = new SkuAvailabilityImpl();
                        skuAvail.setAvailabilityStatus( "Call" );
                        skuAvail.setQuantityOnHand( 0 );
                        skuAvail.setReserveQuantity( 0 );
                        product.setAvailability( skuAvail );
                    }
                }
            }
        }
        return products;
    }


    public List<Product> findActiveProductsByCategory(
                           Category category, int pageNum, int displayCount ) {

        List<Product> products = skuDao.readActiveProductsByCategory(
                                   category.getId(), pageNum, displayCount );

        // get the sku attributes for each product sku
        for ( Product p : products ) {

            List<Long> ids = new ArrayList<Long>(1);
            ids.add( p.getSkus().get(0).getId() );
            SkuExtImpl skuExt = (SkuExtImpl)p.getSkus().get(0);
            skuExt.setAttributes( skuDao.readSkuAttributesBySkuId( ids ) );
        }

        return products;
    }

    public List<Product> findActiveProductsByWithAvailabilityCategory(
               Category category, Long locId, int pageNum, int displayCount ) {

        List<Product> products = findActiveProductsByCategory(
                                            category, pageNum, displayCount );

        // get the location name
        Store store = skuDao.readStoreById( locId );

        // TODO: check the performance of this loop
        // get the sku attributes for each product sku
        /*for ( Product p : products ) {
            if ( p.getAllSkus() == null || p.getAllSkus().size() == 0 )
                continue;

            ProductExtImpl product = (ProductExtImpl)p;

            Long skuId = p.getSkus().get(0).getId();
            SkuAvailability skuAvail = availabilityService.
                        lookupSKUAvailabilityForLocation( skuId, locId, true );
            // check for null before setting...
            if ( skuAvail == null ) { // no record found
                skuAvail = new SkuAvailabilityImpl();
                product.setAvailability( skuAvail );
                skuAvail.setAvailabilityStatus( "Call" );
                skuAvail.setQuantityOnHand( 0 );
                skuAvail.setReserveQuantity( 0 );
            }

            product.setAvailability( skuAvail );
            product.setStockingLocationPresent( true );
            product.setPriceInventoryHidden( false );
            product.setStockingLocationName( store.getName() );
            product.setStore( store );

        }*/
        /*
         * @Comment: Implementation altered to overcome performance issue
         */
        List<Long> skuIds = new ArrayList<Long>();

        if(products != null) {
            for(Product p: products) {
                if( p.getAllSkus() == null || p.getAllSkus().size() == 0) {
                    continue;
                }
                else {
                    skuIds.add(p.getSkus().get(0).getId());
                }
            }
        }

        List<SkuAvailability> skuAvailList = new ArrayList<SkuAvailability>();
        if( !(skuIds.isEmpty()) && (skuIds != null)) {
            skuAvailList = availabilityService.lookupSKUAvailabilityForLocation(skuIds, locId, true);
        }
        if(products != null) {
            for(Product p : products) {
                if ( p.getAllSkus() == null || p.getAllSkus().size() == 0 ) {
                    continue;
                }
                ProductExtImpl product = (ProductExtImpl)p;
                Long skuId = p.getSkus().get(0).getId();
                for(SkuAvailability skuAvail :skuAvailList) {
                    if((skuAvail != null) && (skuId.equals(skuAvail.getSkuId()))) {
                        product.setAvailability( skuAvail );
                        product.setStockingLocationPresent( true );
                        product.setPriceInventoryHidden( false );
                        product.setStockingLocationName( store.getName() );
                        product.setStore( store );
                    }
                    else if(skuAvail == null) {
                        skuAvail = new SkuAvailabilityImpl();
                        skuAvail.setAvailabilityStatus( "Call" );
                        skuAvail.setQuantityOnHand( 0 );
                        skuAvail.setReserveQuantity( 0 );
                        product.setAvailability( skuAvail );
                    }
                }
            }
        }


        return products;

    }

    public int findActiveProductsCountByCategory ( Category category ) {
        return skuDao.readActiveProductsByCategorySize( category.getId() );
    }

    public Product saveProduct(Product product) {
        return productDao.save(product);
    }

    public Category findCategoryById(Long categoryId) {
        return categoryDao.readCategoryById(categoryId);
    }

    public Category findCategoryByName(String categoryName) {
        return categoryDao.readCategoryByName(categoryName);
    }

    public Category saveCategory(Category category) {
        return categoryDao.save(category);
    }

    public void removeCategory(Category category){
    	categoryDao.delete(category);
    }

    public List<Category> findAllCategories() {
        return categoryDao.readAllCategories();
    }

    public List<Product> findAllProducts() {
        //return categoryDao.readAllProducts();
        return skuDao.readAllProducts();
    }

    public List<Sku> findAllSkus() {
        return skuDao.readAllSkus();
    }

    public Sku findSkuById(Long skuId) {
        return skuDao.readSkuById(skuId);
    }

    public Sku saveSku(Sku sku) {
        return skuDao.save(sku);
    }

    public List<Sku> findSkusByIds(List<Long> ids) {
        return skuDao.readSkusById(ids);
    }

    public void setProductDao(ProductDao productDao) {
        this.productDao = productDao;
    }

    public void setSkuDao(SkuDaoExt skuDao) {
        this.skuDao = skuDao;
    }

    public List<Product> findProductsForCategory(Category category) {
        //return productDao.readProductsByCategory(category.getId());
        return skuDao.readProductsByCategory(category.getId());
    }

    public Product findProductWithAvailabilityById ( Long id, Long locId ) {

        Store store = skuDao.readStoreById( locId );

        ProductExtImpl product = (ProductExtImpl)skuDao.readProductById( id );
        Long skuId = product.getSkus().get(0).getId();
        SkuAvailability skuAvail = availabilityService.
                    lookupSKUAvailabilityForLocation( skuId, locId, true );
        // check for null before setting...
        if ( skuAvail == null ) { // no record found
            skuAvail = new SkuAvailabilityImpl();
            product.setAvailability( skuAvail );
            skuAvail.setAvailabilityStatus( "Call" );
            skuAvail.setQuantityOnHand( 0 );
            skuAvail.setReserveQuantity( 0 );
        }

        product.setAvailability( skuAvail );
        product.setStockingLocationPresent( true );
        product.setPriceInventoryHidden( false );
        product.setStockingLocationName( store.getName() );
        product.setStore( store );

        return product;
    }

    public Product findProductById ( Long id ) {
        return skuDao.readProductById( id );
    }

    public List<Product> findProductByIds(List<Long> productIds) {
        return skuDao.readProductByIds(productIds);
    }


    public Product findProductBuSkuNum ( String skuNum ) {
        return skuDao.readProductBySkuNum( skuNum );
    }

    public void setCategoryDao(CategoryDao categoryDao) {
        this.categoryDao = categoryDao;
    }


    public Map<String, List<Category>> getChildCategoryURLMapByCategoryId(Long categoryId) {
        Category category = findCategoryById(categoryId);
        System.out.println ( "Categoy found by id " + category.getName() + ", " + category.getId() );
            if (category != null) {
                /*
                 * @Comment: Hashmap is re-created at the end of "refreshinterval" time.
                 *           This fix is given for drilling down new/updated categories.
                 *           The URL should be re-loaded.
                 */
                if(childCategoryURLMap == null || ((System.currentTimeMillis()-childCategoryURLMapTime)/1000 > refreshinterval)) {
                    System.out.println ( "childCategoryURLMap is null so going to create one" );
                    /*To re-create a category map if already exists*/
                    if(childCategoryURLMap != null) {
                        if(!(childCategoryURLMap.isEmpty())) {
                            childCategoryURLMap.clear();
                        }
                    }
                    childCategoryURLMap = category.getChildCategoryURLMap();
                    childCategoryURLMapTime = System.currentTimeMillis();
                }
            }
            category.setChildCategoryURLMap(childCategoryURLMap);
            return childCategoryURLMap;
    }


    /** added for the demo **/

	public List<SkuAttribute> findAllSkuAttributes() {
		return skuDao.readAllSkuAttributes();
	}

	public List<SkuAttribute> findSkuAttributes(Sku sku) {
		List<Long> ids = new ArrayList<Long>(1);
		ids.add( sku.getId() );
		return skuDao.readSkuAttributesBySkuId( ids );
	}

	public SkuAttribute saveSkuAttribute(SkuAttribute attrib) {
		return skuDao.save( attrib );
	}

    public void deleteSkuAttribute(SkuAttribute attrib) {
        skuDao.delete( attrib );
    }

    public void deleteProduct(Product product) {
        productDao.delete( product );
    }

    public void deleteSku ( Sku sku ) {
        skuDao.delete( sku );
    }

    public void deleteSkuAvailBasedOnSkuId (List<SkuAvailabilityExtImpl> SkuId) {

        System.out.println("In CatalogServiceExtImpl - deleteSkuAvailBasedOnSkuId -- skuid -- "+SkuId);
        skuDao.deleteSkuAvailbasedOnSkuId(SkuId);
    }

    public List<SkuAvailabilityExtImpl>readSKUAvailabilityBasedOnSkuId(Long skuId) {
        List<SkuAvailabilityExtImpl> skuAvail = new ArrayList<SkuAvailabilityExtImpl>();
        skuAvail = skuDao.readSKUAvailabilityForSkuId(skuId);
        if(skuAvail != null) {
            return skuAvail;
        }
        return null;
    }

    /*
     * @comment: Deletes the image files on the server
     * @author: Prathibha
     * (non-Javadoc)
     * @see com.probuild.retail.web.catalog.ext.service.CatalogServiceExt#removeImages(com.probuild.retail.web.catalog.domain.Item)
     */
    public void removeImages(Item item) {

    	String sku = item.getSku().toString();
    	String model = item.getModelNum();

    	File smallImageDir = new File ( PATH + SERVER_IMG_PATH_SMALL);
    	File largeImageDir = new File ( PATH + SERVER_IMG_PATH_LARGE);

        NameFileFilter filter = new NameFileFilter (
              new String[] {
               sku+".gif", sku+"A.gif", sku+"B.gif", sku+"C.gif", sku+"D.gif",
               sku+".jpg", sku+"A.jpg", sku+"B.jpg", sku+"C.jpg", sku+"D.jpg",
               sku+".png", sku+"A.png", sku+"B.png", sku+"C.png", sku+"D.png",
               model+".gif", model+"A.gif", model+"B.gif", model+"C.gif", model+"D.gif",
               model+".jpg", model+"A.jpg", model+"B.jpg", model+"C.jpg", model+"D.jpg",
               model+".png", model+"A.png", model+"B.png", model+"C.png", model+"D.png"},
               IOCase.INSENSITIVE );

        String[] img = smallImageDir.list( filter );

        if(img != null) {
            for ( String fileName : img ) {

                File smallImage = new File( PATH + SERVER_IMG_PATH_SMALL + fileName);
            	if(smallImage.exists()) {

            	    System.out.println("Found small image "+ fileName+ "to be deleted");
            		smallImage.delete();

            		// Delete the small image from image table (moved out of if condition)
                    //skuDao.removeCatalogImageByName(SERVER_IMG_PATH_SMALL + fileName);
            	 }
            }
        }

        String[] imgL = largeImageDir.list( filter );

        if(imgL != null) {
            for(String fileName : imgL ) {

                File largeImage = new File ( PATH + SERVER_IMG_PATH_LARGE + fileName );
            	if (largeImage.exists()) {

            		System.out.println("Found large image "+ fileName+ "to be deleted");
            		largeImage.delete();

                    // Delete the large image from image table (moved out of if condition)
            		//skuDao.removeCatalogImageByName(SERVER_IMG_PATH_LARGE + fileName);
           	    }
            }
        }

        // Removes images from the database
        skuDao.removeCatalogImageByProductId(item.getId());
    }

    /*
     * @comment: Save the jpg image into the database
     * @author:
     * (non-Javadoc)
     * @see com.probuild.retail.web.catalog.ext.service.CatalogServiceExt#saveDBImage(long productId, String name, String type, String imageName, byte[] bytes)
     */
    public void saveDBImage(long productId, String name, char type, String imageName, byte[] bytes, char newImage) {

        skuDao.saveImage(productId, name, type, imageName, bytes, newImage);
    }

    /*
     * @comment: Save the jpg image into the database
     * @author:
     * (non-Javadoc)
     * @see com.probuild.retail.web.catalog.ext.service.CatalogServiceExt#saveDBImage(String type, String imageName, byte[] bytes)
     */
    public byte[] readDBImage(String type, String imageName) {

        CatalogImagesDaoImpl ci = skuDao.findCatalogImageByName(imageName);
        if(ci == null) {
            return null;
        }

        return ci.getImage();
    }


    /*
     * @comment: Returns the database Image from the database
     * @author:
     * (non-Javadoc)
     * @see com.probuild.retail.web.catalog.ext.service.CatalogServiceExt#readDBImageforProductId(Long ProductId, String imageType, String name)
     */
    public List<CatalogImagesDaoImpl> readDBImageforProductId(Long ProductId) {

        List<CatalogImagesDaoImpl> ci = skuDao.findCatalogImageByProductId(ProductId);

        if(ci == null) {
            return null;
        }
        return ci;
    }

    public List<CatalogImagesDaoImpl> readDBImagesforProductIds(List<Long>ProductIds, char newImage) {

        List<CatalogImagesDaoImpl> ci = skuDao.findCatalogImagesByProductIds(ProductIds,newImage);

        if(ci == null) {
            return null;
        }
        return ci;
    }

    /*
     * @comment: Updates the newImage field to N in the database based on Product ID
     * @author:
     * (non-Javadoc)
     * @see com.probuild.retail.web.catalog.ext.service.CatalogServiceExt#updateNewImageToNforProductId(Long ProductId, char newImage)
     */
    public void updateNewImageToNforProductId(List<Long> ProductId, char newImage) {

        skuDao.updateNewImageByProductId(ProductId, newImage);
    }

}
