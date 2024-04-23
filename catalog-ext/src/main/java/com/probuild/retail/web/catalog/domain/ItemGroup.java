package com.probuild.retail.web.catalog.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemGroup implements Serializable {

    /** The Constant serialVersionUID.  */
    private static final long serialVersionUID = 1L;

    protected Long id;
    protected String name;
    protected String url;
    protected String urlKey;
    protected ItemGroup defaultParentCategory;
    protected String description;
    protected String longDescription;
    protected String image;
    protected Date activeStartDate;
    protected Date activeEndDate;
    
    protected ItemGroup parent;
    
    protected int itemCount;
    protected int subCategoryCount;
    
    protected List<ItemGroup> allChildCategories = new ArrayList<ItemGroup>();
    protected List<ItemGroup> allParentCategories = new ArrayList<ItemGroup>();

    /** The category images. */
    protected Map<String, String> categoryImages = new HashMap<String, String>();

    protected String delimintedPath;
    protected String imageSystemLocalPath;
    
    /**
     *	Default constructor
     */
    public ItemGroup() {
        super();
        
        id = new Long(0);
    }

    
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrlKey() {
        return urlKey;
    }
    public void setUrlKey(String urlKey) {
        this.urlKey = urlKey;
    }

    public ItemGroup getDefaultParentCategory() {
        return defaultParentCategory;
    }
    public void setDefaultParentCategory(ItemGroup defaultParentCategory) {
        this.defaultParentCategory = defaultParentCategory;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public String getLongDescription() {
        return longDescription;
    }
    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
    }

    public String getImage() {
        return image;
    }
    public void setImage(String image) {
        this.image = image;
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

    public List<ItemGroup> getAllChildCategories() {
        return allChildCategories;
    }
    public void setAllChildCategories(List<ItemGroup> allChildCategories) {
        this.allChildCategories = allChildCategories;
    }
    
    public List<ItemGroup> getAllParentCategories() {
        return allParentCategories;
    }
    public void setAllParentCategories(List<ItemGroup> allParentCategories) {
        this.allParentCategories = allParentCategories;
    }

    public Map<String, String> getCategoryImages() {
        return categoryImages;
    }
    public void setCategoryImages(Map<String, String> categoryImages) {
        this.categoryImages = categoryImages;
    }
    
    public int getItemCount() {
        return itemCount;
    }
    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }

    public int getSubCategoryCount() {
        return subCategoryCount;
    }
    public void setSubCategoryCount(int subCategoryCount) {
        this.subCategoryCount = subCategoryCount;
    }

    public ItemGroup getParent() {
        return parent;
    }
    public void setParent(ItemGroup parent) {
        this.parent = parent;
    }
    
    public String getDelimintedPath() {
        return delimintedPath;
    }
    public void setDelimintedPath(String delimintedPath) {
        this.delimintedPath = delimintedPath;
    }

    public String getImageSystemLocalPath() {
        return imageSystemLocalPath;
    }
    public void setImageSystemLocalPath(String imageSystemLocalPath) {
        this.imageSystemLocalPath = imageSystemLocalPath;
    }


    public void defaultDates ( ) {
        
        long currentTimeMillis = System.currentTimeMillis();
        long futureTimeMillis = currentTimeMillis + ( 3600000l * 24l * 800l );
        setActiveStartDate( roundOffDate( new Date ( currentTimeMillis ) ) );
        setActiveEndDate( roundOffDate( new Date ( futureTimeMillis ) ) );
        
        
    }
    
    public void postDate() {

        long currentTimeMillis = System.currentTimeMillis();
        long postTimeMillis = currentTimeMillis - (3600000l * 24l);
        setActiveStartDate(roundOffDate(new Date(postTimeMillis)));
        setActiveEndDate(roundOffDate(new Date(postTimeMillis)));

    }
    
    private Date roundOffDate ( Date now ) {
        
        Calendar cal = GregorianCalendar.getInstance();
        cal.setTime( now );
        cal.set( Calendar.HOUR_OF_DAY, 0 );
        cal.set(Calendar.MINUTE, 0 );
        cal.set( Calendar.SECOND, 0);
        
        return cal.getTime();
    }
    
    public String toString ( ) {
        return this.getName();
    }
    
    @Override
    public boolean equals(Object obj) {
        if(obj == null || !(obj instanceof ItemGroup)) { return false; }
        
        ItemGroup grp = (ItemGroup)obj;
        if ( grp.getId().equals( this.getId() ) )
            return true;
        
        return false;
    }

    
    public static class Builder {
        private Long id;
        private String name;
        private String url;
        private String urlKey;
        private ItemGroup defaultParentCategory;
        private String description;
        private String longDescription;
        private String image;
        private Date activeStartDate;
        private Date activeEndDate;
        private List<ItemGroup> allChildCategories;
        private List<ItemGroup> allParentCategories;
        private Map<String, String> categoryImages;
        private int itemCount;
        private int subCategoryCount;
        private ItemGroup parent;
        
        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder urlKey(String urlKey) {
            this.urlKey = urlKey;
            return this;
        }

        public Builder defaultParentCategory(ItemGroup defaultParentCategory) {
            this.defaultParentCategory = defaultParentCategory;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder longDescription(String longDescription) {
            this.longDescription = longDescription;
            return this;
        }

        public Builder image(String image) {
            this.image = image;
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

        public Builder allChildCategories(List<ItemGroup> allChildCategories) {
            this.allChildCategories = allChildCategories;
            return this;
        }

        public Builder allParentCategories(List<ItemGroup> allParentCategories) {
            this.allParentCategories = allParentCategories;
            return this;
        }

        public Builder categoryImages(Map<String, String> categoryImages) {
            this.categoryImages = categoryImages;
            return this;
        }

        public Builder itemCount(int itemCount) {
            this.itemCount = itemCount;
            return this;
        }
        
        public Builder subCategoryCount(int subCategoryCount) {
            this.subCategoryCount = subCategoryCount;
            return this;
        }
        
        public Builder parent ( ItemGroup parent ) {
            this.parent = parent;
            return this;
        }
        
        public ItemGroup build() {
            return new ItemGroup(this);
        }
    }

    private ItemGroup(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.url = builder.url;
        this.urlKey = builder.urlKey;
        this.defaultParentCategory = builder.defaultParentCategory;
        this.description = builder.description;
        this.image = builder.image;
        this.longDescription = builder.longDescription;
        this.activeStartDate = builder.activeStartDate;
        this.activeEndDate = builder.activeEndDate;
        this.allChildCategories = builder.allChildCategories;
        this.allParentCategories = builder.allParentCategories;
        this.categoryImages = builder.categoryImages;
        this.itemCount = builder.itemCount;
        this.subCategoryCount = builder.subCategoryCount;
        this.parent = builder.parent;
    }
}
