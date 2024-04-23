package com.probuild.retail.web.catalog.ext.domain;

import java.util.Map;

import org.broadleafcommerce.store.domain.Store;
import org.broadleafcommerce.store.web.model.FindAStoreForm;

public class FindAStoreFormExt extends FindAStoreForm {

    private Map<Double,Store> sortedStoreMap;
    
    public FindAStoreFormExt() {
        super();
        
        
    }

    public Map<Double, Store> getSortedStoreMap() {
        return sortedStoreMap;
    }
    public void setSortedStoreMap(Map<Double, Store> sortedStoreMap) {
        this.sortedStoreMap = sortedStoreMap;
    }


}
