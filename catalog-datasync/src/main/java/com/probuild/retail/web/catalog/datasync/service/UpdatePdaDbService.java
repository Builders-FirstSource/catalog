/*
 * UpdatePdaDbService
 *
 * Update the PDA database (pda.db) on the iseries location
 *
 * Created on October 27, 2013
 *
 *
 * ProBuild, 2015
 */

package com.probuild.retail.web.catalog.datasync.service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;

import com.probuild.retail.web.catalog.datasync.utils.AS400FileAccess;

public class UpdatePdaDbService {

    private Connection as400Conn = null;
    private Connection sqliteConn = null;
    public final int DB_COMMIT_COUNT = 1000;

    public UpdatePdaDbService() {

    }

    /*
     * Open AS400 and sqlite database connections
     */
    @Deprecated
    public void openConnections ( ) {

        // Establish connections to iSeries and SQLite DBs
        try {

            Class.forName( "com.ibm.as400.access.AS400JDBCDriver" );
            String url ="jdbc:as400://as400.dxlbr.com;S10036FD;naming=sql;errors=full;";
            as400Conn = DriverManager.getConnection( url, "SATCP", "SATCP" );


            Class.forName( "org.sqlite.JDBC" );
            //sqliteConn = DriverManager.getConnection("jdbc:sqlite://pda.db");
            sqliteConn = DriverManager.getConnection("jdbc:sqlite:C:\\pda.db");
            sqliteConn.setAutoCommit(false);

        }
        catch (Exception e) {
            //e.printStackTrace();
            System.out.println ( e.getMessage() );

        }
    }

    
    /*
     * Open AS400 and sqlite database connections
     *
     * @param - Connection string
     */
    public void openConnections (String connection) {

    	
    	System.out.println("In openconnection : connection -> "+connection);
        // Establish connections to iSeries and SQLite DBs
        try {

            Class.forName( "com.ibm.as400.access.AS400JDBCDriver" );
            String url ="jdbc:as400://as400.dxlbr.com;S10036FD;naming=sql;errors=full;";
            as400Conn = DriverManager.getConnection( url, "SATCP", "SATCP" );


            Class.forName( "org.sqlite.JDBC" );
            //sqliteConn = DriverManager.getConnection("jdbc:sqlite://pda.db");
            sqliteConn = DriverManager.getConnection(connection);
            System.out.println("sqliteConn -> "+sqliteConn);
            sqliteConn.setAutoCommit(false);

        }
        catch (Exception e) {
            //e.printStackTrace();
            System.out.println ( e.getMessage() );

        }
    }
    

    /*
     * Close AS400 and sqlite database connections.
     *
     */
    public void closeConnections ( ) {

        try {

            as400Conn.close();
            sqliteConn.close();
        }
        catch ( Exception e ) {
            System.out.println ( e.getMessage() );
        }
    }


    /*
     * Temporary class for testing
     */
    public boolean UpdatePdaDb() {

        return UpdatePdaDb(AS400FileAccess.AS400_DIR);

    }

