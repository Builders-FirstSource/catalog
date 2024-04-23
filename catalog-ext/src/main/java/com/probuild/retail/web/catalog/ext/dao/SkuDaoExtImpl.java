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


import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.broadleafcommerce.catalog.domain.Product;
import org.broadleafcommerce.catalog.domain.Sku;
import org.broadleafcommerce.catalog.domain.SkuAttribute;
import org.broadleafcommerce.profile.util.EntityConfiguration;
import org.broadleafcommerce.store.domain.Store;
import org.broadleafcommerce.store.domain.StoreImpl;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.probuild.retail.web.catalog.ext.domain.ProductExtImpl;
import com.probuild.retail.web.catalog.ext.domain.SkuAvailabilityExtImpl;

@Repository("blSkuDaoExt")
public class SkuDaoExtImpl implements SkuDaoExt {

    @PersistenceContext(unitName="blPU")
    protected EntityManager em;

    @Resource(name="blEntityConfiguration")
    protected EntityConfiguration entityConfiguration;

    public Sku save(Sku sku) {
        return em.merge(sku);
    }

    @SuppressWarnings("unchecked")
    public Sku readSkuById(Long skuId) {
        return (Sku) em.find(entityConfiguration.lookupEntityClass("org.broadleafcommerce.catalog.domain.Sku"), skuId);
    }

    public Sku readFirstSku() {
        Query query = em.createNamedQuery("BC_READ_FIRST_SKU");
        return (Sku) query.getSingleResult();
    }

