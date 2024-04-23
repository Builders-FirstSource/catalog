package com.probuild.retail.web.catalog.domain;

import java.io.Serializable;

public class ItemImage implements Serializable {

    private static final long serialVersionUID = 1L;
    
    protected Long itemId;
    protected String key;
    protected String imagePath;
    protected String localSystemPath;

    /**
     *	Default constructor
     */
    public ItemImage() {
        super();
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
    
    public String getLocalSystemPath() {
        return localSystemPath;
    }

    public void setLocalSystemPath(String localSystemPath) {
        this.localSystemPath = localSystemPath;
    }



    public static class Builder {
        private Long itemId;
        private String key;
        private String imagePath;

        public Builder itemId(Long itemId) {
            this.itemId = itemId;
            return this;
        }

        public Builder key(String key) {
            this.key = key;
            return this;
        }

        public Builder imagePath(String imagePath) {
            this.imagePath = imagePath;
            return this;
        }

        public ItemImage build() {
            return new ItemImage(this);
        }
    }

    private ItemImage(Builder builder) {
        this.itemId = builder.itemId;
        this.key = builder.key;
        this.imagePath = builder.imagePath;
    }
}