    /*
     * This is the main method.  Invokes all methods to update the
     * database.
     *
     * @return boolean success/failure
     */
    @Deprecated
    public boolean UpdatePdaDb(String as400_loc) {

        boolean retVal = false;

        // Open the database connections
        openConnections();

        Map<String,String> locRegMap = regLocMap("compile");
        for(Map.Entry<String,String> entry : locRegMap.entrySet()) {

        	String loc_code = entry.getKey();
        	
        	if (!loc_code.equalsIgnoreCase("1")) continue; 
        	
        	cleanTables();

            buildItemTable("compile", entry.getKey());
            
            System.out.println("LOCATION KEY "+entry.getKey()+" REGION KEY "+entry.getValue());
            
            buildLocationTable("compile");
            
            buildRegionTable("compile");

            buildItemAliasTable("compile");
            
            buildItemPriceTable("compile", entry.getValue()); /*RURGPR*/
            
            buildItemRecvTable("compile", entry.getKey());

            buildCycleCountTable("compile", entry.getKey());
            
            updatePdaInfo(entry.getKey(),entry.getValue());

            // Copy DB file to AS400 location folder
            UploadDb(as400_loc, entry.getKey());
            
            retVal = true;
        }

        // Close all database locations
        closeConnections();
        return retVal;
    }

    
    /*
     * This is the main method.  Invokes all methods to update the
     * database.
     *
     * sourceDir for test setup sourceDir = "//"
     *           for Production sourceDir = "C:\\";
     *
     * @param as400_loc - Location of database upload target on the IFS folder
     * @param connections - Database connection string
     * @param sourceDir  - Database too folder for PDA.db
     * @param dbLib  - AS400 lib for doTrgLfl 
     * 
     * @return boolean success/failure
     */
    public boolean UpdatePdaDb(String as400_loc, String connections, String sourceDir, 
    		String dbLib) {

        boolean retVal = false;

        System.out.println("as400_loc " + as400_loc);
        System.out.println("connections " + connections);
        System.out.println("sourceDir " + sourceDir);
        System.out.println("dbLib " + dbLib);
        dbLib = dbLib.trim();

        // Open the database connections
        openConnections(connections);

        Map<String,String> locRegMap = regLocMap(dbLib);
        
        for (Map.Entry<String,String> entry : locRegMap.entrySet()) {

        	String loc_code = entry.getKey();
        	String region_code = entry.getValue();
        	
        	// Debug only
        	// if (!loc_code.equalsIgnoreCase("1")) continue; 

        	cleanTables();

            buildItemTable(dbLib, loc_code);
            
            System.out.println("LOCATION KEY " + loc_code + " REGION KEY " + entry.getValue());
            
            buildLocationTable(dbLib);
            
            buildRegionTable(dbLib);

            buildItemAliasTable(dbLib);
            
            buildItemPriceTable(dbLib, region_code);
            
            buildItemRecvTable(dbLib, loc_code);
            
            buildCycleCountTable(dbLib, loc_code);

            buildItemLumberTable(dbLib, loc_code, region_code);
            
            /*
             * Update the PDA_Info data
             */
            // updatePdaInfo(loc_code, entry.getValue());
            
            SimpleDateFormat sdf = new SimpleDateFormat ( "MM/dd/yyyy hh:mm a" );

            updateInfo("ITEM_BUILD_DATE", sdf.format( new Date ( ) ));
            updateInfo("PDA_LOCATION", loc_code);
            updateInfo("PDA_REGION", entry.getValue());
            updateInfo("AS400_LIB", dbLib);
            
            String as400_dir = "/QDLS/MC9190/LOC";
            if (dbLib.equalsIgnoreCase("DLCQA")) {
                as400_dir = "/QDLS/MC9190T/LOC";
            }
            updateInfo("AS400_DIR", as400_dir);            
            
            // Copy the database to the IFS folder
            UploadDb(as400_loc, loc_code, sourceDir);
            
            retVal = true;
        }

        // Close all database locations
        closeConnections();
        
        return retVal;
    }

    
    /*
     * This is the main method.  Invokes all methods to update the
     * database.
     *
     * @param as400_loc - File location on the AS400
     * @return boolean success/failure
     */
    @Deprecated
    public boolean UploadDb(String as400_loc, String loc) {

        boolean retVal = true;

        System.out.println ( " UploadDb invoked, as400_loc: " + as400_loc);

        // Filter input string
        as400_loc = as400_loc.trim();

        System.out.println ( " After as400_loc.trim(), as400_loc: " + as400_loc);

        // If string does not end with '/', add one
        if (as400_loc.charAt(as400_loc.length()-1) != '/') as400_loc += '/';

        //For test setup
        //String sourceDir = "//";
        //For Production
        String sourceDir = "C:\\";
        String sourceFile = "pda.db";

        // PA Made me do this
        if (loc.length() == 1) loc = "0" + loc;

        String destinationDir =  as400_loc + "LOC" + loc + "/";
        String destinationFile = sourceFile;

        AS400FileAccess af = new AS400FileAccess();
        if (!af.uploadFile(sourceDir, sourceFile, destinationDir, destinationFile)) {
            retVal = false;
        }
        return retVal;
    }

    
    /*
     * This is the main method.  Invokes all methods to update the
     * database.
     *
     * @param as400_loc - Location of database upload target on the IFS folder
     * @param loc - Store Location code for database (used to set the target under as400_loc)
     * @param sourceDir  - Database too folder for PDA.db
     * 
     * @return boolean success/failure
     */
    public boolean UploadDb(String as400_loc, String loc, String sourceDir) {

        boolean retVal = true;

        System.out.println ( " UploadDb invoked, as400_loc: " + as400_loc +
        		" loc: " + loc + " sourceDir: " + sourceDir);

        // Filter input string
        as400_loc = as400_loc.trim();

        // If string does not end with '/', add one
        if (as400_loc.charAt(as400_loc.length()-1) != '/') as400_loc += '/';

        String sourceFile = "pda.db";

        // PA Made me do this
        if (loc.length() == 1) loc = "0" + loc;

        String destinationDir =  as400_loc + "LOC" + loc + "/";
        String destinationFile = sourceFile;

        AS400FileAccess af = new AS400FileAccess();
        if (!af.uploadFile(sourceDir, sourceFile, destinationDir, destinationFile)) {
            retVal = false;
        }
        return retVal;
    }

    
    /*
     * Build the Sqlite database's item table.
     *
     * @param as400_lib - AS400 Data library
     * @parameter locCode - Store location code
     * @return boolean success/failure
     */
    public boolean buildItemTable (String as400_lib, String locCode) {

        Statement iStmt = null;
        PreparedStatement pStmt = null;

        ResultSet iResult = null;

        boolean success = false;


        System.out.println( "Building Item Table..." );

        try {

            pStmt = sqliteConn.prepareStatement(
                    "INSERT INTO item VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )" );
            // SKU, UPC, Alt Item, Item Desc, List Unit Of Measure, Dept Code, Mfg Item#
            // Vnd Item#, Min Quantity, Max Quantity, Net Item (Y/N), RISBCD (P or C)
            String sql =
                " SELECT RISKU#, RIUPC#, RIAITM, RIDESC, RILSTU, RIDEPT, RIMNF#, " +
                " RIVITM, RIMINQ, RIMAXQ, RINETI, RISBCD" +
                " FROM " + as400_lib + ".DRITEMFL " +
                " WHERE RILOC# = " + locCode + " AND RIRLC# = 0 AND RISKU# > 50000 " +
                " ORDER BY RISKU#";


            iStmt = as400Conn.createStatement();

            iResult = iStmt.executeQuery( sql );

            int count = 0;

            while ( iResult.next ( ) ) {

                int sku = iResult.getInt( "RISKU#" );
                long upc = iResult.getLong( "RIUPC#" );
                String alt = iResult.getString( "RIAITM" );
                String desc = iResult.getString( "RIDESC" );
                String uom = iResult.getString( "RILSTU" );
                String dept = iResult.getString( "RIDEPT" );
                String mnf = iResult.getString( "RIMNF#" );

                String vendorItem = iResult.getString( "RIVITM" );
                int minQ = iResult.getInt( "RIMINQ" );
                int maxQ = iResult.getInt( "RIMAXQ" );
                String netItem = iResult.getString( "RINETI" );
                String subItemCode = iResult.getString( "RISBCD" );

                pStmt.setInt( 1, sku );
                pStmt.setLong( 2, upc );
                pStmt.setString(3, alt.trim() );
                //pStmt.setString( 4, desc.replaceAll( "'", "''" ).trim() );
                pStmt.setString( 4, desc.trim() );
                pStmt.setString( 5, uom );
                pStmt.setString( 6, dept );
                pStmt.setString( 7, mnf.replaceAll( "'", "''" ).trim() );
                pStmt.setString( 8, ckDigitUPCE ( Integer.toString( sku ) ) );
                pStmt.setString( 9, vendorItem );
                pStmt.setInt( 10, minQ );
                pStmt.setInt( 11, maxQ );
                pStmt.setString( 12, netItem );
                pStmt.setString( 13, subItemCode );

                pStmt.addBatch();

                count++;

                if ( count % DB_COMMIT_COUNT == 0 ) {
                    pStmt.executeBatch();
                    sqliteConn.commit();
                    System.out.println ( DB_COMMIT_COUNT + " records added to item table! (" + count + ")" );
                }
            }

            if ( count % DB_COMMIT_COUNT != 0 ) {
                pStmt.executeBatch();
                sqliteConn.commit();
            }

            System.out.println ( count + " records added to item table!");

            pStmt.close();
            iResult.close();
            iStmt.close();

        }
        catch ( SQLException e ) {
            System.out.println ( e.getMessage() );
        }
        catch (Exception e ) {
            System.out.println ( e.getMessage() );
            e.printStackTrace();
        }
        finally {
            try {
                pStmt.close();
                iResult.close();
                iStmt.close();
            }
            catch (Exception e) {
                System.out.println("Close connection failed: " + e.getMessage());
            }
        }


        return ( success );

    }


