package com.probuild.retail.web.catalog.domain;

import java.io.Serializable;

import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;


public class ImportedItem extends Item implements Serializable {

    private static final long serialVersionUID = 1L;

    protected String deliminatedRelatedItems;
    protected String deliminatedGroupPath;
    
    protected String groupImage;
    
    protected String filter1Name;
    protected String filter1Value;
    
    protected String filter2Name;
    protected String filter2Value;
    
    protected String filter3Name;
    protected String filter3Value;
    
    protected String filter4Name;
    protected String filter4Value;
    
    public ImportedItem() {
        super();
    }

    public String getDeliminatedRelatedItems() {
        return deliminatedRelatedItems;
    }

    public void setDeliminatedRelatedItems(String deliminatedRelatedItems) {
        this.deliminatedRelatedItems = deliminatedRelatedItems;
    }

    @NotNull @Length(min=1, max=255)
    public String getDeliminatedGroupPath() {
        return deliminatedGroupPath;
    }

    public void setDeliminatedGroupPath(String deliminatedGroupPath) {
        this.deliminatedGroupPath = deliminatedGroupPath;
    }

    @Length(max=255)
    public String getFilter1Name() {
        return filter1Name;
    }

    public void setFilter1Name(String filter1Name) {
        this.filter1Name = filter1Name;
    }

    @Length(max=255)
    public String getFilter1Value() {
        return filter1Value;
    }

    public void setFilter1Value(String filter1Value) {
        this.filter1Value = filter1Value;
    }

    @Length(max=255)
    public String getFilter2Name() {
        return filter2Name;
    }

    public void setFilter2Name(String filter2Name) {
        this.filter2Name = filter2Name;
    }

    @Length(max=255)
    public String getFilter2Value() {
        return filter2Value;
    }

    public void setFilter2Value(String filter2Value) {
        this.filter2Value = filter2Value;
    }

    @Length(max=255)
    public String getFilter3Name() {
        return filter3Name;
    }

    public void setFilter3Name(String filter3Name) {
        this.filter3Name = filter3Name;
    }

    @Length(max=255)
    public String getFilter3Value() {
        return filter3Value;
    }
    
    public void setFilter3Value(String filter3Value) {
        this.filter3Value = filter3Value;
    }

    @Length(max=255)
    public String getFilter4Name() {
        return filter4Name;
    }

    public void setFilter4Name(String filter4Name) {
        this.filter4Name = filter4Name;
    }

    @Length(max=255)
    public String getFilter4Value() {
        return filter4Value;
    }

    public void setFilter4Value(String filter4Value) {
        this.filter4Value = filter4Value;
    }

    public String getGroupImage() {
        return groupImage;
    }

    public void setGroupImage(String groupImage) {
        this.groupImage = groupImage;
    }

}
