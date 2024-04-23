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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.broadleafcommerce.catalog.domain.Product;
import org.broadleafcommerce.catalog.domain.Sku;
import org.broadleafcommerce.catalog.domain.SkuAttribute;
import org.broadleafcommerce.store.domain.Store;
import org.springframework.transaction.annotation.Transactional;

import com.probuild.retail.web.catalog.ext.domain.SkuAvailabilityExtImpl;


public interface SkuDaoExt {

    public Sku readSkuById(Long skuId);

    public Sku save(Sku sku);

    public Sku readFirstSku();

    public List<Sku> readAllSkus();

    public List<Sku> readSkusById(List<Long> ids);

    public List<SkuAttribute> readAllSkuAttributes ( );

    public SkuAttribute readSkuAttributeById(Long skuId);

    public List<SkuAttribute> readSkuAttributesBySkuId(List<Long> ids);

    public SkuAttribute save ( SkuAttribute skuAttribute );

    public List<Sku> readSkusByAttributeId ( List<Long> ids );

    public void delete(Sku sku);

    public void delete(SkuAttribute skuAttrib);

    public List<Product> readAllProducts();

    public List<Product> readActiveProductsByCategory(Long categoryId);

    public int readActiveProductsByCategorySize(Long categoryId);

    public List<Product> readActiveProductsByCategory(
                            Long categoryId, int pageNum, int displayCount );

    public List<Product> readProductsByCategory(Long categoryId);

    public Product readProductBySkuNum ( String skuNum );

    public Product readProductById ( Long id );

    // just putting this here to avoid overwriting the storeDAO
    public Store readStoreById ( Long id );

    // just putting this here to avoid overwriting creating an new storeDAO
    public void saveImage(long productId, String name, char type, String imageName, byte[] bytes, char newImage);
    public CatalogImagesDaoImpl findCatalogImageByName(String imageName);


    public void removeCatalogImageByName(String imageName);

    public void removeCatalogImageByProductId(Long productId);


    public List<CatalogImagesDaoImpl> findCatalogImageByProductId(Long productId);

    public void updateNewImageByProductId(List<Long> productId, char newImage);

    public List<Product> readProductByIds (List<Long> ids);

    public List<CatalogImagesDaoImpl> findCatalogImagesByProductIds(List<Long> productIds,char newImage);

    public void deleteSkuAvailbasedOnSkuId(List<SkuAvailabilityExtImpl> skuId);

    public List<SkuAvailabilityExtImpl> readSKUAvailabilityForSkuId(Long skuId);

    public void saveSkuAvailability(SkuAvailabilityExtImpl skuAvl );

    public List<SkuAvailabilityExtImpl> readSKUAvailabilityForSkuIds(List<Long> skuIds);

    public SkuAvailabilityExtImpl readSKUAvailabilityForSkuIdLocId(Long skuId,Long locId);


}