    /*
     * Add items from central into Sqlite database's item table.
     *
     * @param as400_lib - AS400 Data library
     * @parameter locCode - Store location code
     * @return boolean success/failure
     * 
     * NOTE:: THE DIMS as400 lib is hard coded - no option for DLCQA lib
     */
    public boolean buildCentralItemTable (String as400_lib, String locCode) {

        Statement iStmt = null;
        ResultSet iResult = null;

        PreparedStatement pStmt = null;

        boolean success = false;

        System.out.println( "Building Central Item Table..." );

        try {

            pStmt = sqliteConn.prepareStatement(
                    "INSERT INTO item VALUES ( ?, ?, ?, ?, ?, ?, ?, ? )" );

            String sql =
                "SELECT ITITEM, ITDEPT, ITLSTU, ITDESC, ITMNF#, IQSKU#, RIUPC# " +
                "FROM DIMS.DIITEMFL LEFT OUTER JOIN " +
                as400_lib + ".DICXRFFL ON IQITEM = ITITEM LEFT OUTER JOIN " +
                as400_lib + ".DRITEMFL ON RISKU# = IQSKU# AND " +
                     "RILOC# = " + locCode + " AND RIRLC# = 0 " +
                "WHERE ITLOC# = 7 AND ITCMP# = '  ' " +
                "ORDER BY IQSKU#";


            iStmt = as400Conn.createStatement();

            iResult = iStmt.executeQuery( sql );

            int count = 0;

            while ( iResult.next ( ) ) {

                int sku = iResult.getInt( "IQSKU#" );
                long upc = iResult.getLong( "RIUPC#" );
                String alt = iResult.getString( "ITITEM" );
                String desc = iResult.getString( "ITDESC" );
                String uom = iResult.getString( "ITLSTU" );
                String dept = iResult.getString( "ITDEPT" );
                String mnf = iResult.getString( "ITMNF#" );


                pStmt.setInt( 1, sku );
                pStmt.setLong( 2, upc );
                pStmt.setString(3, alt.trim() );
                //pStmt.setString( 4, desc.replaceAll( "'", "''" ).trim() );
                pStmt.setString( 4, desc.trim() );
                pStmt.setString( 5, uom );
                pStmt.setString( 6, dept );
                pStmt.setString( 7, mnf.replaceAll( "'", "''" ).trim() );
                pStmt.setString( 8, ckDigitUPCE ( Integer.toString( sku ) ) );

                pStmt.addBatch();

                count++;

                if ( count % DB_COMMIT_COUNT == 0 ) {
                    pStmt.executeBatch();
                    sqliteConn.commit();
                    System.out.println (" records added to item table! (" + count + ")" );
                }
            }

            if ( count % DB_COMMIT_COUNT != 0 ) {
                pStmt.executeBatch();
                sqliteConn.commit();
            }
            System.out.println ( count + " records added to item table ");
        }
        catch ( SQLException e ) {
            System.out.println ( e.getMessage() );
        }
        catch (Exception e ) {
            System.out.println ( e.getMessage() );
            e.printStackTrace();
        }
        finally {
            try {
                pStmt.close();
                iResult.close();
                iStmt.close();
            }
            catch (Exception e) {
                System.out.println("Close connection failed: " + e.getMessage());
            }
        }

        return ( success );

    }