    @SuppressWarnings("unchecked")
    public List<Sku> readAllSkus() {
        Query query = em.createNamedQuery("BC_READ_ALL_SKUS");
        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<Sku> readSkusById(List<Long> ids) {
        Query query = em.createNamedQuery("BC_READ_SKUS_BY_ID");
        query.setParameter("skuIds", ids);
        return query.getResultList();
    }

    public void delete(Sku sku){
    	if (!em.contains(sku)) {
    		sku = readSkuById(sku.getId());
    	}
        em.remove(sku);
    }

    @SuppressWarnings("unchecked")
    public SkuAttribute readSkuAttributeById(Long skuAttribId) {
        return (SkuAttribute) em.find(entityConfiguration.lookupEntityClass("org.broadleafcommerce.catalog.domain.SkuAttribute"), skuAttribId);
    }

	/* (non-Javadoc)
	 * @see org.broadleafcommerce.catalog.dao.SkuDao#readSkusAttributesBySkuId(java.util.List)
	 */
	@SuppressWarnings("unchecked")
	public List<SkuAttribute> readSkuAttributesBySkuId(List<Long> ids) {
		Query query = em.createNamedQuery("PB_READ_SKU_ATTRIB_BY_SKU_ID");
        query.setParameter("skuIds", ids);
        return query.getResultList();
	}

	/* (non-Javadoc)
	 * @see org.broadleafcommerce.catalog.dao.SkuDao#readSkusByAttributeId(java.util.List)
	 */
	@SuppressWarnings("unchecked")
	public List<Sku> readSkusByAttributeId(List<Long> ids) {
		Query query = em.createNamedQuery("PB_READ_SKUS_BY_ATTRIB_ID");
        query.setParameter("skuAttribIds", ids);
        return query.getResultList();
	}

	/* (non-Javadoc)
	 * @see org.broadleafcommerce.catalog.dao.SkuDao#save(org.broadleafcommerce.catalog.domain.SkuAttribute)
	 */
	public SkuAttribute save(SkuAttribute skuAttribute) {
		return em.merge(skuAttribute);
	}

	public void delete(SkuAttribute skuAttribute){
    	if (!em.contains(skuAttribute)) {
    		skuAttribute = readSkuAttributeById(skuAttribute.getId());
    	}
        em.remove(skuAttribute);
    }

	@SuppressWarnings("unchecked")
    public List<SkuAttribute> readAllSkuAttributes() {
        Query query = em.createNamedQuery("PB_READ_ALL_SKU_ATTRIBUTES");
        return query.getResultList();
    }


    @SuppressWarnings("unchecked")
    public List<Product> readAllProducts() {
        Query query = em.createNamedQuery("PB_READ_ALL_PRODUCTS");
        return query.getResultList();
    }


    @SuppressWarnings("unchecked")
    public List<Product> readActiveProductsByCategory(Long categoryId) {
        Query query = em.createNamedQuery("PB_READ_ACTIVE_PRODUCTS_BY_CATEGORY");
        query.setParameter("categoryId", categoryId);
        //query.setHint(getQueryCacheableKey(), true);
        return query.getResultList();
    }


    public int readActiveProductsByCategorySize(Long categoryId) {
        Query query = em.createNamedQuery("PB_READ_ACTIVE_PRODUCTS_BY_CATEGORY_SIZE");
        query.setParameter("categoryId", categoryId);
        return ((Long)query.getSingleResult()).intValue();
    }

    @SuppressWarnings("unchecked")
    public List<Product> readActiveProductsByCategory(
                            Long categoryId, int pageNum, int displayCount ) {
        Query query = em.createNamedQuery("PB_READ_ACTIVE_PRODUCTS_BY_CATEGORY");
        query.setParameter("categoryId", categoryId);
        query.setFirstResult( pageNum * displayCount );
        query.setMaxResults( displayCount );

        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<Product> readProductsByCategory(Long categoryId) {
        Query query = em.createNamedQuery("PB_READ_PRODUCTS_BY_CATEGORY");
        query.setParameter("categoryId", categoryId);
        //query.setHint(getQueryCacheableKey(), true);
        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    public Product readProductBySkuNum ( String skuNum ) {
        Query query = em.createNamedQuery("PB_READ_PRODUCTS_BY_SKU_NUM");
        query.setParameter("skuNum", Integer.valueOf( skuNum ) );
        //query.setHint(getQueryCacheableKey(), true);
        List<Product> products = query.getResultList();

        if ( products.size() == 0 )
            return null;
        else
            return products.get( 0 );
    }

    public Product readProductById ( Long id ) {

        Product product = em.find( ProductExtImpl.class, id );

        return product;
    }

    /*
     * Adding to overcome performance issue on search
     */
    @SuppressWarnings("unchecked")
    public List<Product> readProductByIds (List<Long> ids) {

        Query query = em.createNamedQuery("PB_READ_PRODUCTS_BY_PRODUCTIDS");
        query.setParameter("id", ids);
        List<Product> products = query.getResultList();
        if ( products.size() == 0 ) {
            return null;
        }
        return products;


    }

    /**
     * This should be moved later to it's own DAO
     */
    public Store readStoreById(Long id) {
        // use this: entityConfiguration.lookupEntityClass("org.broadleafcommerce.store.domain.Store")?
        return em.find( StoreImpl.class, id.toString() );
    }


    /**
     * This should be moved later to it's own DAO
     */

    @Transactional
    public void saveImage(long productId, String name, char type, String imageName,
            byte[] bytes, char newImage) {

        // Create new Catalog Image
        CatalogImagesDaoImpl ci = new CatalogImagesDaoImpl();

        ci.setProductId(productId);
        ci.setName(name);
        ci.setType(type);
        ci.setURL(imageName);
        ci.setImage(bytes);
        long time = System.currentTimeMillis();
        ci.setImageDate(new java.sql.Timestamp(time));
        ci.setNewImage(newImage);

        try {
            em.merge(ci);
        }
        catch (Exception e) {
            System.out.println("Exception:" + e);
        }
    }

    /*
     * This should be moved later to it's own DAO
     */
    @Transactional
    public CatalogImagesDaoImpl findCatalogImageByName(String imageName) {

        Query query = em.createNamedQuery("PB_READ_IMAGE_BY_IMAGE_NAME");
        query.setParameter("urlKey", imageName );
        CatalogImagesDaoImpl ci = null;
        try {
            ci = (CatalogImagesDaoImpl) query.getSingleResult();
        }
        catch (NoResultException e ) {
            e.printStackTrace();

        }

        /*if(ci == null) {
            return null;
        }*/
        return ci;
    }

    @SuppressWarnings("unchecked")
    @Transactional
    public List<CatalogImagesDaoImpl> findCatalogImageByProductId(Long productId) {

        Query query = em.createNamedQuery("PB_READ_IMAGE_BY_PRODUCTID");
        query.setParameter("productId", productId );
        //query.setParameter("imageType", imageType );
        //query.setParameter("name", name );
        //query.setMaxResults(1);

        List<CatalogImagesDaoImpl> catalogImage = query.getResultList();

        if(catalogImage != null && !catalogImage.isEmpty()) {
            return catalogImage;
        }
        return null;

    }

    @SuppressWarnings("unchecked")
    @Transactional
    public List<CatalogImagesDaoImpl> findCatalogImagesByProductIds(List<Long> productIds,char newImage) {

        Query query = em.createNamedQuery("PB_READ_IMAGES_BY_PRODUCTIDS");
        query.setParameter("productId", productIds );
        query.setParameter("newImage", newImage);

        List<CatalogImagesDaoImpl> catalogImage = query.getResultList();

        if(catalogImage != null && !catalogImage.isEmpty()) {
            return catalogImage;
        }
        return null;

    }


    @Transactional
    public void updateNewImageByProductId(List<Long> productId, char newImage) {

        Query query = em.createNamedQuery("PB_UPDATE_NEWIMAGE_BY_PRODUCTIDS");
        query.setParameter("productId", productId);
        query.setParameter("newImage", newImage);
        query.executeUpdate();
    }


    @Transactional
    public void removeCatalogImageByName(String imageName) {

        CatalogImagesDaoImpl ci = findCatalogImageByName(imageName);
        em.remove(ci);

    }

    @SuppressWarnings("unchecked")
    @Transactional
    public void removeCatalogImageByProductId(Long productId) {

        Query query = em.createNamedQuery("PB_READ_IMAGE_BY_PRODUCTID");
        query.setParameter("productId", productId );
        List<CatalogImagesDaoImpl> catalogImage = query.getResultList();
        if(catalogImage != null && !catalogImage.isEmpty()) {
            for(CatalogImagesDaoImpl ci : catalogImage) {
                em.remove(ci);
            }
        }
    }


    public void deleteSkuAvailbasedOnSkuId(List<SkuAvailabilityExtImpl> skuId) {

        System.out.println("In SkuAvailabilityDaoImpl - deleteSkuAvailbasedOnSkuId ");

        Query query = em.createNamedQuery("PB_DELETE_SKUAVAIL_BY_SKUID");
        query.setParameter("skuId", skuId.get(0).getSkuId());
        query.executeUpdate();


    }


    @SuppressWarnings("unchecked")
    public SkuAvailabilityExtImpl readSKUAvailabilityForSkuIdLocId(Long skuId,Long locId) {
        Query query = em.createNamedQuery("PB_READ_SKUAVAIL_BY_SKUID_LOCID");
        query.setParameter("skuId", skuId);
        query.setParameter("locId", locId);

        SkuAvailabilityExtImpl skuAvail = (SkuAvailabilityExtImpl)query.getSingleResult();

        if(skuAvail != null)
        {
            return skuAvail;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public List<SkuAvailabilityExtImpl> readSKUAvailabilityForSkuId(Long skuId) {
        Query query = em.createNamedQuery("PB_READ_SKUAVAIL_BY_SKUID");
        query.setParameter("skuId", skuId);
        List<SkuAvailabilityExtImpl> skuAvail = query.getResultList();

        if(skuAvail != null && !(skuAvail.isEmpty()))
        {
            return skuAvail;
        }
        return null;
    }


    @SuppressWarnings("unchecked")
    public List<SkuAvailabilityExtImpl> readSKUAvailabilityForSkuIds(List<Long> skuIds) {
        Query query = em.createNamedQuery("PB_READ_SKUAVAIL_BY_SKUIDS");
        query.setParameter("skuIds", skuIds);
        List<SkuAvailabilityExtImpl> skuAvail = query.getResultList();

        if(skuAvail != null && !(skuAvail.isEmpty()))
        {
            return skuAvail;
        }
        return null;
    }


    public void saveSkuAvailability(SkuAvailabilityExtImpl skuAvl ) {
        try {
            em.merge(skuAvl);
        }
        catch (Exception e) {
            System.out.println("Exception:" + e);
        }

    }


}
