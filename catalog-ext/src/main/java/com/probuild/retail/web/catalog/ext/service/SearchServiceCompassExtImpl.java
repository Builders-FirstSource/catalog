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

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.Query;
import org.broadleafcommerce.catalog.domain.Category;
import org.broadleafcommerce.catalog.domain.Product;
import org.broadleafcommerce.catalog.domain.ProductImpl;
import org.broadleafcommerce.catalog.service.CatalogService;
import org.broadleafcommerce.catalog.web.CatalogSort;
import org.broadleafcommerce.search.dao.SearchInterceptDao;
import org.broadleafcommerce.search.dao.SearchSynonymDao;
import org.broadleafcommerce.search.domain.SearchIntercept;
import org.broadleafcommerce.search.domain.SearchSynonym;
import org.broadleafcommerce.search.service.SearchService;
import org.compass.core.Compass;
import org.compass.core.CompassContext;
import org.compass.core.CompassDetachedHits;
import org.compass.core.CompassHits;
import org.compass.core.CompassIndexSession;
import org.compass.core.CompassQuery;
import org.compass.core.CompassSearchSession;
import org.compass.core.CompassSession;
import org.compass.core.engine.SearchEngineIndexManager;
import org.compass.core.lucene.util.LuceneHelper;

import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.util.UrlPathHelper;

import com.probuild.retail.web.catalog.ext.dao.CatalogImagesDaoImpl;

/**
 * @author dmclain
 *
 */

@Service("blSearchServiceExt")
public class SearchServiceCompassExtImpl implements SearchService {

    private static final Logger LOG = Logger.getLogger(SearchServiceCompassExtImpl.class);

    private static String PATH;
    private String rootCategoryName;

    public String gardenmenutxt = "";
    public String bmmenutxt = "";
    public String hardwaremenutxt = "";
    public String toolsmenutxt = "";
    public String paintmenutxt = "";
    public String plumbmenutxt = "";
    public String electricalmenutxt = "";
    public String homestoragemenutxt = "";
    public String seasonalmenutxt = "";


    @CompassContext
    protected Compass compass;

    @Resource(name = "blCatalogServiceExt")
    protected CatalogServiceExt catalogService;

    @Resource(name = "blSearchInterceptDao")
    protected SearchInterceptDao searchInterceptDao;

    @Resource(name = "blSearchSynonymDao")
    protected SearchSynonymDao searchSynonymDao;




    @SuppressWarnings("unchecked")
    public List<Product> performSearch(String input) {
        LOG.info( "Search for " + input );
        /*
         * Returns the path which gets appended to create the image under tomcat
         */
        String envImageFolder = System.getenv( "CATALOG_IMAGE_DIR" );
        if ( envImageFolder == null ) {
            PATH = "";
        } else {
            System.out.println ( "using image folder at " + envImageFolder );
            PATH = envImageFolder;
        }

        //CompassSearchSession session = compass.openSearchSession();
        CompassSession session = compass.openSession();

        //CompassDetachedHits hits = session.find(input).detach();
        //CompassHits hits = session.find(input);
        //System.out.println(" Number of hits: " + hits.length());

        QueryParser queryParser = new QueryParser ( "zzz-all", new StandardAnalyzer() );
        queryParser.setDefaultOperator(QueryParser.AND_OPERATOR);
        Query query = null;
        try {
            query = queryParser.parse( escapeInches(input) );
        } catch ( ParseException e ) {
            LOG.error( "Parse term exception " + e.getMessage() );
            return new ArrayList<Product>(0);
        }

        CompassQuery myCQ = LuceneHelper.createCompassQuery( session, query );
        System.out.println(" Query: " + myCQ.toString());

        CompassHits hits = myCQ.hits();
        System.out.println(" Number of hits: " + hits.length());

        // Debug
        //if ( LOG.isDebugEnabled() ) {
            /*for (int i = 0; i < hits.length(); i++) {
                Explanation exp = LuceneHelper.getLuceneSearchEngineHits(hits).explain(i);
                System.out.println(exp.toString());
            }*/
        //}


        List<Product> results = new ArrayList<Product>();


        List<Product> resourceProducts = new ArrayList<Product>(hits.length());
        for (int i = 0; i < hits.length(); i++) {
            Product resourceProduct = (Product) hits.data(i);
            resourceProducts.add(resourceProduct);

        }

        List<Long> ids = new ArrayList<Long>();
        for(Product p : resourceProducts) {
            ids.add(p.getId());
        }

        List<Product> loadedProducts = new ArrayList<Product>();


        /*
         * @Comment: A maximum of 1990 records can be sent at once for the Query
         */

        Vector allchunklist = new Vector();
        List eachchunklist = new ArrayList();
        if(ids.size() <= 1990) {
            allchunklist.addElement(ids);

        } else {
            //Split into small chunks
            for(int k=0; k<ids.size(); k++) {
                if((k%1990 == 0) && (eachchunklist.size() > 0)) {
                    allchunklist.addElement(eachchunklist);
                    eachchunklist = new ArrayList();
                } else {
                    eachchunklist.add(ids.get(k));
                }
            }
            //Any residual should be added here
            if(eachchunklist.size() >= 0) {
                allchunklist.addElement(eachchunklist);
            }
        }

        for(int m=0; m<allchunklist.size(); m++ ) {
            ids = (ArrayList)allchunklist.elementAt(m);
            if(ids != null) {
                if(!(ids.isEmpty())) {
                    loadedProducts = catalogService.findProductByIds(ids);
                    //System.out.println("loadedProducts.size() --> "+loadedProducts.size());
                }
            }

            List<Long> productIds = new ArrayList<Long>();

            if (ids != null) {
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
            }
            if(loadedProducts != null) {
                if(!(loadedProducts.isEmpty())) {
                    for(Product loadedProduct : loadedProducts) {
                        if ( loadedProduct.getAllSkus() != null ) {
                            loadedProduct.getAllSkus().size(); // fetch these now
                        }
                        if ( loadedProduct.getProductImages() != null ) {
                            loadedProduct.getProductImages().size(); // fetch these now
                        }
                        results.add( loadedProduct );
                    }
                }
            }
        }
        List<Product> sortedResults = new ArrayList<Product>();
        if(results != null) {
            sortedResults = sortProductsBasedOnCategory(results);
        }
        session.close();
        return sortedResults;
    }

