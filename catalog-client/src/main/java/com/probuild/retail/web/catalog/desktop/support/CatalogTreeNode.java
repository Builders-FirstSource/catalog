package com.probuild.retail.web.catalog.desktop.support;

public class CatalogTreeNode {

    private String type = "";
    private String label = "";
    private long key = 0;
    private Object object = null;

    /**
     *	Default constructor
     */
    public CatalogTreeNode() {
        super();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public long getKey() {
        return key;
    }

    public void setKey(long key) {
        this.key = key;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public String toString() {
        return(label);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int)(key ^ (key >>> 32));
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) { return true; }
        if(obj == null) { return false; }
        if(getClass() != obj.getClass()) { return false; }
        CatalogTreeNode other = (CatalogTreeNode)obj;
        if(key != other.key) { return false; }
        if(type == null) {
            if(other.type != null) { return false; }
        } else if(!type.equals(other.type)) { return false; }
        return true;
    }

    public static class Builder {
        private String type;
        private String label;
        private long key;
        private Object object;

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder label(String label) {
            this.label = label;
            return this;
        }

        public Builder key(long key) {
            this.key = key;
            return this;
        }

        public Builder object(Object object) {
            this.object = object;
            return this;
        }

        public CatalogTreeNode build() {
            return new CatalogTreeNode(this);
        }
    }

    private CatalogTreeNode(Builder builder) {
        this.type = builder.type;
        this.label = builder.label;
        this.key = builder.key;
        this.object = builder.object;
    }
}
