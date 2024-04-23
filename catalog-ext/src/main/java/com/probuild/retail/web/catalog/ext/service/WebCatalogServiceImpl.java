package com.probuild.retail.web.catalog.ext.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.annotation.Resource;

import org.apache.commons.io.IOCase;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.broadleafcommerce.catalog.dao.CategoryDao;
import org.broadleafcommerce.catalog.dao.CategoryXrefDao;
import org.broadleafcommerce.catalog.domain.Category;
import org.broadleafcommerce.catalog.domain.CategoryImpl;
import org.broadleafcommerce.catalog.domain.Product;
import org.broadleafcommerce.catalog.domain.ProductWeight;
import org.broadleafcommerce.catalog.domain.RelatedProduct;
import org.broadleafcommerce.catalog.domain.Sku;
import org.broadleafcommerce.catalog.domain.CategoryXref;
import org.broadleafcommerce.catalog.domain.SkuAttribute;
import org.broadleafcommerce.catalog.domain.SkuAttributeImpl;
import org.broadleafcommerce.inventory.domain.SkuAvailability;
import org.broadleafcommerce.inventory.domain.SkuAvailabilityImpl;
import org.broadleafcommerce.inventory.service.AvailabilityService;
import org.broadleafcommerce.util.WeightUnitOfMeasureType;
import org.broadleafcommerce.util.money.Money;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.probuild.retail.web.catalog.domain.Item;
import com.probuild.retail.web.catalog.domain.ItemFilter;
import com.probuild.retail.web.catalog.domain.ItemGroup;
import com.probuild.retail.web.catalog.domain.ItemImage;
import com.probuild.retail.web.catalog.domain.ItemInventory;
import com.probuild.retail.web.catalog.ext.dao.SkuDaoExt;
import com.probuild.retail.web.catalog.ext.dao.SkuDaoExtImpl;
import com.probuild.retail.web.catalog.ext.domain.CrossSaleProductExtImpl;
import com.probuild.retail.web.catalog.ext.domain.ProductExtImpl;
import com.probuild.retail.web.catalog.ext.domain.SkuAvailabilityExtImpl;
import com.probuild.retail.web.catalog.ext.domain.SkuExtImpl;
import com.probuild.retail.web.catalog.repository.AS400ItemRepository;
import com.probuild.retail.web.catalog.repository.ItemRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service("webCatalogService")
public class WebCatalogServiceImpl implements WebCatalogService {

    public static final Logger logger = LoggerFactory.getLogger(WebCatalogServiceImpl.class);


    private static String PATH;// = "C:/java_dev/apache-tomcat-6.0.20/webapps/catalog";



    @Resource(name="blCategoryDaoExt")
    protected CategoryDao categoryDao;

    @Resource(name="blCategoryXrefDao")
    protected CategoryXrefDao categoryXrefDao;

    @Resource(name="blCatalogServiceExt")
    protected CatalogServiceExt catalogService;

    @Resource(name="blAvailabilityService")
    protected AvailabilityService availabilityService;

    @Resource(name="blSkuDaoExt")
    protected SkuDaoExt skuDao;

    //@PersistenceContext(unitName="blPU")
    //protected EntityManager em;

     public AS400ItemRepository repository;


    public WebCatalogServiceImpl ( ) {
        // set the application path
        String envImageFolder = System.getenv( "CATALOG_IMAGE_DIR" );
        if ( envImageFolder == null ) {
            PATH = "";
        } else {
            System.out.println ( "using image folder at " + envImageFolder );
            PATH = envImageFolder;
        }
    }

    @Transactional
    public List<Category> findAllParentCategories(Long categoryId ) {
        List<Category> parents = null;

        Category category = catalogService.findCategoryById( categoryId );
        // force ORM to fetch the data now, rather than later
        category.getAllParentCategories().size();

        parents = category.getAllParentCategories();

        return parents;
    }

    public List<Product> findActiveProductsByCategory(Category arg0) {
        // TODO Auto-generated method stub
        return null;
    }


    @Transactional
    public List<ItemGroup> findAllCategories() {
            List<Category> categories = catalogService.findAllCategories();

            List<com.probuild.retail.web.catalog.domain.ItemGroup> cats =
                    new ArrayList<ItemGroup>( categories.size() );

            for ( Category c : categories ) {

                ItemGroup cat = transform ( c );

                   //if ( cat.getSubCategoryCount() == 0 ) {
                   //     List<Product> products =
                   //             catalogService.findActiveProductsByCategory( c );
                   //     cat.setItemCount( products.size() );
                   // }

                cats.add( cat );
                //System.out.println ( "*-*-*-*-" + cat.toString() + "*-*-*-*" );
                //c.getAllParentCategories().size();
            }
            return cats;

    } // findAllCategoriesClient

