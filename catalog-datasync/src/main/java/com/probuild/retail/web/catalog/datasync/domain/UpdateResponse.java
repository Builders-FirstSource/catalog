package com.probuild.retail.web.catalog.datasync.domain;

import java.io.Serializable;

public class UpdateResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private int result;
    private int itemsUpdated;
    private int itemsDeleted;

    //Default Constructor
    public UpdateResponse() {
        super();
    }

    public int getResult() {
        return result;
    }
    public void setResult(int result) {
        this.result = result;
    }
    public int getItemsUpdated() {
        return itemsUpdated;
    }
    public void setItemsUpdated(int itemsUpdated) {
        this.itemsUpdated = itemsUpdated;
    }
    public int getItemsDeleted() {
        return itemsDeleted;
    }
    public void setItemsDeleted(int itemsDeleted) {
        this.itemsDeleted = itemsDeleted;
    }

    public static class Builder {

        private int result;
        private int itemsUpdated;
        private int itemsDeleted;

        public Builder result(int result) {
            this.result = result;
            return this;
        }

        public Builder itemsUpdated(int itemsUpdated) {
            this.itemsUpdated = itemsUpdated;
            return this;
        }

        public Builder itemsDeleted(int itemsDeleted) {
            this.itemsDeleted = itemsDeleted;
            return this;
        }

    }

    private UpdateResponse(Builder builder) {
        this.result = builder.result;
        this.itemsUpdated = builder.itemsUpdated;
        this.itemsDeleted = builder.itemsDeleted;
    }

}
