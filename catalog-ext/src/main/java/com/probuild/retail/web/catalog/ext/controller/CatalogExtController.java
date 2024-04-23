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
package com.probuild.retail.web.catalog.ext.controller;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.collections.comparators.ReverseComparator;
import org.apache.commons.lang.StringUtils;

import org.broadleafcommerce.catalog.domain.Category;
import org.broadleafcommerce.catalog.domain.FeaturedProduct;
import org.broadleafcommerce.catalog.domain.Product;
import org.broadleafcommerce.catalog.domain.Sku;
import org.broadleafcommerce.catalog.web.CatalogSort;
import org.broadleafcommerce.inventory.domain.SkuAvailability;
import org.broadleafcommerce.inventory.service.AvailabilityService;
import org.broadleafcommerce.profile.domain.Address;
import org.broadleafcommerce.profile.domain.AddressImpl;
import org.broadleafcommerce.rating.domain.RatingSummary;
import org.broadleafcommerce.rating.service.RatingService;
import org.broadleafcommerce.rating.service.type.RatingType;
import org.broadleafcommerce.search.util.SearchFilterUtil;
import org.broadleafcommerce.store.domain.Store;
import org.broadleafcommerce.store.service.StoreService;
import org.broadleafcommerce.catalog.domain.SkuAttribute;

import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.util.UrlPathHelper;

import com.probuild.retail.web.catalog.ext.domain.CategoryExtImpl;
import com.probuild.retail.web.catalog.ext.domain.ProductExtImpl;
import com.probuild.retail.web.catalog.ext.domain.ProductPbImpl;
import com.probuild.retail.web.catalog.ext.domain.SkuExtImpl;
import com.probuild.retail.web.catalog.ext.domain.SkuPbImpl;
import com.probuild.retail.web.catalog.ext.service.CatalogServiceExt;
import com.probuild.retail.web.catalog.ext.dao.CatalogImagesDaoImpl;
import com.probuild.retail.web.catalog.ext.dao.SkuDaoExtImpl;

@Controller
public class CatalogExtController {

	//TODO Instead of mixing and matching - we should prob be autowiring all the dependencies for this controller.
    //@Resource(name="blCartService")
    //protected CartService cartService;
    //@Resource(name="blCustomerState")
    //protected CustomerState customerState;
    @Resource(name="blStoreService")
    private StoreService storeService;
    @Resource(name="blAvailabilityService")
    protected AvailabilityService availabilityService;

    private final int maxDisplayCount = 10;

    private final UrlPathHelper pathHelper = new UrlPathHelper();
    private CatalogServiceExt catalogService;
    private RatingService ratingService;
    private String defaultCategoryView;
    private String defaultProductView;
    private Long rootCategoryId;
    private String rootCategoryName;
    private String categoryTemplatePrefix;

    private Long selectedStoreId;
    private List<ProductExtImpl> otherStoreAvailability;

    private String searchZip = "92110";
    private String searchDistance = "20";

    private static String PATH;

    public String gardenmenutxt = "";
    public String bmmenutxt = "";
    public String hardwaremenutxt = "";
    public String toolsmenutxt = "";
    public String paintmenutxt = "";
    public String plumbmenutxt = "";
    public String electricalmenutxt = "";
    public String homestoragemenutxt = "";
    public String seasonalmenutxt = "";
    public String gardenmenutxt1 = "";
    public String bmmenutxt1 = "";
    public String hardwaremenutxt1 = "";
    public String toolsmenutxt1 = "";
    public String paintmenutxt1 = "";
    public String plumbmenutxt1 = "";
    public String electricalmenutxt1 = "";
    public String homestoragemenutxt1 = "";
    public String seasonalmenutxt1 = "";


    @RequestMapping(method =  {RequestMethod.GET})
    public String viewCatalog(ModelMap model, HttpServletRequest request) {

    	CatalogSort catalogSort = new CatalogSort();
    	catalogSort.setSort("manufacturerA");
    	return showCatalog(model, request, catalogSort);
    }