    private List<Product> sortProductsBasedOnCategory(List<Product> results) {

        for(int i = results.size(); --i>=0; ) {
            for(int j = 0;j<i;j++) {

                Product jjust;
                Product jplus1;

                if((results.get(j).getDefaultCategory().getName()).compareTo(results.get(j+1).getDefaultCategory().getName()) > 0) {

                        jjust = results.get(j);
                        jplus1 = results.get(j+1);

                        results.remove(j);
                        results.add(j,jplus1);

                        results.remove(j+1);
                        results.add(j+1, jjust);
                    }
            }
        }
        return results;

    }


    public void rebuildProductIndex() {
        LOG.info("Rebuilding product index");
        List<Product> products = catalogService.findAllProducts();

        SearchEngineIndexManager manager = compass.getSearchEngineIndexManager();
        if (!manager.indexExists()) {
            manager.createIndex();
        }

        CompassIndexSession session = compass.openIndexSession();

        for (Product product : products) {
            // not really needed any more
            //if ( !product.getClass().getName().
            //        equals("org.broadleafcommerce.catalog.domain.ProductImpl" ) )
            session.save(product);
        }
        session.commit();
        session.close();
    }

    private String escapeInches ( String term ) {
        if ( StringUtils.countMatches( term, "\"" )%2 == 1 ) {
            return StringUtils.replace( term, "\"", "\\\"" );
        }
        return term;
    }

    public SearchIntercept getInterceptForTerm(String term) {
        return searchInterceptDao.findInterceptByTerm(term);
    }

    public List<SearchIntercept> getAllSearchIntercepts() {
        return searchInterceptDao.findAllIntercepts();
    }

    public void createSearchIntercept(SearchIntercept intercept) {
        searchInterceptDao.createIntercept(intercept);
    }

    public void deleteSearchIntercept(SearchIntercept intercept) {
        searchInterceptDao.deleteIntercept(intercept);
    }

    public void updateSearchIntercept(SearchIntercept intercept) {
        searchInterceptDao.updateIntercept(intercept);
    }

    public void createSearchSynonym(SearchSynonym synonym) {
        searchSynonymDao.createSynonym(synonym);
    }

    public void deleteSearchSynonym(SearchSynonym synonym) {
        searchSynonymDao.deleteSynonym(synonym);
    }

    public List<SearchSynonym> getAllSearchSynonyms() {
        return searchSynonymDao.getAllSynonyms();
    }

    public void updateSearchSynonym(SearchSynonym synonym) {
        searchSynonymDao.updateSynonym(synonym);
    }

    public Compass getCompass() {
        return compass;
    }

    public void setCompass(Compass compass) {
        this.compass = compass;
    }

    public CatalogServiceExt getCatalogService() {
        return catalogService;
    }

    public void setCatalogService(CatalogServiceExt catalogService) {
        this.catalogService = catalogService;
    }

    public SearchInterceptDao getSearchInterceptDao() {
        return searchInterceptDao;
    }

    public void setSearchInterceptDao(SearchInterceptDao searchInterceptDao) {
        this.searchInterceptDao = searchInterceptDao;
    }

    public SearchSynonymDao getSearchSynonymDao() {
        return searchSynonymDao;
    }

    public void setSearchSynonymDao(SearchSynonymDao searchSynonymDao) {
        this.searchSynonymDao = searchSynonymDao;
    }




}
