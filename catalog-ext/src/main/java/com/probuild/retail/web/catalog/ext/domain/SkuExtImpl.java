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

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.catalog.domain.SkuAttribute;
import org.broadleafcommerce.catalog.domain.SkuImpl;
import org.compass.annotations.SearchableProperty;

/**
 * The Class SkuImpl is extended to add fields for Probuild
 * operations.
 */
@Entity
@Table(name = "BLC_SKU_EXT")
public class SkuExtImpl extends SkuPbImpl {

    private static final Log LOG = LogFactory.getLog(SkuImpl.class);

    private static final long serialVersionUID = 1L;

    @Column(name="sku")
    @SearchableProperty(name="skuNum")
    protected Integer sku;
    @Column(name="item_num")
    protected String itemCode;
    @Column(name="alt_item")
    protected String alternateCode;
    @Column(name="upc")
    protected Long upc;
    @Column(name="erp_item_source")
    protected String erpSourceCode;

    @Transient
    protected List<SkuAttribute> attributes;
    
    public SkuExtImpl ( ) { }
    
    
	public Integer getSku() {
		return sku;
	}
	public void setSku(Integer sku) {
		this.sku = sku;
	}
	
	
	public String getItemCode() {
		return itemCode;
	}
	public void setItemCode(String itemCode) {
		this.itemCode = itemCode;
	}
	
	
	public String getAlternateCode() {
		return alternateCode;
	}
	public void setAlternateCode(String alternateCode) {
		this.alternateCode = alternateCode;
	}
	
	
	public Long getUpc() {
		return upc;
	}
	public void setUpc(Long upc) {
		this.upc = upc;
	}
	
	
	public String getErpSourceCode() {
		return erpSourceCode;
	}
	public void setErpSourceCode(String erpSourceCode) {
		this.erpSourceCode = erpSourceCode;
	}


	public List<SkuAttribute> getAttributes() {
		return attributes;
	}
	public void setAttributes(List<SkuAttribute> attributes) {
		this.attributes = attributes;
	}
    
    
}