    @SuppressWarnings("unchecked")
	private String showCatalog (ModelMap model, HttpServletRequest request,
    		CatalogSort catalogSort) {

    	System.out.println ("showCatalog Invoked");

    	String envImageFolder = System.getenv( "CATALOG_IMAGE_DIR" );
        if ( envImageFolder == null ) {
            PATH = "";
        } else {
            System.out.println ( "using image folder at " + envImageFolder );
            PATH = envImageFolder;
        }


    	otherStoreAvailability = new ArrayList<ProductExtImpl>();

        // get location cookie
        parseSelectedStore( request );
        model.addAttribute("selectedStore", selectedStoreId );
        model.addAttribute( "searchZip", searchZip );
        model.addAttribute( "searchDistance", searchDistance );

        addCategoryToModel(request, model);
        boolean productFound = addProductsToModel(request, model, catalogSort);

        String view = defaultCategoryView;
        if (productFound) {

            // check if this is the availability popup
            if ("true".equals(request.getParameter("ajaxItemAvail") ) ) {
                //System.out.println ( "Availability check needs to happen " );

                String distance = ServletRequestUtils.getStringParameter( request, "distance", "30" );
                String zipcode = ServletRequestUtils.getStringParameter( request, "zipcode", "92110" );

                if ( distance.length() == 0 || !StringUtils.isNumeric( distance ) )
                    distance = "30";
                if ( zipcode.length() == 0 || !StringUtils.isNumeric( zipcode ) ) {
                    zipcode = "92110";
                }

                //System.out.println ( "Zip: " + zipcode + " | Distance: " + distance );
                searchZip = zipcode; // hold onto these for later
                searchDistance = distance;
                model.addAttribute( "searchZip", searchZip );
                model.addAttribute( "searchDistance", searchDistance );

                Address searchAddress = new AddressImpl();
                searchAddress.setAddressLine1( "" );
                searchAddress.setAddressLine2( "" );
                searchAddress.setCity( "" );
                //searchAddress.setState( "" );
                searchAddress.setPostalCode( zipcode );
                //searchAddress.setCountry( "" );

                Map<Store,Double> stores = storeService.findStoresByAddress(
                                              searchAddress,
                                              Double.parseDouble( distance ) );
                TreeMap<Double,Store> sortedStoreMap = new TreeMap<Double,Store>();
                // sort by distance
                for ( Store store : stores.keySet() ) {
                    sortedStoreMap.put( stores.get( store ), store );
                }

                for ( Double dist : sortedStoreMap.keySet() ) {
                    Store store = sortedStoreMap.get( dist );
                    //System.out.println ( "Store: " + store.getName() + " distance: " + stores.get( store ) );

                    Long skuId = ( (Product)model.get( "currentProduct" ) ).getAllSkus().get(0).getId();

                    SkuAvailability avail =
                            availabilityService.lookupSKUAvailabilityForLocation( skuId, new Long(store.getId()), true );
                    ProductExtImpl prodAvail = new ProductExtImpl ( );
                    prodAvail.setAvailability( avail );
                    prodAvail.setStockingLocationName( store.getName() );
                    prodAvail.setStore( store );
                    otherStoreAvailability.add( prodAvail );
                }

                model.addAttribute( "otherStoreAvailability", otherStoreAvailability );
                String msg = "";
                if(otherStoreAvailability.isEmpty()) {
                	if( !searchZip.equals("92110") ) {
                		msg = "No stores found for zipcode, "+searchZip;
                		model.addAttribute( "message",msg);
                	}
                }

                view = "catalog/availability";
            } else {

                // TODO: Nice to have: product logic similar to category below
            	/*
                 * @Comment: Retrieve sku attribute values and add to the model (used in defaultProduct.jsp)
                 * @Author: Prathibha
                 * @Date: 01/10/12
                 */
            	HashMap<Object, Object> skuAttributes = new HashMap<Object, Object>();
            	SkuExtImpl sku = (SkuExtImpl)((ProductPbImpl)(model.get( "currentProduct" )) ).getAllSkus().get(0);
            	if((sku != null)) {
            	    if (sku.getAttributes() != null) {
            	        for(SkuAttribute attribute : sku.getAttributes()) {
            	            skuAttributes.put( attribute.getValue(), attribute.getName() );
            	        }
            	    }
            	}
            	model.addAttribute( "skuAttributes", skuAttributes );


            	view = defaultProductView;
            }


        } else {
            Category currentCategory = (Category) model.get("currentCategory");
            if (currentCategory.getUrl() != null && !"".equals(currentCategory.getUrl())) {
                return "redirect:"+currentCategory.getUrl();
            } else if (currentCategory.getDisplayTemplate() != null && !"".equals(currentCategory.getUrl())) {
                view = categoryTemplatePrefix + currentCategory.getDisplayTemplate();
            } else {

                if ("true".equals(request.getParameter("ajax"))) {

                    //List<Product> products = (List<Product>)model.get( "displayProducts" );
                	/*
                     * @Comment: displayProducts is changed to completeProdListInCat for
                     *           displaying filtered items (Products)
                     * @Author: Prathibha
                     * @Date: 01/10/12
                     */
                    List<Product> products = (List<Product>)model.get( "completeProdListInCat" );
                    model.addAttribute("displayProducts", sortProducts(catalogSort, products));
                    model.addAttribute( "displayProductsTotal", products.size() );
                    model.addAttribute( "totalPages", 0 );
                    model.addAttribute( "selectedPage", 0 );



                    view = "catalog/categoryView/mainContentFragment";
                } else {
                    view = defaultCategoryView;
                }

            }
        }

        if (catalogSort == null) {
            model.addAttribute("catalogSort", new CatalogSort());
        }

        //List<Order> wishlists = cartService.findOrdersForCustomer(customerState.getCustomer(request), OrderStatus.NAMED);
        //model.addAttribute("wishlists", wishlists);

        return view;
    }

