package com.probuild.retail.web.catalog.repository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.probuild.retail.web.catalog.domain.Item;
import com.probuild.retail.web.catalog.domain.ItemInventory;

public class AS400ItemRepository implements ItemRepository {
    
    private static final Logger logger = 
                            LoggerFactory.getLogger(AS400ItemRepository.class);
    
    private static final String JDBC_DRIVER = 
                            "com.ibm.as400.access.AS400JDBCDriver";
    
    private static final String BASE_ITEM_SQL =
        "SELECT RISKU# sku, RIUPC# upc, RTRIM(RIDESC) name, RIAITM alt, " +
               "RILSTU uom, RIDEPT dept, RIMNF# modelNum, " + 
               "RTRIM(SUBSTR(RJNAME, 1, 27)) manufacturer, RUPRC1 regularPrice, " +
               "(SELECT MAX(RSPRCE) FROM DRMS.DRSALEFL " +
                   "WHERE RSSTTS = 'A' AND RSSKU# = item.RISKU# AND " +
                       "RSRGPR = item.RIRLC#) salePrice, " +
               "(SELECT MIN(RSFRYR*10000 + RSFRMO*100 + RSFRDY) FROM DRMS.DRSALEFL " +
                   "WHERE RSSTTS = 'A' AND RSSKU# = item.RISKU# AND " +
                       "RSRGPR = item.RIRLC#) saleStartYYYYMMDD, " +
               "(SELECT MAX(RSTOYR*10000 + RSTOMO*100 + RSTODY) FROM DRMS.DRSALEFL " +
                   "WHERE RSSTTS = 'A' AND RSSKU# = item.RISKU# AND " +
                       "RSRGPR = item.RIRLC#) saleEndYYYYMMDD " +
        "FROM DRMS.DRITEMFL item left outer join " +
            "DRMS.DRMANFFL manuf on RIMANF = RJMANF " + 
                "left outer join DRMS.DRUPRCFL " +
                    "price on RISKU# = RUSKU# AND RIRLC# = RURGPR " +
        "WHERE RILOC# = 11 AND RIRLC# = 0 AND (RIITCD = 'I' OR RIITCD = 'K') AND " +
               "NOT EXISTS (SELECT OJSKU# FROM DRMS.DODCITFL WHERE OJSKU# = RISKU# AND OJRGPR = 0)";
    
    private static final String BASE_ITEM_INVENTORY_SQL =
        "SELECT riloc# as locationId, risku# as skuNumber, rilstu as uom, " +
               "ritotq as qtyTotal, ritotf as ftgTotal, " +
               "riavlq as qtyAvailable, riavlf as ftgAvailable, " +
               "ripreq as qtyPreSold, ripref as ftgPreSold, " +
               "riordq as qtyOnOrder, riordf as ftgOnOrder, " +
               "ribckq as qtyBackOrdered, ribckf as ftgBackOrdered, " +
               "rireoq as reorderQty, rireop maxOnhandQty, " +
               "rireof as  reorderFtg, rireop as maxOnhandFtg " +
        "FROM DRMS.DRITEMFL WHERE ricmp# = '  ' and riloc# IN (1, 3, 4, 6, 9, 11, 12, 14, 15, 16)";
    
    protected Connection conn;
    
    protected String url = "jdbc:as400://as400.dxlbr.com;S10036FD;naming=sql;errors=full;";
    protected String user = "SATCP";
    protected String pass = "SATCP";
    
    /**
     *	Default constructor
     */
    public AS400ItemRepository() {
        super();
    }


    public boolean connect() {
        boolean success = false;
        try {
            Class.forName( JDBC_DRIVER );
            conn = DriverManager.getConnection( url, user, pass );
            success = true;
        }
        catch(ClassNotFoundException e) {
            logger.error( "Failed to find JDBC driver", e );
        }
        catch(SQLException e) {
            logger.error( "Failed to connect to ERP database", e );
        }
        
        return success;
    }

    public boolean disconnect() {
        boolean success = false;
        try {
            DbUtils.close( conn );
        }
        catch(SQLException e) {
            success = true;
        }
        return success;
    }

    @SuppressWarnings("unchecked")
    public Item findItem(String itemCode) {
        Item item = null;
        
        QueryRunner run = new QueryRunner();
        ResultSetHandler<Item> h = new BeanHandler(Item.class);
        try {
            item = run.query( conn, BASE_ITEM_SQL + " AND RISKU# = ?", h, itemCode );
            if ( item == null ) // no results
                return null;
            
            if ( item.getSalePrice() == null )
                item.setSalePrice( new BigDecimal ( 0 ) );
            if ( item.getRegularPrice() == null )
                item.setRegularPrice( new BigDecimal ( 0 ) );
        }
        catch(SQLException e) {
            logger.error( "problem getting data", e );
        }
        
        return item;
    }