    /*
     * Build the Sqlite database's item_alias table.
     *
     * @param as400_lib - AS400 Data library
     * @return boolean success/failure
     */
    public boolean buildItemAliasTable (String as400_lib) {

        Statement iStmt = null;
        ResultSet iResult = null;

        PreparedStatement pStmt = null;

        boolean success = false;


        System.out.println( "Building Alias Table..." );

        try {

            pStmt = sqliteConn.prepareStatement(
                "INSERT INTO item_alias VALUES ( ?, ? )" );

            // SKU and alternate UPC
            String sql = "SELECT OSSKU#, OSAUPC FROM " + as400_lib + ".DOAUPCFL ORDER BY OSSKU#";

            iStmt = as400Conn.createStatement();

            iResult = iStmt.executeQuery( sql );

            int count = 0;

            while ( iResult.next ( ) ) {

                String sku = iResult.getString( "OSSKU#" );
                String upc = iResult.getString( "OSAUPC" );

                if ( upc.length() < 12 ) {
                    upc = "0" + upc;
                }

                pStmt.setString(1, sku.trim() );
                pStmt.setString(2, upc.trim() );

                pStmt.addBatch();
                count++;

                count++;

                if ( count % DB_COMMIT_COUNT == 0 ) {
                    pStmt.executeBatch();
                    sqliteConn.commit();
                    System.out.println ( DB_COMMIT_COUNT + " records added to item_alias table! (" + count + ")" );
                }
            }

            if ( count % DB_COMMIT_COUNT != 0 ) {
                pStmt.executeBatch();
                sqliteConn.commit();
            }
            System.out.println ( count + " records added to item_alias table ");
        }
        catch ( SQLException e ) {
            System.out.println ( e.getMessage() );
        }
        catch (Exception e ) {
            System.out.println ( e.getMessage() );
            e.printStackTrace();
        }
        finally {
            try {
                pStmt.close();
                iResult.close();
                iStmt.close();
            }
            catch (Exception e) {
                System.out.println("Close connection failed: " + e.getMessage());
            }
        }

        return ( success );

    }


    /*
     * Build the Sqlite database's item_recv table.
     *
     * @param as400_lib - AS400 Data library
     * @parameter locCode - Store location code
     * @return boolean success/failure
     */
    public boolean buildItemRecvTable (String as400_lib, String locCode ) {

        Statement iStmt = null;
        ResultSet iResult = null;

        PreparedStatement pStmt = null;

        boolean success = false;


        System.out.println( "Building item_recv Table..." );

        try {

            pStmt = sqliteConn.prepareStatement(
                "INSERT INTO item_recv VALUES ( ?, ? )" );

            // SKU and alternate UPC
            String sql = "SELECT OXSKU#, OXUPC# FROM " + as400_lib + ".DORCVIFL " +
                    " Where OXLOC#= " + locCode + " ORDER BY OXSKU#";

            iStmt = as400Conn.createStatement();

            iResult = iStmt.executeQuery( sql );

            int count = 0;

            while ( iResult.next ( ) ) {

                String sku = iResult.getString( "OXSKU#" );
                String upc = iResult.getString( "OXUPC#" );

                if ( upc.length() < 12 ) {
                    upc = "0" + upc;
                }

                pStmt.setString(1, sku.trim() );
                pStmt.setString(2, upc.trim() );

                pStmt.addBatch();
                count++;

                count++;

                if ( count % DB_COMMIT_COUNT == 0 ) {
                    pStmt.executeBatch();
                    sqliteConn.commit();
                    System.out.println ( DB_COMMIT_COUNT + " records added to item_recv table! (" + count + ")" );
                }
            }

            if ( count % DB_COMMIT_COUNT != 0 ) {
                pStmt.executeBatch();
                sqliteConn.commit();
            }
            System.out.println ( count + " records added to item_recv table ");
        }
        catch ( SQLException e ) {
            System.out.println ( e.getMessage() );
        }
        catch (Exception e ) {
            System.out.println ( e.getMessage() );
            e.printStackTrace();
        }
        finally {
            try {
                pStmt.close();
                iResult.close();
                iStmt.close();
            }
            catch (Exception e) {
                System.out.println("Close connection failed: " + e.getMessage());
            }
        }

        return ( success );

    }