    @RequestMapping(method =  {RequestMethod.POST})
    public String sortCatalog (ModelMap model, HttpServletRequest request, @ModelAttribute CatalogSort catalogSort) {
        if ( catalogSort != null && catalogSort.getSort() == null ) {
            catalogSort.setSort( ServletRequestUtils.getStringParameter( request, "catalogSort", null ) );
        }
        return showCatalog(model, request, catalogSort);
    }

    protected void addCategoryToModel(HttpServletRequest request, ModelMap model ) {
        Category rootCategory = null;
        if (getRootCategoryId() != null) {
            rootCategory = catalogService.findCategoryById(getRootCategoryId());
        } else if (getRootCategoryName() != null) {
            rootCategory = catalogService.findCategoryByName(getRootCategoryName());
        }

        if (rootCategory == null) {
            throw new IllegalStateException("Catalog Controller configured incorrectly - root category not found: " + rootCategoryId);
        }

        String url = pathHelper.getRequestUri(request).substring(pathHelper.getContextPath(request).length());
        System.out.println ( "adding url to model: " + url );

        String categoryId = request.getParameter("categoryId");
        if (categoryId != null) {
        	System.out.println ( "Category ID found: " + categoryId );
            Category category = catalogService.findCategoryById(new Long(categoryId));
            if (category != null) {
                url = category.getUrl();
            }
        }

       	System.out.println ( "category found by id " + categoryId );
       	System.out.println ( "URL: " + url );
       	List<Category> categoryList = new ArrayList<Category>();
        categoryList = catalogService.getChildCategoryURLMapByCategoryId(rootCategory.getId()).get(url);
        if(categoryList != null) {
            for ( Category c : categoryList ) {
                System.out.println ( "Category under: " + c.getName() );
            }
        }

        addCategoryListToModel(categoryList, rootCategory, url, model);
        model.addAttribute("rootCategory", rootCategory);
    }

    protected int findProductPositionInList(Product product, List<Product> products) {
        for (int i=0; i < products.size(); i++) {
            Product currentProduct = products.get(i);
            if (product.getId().equals(currentProduct.getId())) {
                return i+1;
            }
        }
        return 0;
    }

    protected boolean addCategoryListToModel(List<Category> categoryList, Category rootCategory, String url, ModelMap model) {
        boolean categoryError = false;

        while (categoryList == null) {
            categoryError = true;

            int pos = url.indexOf("/");
            if (pos == -1) {
                categoryList = new ArrayList<Category>();
                categoryList.add(rootCategory);
            } else {
                url = url.substring(0, url.lastIndexOf("/"));
                categoryList = catalogService.getChildCategoryURLMapByCategoryId(rootCategory.getId()).get(url);
            }
        }

        List<Category> siblingCategories  = new ArrayList<Category>();
        Category currentCategory = (Category) categoryList.get(categoryList.size()-1);

        // reload category to get any changes by admin tool
        currentCategory = catalogService.findCategoryById( currentCategory.getId() );
        if(currentCategory != null) {
            siblingCategories = currentCategory.getAllChildCategories();
        }


        SimpleDateFormat sdf = new SimpleDateFormat ( "EEE yyyy/MM/dd hh:mm:ss a" );
        //for ( Category category : siblingCategories ) {
        //    System.out.println ( "sibling category is " + category.getName() );
        //    System.out.println ( "start " + sdf.format( category.getActiveStartDate() ) + " end " + sdf.format( category.getActiveEndDate() ) );
        //}


        model.addAttribute("breadcrumbCategories", categoryList);
        model.addAttribute("currentCategory", currentCategory );
        model.addAttribute("categoryError", categoryError);
        model.addAttribute("displayCategories", siblingCategories);

        /*
         * @Comment: Returns the sub categories for primary links.
         *           Populates the hover menu (Ref:ParentCategory.jsp)
         */
        CreateHoverMenu(categoryList,model);

        return categoryError;
    }

