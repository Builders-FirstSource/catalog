package com.probuild.retail.web.catalog.ext.util;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.validator.ClassValidator;
import org.hibernate.validator.InvalidValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.caucho.hessian.client.HessianProxyFactory;
import com.probuild.retail.web.catalog.domain.ImportedItem;
import com.probuild.retail.web.catalog.domain.Item;
import com.probuild.retail.web.catalog.ext.service.WebCatalogService;
import com.probuild.retail.web.catalog.repository.AS400ItemRepository;
import com.probuild.retail.web.catalog.repository.ItemRepository;
import com.probuild.retail.web.catalog.upload.BulkImporter;
import com.probuild.retail.web.catalog.upload.ImportProblem;
import com.probuild.retail.web.catalog.upload.Importer;
import com.probuild.retail.web.catalog.upload.XlsBulkImporter;


public class TestBulkImport {

    private static final Logger logger = 
                    LoggerFactory.getLogger( TestBulkImport.class );
    
    public TestBulkImport() {
        super();
    }


    /**
     * @param args
     */
    public static void main(String[] args) {
        
        String filePath = "c:/java_dev/catalog/catalog_bulk.xls";
        
        BulkImporter importer = new XlsBulkImporter ( );
        List<ImportedItem> items = importer.readUserInputtedItems( filePath );
        
        //Importer importWorker = new Importer ( getService() );
        //importWorker.precheck( items );
        
        //for ( ImportProblem prb : importWorker.getProblems() ) {
        //    logger.debug( "{} has problem {}", prb.getItem(), prb.getMessage() );
        //}
        
        // test validator
//        ClassValidator<Item> itemValidator = new ClassValidator<Item>( Item.class );
//        
//        
//        InvalidValue[] invalids = itemValidator.getInvalidValues( items.get( 0 ) );
//        
//        System.out.println ( "Number of errors: " + invalids.length );
//        for ( InvalidValue invalid : invalids ) {
//            System.out.println ( "message: " + invalid.getMessage() );
//            System.out.println ( "field: " + invalid.getPropertyName() );
//            System.out.println ( "value ==> " + invalid.getValue() ); 
//        }
    }

    
    private static WebCatalogService getService ( ) {
        String url = "http://localhost:8080/catalog/services/CatalogService";

        HessianProxyFactory factory = new HessianProxyFactory();
        WebCatalogService service = null;
        try {
            service = (WebCatalogService) factory.create(WebCatalogService.class, url);
        }
        catch(MalformedURLException e) {
            System.out.println ( "Error: ");
            e.printStackTrace();
        }
        
        return service;
    }
}