    /*
     * Build the Sqlite database's cycle_count.
     *
     * Queries DRCYDLFL and pulls the department, sku for all records where the
     *     SDPROS fields is 'Y' and location code matches SDLOC#
     *     
     * @param as400_lib - AS400 Data library
     * @parameter locCode - Store location code
     * 
     * @return boolean success/failure
     */
    public boolean buildCycleCountTable (String as400_lib, String locCode ) {
    
        boolean retVal = false;
        Statement iStmt = null;
        ResultSet iResult = null;

        System.out.println("buildCycleCountTable invoked.  locCode: " + locCode);

        try {
        	
        	Statement sStmt = sqliteConn.createStatement();
        	
            String sql = "SELECT SDDEPT, SDSKU# FROM " + as400_lib + ".DRCYDLFL " +
                            " where SDLOC# = " + locCode;
            
            iStmt = as400Conn.createStatement();
            iResult = iStmt.executeQuery( sql );

            while ( iResult.next ( ) ) {

                Integer department = new Integer(iResult.getInt("SDDEPT"));
                Integer sku = new Integer(iResult.getInt("SDSKU#"));

                sql = "INSERT INTO cycle_count (department, sku) "
                        + "VALUES( '" + department + "', '" + sku + "')";

                sStmt.executeUpdate( sql );
                sqliteConn.commit();
                
            }
        } 
        catch (Exception e ) {
            System.out.println( e.getMessage() );
            e.printStackTrace();
        }

        return retVal;
    }


    /*
     * Modify the item table from DRRITMFL and remove sales/clearance
     * prices.
     *
     * @param location code - location to build item table
     * @param region - region code to build item table
     */
    public boolean buildItemLumberTable(String as400_lib,
            String locCode, String regCode) {

        boolean retVal = false;

        Statement iStmt = null;
        Statement jStmt = null;
        Statement sStmt = null;

        ResultSet iResult = null;
        ResultSet jResult = null;

        System.out.println( "Building Lumber Items ..." );

        try {

            // SKU, Alt Item, Dept Code, List Unit Of Measure
            String sql =
                " SELECT RRSKU#, RRAITM, RRDEPT, RRLSTU, RRSTTS " +
                " FROM " + as400_lib + ".DRRITMFL " +
                " WHERE RRLOC# = " + locCode + " AND RRSKU# > 50000 " +
                " ORDER BY RRSKU#";

            iStmt = as400Conn.createStatement();
            jStmt = as400Conn.createStatement();
            sStmt = sqliteConn.createStatement();

            iResult = iStmt.executeQuery( sql );

            int count = 0;

            // Update the item uom field to match the queried
            // Delete the item_price table for where the
            // DRITMFL.RRSKU#=item.sku_num and item.price_flag<>’R’
            // (No sales or clearance price for the lumber items)

            // Update item_price table where DRRITMFL.RRLSTU =DRUPRCFL.RULSTU
            // where the DRITMFL.RRSKU#=item.sku_num and item.price_flag=’R’

            while ( iResult.next ( ) ) {

                int sku = iResult.getInt( "RRSKU#" );
                String alt = iResult.getString( "RRAITM" );
                String dept = iResult.getString( "RRDEPT" );
                String uom = iResult.getString( "RRLSTU" );
                String status = iResult.getString( "RRSTTS" );

                count++;

                // Update the UOM field in item table
                sql = "update item set uom='" + uom + "' where sku_num=" + sku;
                int result = sStmt.executeUpdate(sql);
                sqliteConn.commit();

                // RURGPR
                sql = "SELECT RUPRC1 FROM " + as400_lib + ".DRUPRCFL " +
                         " WHERE RURGPR = " + regCode + " AND RUSKU#="+ sku +
                         " AND RUSTTS = ' ' AND RULSTU = '" + uom + "'";

                jResult = jStmt.executeQuery( sql );

                if ( jResult.next ( ) ) {

                    double price = jResult.getDouble( "RUPRC1" );

                    // Delete existing pricing for the SKU
                    sql = "DELETE FROM item_price where sku_num=" + sku;
                    result = sStmt.executeUpdate(sql);

                    if (status.equalsIgnoreCase("A")) {

                        System.out.println ( " Inserting " + sku +
                                " price: " + price);

                        sql = "insert into item_price (sku_num, price, " +
                              "price_flag) " +
                              "values(" + sku + "," + price + ",'R')";
                        result = sStmt.executeUpdate(sql);
                    }
                    else {
                        System.out.println ( " Not inserting " + sku +
                                " price: " + price);
                    }
                    sqliteConn.commit();
                }
            }

            System.out.println ( count + " Building Lumber Items modified!");

        }
        catch ( SQLException e ) {
            System.out.println ( e.getMessage() );
        }
        catch (Exception e ) {
            System.out.println ( e.getMessage() );
            e.printStackTrace();
        }
		finally	{
			try	{
                if (iStmt!= null) iStmt.close();
                if (jStmt!= null) jStmt.close();
                if (iResult!= null) iResult.close();
                if (jResult!= null) jResult.close();
                if (sStmt!= null) sStmt.close();
			}
			catch (Exception e) {
                System.out.println("Close connection failed: " + e.getMessage());
			}
		}

        return retVal;
    }

    
    /*
     * Delete contents of the Sqlite database's item, item_alias, item_price,
     * item_recv tables.  This method will remove ALL records from the database.
     *
     * @return boolean success/failure
     */
    public boolean cleanTables ( ) {


        Statement sStmt = null;

        boolean success = false;

        try {

            sStmt = sqliteConn.createStatement();

            sStmt.execute( "DELETE FROM item" );
            sStmt.execute( "DELETE FROM cycle_count" );
            sStmt.execute( "DELETE FROM item_alias" );
            sStmt.execute( "DELETE FROM item_price" );
            sStmt.execute( "DELETE FROM item_recv" );
            sStmt.execute( "DELETE FROM pda_info" );
            sStmt.execute( "DELETE FROM location" );
            sStmt.execute( "DELETE FROM region" );
            sStmt.execute("DELETE FROM inventory");
            sqliteConn.commit();
        }
        catch ( SQLException e ) {
            System.out.println ( e.getMessage() );
        }
        catch (Exception e ) {
            System.out.println ( e.getMessage() );
            e.printStackTrace();
        }
        finally {
            try {
                sStmt.close();
            }
            catch (Exception e) {
                System.out.println("Close connection failed: " + e.getMessage());
            }
        }

        return ( success );

    }