    protected boolean validateProductAndAddToModel(Product product, ModelMap model) {
        Category currentCategory = (Category) model.get("currentCategory");
        Category rootCategory = (Category) model.get("rootCategory");
        int productPosition=0;

        List<Product> productList = catalogService.findActiveProductsByCategory(currentCategory);
        if (productList != null) {
            populateProducts(productList, currentCategory);
            model.addAttribute("products", productList);
        }
        productPosition = findProductPositionInList(product, productList);
        if (productPosition == 0) {
            // look for product in its default category and override category from request URL
            currentCategory = product.getDefaultCategory();
            productList = catalogService.findActiveProductsByCategory(currentCategory);
            //System.out.println ( "Products default category is " + currentCategory.getName() );

            if (productList != null) {
            	//System.out.println ( "Product list size is " + productList.size() );
	            populateProducts(productList, currentCategory);
	            model.addAttribute("products", productList);
	        }
            String url = currentCategory.getGeneratedUrl();

            // override category list settings using this products default
            List<Category> categoryList = catalogService.getChildCategoryURLMapByCategoryId(rootCategory.getId()).get(url);
            if (categoryList != null && ! addCategoryListToModel(categoryList, rootCategory, url, model)) {
                productPosition = findProductPositionInList(product, productList);
            }
        }


        if (productPosition != 0) {

            model.addAttribute("productError", false);
            model.addAttribute("currentProduct", product);
            model.addAttribute("productPosition", productPosition);

            if (productPosition != 1) {
                model.addAttribute("previousProduct", productList.get(productPosition-2));
            }
            if (productPosition < productList.size()) {
                model.addAttribute("nextProduct", productList.get(productPosition));
            }
            model.addAttribute("totalProducts", productList.size());
        } else {
            model.addAttribute("productError", true);
        }

        //WishlistRequest wishlistRequest = new WishlistRequest();
        //wishlistRequest.setAddCategoryId(currentCategory.getId());
        //wishlistRequest.setAddProductId(product.getId());
        //wishlistRequest.setQuantity(1);
        //wishlistRequest.setAddSkuId(product.getSkus().get(0).getId());
        //model.addAttribute("wishlistRequest", wishlistRequest);

        return (productPosition !=0);
    }