    @Transactional
    public List<ItemGroup> findAllChildCategories( Long parentId ) {

        Category c = categoryDao.readCategoryById( parentId );

        List<Category> categories = categoryDao.readAllSubCategories( c );

        List<ItemGroup> groups = new ArrayList<ItemGroup> ( categories.size() );
        for ( Category cat : categories ) {
            cat.getCategoryImages().size();
            ItemGroup group = transform ( cat );

            groups.add( group );
        }

        return groups;

    } // findAllChildCategories


    @Transactional
    public ItemGroup removeCategory(Long groupId) {
        Category category = categoryDao.readCategoryById( groupId );
        categoryDao.delete( category );

        return transform ( category );

    } // removeCategory


    @Transactional
    public List<Product> findAllProducts() {
        return catalogService.findAllProducts();
    }

    @Transactional
    public List<Sku> findAllSkus() {
        return catalogService.findAllSkus();
    }

    @Transactional
    public Category findCategoryById(Long arg0) {
        Category category = catalogService.findCategoryById( arg0 );
        category.getAllChildCategories().size();

        return category;
    }

    @Transactional
    public Category findCategoryByName(String arg0) {
        return catalogService.findCategoryByName( arg0 );
    }

    @Transactional
    public Product findProductById(Long arg0) {
        Product product = catalogService.findProductById( arg0 );

        if ( product != null ) {
            product.getAllSkus().size();
            product.getProductImages().size();
            product.getAllParentCategories().size();
            product.getDefaultCategory().getAllChildCategories();
            product.getDefaultCategory().getAllParentCategories();
            product.getCrossSaleProducts().size();
        }

        return product;
    }

    @Transactional
    public List<Product> findProductsByName(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }



   @Transactional
   public List<Item> findProductsForCategory( Long groupId ) {
       Category c = new CategoryImpl ( );
       c.setId( groupId );
//       List<Product> products = catalogService.findActiveProductsByCategory( c );
       List<Product> products = catalogService.findProductsForCategory( c );

       List<Item> items = new ArrayList<Item>( products.size() );

       for ( Product p : products ) {
           Item item = transform ( p );


           // get related items
           //List<RelatedProduct> relatedProducts = p.getCrossSaleProducts();
           //List<Item> relatedItems = new ArrayList<Item>( relatedProducts.size() );
           //for ( RelatedProduct rp : relatedProducts ) {
           //    Item itm = transform ( rp.getProduct() );
           //    itm.setRelatedId( rp.getId() );
           //}
           //item.setRelatedItems( relatedItems );

//           System.out.println (
//                   "Item " + item.toString() +
//                   " start: " + item.getActiveStartDate() +
//                   " end: " + item.getActiveEndDate() );
//           System.out.println (
//                   "Item " + item.toString() +
//                   " related items: " + item.getRelatedItems().size() );
           //System.out.println (
           //        "********************/////*" );
           items.add( item );

       }

        return items;
    }


   @Transactional
   public List<Item> findAllItemProducts() {

       List<Product> products = catalogService.findAllProducts();
       System.out.println("After findAllProducts -> "+(new Date()).toString());

       List<Item> items = new ArrayList<Item>( products.size() );

       for ( Product p : products ) {
           Item item = transform ( p );
           items.add( item );
       }
       System.out.println("After complete Product to Item transform -> "+(new Date()).toString());
       return items;
    }



    @Transactional
    public Product saveProduct(Product product) {
        List<Sku> skus = new ArrayList<Sku>(product.getAllSkus().size());
        skus.addAll(product.getAllSkus());
        product.getAllSkus().clear();
        for (int i=0; i< skus.size(); i++){
            product.getAllSkus().add(catalogService.saveSku(skus.get(i)));
        }

//      List<RelatedProduct> related = new ArrayList<RelatedProduct>(product.getCrossSaleProducts().size());
//      related.addAll(product.getCrossSaleProducts());
//      product.getCrossSaleProducts().clear();
//      for (int i=0; i< related.size(); i++){
//          product.getCrossSaleProducts().add(catalogService.saveSku(skus.get(i)));
//      }

        return catalogService.saveProduct(mendProductTrees(product));
    }

    @Transactional
    public Product saveProductShallow(Product product) {

        Product existingProd = catalogService.findProductById( product.getId() );
        existingProd.setName( product.getName() );
        existingProd.setLongDescription( product.getLongDescription() );

        List<Sku> skus = new ArrayList<Sku>(product.getAllSkus().size());
        skus.addAll(product.getAllSkus());
        product.getAllSkus().clear();
        for (int i=0; i< skus.size(); i++){
            product.getAllSkus().add(catalogService.saveSku(skus.get(i)));
        }

        return catalogService.saveProduct(mendProductTrees( existingProd ) );
    }


    @Transactional
    public void saveItemPrice( Item item ) {

        /* List<Sku> skus = new ArrayList<Sku>(1);
        skus.add( sku );
        product.setAllSkus( skus );
        catalogService.saveSku( product.getAllSkus().get( 0 ) );*/
        // save the sku if new...TODO


        SkuExtImpl sku = transformToSku ( item );
        Sku saveSku = catalogService.saveSku( sku );

    }


