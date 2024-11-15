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
package com.probuild.retail.web.catalog.ext.dao;

import java.util.List;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.broadleafcommerce.catalog.dao.CategoryDao;
import org.broadleafcommerce.catalog.domain.Category;
import org.broadleafcommerce.catalog.domain.Product;
import org.broadleafcommerce.profile.util.EntityConfiguration;
import org.springframework.stereotype.Repository;

import com.probuild.retail.web.catalog.ext.domain.CategoryExtImpl;

@Repository("blCategoryDaoExt")
public class CategoryDaoExtImpl implements CategoryDao {

    @PersistenceContext(unitName="blPU")
    protected EntityManager em;

    @Resource(name="blEntityConfiguration")
    protected EntityConfiguration entityConfiguration;

    protected String queryCacheableKey = "org.hibernate.cacheable";

    public Category save(Category category) {
        return em.merge(category);
    }

    @SuppressWarnings("unchecked")
    public Category readCategoryById(Long categoryId) {
        //return (Category) em.find(entityConfiguration.lookupEntityClass("org.broadleafcommerce.catalog.domain.Category"), categoryId);
        //return (Category) em.find( CategoryExtImpl.class, categoryId)
        Category category = null;
        try {
            category = (Category) em.find( CategoryExtImpl.class, categoryId);
        }
        catch(EntityNotFoundException ef) {
            ef.printStackTrace();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return category;
    }

    public Category readCategoryByName(String categoryName) {
        Query query = em.createNamedQuery("PB_READ_CATEGORY_BY_NAME");
        query.setParameter("categoryName", categoryName);
        if(query.getResultList().isEmpty()) {
            return null;
        }
        //query.setHint(getQueryCacheableKey(), true);
        return (Category)query.getSingleResult();
    }

    @SuppressWarnings("unchecked")
    public List<Category> readAllCategories() {
        Query query = em.createNamedQuery("PB_READ_ALL_CATEGORIES");
        //query.setHint(getQueryCacheableKey(), true);
        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<Product> readAllProducts() {
        Query query = em.createNamedQuery("PB_READ_ALL_PRODUCTS");
        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<Category> readAllSubCategories(final Category category) {
        Query query = em.createNamedQuery("PB_READ_ALL_SUBCATEGORIES");
        query.setParameter("defaultParentCategory", category);
        return query.getResultList();
    }

    public String getQueryCacheableKey() {
        return queryCacheableKey;
    }

    public void setQueryCacheableKey(String queryCacheableKey) {
        this.queryCacheableKey = queryCacheableKey;
    }

    public void delete(Category category){
    	if (!em.contains(category)) {
    		category = readCategoryById(category.getId());
    	}
        em.remove(category);
    }

}
