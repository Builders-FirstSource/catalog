package com.probuild.retail.web.catalog.ext.dao;

import java.util.List;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.broadleafcommerce.catalog.dao.ProductDao;
import org.broadleafcommerce.catalog.domain.Product;
import org.broadleafcommerce.profile.util.EntityConfiguration;
import org.springframework.stereotype.Repository;

@Repository("blProductDaoExt")
public class ProductDaoExtImpl implements ProductDao {

    @PersistenceContext(unitName="blPU")
    protected EntityManager em;

    @Resource(name="blEntityConfiguration")
    protected EntityConfiguration entityConfiguration;

    protected String queryCacheableKey = "org.hibernate.cacheable";

    public Product save(Product product) {
        return em.merge(product);
    }

    @SuppressWarnings("unchecked")
    public Product readProductById(Long productId) {
        return (Product) em.find(entityConfiguration.lookupEntityClass("org.broadleafcommerce.catalog.domain.Product"), productId);
    }

    @SuppressWarnings("unchecked")
    public List<Product> readProductsByName(String searchName) {
        Query query = em.createNamedQuery("BC_READ_PRODUCTS_BY_NAME");
        query.setParameter("name", searchName + "%");
        query.setHint(getQueryCacheableKey(), true);
        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<Product> readActiveProductsByCategory(Long categoryId) {
        Query query = em.createNamedQuery("PB_READ_ACTIVE_PRODUCTS_BY_CATEGORY");
        query.setParameter("categoryId", categoryId);
        query.setHint(getQueryCacheableKey(), true);
        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<Product> readProductsByCategory(Long categoryId) {
        Query query = em.createNamedQuery("BC_READ_PRODUCTS_BY_CATEGORY");
        query.setParameter("categoryId", categoryId);
        query.setHint(getQueryCacheableKey(), true);
        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<Product> readProductsBySku(Long skuId) {
        Query query = em.createNamedQuery("BC_READ_PRODUCTS_BY_SKU");
        query.setParameter("skuId", skuId);
        query.setHint(getQueryCacheableKey(), true);
        return query.getResultList();
    }

    public void delete(Product product){
        if (!em.contains(product)) {
            product = readProductById(product.getId());
        }
        em.remove(product);     
    }
    
    public String getQueryCacheableKey() {
        return queryCacheableKey;
    }

    public void setQueryCacheableKey(String queryCacheableKey) {
        this.queryCacheableKey = queryCacheableKey;
    }
}