    @Transactional
    public Item saveItem( Item item ) {

        Product product = null;
        SkuExtImpl sku = null;

        product = transform ( item );
        System.out.println ( "Item saved " + product.getId() );

        // save the sku if new...TODO
        Sku saveSku = catalogService.saveSku( product.getAllSkus().get( 0 ) );

        product.getAllSkus().set(0, saveSku );

        // load full the category from DB, seems to be a bug when
        // this is not done. Categories get persisted with no parent.
        Category parent = this.findCategoryById( item.getGroup().getId() );
        product.setDefaultCategory( parent );
        if ( product.getAllParentCategories() != null &&
                            product.getAllParentCategories().size() > 0 ) {
            product.getAllParentCategories().clear();
            product.getAllParentCategories().add( parent );
        }

        // save the product
        Product savedProduct =
                    catalogService.saveProduct(mendProductTrees(product));

        return transform ( savedProduct );
    }

    @Transactional
    public int saveItems ( List<Item> items ) {
        int count = 0;
        for ( Item item : items ) {
            Item saved = saveItem ( item );
            count++;
        }

        return count;
    }

    @Transactional
    public void removeItem(Item item) {

        /*
         * @Comment: Fix for Image replace issue
         */
        removeImages(item);

        Product product = catalogService.findProductById( item.getId() );


        /*
         * Remove entries in BLC_SKU_AVAILABILITY based on skuId
         */
        System.out.println("Deleting - item.getSkuId() --> "+item.getSkuId());

        List<SkuAvailabilityExtImpl> skuAvail = catalogService.readSKUAvailabilityBasedOnSkuId(item.getSkuId());
        if(skuAvail != null && !(skuAvail.isEmpty())) {

            System.out.println("Inside skuAvailable");
            catalogService.deleteSkuAvailBasedOnSkuId(skuAvail);

        }


        // clear the filters
        removeAllItemFilters( product.getAllSkus().get( 0 ) );
        catalogService.deleteSku ( product.getAllSkus().get( 0 ) );
        catalogService.deleteProduct( product );

    }

    @Transactional
    public void removeImages(Item item) {
        catalogService.removeImages(item);
    }


    @Transactional
    public Item findItemByItemId( Long id ) {

        // save the product
        Product product = catalogService.findProductById( id );

        if ( product == null )
            return null;
        else
            return transform ( product );
    }

    @Transactional
    public Item findItemBySkuNum( String skuNum ) {

        // save the product
        Product product = catalogService.findProductBuSkuNum( skuNum );

        if ( product == null ) {
            return null;
        }
        else {
            return transform ( product );
        }
    }

    @Transactional
    public Product saveProductRelatedProduct ( RelatedProduct item ) {
        Product parentProd = catalogService.findProductById( item.getProduct().getId() );
        Product relatedProd = catalogService.findProductById( item.getRelatedProduct().getId() );

        //item.setProduct( parentProd );
        //item.setRelatedProduct( relatedProd );

        parentProd.getCrossSaleProducts().add( item );

        //for ( RelatedProduct r : parentProd.getCrossSaleProducts() ) {
        //  System.out.println ( "Related added to " + r.getProduct().getName() + " is " + r.getRelatedProduct().getName() );
        //}


        Product saved = catalogService.saveProduct( mendProductTrees( parentProd ) );

        return saved;

    }

    @Transactional
    public Product saveProductImages(long productId, Map<String,String> images ) {

        Product product = catalogService.findProductById( productId );

        product.setProductImages( images );

        product = catalogService.saveProduct( product );

        return product;
    }


    @Transactional
    public Category saveCategory(Category category) {
        Category cat = catalogService.saveCategory(mendCategoryTrees(category));
        int index = 0;
        for(Category parentCategory : cat.getAllParentCategories()){
            parentCategory.getAllChildCategories().size();
            if(!parentCategory.getAllChildCategories().contains(cat)){
                parentCategory.getAllChildCategories().add(index,cat);
                catalogService.saveCategory(parentCategory);
                saveCategoryDisplayOrders(parentCategory);
            }
            index++;
        }
        if(cat.getAllParentCategories().indexOf(cat.getDefaultParentCategory()) < 0){
//          cat.getAllParentCategories().add(cat.getDefaultParentCategory());
            cat.getDefaultParentCategory().getAllChildCategories().add(cat);
            catalogService.saveCategory(cat.getDefaultParentCategory());
        }
        return cat;
    }