    /*
     * Update the pda_info table with database update date and location.
     *
     * @param locId - Store location
     * @param RegId - Store Region 
     * @return boolean success/failure
     */
    public boolean updatePdaInfo(String locId, String regId) {


        boolean success = false;


        SimpleDateFormat sdf = new SimpleDateFormat ( "MM/dd/yyyy hh:mm a" );

        updateInfo("ITEM_BUILD_DATE", sdf.format( new Date ( ) ));
        updateInfo("PDA_LOCATION", locId);
        updateInfo("PDA_REGION", regId);

        return ( success );

    }

    
    /*
     * Update the pda_info table with key and value
     *
     * @param - pda_info table key
     * @param - pda_info table value
     * 
     * @return boolean success/failure
     */
    public boolean updateInfo(String key, String value) {


        Statement sStmt = null;

        boolean success = false;

        try {

            sStmt = sqliteConn.createStatement();

            sStmt.execute("insert into pda_info (key, value) values ('" + key + "','" + value + "')");

            sqliteConn.commit();

        }
        catch ( SQLException e ) {
            System.out.println ( e.getMessage() );
        }
        catch (Exception e ) {
            System.out.println ( e.getMessage() );
            e.printStackTrace();
        }
        finally {
            try {
                sStmt.close();
            }
            catch (Exception e) {
                System.out.println("Close connection failed: " + e.getMessage());
            }
        }

        return ( success );

    }

    
    /*
     * Get the check digit
     *
     * @param sku - sku to add check digit
     */
    public static String ckDigitUPCE( String sku ) {

        // this array is used in the calculation
        final int[] skuControl = { 1, 2, 1, 2, 1, 2 };

        //char[] skuDigit;  // holds each digit of the passed in sku
        int[] skuNumber = {0,0,0,0,0,0};  // hold the integer value of each digit
        int[] skuResult = {0,0,0,0,0,0};  // holds result of control * digit

        // break the string into an array
        char[] skuDigit = sku.toCharArray( );
        //convert the ascii digits into integers


        if ( sku.length() < 6 )
            return ( "0" );


        for ( int i = 0; i < 6; i++ ) {
            skuNumber[i] = Character.getNumericValue( skuDigit[i] );

            // multiple each digit by the control array
            skuResult[i] = skuNumber[i] * skuControl[i];
        }

        int result = 0;

        // check the results array for any digits > 9
        // if found break them apart and add the ones to the tens
        for ( int i = 0; i < 6; i++ ) {
            // check for digit > 9
            if ( ( skuResult[i] / 10 ) == 0 ) {
                result = result + skuResult[i];
            }

            // digit was 10 or greater so we need to add 1 + 0
            else {
                int tens = skuResult[i] / 10;
                int ones = skuResult[i] % 10;
                result = result + tens + ones;
            }
        }

        // subtract result from next highest value of ten
        int difference = result % 10;
        result = 10 - difference;

        // 10 - 0 fix
        if ( result == 10 )
            result = 0;

        // convert result into a string and return
        Integer ckDigit = new Integer( result );

        return ( ckDigit.toString( ) );

    }

    /*
     * Test method
     */
    public static void main(String [ ] args) {

        // (new UpdatePdaDbService()).UpdatePdaDb("QDLS/MC9190T", "jdbc:sqlite:C:\\pda.db", "//");
        (new UpdatePdaDbService()).UpdatePdaDb("QDLS/MC9190T", "jdbc:sqlite:C:\\pda.db", "C://",
        		"DLCQA");

    }


