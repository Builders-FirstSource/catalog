package com.probuild.retail.web.catalog.ext.util;

import java.net.MalformedURLException;

import com.caucho.hessian.client.HessianProxyFactory;
import com.probuild.retail.web.catalog.ext.service.WebCatalogService;

public class TestHessian {

    /**
     *	Default constructor
     */
    public TestHessian() {
        super();
    }


    /**
     * @param args
     */
    public static void main(String[] args) {
        
        String url = "http://localhost:8080/catalog/services/CatalogService";

        HessianProxyFactory factory = new HessianProxyFactory();
        WebCatalogService service;
        try {
            
            service = (WebCatalogService) factory.create(WebCatalogService.class, url);
            System.out.println("findAllCategoriesClient(): " + service.findAllCategories().get(3).getName() );
        }
        catch(MalformedURLException e) {
            System.out.println ( "Error: ");
            e.printStackTrace();
        }

        



    }

}
