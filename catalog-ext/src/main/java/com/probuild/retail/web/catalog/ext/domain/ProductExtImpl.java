package com.probuild.retail.web.catalog.ext.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.catalog.domain.Product;
import org.broadleafcommerce.inventory.domain.SkuAvailability;
import org.broadleafcommerce.store.domain.Store;

@Entity
@Table(name = "BLC_PRODUCT_EXT")
public class ProductExtImpl extends ProductPbImpl implements Product {

    @SuppressWarnings("unused")
    private static final Log LOG = LogFactory.getLog(ProductExtImpl.class);
    
    private static final long serialVersionUID = 1L;

    @Column(name="department")
    protected Integer department;
    @Column(name="vendor")
    protected String vendorName;
    @Column(name="vendor_key")
    protected Long vendorKey;
    @Column(name="manufacturer_id")
    protected Long manufacturerKey;
    @Column(name="uom")
    protected String uom;
    
    @Transient
    protected SkuAvailability availability;
    @Transient
    protected boolean priceInventoryHidden;
    @Transient
    protected boolean stockingLocationPresent;
    @Transient
    protected String stockingLocationName;
    @Transient
    protected Store store;
    
    public ProductExtImpl ( ) { 
        priceInventoryHidden = true;
        stockingLocationPresent = false;
    }
    
    
    public Integer getDepartment() {
        return department;
    }
    public void setDepartment(Integer department) {
        this.department = department;
    }
    
    
    public String getVendorName() {
        return vendorName;
    }
    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }
    
    
    public Long getVendorKey() {
        return vendorKey;
    }
    public void setVendorKey(Long vendorKey) {
        this.vendorKey = vendorKey;
    }
    
    
    public Long getManufacturerKey() {
        return manufacturerKey;
    }
    public void setManufacturerKey(Long manufacturerKey) {
        this.manufacturerKey = manufacturerKey;
    }
    
    
    public String getUom() {
        return uom;
    }
    public void setUom(String uom) {
        this.uom = uom;
    }

    
    public SkuAvailability getAvailability() {
        return availability;
    }
    public void setAvailability(SkuAvailability availability) {
        this.availability = availability;
    }


    public boolean isPriceInventoryHidden() {
        return priceInventoryHidden;
    }
    public void setPriceInventoryHidden(boolean priceInventoryHidden) {
        this.priceInventoryHidden = priceInventoryHidden;
    }


    public boolean isStockingLocationPresent() {
        return stockingLocationPresent;
    }
    public void setStockingLocationPresent(boolean stockingLocationPresent) {
        this.stockingLocationPresent = stockingLocationPresent;
    }


    public String getStockingLocationName() {
        return stockingLocationName;
    }
    public void setStockingLocationName(String stockingLocationName) {
        this.stockingLocationName = stockingLocationName;
    }


    public Store getStore() {
        return store;
    }
    public void setStore(Store store) {
        this.store = store;
    }

    
}