    @Transactional
    public ItemGroup saveItemGroup( ItemGroup group ) {

        Category category = null;
        Category parent =
                catalogService.findCategoryById( group.getParent().getId() );
        //System.out.println ( "Parent is " + parent.getName() );

        // is the group new or exisiting?
        if ( group.getId().longValue() == 0 ) { // new
            //System.out.println ( "Adding new group " + group.getName() );

            category = transform ( group );
            category.setDefaultParentCategory( parent );
            category.setId( null );

            Map<String,String> images = new HashMap<String,String>();
            category.setCategoryImages( images );

        } else {
            category = catalogService.findCategoryById( group.getId() );

            // apply changes here
            category.setName( group.getName() );
            category.setDefaultParentCategory( parent );
            category.setUrlKey( group.getName().replaceAll( " ", "_") );
            category.setActiveEndDate( zeroOutTime( group.getActiveEndDate() ) );
            category.setActiveStartDate( zeroOutTime( group.getActiveStartDate() ) );

        }

        Map<String,String> images = category.getCategoryImages();
        images.clear();
        //System.out.println ( "Adding image " + group.getImage() );
        images.put( "small", group.getImage() );
        images.put( "large", group.getImage() );

        Category cat = catalogService.saveCategory(mendCategoryTrees(category));
        System.out.println ( "Added category " + cat.getId() );

        int index = 0;
        for(Category parentCategory : cat.getAllParentCategories()){
            parentCategory.getAllChildCategories().size();
            if(!parentCategory.getAllChildCategories().contains(cat)){
                parentCategory.getAllChildCategories().add(index,cat);
                catalogService.saveCategory(parentCategory);
                saveCategoryDisplayOrders(parentCategory);
            }
            index++;
        }
        if(cat.getAllParentCategories().indexOf(cat.getDefaultParentCategory()) < 0){
            cat.getDefaultParentCategory().getAllChildCategories().add(cat);
            catalogService.saveCategory(cat.getDefaultParentCategory());
        }

        return transform(cat);
    }


    @Transactional
    public SkuAttribute saveSkuAttribute ( SkuAttribute attrib ) {
        return catalogService.saveSkuAttribute( attrib );
    }

    @Transactional
    public int saveItemFilters ( List<ItemFilter> filters ) {

        if ( filters == null || filters.size() == 0 )
            return 0;

        Sku sku = catalogService.findSkuById( filters.get( 0 ).getItemId() );
        // remove existing sku attributes on item
        for ( SkuAttribute attrib : catalogService.findSkuAttributes( sku ) ) {
            catalogService.deleteSkuAttribute( attrib );
        }

        // add the filters passed in
        for ( ItemFilter fltr : filters ) {
            SkuAttribute attrib = transform ( fltr );

            catalogService.saveSkuAttribute( attrib );
        }

        return filters.size();
    }

    @Transactional
    public List<SkuAttribute> findAllSkuAttributes ( ) {
        List<SkuAttribute> attribs = catalogService.findAllSkuAttributes();
        attribs.size();

        return attribs;
    }

    @Transactional
    public List<ItemFilter> findAllItemFilters ( ) {
        List<SkuAttribute> attribs = catalogService.findAllSkuAttributes();
        List<ItemFilter> filters = new ArrayList<ItemFilter>( attribs.size() );

        for ( SkuAttribute attrib : attribs ) {
            filters.add( transform ( attrib ) );
        }

        return filters;
    }

    @Transactional
    public void removeAllItemFilters ( Sku sku ) {
        List<SkuAttribute> attribs = catalogService.findSkuAttributes( sku );

        for ( SkuAttribute attrib : attribs ) {
            catalogService.deleteSkuAttribute( attrib );
        }

    }


    @Transactional
    public List<SkuAttribute> findSkuAttributes(Sku sku) {

        List<SkuAttribute> attribs = catalogService.findSkuAttributes( sku );
        attribs.size();

        return attribs;
    }

    @Transactional
    public List<ItemFilter> findItemFilters ( Long productId ) {
        Product product = catalogService.findProductById( productId );

        List<SkuAttribute> attribs = catalogService.findSkuAttributes(
                                                product.getAllSkus().get(0) );
        List<ItemFilter> filters = new ArrayList<ItemFilter>( attribs.size() );

        for ( SkuAttribute attrib : attribs ) {
            filters.add( transform ( attrib ) );
        }

        return filters;
    }

    private Category mendCategoryTrees(Category category){
        addAllCategories(category.getAllChildCategories(), mendCategoriesList(category.getAllChildCategories()));
        addAllCategories(category.getAllParentCategories(), mendCategoriesList(category.getAllParentCategories()));
        if(category.getDefaultParentCategory() != null){
            category.setDefaultParentCategory(catalogService.findCategoryById(category.getDefaultParentCategory().getId()));
        }
        return category;
    }

    private Product mendProductTrees(Product product){
        addAllCategories(product.getAllParentCategories(), mendCategoriesList(product.getAllParentCategories()));
        product.setDefaultCategory(mendCategoryTrees(product.getDefaultCategory()));
        return product;
    }

