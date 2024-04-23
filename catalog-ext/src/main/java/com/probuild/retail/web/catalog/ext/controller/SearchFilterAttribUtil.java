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
package com.probuild.retail.web.catalog.ext.controller;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanToPropertyValueTransformer;
import org.apache.commons.lang.ArrayUtils;
import org.broadleafcommerce.catalog.domain.Product;
import org.broadleafcommerce.catalog.domain.SkuAttribute;
import org.broadleafcommerce.catalog.web.taglib.SearchFilterItemTag;
import org.broadleafcommerce.catalog.web.taglib.SearchFilterTag;
import org.broadleafcommerce.util.money.Money;

import com.probuild.retail.web.catalog.ext.domain.SkuExtImpl;

/**
 * SearchFilterUtil exposes a simple static method for filtering out products that do not match the
 * criteria POSTed to a controller by a {@link SearchFilterTag}.
 */
public class SearchFilterAttribUtil {
    /**
     * filterProducts iterates over the products for each allowed parameter, filtering out products that do
     * not match the values passed in via the parameters argument. There are 2 ways that a product can be
     * filtered out, corresponding to the multiSelect and sliderRange displayTypes on {@link SearchFilterItemTag}.
     * For multiSelect items, the method will remove the product if the property specified in allowedParameters's
     * toString() method returns a String equal to one of the Strings in the corresponding String[] in parameters.
     * For sliderRange items, the property on the product must be of type {@link Money}. The product will be filtered
     * out if it's property is greater than the Money value parsed out of max-(property name) or smaller than the Money
     * value parsed from min-(property name)
     * @param products the list of products to filter
     * @param parameters the parameters passed to the controller. Generally request.getParameterMap()
     * @param allowedParameters an array of the allowed parameters to filter on
     */
    public static void filterProducts(List<Product> products,
                  Map<String, String[]>parameters, String[] allowedParameters) {

        List<Product> noMatchProducts = new ArrayList<Product>();

        for (String parameter : allowedParameters) {
        	String[] values = parameters.get(parameter);

            //BeanToPropertyValueTransformer reader = new BeanToPropertyValueTransformer(parameter, true);
            if (parameters.containsKey(parameter)) { // we're doing a multi-select
            	for (Iterator<Product> itr = products.iterator(); itr.hasNext(); ) {
                    Product product = itr.next();
                    //if ( !productContainsAttribute ( product, values ) )
                    //	itr.remove();
                    //System.out.print( product.getName() + " = " );
                    //for ( String v : values )
                    //    System.out.println( product.getName() + " attr: " + v );

                    if ( !productContainsAttribute ( product, values ) ) {
                        noMatchProducts.add(product);
                        //System.out.println( " !no match" );
                    } else {
                        //System.out.println( " !matched" );
                    }

                }
            }
        }


    	products.removeAll( noMatchProducts );
    }


    private static boolean productContainsAttribute (
    									Product product, String[] values ) {

    	SkuExtImpl sku = (SkuExtImpl)product.getAllSkus().get(0);
        List<SkuAttribute> attribs = sku.getAttributes();

        boolean found = false;
    	for ( String value : values ) { // loop through all attributes and check against selected values

    	    /*
    	     * @Comment: Fix nullpointerexception when a manufacture and filter parameter was considered
    	     */
    	    if(attribs != null) {
            	for ( SkuAttribute a : attribs ) {

            		//System.out.println ( "Checking " + value + " to " + a.getValue() );
            		if ( a.getValue().equals( value ) ) {
            			//System.out.println ( "Adding sku " + sku.getName() + " to filtered results" );
            			found = true;
            			return found; // don't wait any longer

            		}
            	}
    	    }
        	// if after looping through attributes and checked value not found we are done
//        	if ( !found )
//        		return false;
        }

    	return found;

    }


    private static boolean productContainsAttributeExclusive(
                                            Product product, String[] values) {

        SkuExtImpl sku = (SkuExtImpl)product.getAllSkus().get(0);
        List<SkuAttribute> attribs = sku.getAttributes();

        for(String value : values) { // loop through all attributes and check against selected values
            boolean found = false;

            for(SkuAttribute a : attribs) {

                // System.out.println ( "Checking " + value + " to " + a.getValue() );
                if(a.getValue().equals(value)) {
                    // System.out.println ( "Adding sku " + sku.getName() + " to filtered results" );
                    found = true;

                }
            }

            // if after looping through attributes and checked value not found we are done
            if(!found) return false;
        }

        return true;

    }
}
