package com.probuild.retail.web.catalog.repository;

import java.util.List;

import com.probuild.retail.web.catalog.domain.Item;

public interface ItemRepository {

    /**
     * Set the ERP [JDBC] connection information. The ERP is the item
     * repository.
     * 
     * @param url JDBC connection string usually
     * @param user ERP user
     * @param password ERP user password
     */
    public void setConnection ( String url, String user, String password );
    
    /**
     * Called to establish a connection to item repository.
     * 
     * @return true if connected ok
     * @throws Exception 
     */
    public boolean connect ( );
    
    /**
     * Close the connection established with the previous call to connect
     * 
     * @return true if closed ok
     */
    public boolean disconnect ( );
    
    /**
     * Find the item in the repository that has item code given
     * 
     * @param itemCode usually the SKU number
     * @return Item if found, null otherwise
     */
    public Item findItem ( String itemCode );
    
    /**
     * Find items in repository that match the item codes given.
     * Item codes that do not match an item in repository will be
     * excluded in returned list.
     * 
     * @param itemCodes usually a list of SKU numbers
     * @return Items found, or empty list
     */
    public List<Item> findItems ( List<String> itemCodes );
    
    /**
     * Find items in repository that match the item range given.
     * @param itemCodeBegin start sku
     * @param itemCodeEnd end sku
     * @return list of items in range
     */
    public List<Item> findItems(String itemCodeBegin, String itemCodeEnd);
    
    /**
     * Find items in repository that match the dept id given and
     * the item code range.
     * 
     * @param deptId usually the department number
     * @param itemCodeBegin lower bound search
     * @param itemCodeEnd upper bound of search
     * @return Items found, or empty list
     */
    public List<Item> findItemsByDept ( Long deptId, String itemCodeBegin, String itemCodeEnd );
    
    /**
     * Find items in repository that match the manufacturer id given 
     * and the item code range.
     * 
     * @param manufId usually the manufacturer ID
     * @param itemCodeBegin lower bound search
     * @param itemCodeEnd upper bound of search
     * @return Items found, or empty list
     */
    public List<Item> findItemsByManuf ( Long manufId, String itemCodeBegin, String itemCodeEnd );
    
    /**
     * Find items in repository that match the vendor id given 
     * and the item code range.
     * 
     * @param vendId usually the vendor ID
     * @param itemCodeBegin lower bound search
     * @param itemCodeEnd upper bound of search
     * @return Items found, or empty list
     */    
    public List<Item> findItemsByVendor(Long vendId, String itemCodeBegin, String itemCodeEnd);
}