    private List<Category> mendCategoriesList(List<Category> categories){
        List<Category> mendedCategories = new ArrayList<Category>();
        for(Category category : categories){
            mendedCategories.add(catalogService.findCategoryById(category.getId()));
        }
        return mendedCategories;
    }

    private void addAllCategories(List<Category> categories, List<Category> newCategories){
        categories.clear();
        for(Category category : newCategories){
            categories.add(category);
        }
    }

    private void saveCategoryDisplayOrders(Category category){
        int index = 0;
        for(Category childCategory : category.getAllChildCategories()){
            System.out.println ( "Child category: " + childCategory.getName() );
            CategoryXref categoryXref = categoryXrefDao.readXrefByIds(category.getId(), childCategory.getId());
            categoryXref.setDisplayOrder(new Long(index));
            index++;
            categoryXrefDao.save(categoryXref);
        }

    }


    public boolean imageExists (String type, String imageName) {

        byte[] imgBytes = null;

        System.out.println ( "imageExists " + " imageName -" + imageName);

        // Read from DB to see if image exists
        imgBytes = catalogService.readDBImage(type, imageName);
        return (imgBytes != null);
    }



    @SuppressWarnings("unchecked")
    @Transactional
    public int saveItemInventory( List<ItemInventory> inventories) {

        if(inventories != null) {
            System.out.println("inventories.get(0).getSkuId() -> "+inventories.get(0).getSkuId());
            List<SkuAvailabilityExtImpl> skuAvail = skuDao.readSKUAvailabilityForSkuId(inventories.get(0).getSkuId());
            for (ItemInventory inventory : inventories) {

                if (inventory != null) {

                    boolean found = false;
                    if(skuAvail != null) {
                        // Locate availablity record for inventory item
                        //for (SkuAvailabilityExtImpl s : skuAvail) {
                        for (SkuAvailabilityExtImpl s : skuAvail) {

                            if ((s != null) && (s.getLocId()).equals(Long.valueOf(inventory.getLocationId().longValue())) ){

                                // Sku availablity exists, check for update
                                if (s.getQtyOnHand() != inventory.getQtyTotal().intValue()) {
                                    s.setAvailabilityDate(new Date());
                                    s.setAvailabilityStatus(inventory.calculateStatus());
                                    s.setQtyOnHand(inventory.getQtyTotal().intValue());
                                    skuDao.saveSkuAvailability(s);
                                }
                                found = true;
                                break;
                            }
                        }
                    }
                    // No sku availablity to match inventory item, add an entry
                    if (!found) {
                        SkuAvailabilityExtImpl sa = new SkuAvailabilityExtImpl();
                        sa.setSkuId(inventory.getSkuId());
                        sa.setLocId(inventory.getLocationId().longValue());
                        sa.setAvailabilityDate(new Date());
                        sa.setAvailabilityStatus(inventory.calculateStatus());
                        sa.setQtyOnHand(inventory.getQtyTotal().intValue());
                        skuDao.saveSkuAvailability(sa);
                    }

                }
            }
            return inventories.size();
        }
        return 0;
     }

    /*@SuppressWarnings("unchecked")
    @Transactional
    public int saveItemInventory( List<ItemInventory> inventories) {

        List<SkuAvailabilityExtImpl> skuAvail = new ArrayList<SkuAvailabilityExtImpl>();
        skuAvail = skuDao.readSKUAvailabilityForSkuId(inventories.get(0).getSkuId());
        if(skuAvail == null) {
            System.out.println("skuAvail is null, so creating it");
            for(ItemInventory inventory : inventories) {
                SkuAvailabilityExtImpl s = new SkuAvailabilityExtImpl();
                s.setSkuId(inventory.getSkuId());
                s.setLocId(inventory.getLocationId().longValue());
                s.setQtyOnHand(-1);
                s.setAvailabilityDate(new Date());
                s.setAvailabilityStatus(inventory.calculateStatus());
                s.setQtyOnHand(inventory.getQtyTotal().intValue());
                skuDao.saveSkuAvailability(s);
            }
        }

        for(ItemInventory inventory : inventories) {
            if(inventory != null && skuAvail != null) {
               for(SkuAvailabilityExtImpl s : skuAvail) {
                    if(s == null) {
                        s = new SkuAvailabilityExtImpl();
                        s.setSkuId(inventory.getSkuId());
                        s.setLocId(inventory.getLocationId().longValue());
                        s.setQtyOnHand(-1);
                    }
                    if((s.getLocId()).equals(Long.valueOf(inventory.getLocationId().longValue())) && (s.getQtyOnHand() != inventory.getQtyTotal().intValue())) {
                        s.setAvailabilityDate(new Date());
                        s.setAvailabilityStatus(inventory.calculateStatus());
                        s.setQtyOnHand(inventory.getQtyTotal().intValue());
                        skuDao.saveSkuAvailability(s);
                    }
                }
            }
        }
        return inventories.size();
     }*/
      // for each inventory record update or create new
         //for ( ItemInventory inv : inventories ) {
             /*if(inv != null) {
                 SkuAvailability avail =
                     availabilityService.lookupSKUAvailabilityForLocation(
                           inv.getSkuId(), inv.getLocationId().longValue(), true );

                 if ( avail == null ) { // if new inventory
                     avail = new SkuAvailabilityImpl();
                     avail.setSkuId( inv.getSkuId() );
                     avail.setLocationId( inv.getLocationId().longValue() );
                 }
                 avail.setAvailabilityDate( new Date() ); // set date to now
                 avail.setAvailabilityStatus( inv.calculateStatus() );
                 avail.setQuantityOnHand( inv.getQtyTotal().intValue() );

                 availabilityService.save( avail );
             }*/

