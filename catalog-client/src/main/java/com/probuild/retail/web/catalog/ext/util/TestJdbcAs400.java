package com.probuild.retail.web.catalog.ext.util;

import java.util.ArrayList;
import java.util.List;

import com.probuild.retail.web.catalog.domain.Item;
import com.probuild.retail.web.catalog.repository.AS400ItemRepository;
import com.probuild.retail.web.catalog.repository.ItemRepository;


public class TestJdbcAs400 {

    /**
     *	Default constructor
     */
    public TestJdbcAs400() {
        super();
    }


    /**
     * @param args
     */
    public static void main(String[] args) {
        
        String url = "jdbc:as400://as400.dxlbr.com;S10036FD;naming=sql;errors=full;";
        String user = "SATCP";
        String pass = "SATCP";

        ItemRepository repository = new AS400ItemRepository();
        repository.setConnection( url, user, pass );
        repository.connect();
        
        List<String> codes = new ArrayList<String>(4);
        codes.add( "112705" );
        codes.add( "112509" );
        codes.add( "111768" );
        
        List<Item> items = repository.findItems( codes );
        
        if ( items == null ) {
            System.out.println ( "Null item" );
        }
        else {
            for ( Item item : items )
                System.out.println ( "Item " + item.getName() + "|" + item.getSku() + "|" + item.getManufacturer() + "|" + item.getRegularPrice() );
        }
        
        repository.disconnect();
    }

}
