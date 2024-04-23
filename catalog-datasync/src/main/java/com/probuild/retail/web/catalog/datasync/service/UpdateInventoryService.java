package com.probuild.retail.web.catalog.datasync.service;

import java.util.Date;
import java.util.List;

import com.caucho.hessian.client.HessianProxyFactory;
import com.probuild.retail.web.catalog.domain.Item;
import com.probuild.retail.web.catalog.domain.ItemInventory;
import com.probuild.retail.web.catalog.ext.service.WebCatalogService;
import com.probuild.retail.web.catalog.repository.AS400ItemRepository;
import com.probuild.retail.web.catalog.datasync.domain.UpdateResponse;

public class UpdateInventoryService {

    private static String CATALOG_SERVICE_URL = "";
    private WebCatalogService service;
    public AS400ItemRepository AS400repository;
    public int success = 0;

    public UpdateInventoryService() {
        super();
    }


    public UpdateInventoryService(WebCatalogService service) {
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

    public UpdateResponse updateInventoryService() {

        //CATALOG_SERVICE_URL = "http://localhost:8085/catalog/services/CatalogService";
        System.out.println("Before Webservice connection -> "+(new Date()).toString());
        CATALOG_SERVICE_URL = "http://localhost:8080/catalog/services/CatalogService";
        HessianProxyFactory factory = new HessianProxyFactory();
        final WebCatalogService service;
        UpdateResponse updatedInventoryResponse = new UpdateResponse();
        try {

            factory.setUser( "jdoe" );
            factory.setPassword( "foo" );
            service = (WebCatalogService) factory.create(WebCatalogService.class, CATALOG_SERVICE_URL );
            // make a quick call to test
            //service.imageExists( "", "cat" ); // should return false
            System.out.println("After Webservice connection -> "+(new Date()).toString());
            UpdateInventoryService uIS = new UpdateInventoryService(service);
            updatedInventoryResponse = uIS.updateInventory();
        }
        catch(Exception e) {
            success = 1;
            e.printStackTrace();
        }
        updatedInventoryResponse.setResult(success);

        return updatedInventoryResponse;
    }

    public UpdateResponse updateInventory() {

        System.out.println("Update Inventory Start -> "+(new Date()).toString());

        int totalItems = 0;
        int deletedItems = 0;

        UpdateResponse updatedInvResp = new UpdateResponse();
        List<Item>items = service.findAllItemProducts();
        if(items != null) {
            System.out.println("items - "+items.size());
            totalItems = items.size();
            for(Item item : items) {

                System.out.println("Updating inventory on item "+item.getName()+" item.getSkuId() "+item.getSkuId());
                List<ItemInventory> invItems = AS400repository.findItemInventory((item.getSku()).toString());

                if(invItems == null || invItems.isEmpty()) {
                    System.out.println("Item inventory for "+item.getSku()+ " not found in the repository");
                    service.removeItem(item);
                    ++deletedItems;
                    continue;
                }
                if(invItems != null) {
                    for(ItemInventory invItm : invItems) {

                        if(invItm != null) {
                            invItm.setSkuId(item.getSkuId());
                        }
                    }
                    int value = service.saveItemInventory(invItems);
                    if(value == 0) {
                        System.out.println("No inventory items to be saved for "+item.getName());
                    } else {
                        System.out.println("..Saved inventory item -> "+item.getName());
                    }
                }
            }
        }
        AS400repository.disconnect();
        updatedInvResp.setItemsUpdated(totalItems);
        updatedInvResp.setItemsDeleted(deletedItems);
        return updatedInvResp;
    }


}