    @SuppressWarnings("unchecked")
    protected boolean addProductsToModel(HttpServletRequest request, ModelMap model,
    		CatalogSort catalogSort) {



    	boolean productFound = false;
    	String productId = request.getParameter("productId");

        if (productId != null) {
            Product product;
            if ( selectedStoreId != null && selectedStoreId.intValue() > 0 )
                product = catalogService.findProductWithAvailabilityById(
                                        new Long(productId), selectedStoreId );
            else
                product = catalogService.findProductById(new Long(productId));

            if (product != null) {
                productFound = validateProductAndAddToModel(product, model);
                //System.out.println ( "Product found after validate " + productFound );
                //addRatingSummaryToModel(productId, model); -not used
            }
        } else {
            Category currentCategory = (Category) model.get("currentCategory");

            String pageNum = ServletRequestUtils.getStringParameter(request, "page", "0" );


            List<Product> productList;
            if ( selectedStoreId.intValue() == 0 )
                productList =
                    catalogService.findActiveProductsByCategory(
                                    currentCategory,
                                    Integer.valueOf( pageNum ).intValue(),
                                    maxDisplayCount );
            else
                productList =
                    catalogService.findActiveProductsByWithAvailabilityCategory(
                                     currentCategory, selectedStoreId,
                                     Integer.valueOf( pageNum ).intValue(),
                                     maxDisplayCount );

            /*
             * @Comment: Retrieving the complete Product list under a category
             * @Author: Prathibha
             * @Date: 01/10/12
             */
            List<Product> completeProdListInCat;
            if ( selectedStoreId.intValue() == 0 )
            	completeProdListInCat =
                    catalogService.findActiveProductsByCategory(
                                    currentCategory);
            else
            	completeProdListInCat =
                    catalogService.findActiveProductsByWithAvailabilityCategory(
                                     currentCategory, selectedStoreId);

            model.addAttribute("completeProdListInCat", completeProdListInCat);
            model.addAttribute("catalogSort", catalogSort);


            /*
             * @Comment: Extract all product images in the category and dumps it under tomcat
             * (GET THE SAME CODE ON SEARCHSERVICECOMPASSEXTIMPL.JAVA)
             *
             * */

            List<Long> ids = new ArrayList<Long>();
            for(Product p : completeProdListInCat) {
                ids.add(p.getId());

            }

            List<Long> productIds = new ArrayList<Long>();
            if(!(ids.isEmpty())) {

                List<CatalogImagesDaoImpl> catImages = catalogService.readDBImagesforProductIds(ids,'Y');
                if(catImages != null) {
                    for(CatalogImagesDaoImpl c: catImages) {
                        String imagepath = PATH+c.getURL();
                        try {
                            byte[] image = c.getImage();
                            InputStream in = new ByteArrayInputStream(image);
                            BufferedImage bImageFromConvert = ImageIO.read(in);
                            ImageIO.write(bImageFromConvert, "jpg", new File(imagepath));
                            productIds.add(c.getProductId());
                            in.close();
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if(productIds.size() != 0) {
                        catalogService.updateNewImageToNforProductId(productIds, 'N');
                    }
                }
            }

            int totalActiveItemCount = catalogService.findActiveProductsCountByCategory( currentCategory );
            int totalPages = 0;
            if ( totalActiveItemCount > 0 )
                totalPages = (int)Math.ceil( totalActiveItemCount/(double)maxDisplayCount );

            //for ( Product p : productList ) {
            //    ProductExtImpl prod = (ProductExtImpl)p;
            //    System.out.println ( prod.getName() );
            //}
            /*SearchFilterUtil.filterProducts(productList, request.getParameterMap(),
            		new String[] {"manufacturer", "skus[0].salePrice"});
              SearchFilterAttribUtil.filterProducts(productList, request.getParameterMap(),
            		new String[] {"filter"});
            */
            /*
             * @Comment: productList is changed to completeProdListInCat
             * @Author: Prathibha
             * @Date: 01/10/12
             */
            SearchFilterUtil.filterProducts(completeProdListInCat, request.getParameterMap(),
            		new String[] {"manufacturer", "skus[0].salePrice"});
            //System.out.println ( "After general filter product count " + productList.size() );
            // insert custom filter here for demo
            SearchFilterAttribUtil.filterProducts(completeProdListInCat, request.getParameterMap(),
            		new String[] {"filter"});
            // end custom filter

            if ((catalogSort != null) && (catalogSort.getSort() != null)) {
                populateProducts(productList, currentCategory);
                model.addAttribute("displayProducts", sortProducts(catalogSort, productList));
                model.addAttribute( "displayProductsTotal", totalActiveItemCount );
                model.addAttribute( "totalPages", totalPages );
                model.addAttribute( "selectedPage", pageNum );
            }
            else {
                catalogSort = new CatalogSort();
                catalogSort.setSort("featured");
                populateProducts(productList, currentCategory);
                model.addAttribute("displayProducts", sortProducts(catalogSort, productList));
                model.addAttribute( "displayProductsTotal", totalActiveItemCount );
                model.addAttribute( "totalPages", totalPages );
                model.addAttribute( "selectedPage", pageNum );
            }
        }

        return productFound;
    }

    private void addRatingSummaryToModel(String productId, ModelMap model) {
        RatingSummary ratingSummary = ratingService.readRatingSummary(productId, RatingType.PRODUCT);
        model.addAttribute("ratingSummary", ratingSummary);
    }

    private void populateProducts (List<Product> productList, Category currentCategory ) {

        for (FeaturedProduct featuredProduct : currentCategory.getFeaturedProducts()) {
            for (Product product: productList) {
                if ((product.equals(featuredProduct.getProduct()))) {
                    product.setPromoMessage(featuredProduct.getPromotionMessage());
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private List<Product> sortProducts (CatalogSort catalogSort, List<Product> displayProducts) {
        if (catalogSort.getSort().equals("priceL")) {
            Collections.sort(displayProducts, new BeanComparator("skus[0].salePrice"));
        }
        else if (catalogSort.getSort().equals("priceH")) {
            Collections.sort(displayProducts, new ReverseComparator(new BeanComparator("skus[0].salePrice")));
        }
        else if (catalogSort.getSort().equals("manufacturerA")) {
            Collections.sort(displayProducts, new BeanComparator("manufacturer"));
        }
        else if (catalogSort.getSort().equals("manufacturerZ")) {
            Collections.sort(displayProducts, new ReverseComparator(new BeanComparator("manufacturer")));
        }
        else if (catalogSort.getSort().equals("featured")) {
            Collections.sort(displayProducts, new ReverseComparator(new BeanComparator("promoMessage")));
        }

        return displayProducts;
    }

    private String parseStringBuffer(StringBuffer sb) {
        String str = "";
        try {
            str = sb.toString();
            if(str !=null && str.length() > 1)
                str = str.substring(0, str.length()-1);
        } catch (Exception exp) { exp.printStackTrace(); }
        return str;
    }



    // check to see if cookie present
    private void parseSelectedStore ( HttpServletRequest req ) {
        Cookie[] cookie = req.getCookies();
        if ( cookie == null ) {
            selectedStoreId = new Long ( 0 );
            return;
        }

        for ( Cookie c : cookie ) {
            if ( "catalog".equals( c.getName()  ) ) {
                try {
                    selectedStoreId = new Long(c.getValue().substring( 7 ) );
                    break;
                }
                catch(StringIndexOutOfBoundsException se) {
                    se.printStackTrace();
                }
            }
        }

        if ( selectedStoreId == null )
            selectedStoreId = new Long ( 0 );
    }




    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(Long.class, new CustomNumberEditor(Long.class, false));
        binder.registerCustomEditor(Integer.class, new CustomNumberEditor(Integer.class, false));
    }

    public Long getRootCategoryId() {

        return rootCategoryId;
    }

    public String getRootCategoryName() {
        return rootCategoryName;
    }

    public void setRootCategoryId(Long rootCategoryId) {

        this.rootCategoryId = rootCategoryId;
    }

    public void setRootCategoryName(String rootCategoryName) {
        this.rootCategoryName = rootCategoryName;
    }

    public CatalogServiceExt getCatalogService() {
        return catalogService;
    }

    public void setCatalogService(CatalogServiceExt catalogService) {
        this.catalogService = catalogService;
    }

    public RatingService getRatingService() {
        return ratingService;
    }

    public void setRatingService(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    public String getDefaultCategoryView() {
        return defaultCategoryView;
    }

    public void setDefaultCategoryView(String defaultCategoryView) {
        this.defaultCategoryView = defaultCategoryView;
    }

    public String getDefaultProductView() {
        return defaultProductView;
    }

    public void setDefaultProductView(String defaultProductView) {
        this.defaultProductView = defaultProductView;
    }

    public String getCategoryTemplatePrefix() {
        return categoryTemplatePrefix;
    }

    public void setCategoryTemplatePrefix(String categoryTemplatePrefix) {
        this.categoryTemplatePrefix = categoryTemplatePrefix;
    }


    public void CreateHoverMenu(List<Category> categoryList, ModelMap model) {


        List<Category> gardenSiblings = new ArrayList<Category>();
        List<Category> bmSiblings = new ArrayList<Category>();
        List<Category> elecSiblings = new ArrayList<Category>();
        List<Category> hardSiblings = new ArrayList<Category>();
        List<Category> homesSiblings = new ArrayList<Category>();
        List<Category> paintSiblings = new ArrayList<Category>();
        List<Category> plumbSiblings = new ArrayList<Category>();
        List<Category> seasonSiblings = new ArrayList<Category>();
        List<Category> toolsSiblings = new ArrayList<Category>();

        Category currentCategory = (Category) categoryList.get(categoryList.size()-1);

        String storeLink = "/catalog/store/";
            //Get sibling categories for garden
            currentCategory = catalogService.findCategoryByName("Garden");
            if(currentCategory != null) {
                String sibName = "";
                String sibLink = "";
                StringBuffer sb = new StringBuffer();
                StringBuffer sb1 = new StringBuffer();
                gardenSiblings = sortCategories(currentCategory.getAllChildCategories());
                //gardenSiblings = currentCategory.getAllChildCategories();
                //model.addAttribute("gardenSiblings", gardenSiblings);
                for ( Category category : gardenSiblings ) {

                    sibName = category.getName();
                    sibLink = storeLink+currentCategory.getName()+"/"+category.getUrlKey();
                    sb.append("[\"").append(sibName).append("\",\"").append(sibLink).append("\"],");
                    sb1.append("[\"").append(replaceCommas(sibName)).append("\",\"").append(replaceCommas(sibLink)).append("\"],");
                }
                gardenmenutxt = parseStringBuffer(sb);
                gardenmenutxt1 = parseStringBuffer(sb1);
            }
            model.addAttribute("gardenmenutxt", gardenmenutxt);
            model.addAttribute("gardenmenutxt1", gardenmenutxt1);
            //System.out.println("gardenmenutxt   " + gardenmenutxt );


            currentCategory = catalogService.findCategoryByName("Building Materials");
            if(currentCategory != null) {
                String sibName = "";
                String sibLink = "";
                StringBuffer sb = new StringBuffer();
                StringBuffer sb1 = new StringBuffer();
                /*
                 * @Comment: Name and Url key are different, in building materials only
                 */
                String bmURLKey = currentCategory.getUrlKey();
                bmSiblings = sortCategories(currentCategory.getAllChildCategories());
                for ( Category category : bmSiblings ) {
                    sibName = category.getName();
                    sibLink = storeLink+bmURLKey+"/"+category.getUrlKey();
                    sb.append("[\"").append(sibName).append("\",\"").append(sibLink).append("\"],");
                    sb1.append("[\"").append(replaceCommas(sibName)).append("\",\"").append(replaceCommas(sibLink)).append("\"],");
                }
                bmmenutxt = parseStringBuffer(sb);
                bmmenutxt1 = parseStringBuffer(sb1);
            }
            model.addAttribute("bmmenutxt", bmmenutxt);
            model.addAttribute("bmmenutxt1", bmmenutxt1);
            //System.out.println("bmmenutxt   " + bmmenutxt );



            currentCategory = catalogService.findCategoryByName("Electrical");
            if(currentCategory != null) {
                String sibName = "";
                String sibLink = "";
                StringBuffer sb = new StringBuffer();
                StringBuffer sb1 = new StringBuffer();
                elecSiblings = sortCategories(currentCategory.getAllChildCategories());
                for ( Category category : elecSiblings ) {
                    sibName = category.getName();
                    sibLink = storeLink+currentCategory.getName()+"/"+category.getUrlKey();
                    sb.append("[\"").append(sibName).append("\",\"").append(sibLink).append("\"],");
                    sb1.append("[\"").append(replaceCommas(sibName)).append("\",\"").append(replaceCommas(sibLink)).append("\"],");
                }
                electricalmenutxt = parseStringBuffer(sb);
                electricalmenutxt1 = parseStringBuffer(sb1);
            }
            model.addAttribute("electricalmenutxt", electricalmenutxt);
            model.addAttribute("electricalmenutxt1", electricalmenutxt1);



            currentCategory = catalogService.findCategoryByName("Hardware");
            if(currentCategory != null) {
                String sibName = "";
                String sibLink = "";
                StringBuffer sb = new StringBuffer();
                StringBuffer sb1 = new StringBuffer();
                hardSiblings = sortCategories(currentCategory.getAllChildCategories());
                for ( Category category : hardSiblings ) {
                    sibName = category.getName();
                    sibLink = storeLink+currentCategory.getName()+"/"+category.getUrlKey();
                    sb.append("[\"").append(sibName).append("\",\"").append(sibLink).append("\"],");
                    sb1.append("[\"").append(replaceCommas(sibName)).append("\",\"").append(replaceCommas(sibLink)).append("\"],");
                }
                hardwaremenutxt = parseStringBuffer(sb);
                hardwaremenutxt1 = parseStringBuffer(sb1);
            }
            model.addAttribute("hardwaremenutxt", hardwaremenutxt);
            model.addAttribute("hardwaremenutxt1", hardwaremenutxt1);


            currentCategory = catalogService.findCategoryByName("Home&Storage");
            if(currentCategory != null) {
                String sibName = "";
                String sibLink = "";
                StringBuffer sb = new StringBuffer();
                StringBuffer sb1 = new StringBuffer();
                homesSiblings = sortCategories(currentCategory.getAllChildCategories());
                for ( Category category : homesSiblings ) {
                    sibName = category.getName();
                    sibLink = storeLink+currentCategory.getName()+"/"+category.getUrlKey();
                    sb.append("[\"").append(sibName).append("\",\"").append(sibLink).append("\"],");
                    sb1.append("[\"").append(replaceCommas(sibName)).append("\",\"").append(replaceCommas(sibLink)).append("\"],");
                }
                homestoragemenutxt = parseStringBuffer(sb);
                homestoragemenutxt1 = parseStringBuffer(sb1);
            }
            model.addAttribute("homestoragemenutxt", homestoragemenutxt);
            model.addAttribute("homestoragemenutxt1", homestoragemenutxt1);


            currentCategory = catalogService.findCategoryByName("Paint");
            if(currentCategory != null) {
                String sibName = "";
                String sibLink = "";
                StringBuffer sb = new StringBuffer();
                StringBuffer sb1 = new StringBuffer();

                paintSiblings = sortCategories(currentCategory.getAllChildCategories());
                for ( Category category : paintSiblings ) {
                    sibName = category.getName();
                    sibLink = storeLink+currentCategory.getName()+"/"+category.getUrlKey();
                    sb.append("[\"").append(sibName).append("\",\"").append(sibLink).append("\"],");
                    sb1.append("[\"").append(replaceCommas(sibName)).append("\",\"").append(replaceCommas(sibLink)).append("\"],");
                }
                paintmenutxt = parseStringBuffer(sb);
                paintmenutxt1 = parseStringBuffer(sb1);
            }
            model.addAttribute("paintmenutxt", paintmenutxt);
            model.addAttribute("paintmenutxt1", paintmenutxt1);

            currentCategory = catalogService.findCategoryByName("Plumbing");
            if(currentCategory != null) {
                String sibName = "";
                String sibLink = "";
                StringBuffer sb = new StringBuffer();
                StringBuffer sb1 = new StringBuffer();
                plumbSiblings = sortCategories(currentCategory.getAllChildCategories());
                for ( Category category : plumbSiblings ) {
                    sibName = category.getName();
                    sibLink = storeLink+currentCategory.getName()+"/"+category.getUrlKey();
                    sb.append("[\"").append(sibName).append("\",\"").append(sibLink).append("\"],");
                    sb1.append("[\"").append(replaceCommas(sibName)).append("\",\"").append(replaceCommas(sibLink)).append("\"],");
                }
                plumbmenutxt = parseStringBuffer(sb);
                plumbmenutxt1 = parseStringBuffer(sb1);
            }
            model.addAttribute("plumbmenutxt", plumbmenutxt);
            model.addAttribute("plumbmenutxt1", plumbmenutxt1);



            currentCategory = catalogService.findCategoryByName("Seasonal");
            if(currentCategory != null) {
                String sibName = "";
                String sibLink = "";
                StringBuffer sb = new StringBuffer();
                StringBuffer sb1 = new StringBuffer();
                seasonSiblings = sortCategories(currentCategory.getAllChildCategories());
                for ( Category category : seasonSiblings ) {
                    sibName = category.getName();
                    sibLink = storeLink+currentCategory.getName()+"/"+category.getUrlKey();
                    sb.append("[\"").append(sibName).append("\",\"").append(sibLink).append("\"],");
                    sb1.append("[\"").append(replaceCommas(sibName)).append("\",\"").append(replaceCommas(sibLink)).append("\"],");
                }
                seasonalmenutxt = parseStringBuffer(sb);
                seasonalmenutxt1 = parseStringBuffer(sb1);
            }
            model.addAttribute("seasonalmenutxt", seasonalmenutxt);
            model.addAttribute("seasonalmenutxt1", seasonalmenutxt1);

            currentCategory = catalogService.findCategoryByName("Tools");
            if(currentCategory != null) {
                String sibName = "";
                String sibLink = "";
                StringBuffer sb = new StringBuffer();
                StringBuffer sb1 = new StringBuffer();
                toolsSiblings = sortCategories(currentCategory.getAllChildCategories());
                for ( Category category : toolsSiblings ) {
                    sibName = category.getName();
                    sibLink = storeLink+currentCategory.getName()+"/"+category.getUrlKey();
                    sb.append("[\"").append(sibName).append("\",\"").append(sibLink).append("\"],");
                    sb1.append("[\"").append(replaceCommas(sibName)).append("\",\"").append(replaceCommas(sibLink)).append("\"],");
                }
                toolsmenutxt = parseStringBuffer(sb);
                toolsmenutxt1 = parseStringBuffer(sb1);
            }
            model.addAttribute("toolsmenutxt", toolsmenutxt);
            model.addAttribute("toolsmenutxt1", toolsmenutxt1);
        }

    private String replaceCommas(String str) {

        String string = str.replaceAll(",", "@");
        return(string);
    }

    private List<Category> sortCategories(List<Category> categories) {

        // Sort logic (Bubble sort)
        for(int i = categories.size(); --i>=0; ) {
            for(int j = 0;j<i;j++) {

                Category jjust;
                Category jplus1;
                    if((categories.get(j).getName()).compareTo(categories.get(j+1).getName()) > 0) {

                        jjust = categories.get(j);
                        jplus1 = categories.get(j+1);

                        categories.remove(j);
                        categories.add(j,jplus1);

                        categories.remove(j+1);
                        categories.add(j+1, jjust);
                    }
            }
        }
        return categories;
    }
}
