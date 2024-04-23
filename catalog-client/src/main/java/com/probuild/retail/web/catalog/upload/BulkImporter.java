package com.probuild.retail.web.catalog.upload;

import java.util.List;

import com.probuild.retail.web.catalog.domain.ImportedItem;
import com.probuild.retail.web.catalog.domain.Item;

public interface BulkImporter {

    public List<ImportedItem> readUserInputtedItems ( String fileName );
    
    public boolean writeUserInputtedTemplate ( List<Item> items, String fileName );
    
    public List<ImportedItem> getItemsRead();
}
