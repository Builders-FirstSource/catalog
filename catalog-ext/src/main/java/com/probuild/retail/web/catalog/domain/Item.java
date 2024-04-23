package com.probuild.retail.web.catalog.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;
import org.hibernate.validator.Range;

public class Item implements Serializable {

    private static final long serialVersionUID = 1L;

    protected Long id;
    protected Long skuId;
    protected Long relatedId; // related Item key if nested
    protected Integer sku;
    protected String upc;
    protected String alt;
    protected String name;
    protected String descr;
    protected String uom;
    protected Integer dept;

    protected String modelNum;
    protected String manufacturer;

    protected BigDecimal regularPrice;
    protected BigDecimal salePrice;
    protected String saleStartYYYYMMDD;
    protected String saleEndYYYYMMDD;

    protected BigDecimal weight;
    protected String weightUnits;

    protected BigDecimal width;
    protected BigDecimal height;
    protected BigDecimal depth;

    protected ItemGroup group;

    protected List<ItemImage> images;

    protected List<Item> relatedItems;

    protected Date activeStartDate;
    protected Date activeEndDate;

    /**
     *	Default constructor
     */
    public Item() {
        super();
        id = new Long(0);

        activeEndDate = new Date();
        activeStartDate = new Date();
        
        regularPrice = new BigDecimal ( 0 );
        salePrice = new BigDecimal ( 0 );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRelatedId() {
        return relatedId;
    }

    public void setRelatedId(Long relatedId) {
        this.relatedId = relatedId;
    }

    @Range(min=100000, max=999999)
    public Integer getSku() {
        return sku;
    }

    public void setSku(Integer sku) {
        this.sku = sku;
    }

    public String getUpc() {
        return upc;
    }

    public void setUpc(String upc) {
        this.upc = upc;
    }

    public String getAlt() {
        return alt;
    }

    public void setAlt(String alt) {
        this.alt = alt;
    }

    @NotNull @Length(max=100)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescr() {
        return descr;
    }

    public void setDescr(String descr) {
        this.descr = descr;
    }

    public String getModelNum() {
        return modelNum;
    }

    public void setModelNum(String modelNum) {
        this.modelNum = modelNum;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public ItemGroup getGroup() {
        return group;
    }

    public void setGroup(ItemGroup group) {
        this.group = group;
    }

    public Date getActiveStartDate() {
        return activeStartDate;
    }

    public void setActiveStartDate(Date activeStartDate) {
        this.activeStartDate = activeStartDate;
    }

    public Date getActiveEndDate() {
        return activeEndDate;
    }

    public void setActiveEndDate(Date activeEndDate) {
        this.activeEndDate = activeEndDate;
    }

    public List<ItemImage> getImages() {
        return images;
    }

    public void setImages(List<ItemImage> images) {
        this.images = images;
    }

    public List<Item> getRelatedItems() {
        return relatedItems;
    }

    public void setRelatedItems(List<Item> relatedItems) {
        this.relatedItems = relatedItems;
    }

    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }

    public BigDecimal getRegularPrice() {
        return regularPrice;
    }

    public void setRegularPrice(BigDecimal regularPrice) {
        this.regularPrice = regularPrice;
    }

    public BigDecimal getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(BigDecimal salePrice) {
        this.salePrice = salePrice;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    public String getWeightUnits() {
        return weightUnits;
    }

    public void setWeightUnits(String weightUnits) {
        this.weightUnits = weightUnits;
    }

    public BigDecimal getWidth() {
        return width;
    }

    public void setWidth(BigDecimal width) {
        this.width = width;
    }

    public BigDecimal getHeight() {
        return height;
    }

    public void setHeight(BigDecimal height) {
        this.height = height;
    }

    public BigDecimal getDepth() {
        return depth;
    }

    public void setDepth(BigDecimal depth) {
        this.depth = depth;
    }

    public String getSaleStartYYYYMMDD() {
        return saleStartYYYYMMDD;
    }

    public void setSaleStartYYYYMMDD(String saleStartYYYYMMDD) {
        this.saleStartYYYYMMDD = saleStartYYYYMMDD;
    }

    public String getSaleEndYYYYMMDD() {
        return saleEndYYYYMMDD;
    }

    public void setSaleEndYYYYMMDD(String saleEndYYYYMMDD) {
        this.saleEndYYYYMMDD = saleEndYYYYMMDD;
    }

    public String getUom() {
        return uom;
    }

    public void setUom(String uom) {
        this.uom = uom;
    }

    public Integer getDept() {
        return dept;
    }

    public void setDept(Integer dept) {
        this.dept = dept;
    }

    public String toString() {
        return this.getName() + ", " + this.getSku();
    }

    public void defaultDates() {

        long currentTimeMillis = System.currentTimeMillis();
        long futureTimeMillis = currentTimeMillis + (3600000l * 24l * 800l);
        setActiveStartDate(roundOffDate(new Date(currentTimeMillis)));
        setActiveEndDate(roundOffDate(new Date(futureTimeMillis)));

    }

    public void postDate() {

        long currentTimeMillis = System.currentTimeMillis();
        long postTimeMillis = currentTimeMillis - (3600000l * 24l);
        setActiveStartDate(roundOffDate(new Date(postTimeMillis)));
        setActiveEndDate(roundOffDate(new Date(postTimeMillis)));

    }

    private Date roundOffDate(Date now) {

        Calendar cal = GregorianCalendar.getInstance();
        cal.setTime(now);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);

        return cal.getTime();
    }

    public static class Builder {
        private Long id;
        private Long skuId;
        private Long relatedId;
        private Integer sku;
        private String upc;
        private String alt;
        private String name;
        private String descr;
        private String uom;
        private Integer dept;
        private String modelNum;
        private String manufacturer;
        private BigDecimal regularPrice;
        private BigDecimal salePrice;
        private String saleStartYYYYMMDD;
        private String saleEndYYYYMMDD;
        private BigDecimal weight;
        private String weightUnits;
        private BigDecimal width;
        private BigDecimal height;
        private BigDecimal depth;
        private ItemGroup group;
        private List<ItemImage> images;
        private List<Item> relatedItems;
        private Date activeStartDate;
        private Date activeEndDate;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder skuId(Long skuId) {
            this.skuId = skuId;
            return this;
        }

        public Builder relatedId(Long relatedId) {
            this.relatedId = relatedId;
            return this;
        }

        public Builder sku(Integer sku) {
            this.sku = sku;
            return this;
        }

        public Builder upc(String upc) {
            this.upc = upc;
            return this;
        }

        public Builder alt(String alt) {
            this.alt = alt;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder descr(String descr) {
            this.descr = descr;
            return this;
        }

        public Builder uom(String uom) {
            this.uom = uom;
            return this;
        }

        public Builder dept(Integer dept) {
            this.dept = dept;
            return this;
        }

        public Builder modelNum(String modelNum) {
            this.modelNum = modelNum;
            return this;
        }

        public Builder manufacturer(String manufacturer) {
            this.manufacturer = manufacturer;
            return this;
        }

        public Builder regularPrice(BigDecimal regularPrice) {
            this.regularPrice = regularPrice;
            return this;
        }

        public Builder salePrice(BigDecimal salePrice) {
            this.salePrice = salePrice;
            return this;
        }

        public Builder saleStartYYYYMMDD(String saleStartYYYYMMDD) {
            this.saleStartYYYYMMDD = saleStartYYYYMMDD;
            return this;
        }

        public Builder saleEndYYYYMMDD(String saleEndYYYYMMDD) {
            this.saleEndYYYYMMDD = saleEndYYYYMMDD;
            return this;
        }

        public Builder weight(BigDecimal weight) {
            this.weight = weight;
            return this;
        }

        public Builder weightUnits(String weightUnits) {
            this.weightUnits = weightUnits;
            return this;
        }

        public Builder width(BigDecimal width) {
            this.width = width;
            return this;
        }

        public Builder height(BigDecimal height) {
            this.height = height;
            return this;
        }

        public Builder depth(BigDecimal depth) {
            this.depth = depth;
            return this;
        }

        public Builder group(ItemGroup group) {
            this.group = group;
            return this;
        }

        public Builder images(List<ItemImage> images) {
            this.images = images;
            return this;
        }

        public Builder relatedItems(List<Item> relatedItems) {
            this.relatedItems = relatedItems;
            return this;
        }

        public Builder activeStartDate(Date activeStartDate) {
            this.activeStartDate = activeStartDate;
            return this;
        }

        public Builder activeEndDate(Date activeEndDate) {
            this.activeEndDate = activeEndDate;
            return this;
        }

        public Item build() {
            return new Item(this);
        }
    }

    private Item(Builder builder) {
        this.id = builder.id;
        this.skuId = builder.skuId;
        this.relatedId = builder.relatedId;
        this.sku = builder.sku;
        this.upc = builder.upc;
        this.alt = builder.alt;
        this.name = builder.name;
        this.descr = builder.descr;
        this.uom = builder.uom;
        this.dept = builder.dept;
        this.modelNum = builder.modelNum;
        this.manufacturer = builder.manufacturer;
        this.regularPrice = builder.regularPrice;
        this.salePrice = builder.salePrice;
        this.saleStartYYYYMMDD = builder.saleStartYYYYMMDD;
        this.saleEndYYYYMMDD = builder.saleEndYYYYMMDD;
        this.weight = builder.weight;
        this.weightUnits = builder.weightUnits;
        this.width = builder.width;
        this.height = builder.height;
        this.depth = builder.depth;
        this.group = builder.group;
        this.images = builder.images;
        this.relatedItems = builder.relatedItems;
        this.activeStartDate = builder.activeStartDate;
        this.activeEndDate = builder.activeEndDate;
    }
}
