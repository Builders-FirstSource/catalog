package com.probuild.retail.web.catalog.domain;

import java.io.Serializable;

public class ItemFilter implements Serializable {

    private static final long serialVersionUID = 1L;

    protected Long id;
    protected Long itemId;
    protected String name;
    protected String value;

    public ItemFilter() {
        super();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    
    public String toString ( ) {
        return this.name + "=" + this.value;
    }
    
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) { return true; }
        if(obj == null) { return false; }
        if(getClass() != obj.getClass()) { return false; }
        ItemFilter other = (ItemFilter)obj;
        if(name == null) {
            if(other.name != null) { return false; }
        } else if(!name.equals(other.name)) { return false; }
        if(value == null) {
            if(other.value != null) { return false; }
        } else if(!value.equals(other.value)) { return false; }
        return true;
    }


    public static class Builder {
        private Long id;
        private Long itemId;
        private String name;
        private String value;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder itemId(Long itemId) {
            this.itemId = itemId;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder value(String value) {
            this.value = value;
            return this;
        }

        public ItemFilter build() {
            return new ItemFilter(this);
        }
    }

    private ItemFilter(Builder builder) {
        this.id = builder.id;
        this.itemId = builder.itemId;
        this.name = builder.name;
        this.value = builder.value;
    }
}
