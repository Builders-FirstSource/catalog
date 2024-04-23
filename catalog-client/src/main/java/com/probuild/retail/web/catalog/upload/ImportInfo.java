package com.probuild.retail.web.catalog.upload;

import com.probuild.retail.web.catalog.domain.Item;

public class ImportInfo {

    protected Item item;
    protected String message;

    public ImportInfo() {
        super();

    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public static class Builder {
        private Item item;
        private String message;

        public Builder item(Item item) {
            this.item = item;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public ImportInfo build() {
            return new ImportInfo(this);
        }
    }

    private ImportInfo(Builder builder) {
        this.item = builder.item;
        this.message = builder.message;
    }
}