    public byte[] readImage(String type, String imageName) {
        byte[] imgBytes = null;

        // Persist Image in Database
        imgBytes = catalogService.readDBImage(type, imageName);
        if(imgBytes != null) {

        try {
            InputStream in = new FileInputStream ( new File ( PATH + imageName) );
            imgBytes = IOUtils.toByteArray( in );
            /*
             * @Comment: Closing InputStream (To fix Image replace issue)
             */
            in.close();
        }
        catch(FileNotFoundException e) {
            System.out.println ( "File not found " + e.getMessage() );
        }
        catch(IOException e) {
            System.out.println ( "File could not be read " + e.getMessage() );
        }

        }
        return imgBytes;
    }


    /*
     * Sends the image information to the catalog server for save to DB
     *
     * (non-Javadoc)
     * @see com.probuild.retail.web.catalog.ext.service.WebCatalogService#sendImage(long, java.lang.String, char, java.lang.String, byte[])
     */
    public long sendImage(long productId, String largeSmall, char type, String imageName,
            byte[] bytes, char newImage) {



        //long byteCount = bytes.length;


        // Persist Image in Database
        System.out.println ( "WebCatalogServiceImpl->sendImage - productID " + productId +
                " imageName -" + imageName);

        catalogService.saveDBImage(productId, largeSmall, type, imageName, bytes,newImage);
        byte[] tempByte = catalogService.readDBImage(largeSmall, imageName);

        /*try {
            OutputStream out =
                        new FileOutputStream ( new File( PATH + imageName) );
            out.write( tempByte );

             * @Comment: Closing OutputStream (To fix Image replace issue)

            out.close();

        }
        catch(FileNotFoundException e) {
            System.out.println ( "Failed to write file " + e.getMessage() );
        }
        catch(IOException e) {
            System.out.println ( "Failed to write file " + e.getMessage() );
        }*/

        return tempByte.length;
    }

    public String getSplashHtml ( ) {
        String html = "";

        File splashFile = new File( PATH + "/WEB-INF/jsp/splash.html" );

        if ( !splashFile.exists() )
            return html;

        FileInputStream in = null;
        try {
            in = new FileInputStream( splashFile );
            html =  IOUtils.toString( in );

        }
        catch(FileNotFoundException e) {
            System.out.println ( "File not found: "  + PATH + "/WEB-INF/jsp/splash.html" );
        }
        catch(IOException e) {
            System.out.println ( "get splash failed " + e.getMessage() );
        }
        finally {
            IOUtils.closeQuietly( in );
        }

        return html;
    }

    public boolean saveSplashHtml ( String html ) {

        boolean success = false;

        File splashFile = new File( PATH + "/WEB-INF/jsp/splash.html" );

        if ( !splashFile.exists() )
            return success;

        FileOutputStream out = null;
        try {
            out = new FileOutputStream ( splashFile );
            IOUtils.write( html, out );
        }
        catch(FileNotFoundException e) {
            System.out.println ( "File not found: "  + PATH + "/WEB-INF/jsp/splash.html" );
        }
        catch(IOException e) {
            System.out.println ( "get splash failed " + e.getMessage() );
        }
        finally {
            IOUtils.closeQuietly( out );
        }

        return success;
    }



    public String getParentMenuHtml ( ) {
        String html = "";

        File splashFile = new File( PATH + "/WEB-INF/jsp/parentCategory.jsp" );

        if ( !splashFile.exists() )
            return html;

        FileInputStream in = null;
        try {
            in = new FileInputStream( splashFile );
            html =  IOUtils.toString( in );

        }
        catch(FileNotFoundException e) {
            System.out.println ( "File not found: "  + PATH + "/WEB-INF/jsp/parentCategory.jsp" );
        }
        catch(IOException e) {
            System.out.println ( "get parent menu failed " + e.getMessage() );
        }
        finally {
            IOUtils.closeQuietly( in );
        }

        return html;
    }

