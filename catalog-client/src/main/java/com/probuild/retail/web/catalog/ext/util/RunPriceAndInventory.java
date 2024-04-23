package com.probuild.retail.web.catalog.ext.util;

import java.util.Date;
import java.util.List;

import com.probuild.retail.web.catalog.domain.Item;
import com.probuild.retail.web.catalog.domain.ItemInventory;
import com.probuild.retail.web.catalog.ext.service.WebCatalogService;
import com.probuild.retail.web.catalog.repository.AS400ItemRepository;



public class RunPriceAndInventory {

    public AS400ItemRepository AS400repository;
    private WebCatalogService service;

    public RunPriceAndInventory() {
        super();
    }

    public RunPriceAndInventory(WebCatalogService service) {
        super();
        this.service = service;

        AS400repository = new AS400ItemRepository();
        if(AS400repository.connect()) {
            System.out.println("AS400Repository Connection success");
        } else {
            System.out.println("AS400Repository Connection failed");
        }
    }

    public void updateInventory() {

        System.out.println("Update Inventory Start -> "+(new Date()).toString());
        System.out.println("Fetching items from database ........");

        List<Item>items = service.findAllItemProducts();
        System.out.println("items - "+items.size());

        //List<ItemInventory> invItems = new ArrayList<ItemInventory>();
        for(Item item : items) {

            System.out.println("Updating inventory on item "+item.getName());

            List<ItemInventory> invItems = AS400repository.findItemInventory((item.getSku()).toString());
            if(invItems == null) {
                System.out.println("Item inventory for "+item.getSku()+ " not found in the repository");
                continue;
            }
            if(invItems != null) {
                System.out.println(" invItems.size() "+invItems.size());
                for(ItemInventory invItm : invItems) {

                    if(invItm != null) {
                        invItm.setSkuId(item.getSkuId());
                    }
                }
                service.saveItemInventory(invItems);
                System.out.println("...Saved inventory item -> "+item.getName());
            }
        }
        AS400repository.disconnect();
        System.out.println("Update Inventory End -> "+(new Date()).toString());
    }

    public void updatePricing() {

        System.out.println("Update Price Start -> "+(new Date()).toString());
        System.out.println("Fetching items from database ........");

        List<Item>items = service.findAllItemProducts();
        System.out.println("items - "+items.size());

        for (Item itm : items) {

            System.out.println("Updating price on item "+itm.getName());
            Item rItm = AS400repository.findItem((itm.getSku()).toString());
            if(rItm == null) {
                System.out.println("Item "+itm.getSku().toString()+" not found in repository, deleting");
                service.removeItem(itm);
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
        AS400repository.disconnect();
        System.out.println("Update Price End -> "+(new Date()).toString());

    }



}
