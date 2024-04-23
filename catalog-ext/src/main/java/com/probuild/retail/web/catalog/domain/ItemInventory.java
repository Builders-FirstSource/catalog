package com.probuild.retail.web.catalog.domain;

import java.io.Serializable;
import java.math.BigDecimal;

public class ItemInventory implements Serializable {

    private static final long serialVersionUID = 1L;

    protected Long skuId;
    protected String skuNumber;
    protected String uom;
    protected Integer locationId; // store number
    protected BigDecimal qtyTotal;
    protected BigDecimal ftgTotal;
    protected BigDecimal qtyAvailable;
    protected BigDecimal ftgAvailable;
    protected BigDecimal qtyPreSold;
    protected BigDecimal ftgPreSold;
    protected BigDecimal qtyOnOrder;
    protected BigDecimal ftgOnOrder;
    protected BigDecimal qtyBackOrdered;
    protected BigDecimal ftgBackOrdered;

    protected BigDecimal reorderQty;
    protected BigDecimal maxOnhandQty;
    protected BigDecimal reorderFtg;
    protected BigDecimal maxOnhandFtg;

    protected String status;


    public ItemInventory() {
        super();
    }


    public Long getSkuId() {
        return skuId;
    }
    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }

    public String getSkuNumber() {
        return skuNumber;
    }
    public void setSkuNumber(String skuNumber) {
        this.skuNumber = skuNumber;
    }

    public String getUom() {
        return uom;
    }
    public void setUom(String uom) {
        this.uom = uom;
    }

    public Integer getLocationId() {
        return locationId;
    }
    public void setLocationId(Integer locationId) {
        this.locationId = locationId;
    }

    public BigDecimal getQtyAvailable() {
        return qtyAvailable;
    }
    public void setQtyAvailable(BigDecimal qtyAvailable) {
        this.qtyAvailable = qtyAvailable;
    }

    public BigDecimal getFtgAvailable() {
        return ftgAvailable;
    }
    public void setFtgAvailable(BigDecimal ftgAvailable) {
        this.ftgAvailable = ftgAvailable;
    }

    public BigDecimal getQtyPreSold() {
        return qtyPreSold;
    }
    public void setQtyPreSold(BigDecimal qtyPreSold) {
        this.qtyPreSold = qtyPreSold;
    }

    public BigDecimal getFtgPreSold() {
        return ftgPreSold;
    }
    public void setFtgPreSold(BigDecimal ftgPreSold) {
        this.ftgPreSold = ftgPreSold;
    }

    public BigDecimal getQtyOnOrder() {
        return qtyOnOrder;
    }
    public void setQtyOnOrder(BigDecimal qtyOnOrder) {
        this.qtyOnOrder = qtyOnOrder;
    }

    public BigDecimal getFtgOnOrder() {
        return ftgOnOrder;
    }
    public void setFtgOnOrder(BigDecimal ftgOnOrder) {
        this.ftgOnOrder = ftgOnOrder;
    }

    public BigDecimal getReorderQty() {
        return reorderQty;
    }
    public void setReorderQty(BigDecimal reorderQty) {
        this.reorderQty = reorderQty;
    }

    public BigDecimal getMaxOnhandQty() {
        return maxOnhandQty;
    }
    public void setMaxOnhandQty(BigDecimal maxOnhandQty) {
        this.maxOnhandQty = maxOnhandQty;
    }

    public BigDecimal getQtyBackOrdered() {
        return qtyBackOrdered;
    }
    public void setQtyBackOrdered(BigDecimal qtyBackOrdered) {
        this.qtyBackOrdered = qtyBackOrdered;
    }

    public BigDecimal getFtgBackOrdered() {
        return ftgBackOrdered;
    }
    public void setFtgBackOrdered(BigDecimal ftgBackOrdered) {
        this.ftgBackOrdered = ftgBackOrdered;
    }

    public BigDecimal getReorderFtg() {
        return reorderFtg;
    }
    public void setReorderFtg(BigDecimal reorderFtg) {
        this.reorderFtg = reorderFtg;
    }

    public BigDecimal getMaxOnhandFtg() {
        return maxOnhandFtg;
    }
    public void setMaxOnhandFtg(BigDecimal maxOnhandFtg) {
        this.maxOnhandFtg = maxOnhandFtg;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getQtyTotal() {
        return qtyTotal;
    }
    public void setQtyTotal(BigDecimal qtyTotal) {
        this.qtyTotal = qtyTotal;
    }

    public BigDecimal getFtgTotal() {
        return ftgTotal;
    }
    public void setFtgTotal(BigDecimal ftgTotal) {
        this.ftgTotal = ftgTotal;
    }


    public String calculateStatus ( ) {

        /*
         * @Comment: Status returns "Call" when totalQty < 2 and
         *           "Available" if >=2
         */

        if ( qtyTotal.doubleValue() < 2 ) {
            status = "Call";
        } else {
            status = "Available";
        }
        return status;

        /*// if no min/max
        if ( reorderQty.doubleValue() == 0 ) {
            if ( qtyTotal.doubleValue() > 0 && qtyTotal.doubleValue() < 5 ) {
                status = "Low Stock";
            } else if ( qtyTotal.doubleValue() >= 5 ) {
                status =  "Available";
            } else {
                status = "Call";
            }

            return status;
        }

        // handle case where min/max set to not stocked ( 9999 )
        if ( qtyTotal.doubleValue() == 0 && reorderQty.doubleValue() >= 9999 ) {
            status = "CALL";
            return status;
        }

        if ( qtyTotal.doubleValue() >= reorderQty.doubleValue() )
            status = "Available";
        else if ( qtyTotal.doubleValue() > 0 &&
                        qtyTotal.doubleValue() < reorderQty.doubleValue() )
            status = "Low Stock";
        else
            status = "Call";
         */


    }




    public static class Builder {
        private Long skuId;
        private String skuNumber;
        private String uom;
        private Integer locationId;
        private BigDecimal qtyAvailable;
        private BigDecimal ftgAvailable;
        private BigDecimal qtyPreSold;
        private BigDecimal ftgPreSold;
        private BigDecimal qtyOnOrder;
        private BigDecimal ftgOnOrder;
        private BigDecimal qtyBackordered;
        private BigDecimal ftgBackordered;
        private BigDecimal reorderQty;
        private BigDecimal maxOnhandQty;
        private String status;

        public Builder skuId(Long skuId) {
            this.skuId = skuId;
            return this;
        }

        public Builder skuNumber(String skuNumber) {
            this.skuNumber = skuNumber;
            return this;
        }

        public Builder uom(String uom) {
            this.uom = uom;
            return this;
        }

        public Builder locationId(Integer locationId) {
            this.locationId = locationId;
            return this;
        }

        public Builder qtyAvailable(BigDecimal qtyAvailable) {
            this.qtyAvailable = qtyAvailable;
            return this;
        }

        public Builder ftgAvailable(BigDecimal ftgAvailable) {
            this.ftgAvailable = ftgAvailable;
            return this;
        }

        public Builder qtyPreSold(BigDecimal qtyPreSold) {
            this.qtyPreSold = qtyPreSold;
            return this;
        }

        public Builder ftgPreSold(BigDecimal ftgPreSold) {
            this.ftgPreSold = ftgPreSold;
            return this;
        }

        public Builder qtyOnOrder(BigDecimal qtyOnOrder) {
            this.qtyOnOrder = qtyOnOrder;
            return this;
        }

        public Builder ftgOnOrder(BigDecimal ftgOnOrder) {
            this.ftgOnOrder = ftgOnOrder;
            return this;
        }

        public Builder qtyBackOrdered(BigDecimal qtyBackordered) {
            this.qtyBackordered = qtyBackordered;
            return this;
        }

        public Builder ftgBackOrdered(BigDecimal ftgBackordered) {
            this.ftgBackordered = ftgBackordered;
            return this;
        }

        public Builder reorderQty(BigDecimal reorderQty) {
            this.reorderQty = reorderQty;
            return this;
        }

        public Builder maxOnhandQty(BigDecimal maxOnhandQty) {
            this.maxOnhandQty = maxOnhandQty;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public ItemInventory build() {
            return new ItemInventory(this);
        }
    }

    private ItemInventory(Builder builder) {
        this.skuId = builder.skuId;
        this.skuNumber = builder.skuNumber;
        this.uom = builder.uom;
        this.locationId = builder.locationId;
        this.qtyAvailable = builder.qtyAvailable;
        this.ftgAvailable = builder.ftgAvailable;
        this.qtyPreSold = builder.qtyPreSold;
        this.ftgPreSold = builder.ftgPreSold;
        this.qtyOnOrder = builder.qtyOnOrder;
        this.ftgOnOrder = builder.ftgOnOrder;
        this.qtyBackOrdered = builder.qtyBackordered;
        this.ftgBackOrdered = builder.ftgBackordered;
        this.reorderQty = builder.reorderQty;
        this.maxOnhandQty = builder.maxOnhandQty;
        this.status = builder.status;
    }
}