    public boolean saveParentMenuHtml ( String html ) {

        boolean success = false;

        File splashFile = new File( PATH + "/WEB-INF/jsp/parentCategory.jsp" );

        if ( !splashFile.exists() )
            return success;

        FileOutputStream out = null;
        try {
            out = new FileOutputStream ( splashFile );
            IOUtils.write( html, out );
        }
        catch(FileNotFoundException e) {
            System.out.println ( "File not found: "  + PATH + "/WEB-INF/jsp/parentCategory.jsp" );
        }
        catch(IOException e) {
            System.out.println ( "get paren menu failed " + e.getMessage() );
        }
        finally {
            IOUtils.closeQuietly( out );
        }

        return success;
    }


    private ItemGroup transform ( Category category ) {

        ItemGroup group = new ItemGroup.Builder()
                    .activeEndDate( new Date ( category.getActiveEndDate().getTime() ) )
                    .activeStartDate( new Date ( category.getActiveStartDate().getTime() ) )
                    .description( category.getDescription() )
                    .id( category.getId() )
                    .name( category.getName() )
                    .longDescription( category.getLongDescription() )
                    .image( category.getCategoryImage( "small" ) )
                    .build();

        // we don't want to recurse up the entire parent tree with transform

        ItemGroup parent = null;
        if ( category.getDefaultParentCategory() != null ) {
            parent = new ItemGroup.Builder()
                    .id( category.getDefaultParentCategory().getId() )
                    .name( category.getDefaultParentCategory().getName() )
                    .build();
        }
        group.setParent( parent );

        return group;
    }


    private Category transform ( ItemGroup group ) {

        Category category = new CategoryImpl();
        category.setId( group.getId() );
        category.setName( group.getName() );
        category.setActiveStartDate( zeroOutTime( group.getActiveStartDate() ) );
        category.setActiveEndDate( zeroOutTime( group.getActiveEndDate() ) );
        category.setDescription( group.getDescription() );
        category.setDisplayTemplate( "" );
        category.setUrl( "" );
        category.setUrlKey( group.getName().replaceAll( " ", "_") );

        Map<String,String> images = new HashMap<String,String>();
        images.put( "small", group.getImage() );
        //images.put( "large", group.getImage() );
        category.setCategoryImages( images );

        return category;
    }

    private Item transform ( Product product ) {

        SkuExtImpl sku = (SkuExtImpl)product.getAllSkus().get(0);

        Item item = new Item.Builder()
            .id( product.getId() )
            .skuId( sku.getId() )
            .name( product.getName() )
            .descr( product.getLongDescription() )
            .sku( sku.getSku() )
            .upc( sku.getUpc().toString() )
            .alt( sku.getAlternateCode() )
            .regularPrice( sku.getRetailPrice().getAmount() )
            .salePrice( sku.getSalePrice().getAmount() )
            .modelNum( product.getModel() )
            .manufacturer( product.getManufacturer() )
            .activeEndDate( new Date ( product.getActiveEndDate().getTime() ) )
            .activeStartDate( new Date ( product.getActiveStartDate().getTime() ) )
            .build();

        if ( product.getDimension() != null ) {
            item.setWidth( product.getWidth() );
            item.setHeight( product.getHeight() );
            item.setDepth( product.getDepth() );
        }

        if ( product.getWeight() != null &&
                        product.getWeight().getWeightUnitOfMeasure() != null ) {
            item.setWeight( product.getWeight().getWeight() );
            item.setWeightUnits(
                product.getWeight().getWeightUnitOfMeasure().getType() );
        }

        Category category = product.getDefaultCategory();

//        ItemGroup group = new ItemGroup.Builder()
//                    .id( category.getId() )
//                    .name( category.getName() )
//                    .build();
        ItemGroup group = transform ( category );

        Map<String,String> imageMap = product.getProductImages();
        List<ItemImage> images = new ArrayList<ItemImage>( imageMap.size() );
        for ( String key : imageMap.keySet() ) {
            ItemImage img = new ItemImage.Builder().
                                            itemId( product.getId() ).
                                            key( key ).
                                            imagePath( imageMap.get( key ) ).
                                            build();

            images.add( img );
        }


        List<RelatedProduct> relateds = product.getCrossSaleProducts();
        List<Item> relatedItems = new ArrayList<Item>( relateds.size() );
        for ( RelatedProduct rp : relateds ) {
            //Item itm = transform( rp.getProduct() );
            SkuExtImpl relatedSku = (SkuExtImpl)rp.getRelatedProduct().getAllSkus().get(0);

            Item itm = new Item.Builder()
                .id( rp.getRelatedProduct().getId() )
                .sku( relatedSku.getSku() )
                .name( rp.getRelatedProduct().getName() )
                .descr( rp.getRelatedProduct().getLongDescription() )
                .modelNum( rp.getRelatedProduct().getModel() )
                .manufacturer( rp.getRelatedProduct().getManufacturer() )
                .activeEndDate( new Date ( rp.getRelatedProduct().getActiveEndDate().getTime() ) )
                .activeStartDate( new Date ( rp.getRelatedProduct().getActiveStartDate().getTime() ) )
                .group( transform ( rp.getRelatedProduct().getDefaultCategory() ) )
                .build();

            itm.setRelatedId( rp.getId() );
            relatedItems.add( itm );
        }


        item.setRelatedItems( relatedItems );
        item.setImages( images );
        item.setGroup( group );

        return item;
    }

