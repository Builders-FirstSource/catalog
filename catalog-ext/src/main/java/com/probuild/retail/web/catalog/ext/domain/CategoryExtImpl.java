/*
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.probuild.retail.web.catalog.ext.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.cache.CacheFactoryException;
import org.broadleafcommerce.catalog.domain.Category;
import org.broadleafcommerce.catalog.domain.FeaturedProduct;
import org.broadleafcommerce.catalog.domain.FeaturedProductImpl;
import org.broadleafcommerce.media.domain.Media;
import org.broadleafcommerce.media.domain.MediaImpl;
import org.broadleafcommerce.util.UrlUtil;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.MapKey;
import org.hibernate.annotations.OrderBy;

/**
 * The Class CategoryImpl is the default implementation of {@link Category}. A
 * category is a group of products. <br>
 * <br>
 * If you want to add fields specific to your implementation of
 * BroadLeafCommerce you should extend this class and add your fields. If you
 * need to make significant changes to the CategoryImpl then you should implment
 * your own version of {@link Category}. <BR>
 * <BR>
 * This implementation uses a Hibernate implementation of JPA configured through
 * annotations. The Entity references the following tables: BLC_CATEGORY,
 * BLC_CATEGORY_XREF, BLC_CATEGORY_IMAGE
 * @see {@link Category}
 * @author btaylor
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "BLC_CATEGORY")
//@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class CategoryExtImpl implements Category {

    private static final Log LOG = LogFactory.getLog(CategoryExtImpl.class);

    private static final long serialVersionUID = 1L;

    /** The id. */
    @Id
    @GeneratedValue(generator = "CategoryId", strategy = GenerationType.TABLE)
    @TableGenerator(name = "CategoryId", table = "SEQUENCE_GENERATOR", pkColumnName = "ID_NAME", valueColumnName = "ID_VAL", pkColumnValue = "CategoryImpl", allocationSize = 50)
    @Column(name = "CATEGORY_ID")
    protected Long id;

    /** The name. */
    @Column(name = "NAME", nullable=false)
    @Index(name="CATEGORY_NAME_INDEX", columnNames={"NAME"})
    protected String name;

    /** The url. */
    @Column(name = "URL")
    protected String url;

    /** The url key. */
    @Column(name = "URL_KEY")
    @Index(name="CATEGORY_URLKEY_INDEX", columnNames={"URL_KEY"})
    protected String urlKey;

    /** The default parent category. */
    @ManyToOne(targetEntity = CategoryExtImpl.class)
    @JoinColumn(name = "DEFAULT_PARENT_CATEGORY_ID")
    @Index(name="CATEGORY_PARENT_INDEX", columnNames={"DEFAULT_PARENT_CATEGORY_ID"})
    protected Category defaultParentCategory;

    /** The description. */
    @Column(name = "DESCRIPTION")
    protected String description;

    /** The active start date. */
    @Column(name = "ACTIVE_START_DATE")
    protected Date activeStartDate;

    /** The active end date. */
    @Column(name = "ACTIVE_END_DATE")
    protected Date activeEndDate;

    /** The display template. */
    @Column(name = "DISPLAY_TEMPLATE")
    protected String displayTemplate;

    /** The all child categories. */
    @ManyToMany(targetEntity = CategoryExtImpl.class)
    @JoinTable(name = "BLC_CATEGORY_XREF", joinColumns = @JoinColumn(name = "CATEGORY_ID"), inverseJoinColumns = @JoinColumn(name = "SUB_CATEGORY_ID", referencedColumnName = "CATEGORY_ID"))
    @OrderBy(clause = "DISPLAY_ORDER")
    //@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @BatchSize(size = 50)
    protected List<Category> allChildCategories = new ArrayList<Category>();

    /** The all parent categories. */
    @ManyToMany(targetEntity = CategoryExtImpl.class)
    @JoinTable(name = "BLC_CATEGORY_XREF", joinColumns = @JoinColumn(name = "SUB_CATEGORY_ID"), inverseJoinColumns = @JoinColumn(name = "CATEGORY_ID", referencedColumnName = "CATEGORY_ID", nullable = true))
    @Cascade(value={org.hibernate.annotations.CascadeType.MERGE, org.hibernate.annotations.CascadeType.PERSIST})
    @OrderBy(clause = "DISPLAY_ORDER")
    //@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @BatchSize(size = 50)
    protected List<Category> allParentCategories = new ArrayList<Category>();

    /** The category images. */
    @CollectionOfElements
    @JoinTable(name = "BLC_CATEGORY_IMAGE", joinColumns = @JoinColumn(name = "CATEGORY_ID"))
    @MapKey(columns = { @Column(name = "NAME", length = 5) })
    @Column(name = "URL")
    //@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @BatchSize(size = 50)
    protected Map<String, String> categoryImages = new HashMap<String, String>();

    @ManyToMany(targetEntity = MediaImpl.class)
    @JoinTable(name = "BLC_CATEGORY_MEDIA_MAP", inverseJoinColumns = @JoinColumn(name = "MEDIA_ID", referencedColumnName = "MEDIA_ID"))
    @MapKey(columns = {@Column(name = "MAP_KEY")})
    @Cascade(value={org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN})
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @BatchSize(size = 50)
    protected Map<String, Media> categoryMedia = new HashMap<String , Media>();

    /** The long description. */
    @Column(name = "LONG_DESCRIPTION")
    protected String longDescription;

    @OneToMany(mappedBy = "category", targetEntity = FeaturedProductImpl.class, cascade = {CascadeType.ALL})
    @Cascade(value={org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN})
    protected List<FeaturedProduct> featuredProducts = new ArrayList<FeaturedProduct>();

    @Transient
    protected Map<String, List<Category>> childCategoryURLMap = new HashMap<String, List<Category>>();

    @Transient
    protected List<Category> childCategories = new ArrayList<Category>();

    @Transient
    protected List<Category> sortedChildCategories = new ArrayList<Category>();

    /*
     * (non-Javadoc)
     * @see org.broadleafcommerce.catalog.domain.Category#getId()
     */
    public Long getId() {
        return id;
    }

    /*
     * (non-Javadoc)
     * @see org.broadleafcommerce.catalog.domain.Category#setId(java.lang.Long)
     */
    public void setId(final Long id) {
        this.id = id;
    }

    /*
     * (non-Javadoc)
     * @see org.broadleafcommerce.catalog.domain.Category#getName()
     */
    public String getName() {
        return name;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.broadleafcommerce.catalog.domain.Category#setName(java.lang.String)
     */
    public void setName(final String name) {
        this.name = name;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.broadleafcommerce.catalog.domain.Category#getDefaultParentCategory()
     */
    public Category getDefaultParentCategory() {
        return defaultParentCategory;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.broadleafcommerce.catalog.domain.Category#setDefaultParentCategory
     * (org.broadleafcommerce.catalog.domain.Category)
     */
    public void setDefaultParentCategory(final Category defaultParentCategory) {
        this.defaultParentCategory = defaultParentCategory;
    }

    /*
     * (non-Javadoc)
     * @see org.broadleafcommerce.catalog.domain.Category#getUrl()
     */
    public String getUrl() {
        return url;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.broadleafcommerce.catalog.domain.Category#setUrl(java.lang.String)
     */
    public void setUrl(final String url) {
        this.url = url;
    }

    /*
     * (non-Javadoc)
     * @see org.broadleafcommerce.catalog.domain.Category#getUrlKey()
     */
    public String getUrlKey() {
        if ((urlKey == null || "".equals(urlKey.trim())) && getName() != null) {
            return UrlUtil.generateUrlKey(getName());
        }
        return urlKey;
    }

    /*
     * (non-Javadoc)
     * @see org.broadleafcommerce.catalog.domain.Category#getGeneratedUrl()
     */
    public String getGeneratedUrl() {
        return buildLink(null, this, false);
    }

    /**
     * Builds the link.
     * @param link the link
     * @param category the category
     * @param ignoreTopLevel the ignore top level
     * @return the string
     */
    private String buildLink(final String link, final Category category, final boolean ignoreTopLevel) {
        if (category == null || (ignoreTopLevel && category.getDefaultParentCategory() == null)) {
            return link;
        }

        String lLink;
    	if (link == null) {
    		lLink = category.getUrlKey();
        } else {
        	lLink = category.getUrlKey() + "/" + link;
        }
        return buildLink(lLink, category.getDefaultParentCategory(), ignoreTopLevel);
    }

    /*
     * (non-Javadoc)
     * @see
     * org.broadleafcommerce.catalog.domain.Category#setUrlKey(java.lang.String)
     */
    public void setUrlKey(final String urlKey) {
        this.urlKey = urlKey;
    }

    /*
     * (non-Javadoc)
     * @see org.broadleafcommerce.catalog.domain.Category#getDescription()
     */
    public String getDescription() {
        return description;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.broadleafcommerce.catalog.domain.Category#setDescription(java.lang
     * .String)
     */
    public void setDescription(final String description) {
        this.description = description;
    }

    /*
     * (non-Javadoc)
     * @see org.broadleafcommerce.catalog.domain.Category#getActiveStartDate()
     */
    public Date getActiveStartDate() {
        return activeStartDate;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.broadleafcommerce.catalog.domain.Category#setActiveStartDate(java
     * .util.Date)
     */
    public void setActiveStartDate(final Date activeStartDate) {
        this.activeStartDate = activeStartDate;
    }

    /*
     * (non-Javadoc)
     * @see org.broadleafcommerce.catalog.domain.Category#getActiveEndDate()
     */
    public Date getActiveEndDate() {
        return activeEndDate;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.broadleafcommerce.catalog.domain.Category#setActiveEndDate(java.util
     * .Date)
     */
    public void setActiveEndDate(final Date activeEndDate) {
        this.activeEndDate = activeEndDate;
    }

    /*
     * (non-Javadoc)
     * @see org.broadleafcommerce.catalog.domain.Category#isActive()
     */
    public boolean isActive() {
        if (LOG.isDebugEnabled()) {
            if (!DateUtil.isActive(getActiveStartDate(), getActiveEndDate(), false)) {
                LOG.debug("category, " + id + ", inactive due to date");
            }
        }
        return DateUtil.isActive(getActiveStartDate(), getActiveEndDate(), false);
    }

    /*
     * (non-Javadoc)
     * @see org.broadleafcommerce.catalog.domain.Category#getDisplayTemplate()
     */
    public String getDisplayTemplate() {
        return displayTemplate;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.broadleafcommerce.catalog.domain.Category#setDisplayTemplate(java
     * .lang.String)
     */
    public void setDisplayTemplate(final String displayTemplate) {
        this.displayTemplate = displayTemplate;
    }

    /**
     * Gets the child categories.
     *
     * @return the child categories
     */
    public List<Category> getAllChildCategories(){
    	return allChildCategories;
    }

    /**
     * Checks for child categories.
     *
     * @return true, if successful
     */
    public boolean hasAllChildCategories(){
    	return !allChildCategories.isEmpty();
    }

    /**
     * Sets the all child categories.
     *
     * @param allChildCategories the new all child categories
     */
    public void setAllChildCategories(final List<Category> childCategories){
    	this.allChildCategories.clear();
    	for(Category category : childCategories){
    		this.allChildCategories.add(category);
    	}
    }



    /*
     * (non-Javadoc)
     * @see org.broadleafcommerce.catalog.domain.Category#getChildCategories()
     */
    public List<Category> getChildCategories() {
    	if (childCategories.isEmpty()) {
    	    //System.out.println ( "getChildCategories " + allChildCategories.size() );
            for (Category category : allChildCategories) {
                //System.out.println ( "child: " + category.getName() + " active: " + category.isActive() );
                if (category.isActive()) {
                    childCategories.add(category);
                }
            }
        }
    	/*
    	 * @Comment: Returns sorted child categories.
    	 */
    	List<Category> sortedChildCategories = sortChildCategories(childCategories);

    	return sortedChildCategories;
    }

    /*
     * (non-Javadoc)
     * @see org.broadleafcommerce.catalog.domain.Category#hasChildCategories()
     */
    public boolean hasChildCategories() {
        return getChildCategories().size() > 0;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.broadleafcommerce.catalog.domain.Category#setAllChildCategories(java
     * .util.List)
     */
    public void setChildCategories(final List<Category> childCategories) {
        this.childCategories.clear();
    	for(Category category : childCategories){
    		this.childCategories.add(category);
    	}
    }

    /*
     * (non-Javadoc)
     * @see org.broadleafcommerce.catalog.domain.Category#getCategoryImages()
     */
    public Map<String, String> getCategoryImages() {
        return categoryImages;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.broadleafcommerce.catalog.domain.Category#getCategoryImage(java.lang
     * .String)
     */
    public String getCategoryImage(final String imageKey) {
        return categoryImages.get(imageKey);
    }

    /*
     * (non-Javadoc)
     * @see
     * org.broadleafcommerce.catalog.domain.Category#setCategoryImages(java.
     * util.Map)
     */
    public void setCategoryImages(final Map<String, String> categoryImages) {
    	this.categoryImages.clear();
//    	for(String key : categoryImages.keySet()){
//    		this.categoryImages.put(key, categoryImages.get(key));
//    	}
    	for(Map.Entry<String, String> me : categoryImages.entrySet()) {
    		this.categoryImages.put(me.getKey(), me.getValue());
    	}
    }

    /*
     * (non-Javadoc)
     * @see org.broadleafcommerce.catalog.domain.Category#getLongDescription()
     */
    public String getLongDescription() {
        return longDescription;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.broadleafcommerce.catalog.domain.Category#setLongDescription(java
     * .lang.String)
     */
    public void setLongDescription(final String longDescription) {
        this.longDescription = longDescription;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.broadleafcommerce.catalog.domain.Category#getChildCategoryURLMap()
     */
    public Map<String, List<Category>> getChildCategoryURLMap() {
    	//HydratedCacheManagerImpl manager = HydratedCacheManagerImpl.getInstance();
    	//Object hydratedItem = ((HydratedCacheManager) manager).getHydratedCacheElementItem(CategoryExtImpl.class.getName(), getId(), "childCategoryURLMap");
    	//if (hydratedItem != null) {
    	//	return (Map<String, List<Category>>) hydratedItem;
    	//}
        if(childCategoryURLMap.isEmpty()) {
            childCategoryURLMap = createChildCategoryURLMap();
        }
        for(Map.Entry<String,List<Category>> me :childCategoryURLMap.entrySet()) {
            System.out.println("me.getKey() -- > "+me.getKey());
            for(Category c : me.getValue()) {
                System.out.println(c.getName());
            }
        }
        System.out.println("childCategoryURLMap.size() -- >"+childCategoryURLMap.size());
        //((HydratedCacheManager) manager).addHydratedCacheElementItem(CategoryExtImpl.class.getName(), getId(), "childCategoryURLMap", childCategoryURLMap);
        return childCategoryURLMap;
    }

    /*
     * (non-Javadoc)
     * @see org.broadleafcommerce.catalog.domain.Category#setChildCategoryURLMap(java.util.Map)
     */
    public void setChildCategoryURLMap(final Map<String, List<Category>> cachedChildCategoryUrlMap) {
    	for(Map.Entry<String,List<Category>> me : cachedChildCategoryUrlMap.entrySet()) {
    	    this.childCategoryURLMap.put(me.getKey(), me.getValue());
    	}

    }

    public Map<String, List<Category>> createChildCategoryURLMap() {

        try {
    	childCategoryURLMap = new HashMap<String, List<Category>>();
        final Map<String, List<Category>> newMap = new HashMap<String, List<Category>>();
        fillInURLMapForCategory(newMap, this, "", new ArrayList<Category>());
        childCategoryURLMap = newMap;
        return childCategoryURLMap;
		} catch (CacheFactoryException e) {
			throw new RuntimeException(e);
		}
    }

    /**
     * Fill in url map for category.
     * @param categoryUrlMap the category url map
     * @param category the category
     * @param startingPath the starting path
     * @param startingCategoryList the starting category list
     */


    private void fillInURLMapForCategory(final Map<String, List<Category>> categoryUrlMap, final Category category, final String startingPath, final List<Category> startingCategoryList) throws CacheFactoryException {
        final String urlKey = category.getUrlKey();
        if (urlKey == null) {
        	throw new CacheFactoryException("Cannot create childCategoryURLMap - the urlKey for a category("+category.getId()+") was null");
        }
    	final String currentPath = startingPath + "/" + category.getUrlKey();
    	//System.out.println ( "Current Path: " + currentPath );
    	//System.out.println ( "--Category: " + category.getName() );
    	//System.out.println ( "--Category children: " + category.getChildCategories().size() );
        final List<Category> newCategoryList = new ArrayList<Category>(startingCategoryList);
        newCategoryList.add(category);



        /*
         * TODO create a simplified, non-persistent version of category to place in this map instead
         * of the actual persistent entity, since we do not intend to fully eager populate every
         * lazy collection in the category hierarchy of elements.
         */
        //populate some lazy items for our cached map
        category.getCategoryImages().size();
        category.getCategoryMedia().size();
        category.getAllParentCategories().size();
        //System.out.println("category.getName()-->   "+category.getName()+"   category.getAllChildCategories().size() --> "+category.getAllChildCategories().size());
        category.getAllChildCategories().size();
        category.getFeaturedProducts().size();

        categoryUrlMap.put(currentPath, newCategoryList);
        for (Category currentCategory : category.getChildCategories()) {
            //System.out.println ( "Current category building from: " + currentCategory.getName() + " --> " );
            fillInURLMapForCategory(categoryUrlMap, currentCategory, currentPath, newCategoryList);
        }
    }

    public List<Category> getAllParentCategories() {
        return allParentCategories;
    }

    public void setAllParentCategories(final List<Category> allParentCategories) {
    	this.allParentCategories.clear();
    	for(Category category : allParentCategories){
    		this.allParentCategories.add(category);
    	}
    }

    public List<FeaturedProduct> getFeaturedProducts() {
        return featuredProducts;
    }

    public void setFeaturedProducts(final List<FeaturedProduct> featuredProducts) {
    	this.featuredProducts.clear();
    	for(FeaturedProduct featuredProduct : featuredProducts){
    		this.featuredProducts.add(featuredProduct);
    	}
    }

    /*
     * (non-Javadoc)
     * @see org.broadleafcommerce.catalog.domain.Category#getCategoryMedia()
     */
    public Map<String, Media> getCategoryMedia() {
        return categoryMedia;
    }

    /*
     * (non-Javadoc)
     * @see org.broadleafcommerce.catalog.domain.Category#setCategoryMedia(java.util.Map)
     */
    public void setCategoryMedia(final Map<String, Media> categoryMedia) {
    	this.categoryMedia.clear();
    	for(Map.Entry<String, Media> me : categoryMedia.entrySet()) {
    		this.categoryMedia.put(me.getKey(), me.getValue());
    	}
    }

    List<Category> sortChildCategories(List<Category> childCategories) {

    	// Sort logic (Bubble sort)
    	for(int i = childCategories.size(); --i>=0; ) {
    		for(int j = 0;j<i;j++) {

    			Category jjust;
    			Category jplus1;
    				if((childCategories.get(j).getName()).compareTo(childCategories.get(j+1).getName()) > 0) {

    					jjust = childCategories.get(j);
    	                jplus1 = childCategories.get(j+1);

    	                childCategories.remove(j);
    	                childCategories.add(j,jplus1);

    	                childCategories.remove(j+1);
    	                childCategories.add(j+1, jjust);
    				}
    		}
    	}

    	return childCategories;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((url == null) ? 0 : url.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final CategoryExtImpl other = (CategoryExtImpl) obj;

        if (id != null && other.id != null) {
            return id.equals(other.id);
        }

        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (url == null) {
            if (other.url != null)
                return false;
        } else if (!url.equals(other.url))
            return false;
        return true;
    }

}
