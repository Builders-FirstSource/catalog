package com.probuild.retail.web.catalog.domain;

import java.io.Serializable;


public class ItemGroupSelect extends ItemGroup implements Serializable {
    
    private static final long serialVersionUID = 1L;

    public ItemGroupSelect ( ) { }
    
    public ItemGroupSelect ( ItemGroup group ) {
        this.id = group.getId();
        this.name = group.getName();
        this.parent = group.getParent();
        this.activeEndDate = group.getActiveEndDate();
        this.activeStartDate = group.getActiveStartDate();
    }
    
    public String toString ( ) {
        if ( this.parent == null ) {
            return this.getName();
        } else {
            return this.parent.getName() + "->" + this.getName();
        }
    }
    
    
}