    /*
     * Get the check digit for input sku
     *
     * @param as400_lib - AS400 Data library
     * @return map containing the region. location map
     */
    public Map<String,String> regLocMap(String as400_lib) {

        Statement iStmt = null;
        ResultSet iResult = null;
        Map<String,String> locRegMap = new HashMap<String, String>();

        try {
        	
            String sql = "SELECT WRLOC#, WRRGPR FROM " + as400_lib +".DRWBLCFL";
            iStmt = as400Conn.createStatement();
            iResult = iStmt.executeQuery( sql );
            while ( iResult.next ( ) ) {
                Integer locationId = iResult.getInt("WRLOC#");
                Integer regionId = iResult.getInt("WRRGPR");
                locRegMap.put(locationId.toString(),regionId.toString());

            }

         } catch(Exception e) {
            e.printStackTrace();
        }
        
        return locRegMap;
    }

    
    /*
     * Get the check digit for input sku
     *
     * @param as400_lib - AS400 Data library
     * @return map containing the region. location map
     */
    public void buildLocationTable(String as400_lib) {

        Statement iStmt = null;
        ResultSet iResult = null;
        PreparedStatement pStmt = null;


        System.out.println( "Building Location Table..." );

        try {

            pStmt = sqliteConn.prepareStatement(
                "INSERT INTO location VALUES ( ?, ?, ? )" );

            // SKU and alternate UPC
            String sql = "SELECT WRLOC#, WRLNME, WRRGPR FROM " + 
            			as400_lib + ".DRWBLCFL ORDER BY WRLOC#";

            iStmt = as400Conn.createStatement();

            iResult = iStmt.executeQuery( sql );

            int count = 0;

            while ( iResult.next ( ) ) {

                String locNum = iResult.getString( "WRLOC#" );
                String locName = iResult.getString( "WRLNME" );
                String regCode = iResult.getString("WRRGPR");

                pStmt.setString(1, locNum.trim() );
                pStmt.setString(2, locName.trim() );
                pStmt.setString(3, regCode.trim() );

                pStmt.addBatch();
                //count++;

                count++;

                if ( count % DB_COMMIT_COUNT == 0 ) {
                    pStmt.executeBatch();
                    sqliteConn.commit();
                    System.out.println ( DB_COMMIT_COUNT + " records added to location table! (" + count + ")" );
                }
            }

            if ( count % DB_COMMIT_COUNT != 0 ) {
                pStmt.executeBatch();
                sqliteConn.commit();
            }
            System.out.println ( count + " records added to location table ");
        }
        catch ( SQLException e ) {
            System.out.println ( e.getMessage() );
        }
        catch (Exception e ) {
            System.out.println ( e.getMessage() );
            e.printStackTrace();
        }
        finally {
            try {
                pStmt.close();
                iResult.close();
                iStmt.close();
            }
            catch (Exception e) {
                System.out.println("Close connection failed: " + e.getMessage());
            }
        }
    }


    /*
     * Populate the region code table
     *
     * @param as400_lib - AS400 Data library
     */
    public void buildRegionTable(String as400_lib) {
    	
        Statement iStmt = null;
        ResultSet iResult = null;
        PreparedStatement pStmt = null;


        System.out.println( "Building Region Table..." );

        try {

            pStmt = sqliteConn.prepareStatement(
                "INSERT INTO region VALUES ( ?, ? )" );


            String sql = "SELECT WBRGPR, WBRNME FROM " +
            		as400_lib + ".DRWBRGFL ORDER BY WBRGPR";

            iStmt = as400Conn.createStatement();

            iResult = iStmt.executeQuery( sql );

            int count = 0;

            while ( iResult.next ( ) ) {

                String regNum = iResult.getString( "WBRGPR" );
                String regName = iResult.getString( "WBRNME" );


                System.out.println("WBRGPR == "+regNum+"  WBRNME=== "+ regName);


                pStmt.setString(1, regNum.trim() );
                pStmt.setString(2, regName.trim() );


                pStmt.addBatch();
                count++;

                if ( count % DB_COMMIT_COUNT == 0 ) {
                    pStmt.executeBatch();
                    sqliteConn.commit();
                    System.out.println ( DB_COMMIT_COUNT + " records added to region table! (" + count + ")" );
                }
            }

            if ( count % DB_COMMIT_COUNT != 0 ) {
                pStmt.executeBatch();
                sqliteConn.commit();
            }
            System.out.println ( count + " records added to region table ");
        }
        catch ( SQLException e ) {
            System.out.println ( e.getMessage() );
        }
        catch (Exception e ) {
            System.out.println ( e.getMessage() );
            e.printStackTrace();
        }
        finally {
            try {
                pStmt.close();
                iResult.close();
                iStmt.close();
            }
            catch (Exception e) {
                System.out.println("Close connection failed: " + e.getMessage());
            }
        }
    }

