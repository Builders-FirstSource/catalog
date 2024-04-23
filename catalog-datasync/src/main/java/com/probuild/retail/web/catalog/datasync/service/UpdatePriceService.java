package com.probuild.retail.web.catalog.datasync.service;

import java.util.Date;
import java.util.List;

import com.caucho.hessian.client.HessianProxyFactory;
import com.probuild.retail.web.catalog.domain.Item;
import com.probuild.retail.web.catalog.ext.service.WebCatalogService;
import com.probuild.retail.web.catalog.repository.AS400ItemRepository;
import com.probuild.retail.web.catalog.datasync.domain.UpdateResponse;

public class UpdatePriceService {

    private static String CATALOG_SERVICE_URL = "";
    private WebCatalogService service;
    public AS400ItemRepository AS400repository;
    private int success = 0;
    public UpdatePriceService() {
        super();
    }


    public UpdatePriceService(WebCatalogService service) {
        super();
        this.service = service;

        AS400repository = new AS400ItemRepository();
        if(AS400repository.connect()) {
            System.out.println("AS400Repository Connection success");
        } else {
            System.out.println("AS400Repository Connection failed");
            success = 1;
        }
        System.out.println("After AS400 connection status -> "+(new Date()).toString());

    }

    public UpdateResponse updatePricingService() {

        System.out.println("Before Webservice connection -> "+(new Date()).toString());
        //CATALOG_SERVICE_URL = "http://localhost:8085/catalog/services/CatalogService";
        CATALOG_SERVICE_URL = "http://localhost:8080/catalog/services/CatalogService";
        HessianProxyFactory factory = new HessianProxyFactory();
        final WebCatalogService service;
        UpdateResponse updatedPriceResponse = new UpdateResponse();
        try {

            factory.setUser( "jdoe" );
            factory.setPassword( "foo" );
            service = (WebCatalogService) factory.create(WebCatalogService.class, CATALOG_SERVICE_URL );
            // make a quick call to test
            //service.imageExists( "", "cat" ); // should return false
            System.out.println("After Webservice connection -> "+(new Date()).toString());
            UpdatePriceService uPS = new UpdatePriceService(service);
            updatedPriceResponse = uPS.updatePricing();

        }
        catch(Exception e) {
            success = 1;
            e.printStackTrace();
        }
        updatedPriceResponse.setResult(success);

        return updatedPriceResponse;
    }

    public UpdateResponse updatePricing() {

        System.out.println("Update Price Start -> "+(new Date()).toString());

        int totalItems = 0;
        int deletedItems = 0;
        UpdateResponse updatedPrResp = new UpdateResponse();

        List<Item>items = service.findAllItemProducts();

        if (items == null) {
            System.out.println("No items found");

        } else {
            System.out.println("items - "+items.size());
            totalItems = items.size();

            for (Item itm : items) {

                System.out.println("Updating price on "+itm.getName());
                Item rItm = AS400repository.findItem((itm.getSku()).toString());
                if(rItm == null) {
                    System.out.println("Deleting Item "+itm.getSku().toString()+". Not found in repository.");
                    service.removeItem(itm);
                    ++deletedItems;
                    continue;
                }


                if ( (rItm.getRegularPrice().doubleValue() != itm.getRegularPrice().doubleValue()) ||

                        ((rItm.getSalePrice().doubleValue() != itm.getSalePrice().doubleValue()) &&  rItm.getSalePrice().doubleValue() != 0 ) ||

                        ((rItm.getRegularPrice().doubleValue() != itm.getSalePrice().doubleValue()) &&  (rItm.getSalePrice().doubleValue() == 0)  )) {

                           itm.setRegularPrice(rItm.getRegularPrice());

                           System.out.println("...Updating regular price on item " + itm.getName() + " to " + rItm.getRegularPrice());

                           if(rItm.getSalePrice().doubleValue() == 0) {

                              itm.setSalePrice(rItm.getRegularPrice());

                              System.out.println("...set sale price to regular price " + itm.getName() + " to " + rItm.getRegularPrice());

                           } else {

                              itm.setSalePrice(rItm.getSalePrice());

                              System.out.println("...set sale price on item " + itm.getName() + " to " + rItm.getSalePrice());

                          }

                          service.saveItem ( itm );

                  }
            }

        }

        System.out.println("Update Price End -> "+(new Date()).toString());
        updatedPrResp.setItemsUpdated(totalItems);
        updatedPrResp.setItemsDeleted(deletedItems);
        AS400repository.disconnect();
        return updatedPrResp;
    }
}
