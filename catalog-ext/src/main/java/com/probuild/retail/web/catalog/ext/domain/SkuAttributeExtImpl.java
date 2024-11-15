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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import org.broadleafcommerce.catalog.domain.Sku;
import org.broadleafcommerce.catalog.domain.SkuAttribute;
import org.hibernate.annotations.Index;

/**
 * The Class SkuAttributeImpl is the default implentation of {@link SkuAttribute}.
 * A SKU Attribute is a designator on a SKU that differentiates it from other similar SKUs
 * (for example: Blue attribute for hat).
 * If you want to add fields specific to your implementation of BroadLeafCommerce you should extend
 * this class and add your fields.  If you need to make significant changes to the SkuImpl then you
 * should implement your own version of {@link Sku}.
 * <br>
 * <br>
 * This implementation uses a Hibernate implementation of JPA configured through annotations.
 * The Entity references the following tables:
 * BLC_SKU_ATTRIBUTES,
 * 
 * 
 *   @see {@link SkuAttribute}, {@link SkuPbImpl}
 *   @author btaylor
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "BLC_SKU_ATTRIBUTE")
public class SkuAttributeExtImpl implements SkuAttribute {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The id. */
    @Id
    @GeneratedValue(generator = "SkuAttributeId", strategy = GenerationType.TABLE)
    @TableGenerator(name = "SkuAttributeId", table = "SEQUENCE_GENERATOR", pkColumnName = "ID_NAME", valueColumnName = "ID_VAL", pkColumnValue = "SkuAttributeImpl", allocationSize = 50)
    @Column(name = "SKU_ATTR_ID")
    protected Long id;

    /** The sku. */
    @ManyToOne(targetEntity = SkuExtImpl.class, optional=false)
    @JoinColumn(name = "SKU_ID")
    @Index(name="SKUATTR_SKU_INDEX", columnNames={"SKU_ID"})
    protected Sku sku;

    /** The name. */
    @Column(name = "NAME", nullable=false)
    @Index(name="SKUATTR_NAME_INDEX", columnNames={"NAME"})
    protected String name;

    /** The value. */
    @Column(name = "VALUE", nullable=false)
    protected String value;

    /** The searchable. */
    @Column(name = "SEARCHABLE")
    protected Boolean searchable;

    /* (non-Javadoc)
     * @see org.broadleafcommerce.catalog.domain.SkuAttribute#getId()
     */
    public Long getId() {
        return id;
    }

    /* (non-Javadoc)
     * @see org.broadleafcommerce.catalog.domain.SkuAttribute#setId(java.lang.Long)
     */
    public void setId(Long id) {
        this.id = id;
    }

    /* (non-Javadoc)
     * @see org.broadleafcommerce.catalog.domain.SkuAttribute#getValue()
     */
    public String getValue() {
        return value;
    }

    /* (non-Javadoc)
     * @see org.broadleafcommerce.catalog.domain.SkuAttribute#setValue(java.lang.String)
     */
    public void setValue(String value) {
        this.value = value;
    }

    /* (non-Javadoc)
     * @see org.broadleafcommerce.catalog.domain.SkuAttribute#getSearchable()
     */
    public Boolean getSearchable() {
        return searchable;
    }

    /* (non-Javadoc)
     * @see org.broadleafcommerce.catalog.domain.SkuAttribute#setSearchable(java.lang.Boolean)
     */
    public void setSearchable(Boolean searchable) {
        this.searchable = searchable;
    }

    /* (non-Javadoc)
     * @see org.broadleafcommerce.catalog.domain.SkuAttribute#getSku()
     */
    public Sku getSku() {
        return sku;
    }

    /* (non-Javadoc)
     * @see org.broadleafcommerce.catalog.domain.SkuAttribute#setSku(org.broadleafcommerce.catalog.domain.Sku)
     */
    public void setSku(Sku sku) {
        this.sku = sku;
    }

    /* (non-Javadoc)
     * @see org.broadleafcommerce.catalog.domain.SkuAttribute#getName()
     */
    public String getName() {
        return name;
    }

    /* (non-Javadoc)
     * @see org.broadleafcommerce.catalog.domain.SkuAttribute#setName(java.lang.String)
     */
    public void setName(String name) {
        this.name = name;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((sku == null) ? 0 : sku.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SkuAttributeExtImpl other = (SkuAttributeExtImpl) obj;

        if (id != null && other.id != null) {
            return id.equals(other.id);
        }

        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (sku == null) {
            if (other.sku != null)
                return false;
        } else if (!sku.equals(other.sku))
            return false;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }

}