    /*
     * Populate the price table
     *
     * @param as400_lib - AS400 Data library
     * @return boolean indicating a successful table build
     */
    public boolean buildItemPriceTable (String as400_lib, String regionCode ) {

        Statement iStmt = null;
        ResultSet iResult = null;

        PreparedStatement pStmt = null;

        int count = 0;
        boolean retVal  = false;

        System.out.println( "Building Price Table..." );

        /*
         * Update items with price
         */
        try {

            pStmt = sqliteConn.prepareStatement( "INSERT INTO item_price VALUES (?,?,?)" );

            //RURGPR
            String sql = "SELECT RUSKU#, RUPRC1 " +
                         " FROM " + as400_lib + ".DRUPRCFL " +
                         " WHERE RURGPR = "+ regionCode + " AND RUSKU# > 50000 AND " +
                         " RUSTTS = ' ' AND RUTYPE = 'P' " +
                         " ORDER BY RUSKU#";

            iStmt = as400Conn.createStatement();

            iResult = iStmt.executeQuery( sql );

            while ( iResult.next ( ) )
            {
                int sku = iResult.getInt( "RUSKU#" );
                double price = iResult.getDouble( "RUPRC1" );

                pStmt.setInt(1, sku );
                pStmt.setDouble( 2, price );
                pStmt.setString(3, "R" );

                pStmt.addBatch();

                count++;

                if ( count % DB_COMMIT_COUNT == 0 ) {
                    pStmt.executeBatch();
                    sqliteConn.commit();
                    System.out.println ( DB_COMMIT_COUNT + " records added to price table! (" + count + ")" );
                }
            }


            if ( count % DB_COMMIT_COUNT != 0 ) {
                pStmt.executeBatch();
                sqliteConn.commit();
            }
            System.out.println ( count + " records added to price table ");
        }
        catch ( SQLException e ) {
            System.out.println ( e.getMessage() );
        }
        catch (Exception e ) {
            System.out.println ( e.getMessage() );
            e.printStackTrace();
        }
        finally {
            try {
                pStmt.close();
                iResult.close();
                iStmt.close();
            }
            catch (Exception e) {
                System.out.println("Close connection failed: " + e.getMessage());
            }
        }


        /*
         * Update items with sales price
         */
        try {

            pStmt = sqliteConn.prepareStatement( "INSERT INTO item_price VALUES (?,?,?)" );

            String sql = "SELECT RSSKU#, RSPRCE " +
                  " FROM " + as400_lib + ".DRSALEFL " +
                  " WHERE RSRGPR = "+regionCode+" AND RSSTTS = 'A' " +
                  " ORDER BY RSSKU#";

            iStmt = as400Conn.createStatement();

            iResult = iStmt.executeQuery( sql );

            count = 0;

            while ( iResult.next ( ) ) {

                int sku = iResult.getInt( "RSSKU#" );
                double price = iResult.getDouble( "RSPRCE" );

                pStmt.setInt(1, sku );
                pStmt.setDouble( 2, price );
                pStmt.setString(3, "S" );

                pStmt.addBatch();

                count++;

                if ( count % DB_COMMIT_COUNT == 0 ) {
                    pStmt.executeBatch();
                    sqliteConn.commit();
                    System.out.println ( DB_COMMIT_COUNT + " sale records added to table! (" + count + ")" );
                }
            }

            if ( count % DB_COMMIT_COUNT != 0 ) {
                pStmt.executeBatch();
                sqliteConn.commit();
            }
            System.out.println ( count + " sale records added to price table ");
        }
        catch ( SQLException e ) {
            System.out.println ( e.getMessage() );
        }
        catch (Exception e ) {
            System.out.println ( e.getMessage() );
            e.printStackTrace();
        }
        finally {
            try {
                pStmt.close();
                iResult.close();
                iStmt.close();
            }
            catch (Exception e) {
                System.out.println("Close connection failed: " + e.getMessage());
            }
        }


        /*
         * Update items with clearance price
         */
        try {

            pStmt = sqliteConn.prepareStatement( "INSERT INTO item_price VALUES (?,?,?)" );

            String sql = "SELECT OJSKU#, OJCLRP " +
                  " FROM " + as400_lib + ".DODCITFL " +
                  " WHERE OJRGPR = "+regionCode+" AND OJSTTS = 'A' " +
                  " ORDER BY OJSKU#";

            iStmt = as400Conn.createStatement();

            iResult = iStmt.executeQuery( sql );

            count = 0;

            while ( iResult.next ( ) ) {

                int sku = iResult.getInt( "OJSKU#" );
                double price = iResult.getDouble( "OJCLRP" );

                pStmt.setInt(1, sku );
                pStmt.setDouble( 2, price );
                pStmt.setString(3, "C" );

                pStmt.addBatch();

                count++;

                if ( count % DB_COMMIT_COUNT == 0 ) {
                    pStmt.executeBatch();
                    sqliteConn.commit();
                    System.out.println ( DB_COMMIT_COUNT + " clearance records added to table! (" + count + ")" );
                }
            }

            if ( count % DB_COMMIT_COUNT != 0 ) {
                pStmt.executeBatch();
                sqliteConn.commit();
            }
            System.out.println ( count + " clearance records added to price table ");
        }
        catch ( SQLException e ) {
            System.out.println ( e.getMessage() );
        }
        catch (Exception e ) {
            System.out.println ( e.getMessage() );
            e.printStackTrace();
        }
        finally {
            try {
                pStmt.close();
                iResult.close();
                iStmt.close();
            }
            catch (Exception e) {
                System.out.println("Close connection failed: " + e.getMessage());
            }
        }

        return ( retVal );

    }
}