    private SkuExtImpl transformToSku ( Item item ) {
        SkuExtImpl sku = new SkuExtImpl();

        sku.setId( item.getSkuId() );
        sku.setName( item.getName() );
        sku.setDescription( item.getName() );
        sku.setLongDescription( item.getDescr() );
        sku.setSku( item.getSku() );
        sku.setUpc( new Long ( item.getUpc() ) );
        sku.setAlternateCode( item.getAlt() );
        sku.setRetailPrice( new Money ( item.getRegularPrice() ) );
        sku.setSalePrice( new Money ( item.getSalePrice() ) );
        sku.setActiveEndDate( zeroOutTime( item.getActiveEndDate() ) );
        sku.setActiveStartDate( zeroOutTime( item.getActiveStartDate() ) );
        sku.setDiscountable( false );

        return sku;
    }

    private Product transformToProduct ( Item item ) {
        ProductExtImpl product = new ProductExtImpl();

        Category parent = transform( item.getGroup() );
        List<Category> parents = new ArrayList<Category>(1);
        parents.add( parent );
        System.out.println ( "+++" + item.getName() );
        System.out.println ( "Parent group is " + parent.getName() + ", " + parent.getId() );
        System.out.println ( "UOM is " + item.getUom() );
        System.out.println ( "Dept is " + item.getDept() );
        product.setId( item.getId() );
        product.setName( item.getName() );
        product.setDescription( item.getName() );
        product.setUom( item.getUom() );
        product.setDepartment( item.getDept() );
        product.setDefaultCategory( parent );
        product.setAllParentCategories( parents );
        product.setActiveEndDate( zeroOutTime( item.getActiveEndDate() ) );
        product.setActiveStartDate( zeroOutTime( item.getActiveStartDate() ) );
        product.setLongDescription( item.getDescr() );
        product.setManufacturer( item.getManufacturer() );
        product.setModel( item.getModelNum() );
        product.setDepth( item.getDepth() );
        product.setWidth( item.getWidth() );
        product.setHeight( item.getHeight() );

        ProductWeight weight = new ProductWeight();
        weight.setWeight( item.getWeight() );
        weight.setWeightUnitOfMeasure( WeightUnitOfMeasureType.POUNDS );

        product.setWeight( weight );


        return product;
    }

    private Product transform ( Item item ) {

        Product product = transformToProduct ( item );
        SkuExtImpl sku = transformToSku ( item );

        List<Sku> skus = new ArrayList<Sku>(1);
        skus.add( sku );
        product.setAllSkus( skus );


        // set images
        Map<String,String> images = new HashMap<String,String>();
        for ( ItemImage img : item.getImages() ) {
            images.put( img.getKey(), img.getImagePath() );
        }
        product.setProductImages( images );


        // set related items
        List<RelatedProduct> relatedProds =
                new ArrayList<RelatedProduct>( item.getRelatedItems().size() );
        for ( Item itm : item.getRelatedItems() ) {
            CrossSaleProductExtImpl cItm = new CrossSaleProductExtImpl ( );
            cItm.setId( itm.getRelatedId() );
            cItm.setProduct( product );
            cItm.setRelatedProduct( transformToProduct ( itm ) );

            relatedProds.add( cItm );
        }
        product.setCrossSaleProducts( relatedProds );


        return product;
    }


    private ItemFilter transform ( SkuAttribute attrib ) {

        ItemFilter filter = new ItemFilter.Builder().
                                id( attrib.getId() ).
                                itemId( attrib.getSku().getId() ).
                                name( attrib.getName() ).
                                value( attrib.getValue() ).
                                build();

        return filter;
    }

    private SkuAttribute transform ( ItemFilter filter ) {
        SkuAttribute attrib = new SkuAttributeImpl();
        attrib.setId( filter.getId() );
        attrib.setName( filter.getName() );
        attrib.setValue( filter.getValue() );

        Sku sku = catalogService.findSkuById( filter.getItemId() );
        attrib.setSku( sku );


        return attrib;
    }


    private Date zeroOutTime ( Date date ) {

        if ( date == null )
            date = new Date();

        Calendar cal = GregorianCalendar.getInstance();
        cal.setTime( date );
        cal.set( Calendar.HOUR, 0 );
        cal.set( Calendar.MINUTE, 0 );
        cal.set( Calendar.SECOND, 0 );
        cal.set( Calendar.MILLISECOND, 0 );

        System.out.println ( "Zeroed out: " + cal.getTimeInMillis() );

        return cal.getTime();

    }

}