    @SuppressWarnings("unchecked")
    public List<Item> findItems(List<String> itemCodes) {
        List<Item> item = null;
        
        QueryRunner run = new QueryRunner();
        ResultSetHandler<List<Item>> h = new BeanListHandler(Item.class);

        try {
            String codes = Arrays.toString( itemCodes.toArray() );
            codes = codes.substring( 1, codes.length() - 1 ); // remove [[ ]]
            item = 
              run.query( conn, BASE_ITEM_SQL + " AND RISKU# IN (" + codes + ")", h );

        }
        catch(SQLException e) {
            logger.error( "problem getting data", e );
        }
        
        if ( item == null )
            item = new ArrayList<Item>(0);
        
        return item;
    }

    @SuppressWarnings("unchecked")
    public List<Item> findItemsByDept(Long deptId, String itemCodeBegin, String itemCodeEnd) {
        List<Item> item = null;
        
        QueryRunner run = new QueryRunner();
        ResultSetHandler<List<Item>> h = new BeanListHandler(Item.class);

        try {
            item = run.query( conn, 
              BASE_ITEM_SQL + " AND RIDEPT = ? AND RISKU# >= ? AND RISKU# <= ?",
              h, deptId, itemCodeBegin, itemCodeEnd  );

        }
        catch(SQLException e) {
            logger.error( "problem getting data", e );
        }
        
        if ( item == null )
            item = new ArrayList<Item>(0);
        
        return item;
    }

    @SuppressWarnings("unchecked")
    public List<Item> findItemsByManuf(Long manufId, String itemCodeBegin, String itemCodeEnd) {
        List<Item> item = null;
        
        QueryRunner run = new QueryRunner();
        ResultSetHandler<List<Item>> h = new BeanListHandler(Item.class);

        try {
            item = run.query( conn, 
              BASE_ITEM_SQL + " AND RIMANF = ? AND RISKU# >= ? AND RISKU# <= ?",
              h, manufId, itemCodeBegin, itemCodeEnd  );

        }
        catch(SQLException e) {
            logger.error( "problem getting data", e );
        }
        
        if ( item == null )
            item = new ArrayList<Item>(0);
        
        return item;
    }

    
    @SuppressWarnings("unchecked")
    public List<Item> findItemsByVendor(Long vendId, String itemCodeBegin, String itemCodeEnd) {
        List<Item> item = null;
        
        QueryRunner run = new QueryRunner();
        ResultSetHandler<List<Item>> h = new BeanListHandler(Item.class);

        try {
            item = run.query( conn, 
              BASE_ITEM_SQL + " AND RIVEND = ? AND RISKU# >= ? AND RISKU# <= ?",
              h, vendId, itemCodeBegin, itemCodeEnd  );

        }
        catch(SQLException e) {
            logger.error( "problem getting data", e );
        }
        
        if ( item == null )
            item = new ArrayList<Item>(0);
        
        return item;
    }    
    
    @SuppressWarnings("unchecked")
    public List<Item> findItems(String itemCodeBegin, String itemCodeEnd) {
        List<Item> item = null;
        
        QueryRunner run = new QueryRunner();
        ResultSetHandler<List<Item>> h = new BeanListHandler(Item.class);

        try {
            item = run.query( conn, 
              BASE_ITEM_SQL + " AND RISKU# >= ? AND RISKU# <= ?",
              h, itemCodeBegin, itemCodeEnd  );

        }
        catch(SQLException e) {
            logger.error( "problem getting data", e );
        }
        
        if ( item == null )
            item = new ArrayList<Item>(0);
        
        return item;
    }
    
    
    @SuppressWarnings("unchecked")
    public List<ItemInventory> findItemInventory( String itemCode ) {
        List<ItemInventory> item = null;
        
        QueryRunner run = new QueryRunner();
        ResultSetHandler<List<ItemInventory>> h = 
                                    new BeanListHandler(ItemInventory.class);

        try {
            item = 
              run.query( conn, BASE_ITEM_INVENTORY_SQL + " AND RISKU# = ?", 
                                                                 h, itemCode );

        }
        catch(SQLException e) {
            logger.error( "problem getting data", e );
        }
        
        if ( item == null )
            item = new ArrayList<ItemInventory>(0);
        
        return item;
    }
    
    public void setConnection(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.pass = password;

    }

}
