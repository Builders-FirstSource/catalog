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
package com.probuild.retail.web.catalog.ext.taglib;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.apache.commons.beanutils.BeanToPropertyValueTransformer;
import org.apache.commons.lang.StringUtils;
import org.broadleafcommerce.catalog.domain.Category;
import org.broadleafcommerce.catalog.domain.Product;
import org.broadleafcommerce.catalog.domain.SkuAttribute;
import org.broadleafcommerce.catalog.web.taglib.SearchFilterTag;
import org.broadleafcommerce.util.money.Money;

import com.probuild.retail.web.catalog.ext.domain.SkuExtImpl;
import com.probuild.retail.web.catalog.ext.domain.ProductExtImpl;
import com.probuild.retail.web.catalog.ext.service.CatalogServiceExt;

/**
 * <p>The SearchFilterItemTag renders form elements designed to help filter a list of products. There
 * are two different filter options currently implemented: multiSelect and sliderRange.</p>
 * <p>multiSelect, the default displayType, renders an unordered list of the unique values for properties.
 * Each item consists of a checkbox, a string containing either the string representation of the property
 * or, if set, the propertyDisplay property of a product. Javascript is also rendered that makes clicking on
 * the strings check the corresponding checkbox as well as apply the css class 'searchFilterDisabledSelect'
 * to unchecked options.</p>
 * <p>sliderRange relies on the designated property being of type {@link Money} and renders a jQuery slider with
 * minimum and maximum values corresponding to the minimum and maximum values of the property. The slider renders
 * with javascript that causes 2 text input boxes to be updated with the values of the slider after each change.</p>
 * <p>After all changes, the javascript function updateSearchFilterResults will be called, this funciton should
 * be defined before the SearchFilterTag.</p>
 *
 */
public class SearchFilterAttribItemTag extends SimpleTagSupport {

    protected String property;
    protected String propertyDisplay;

    protected String displayTitle;
    protected String displayType = "multiSelect";

    private CatalogServiceExt catalogService;

    @Override
    public void doTag() throws JspException, IOException {

        JspWriter out = getJspContext().getOut();
        out.println("<h3>"+getDisplayTitle()+"</h3>");

        if (displayType.equals("multiSelect"))
            doMultiSelect(out);

        super.doTag();
    }

    private void doMultiSelect(JspWriter out) throws JspException, IOException {
        List<Product> products = ((SearchFilterTag) getParent()).getProducts();


        if(products != null ){
        	doProductMultiSelect(out, products);
        }

        //if(categories != null){
           	//doCategoryMultiSelect(out, categories);
        //}
    }

    @SuppressWarnings("unused")
    private void doCategoryMultiSelect(JspWriter out, List<Category> categories) throws JspException, IOException{
        String propertyCss = property.replaceAll("[\\.\\[\\]]", "_");
    	out.println("<ul class='searchFilter-"+propertyCss+"'>");
    	for (Category category : categories) {
    		String catUrl = getUrl(category);
    		out.println("<li vaue='"+category.getName()+"'>"+catUrl);
    	}
    	out.println("</ul>");
    }

    private void doProductMultiSelect(JspWriter out, List<Product> products) throws JspException, IOException{
        BeanToPropertyValueTransformer valueTransformer = new BeanToPropertyValueTransformer(property, true);
        BeanToPropertyValueTransformer displayTransformer;
        if (propertyDisplay != null) {
            displayTransformer = new BeanToPropertyValueTransformer(propertyDisplay, true);
        } else {
            displayTransformer = valueTransformer;
        }

        HashMap<Object, Integer> countMap = new HashMap<Object, Integer>();
        HashMap<Object, Object> valueDisplayMap = new HashMap<Object, Object>();
        for (Product product : products) {

        	SkuExtImpl sku = (SkuExtImpl)product.getAllSkus().get(0);
        	if(sku != null) {
        	    if (sku.getAttributes() != null) {
        	    for ( SkuAttribute attrib : sku.getAttributes() ) {
        	        valueDisplayMap.put( attrib.getValue(), attrib.getName() );
        	        String value = attrib.getValue();
        	        Integer integer = countMap.get(value);
        	        if (integer == null) {
        	            countMap.put(value, new Integer(1));
        	        } else {
        	            countMap.put(value, new Integer(integer + 1));
        	        }
        	    }
        	}
        	}
        }

        String propertyCss = property.replaceAll("[\\.\\[\\]]", "_");

        List sortedValues = new ArrayList(valueDisplayMap.keySet());
        TreeSet sortedValuesTree = new TreeSet ( sortedValues );
        Object[] sortedValue = sortedValuesTree.toArray();

        List sortedDisplayList = new ArrayList(valueDisplayMap.values() );
        TreeSet sortedDisplayTree = new TreeSet ( sortedDisplayList );
        Object[] sortedDisplay = sortedDisplayTree.toArray();



        //for (Object value : countMap.keySet()) {
        for ( Object display : sortedDisplay ) {
            //Object display = valueDisplayMap.get(value);


            //Object display = sorted.get(value);
            //System.out.println ( "Display: " + display + " , value: " + value );
        	out.println("<h4>"+display+"</h4>");
        	out.println("<ul class='searchFilter-"+propertyCss+"'>");

        	for ( Object value : sortedValue ) {

        		//System.out.println ( "Comparing " + display + " to " + value );

        		if ( valueDisplayMap.get( value ).equals( display ) ) {
        		    String val = StringUtils.replace( value.toString(), "'", "&#39;" );
        			out.println(
        				"<li value='"+ val +"'><input type='checkbox' class='searchFilter-"+propertyCss+"Checkbox' name='"+property+"' value='" + val + "'/> " +
        				"<span class='searchFilter-"+propertyCss+"Display'>" + val + "</span>" + " <span class='searchFilter"+propertyCss+"-count'>(" + countMap.get(value).toString() + ")</span></li>");
        		}

        	}
        	out.println("</ul>");
        }
//        out.println("</ul>");


        out.println("<script>" +
                " var " + propertyCss + "Checked = 0;\r\n" +
                "     $('.searchFilter-" + propertyCss + "Checkbox').click(function() {\r\n "+
                "        var value = $(this).attr('value');\r\n" +
                "        var checkbox = $(this).find(':checkbox');\r\n" +
                "        if (" + propertyCss + "Checked == 0) {\r\n" +
                "            $('.searchFilter-" + propertyCss + " li').each(function(){$(this).addClass('searchFilterDisabledSelect')});\r\n" +
                "            $(this).removeClass('searchFilterDisabledSelect');\r\n" +
                "            checkbox.attr('checked',true);\r\n" +
                "            " + propertyCss + "Checked++;\r\n" +
                "        } else if (checkbox.attr('checked') == true) {\r\n" +
                "            $(this).addClass('searchFilterDisabledSelect');\r\n" +
                "            if (" + propertyCss + "Checked == 1) {\r\n" +
                "                // unchecking the only checked category, so reactivate all categories\r\n" +
                "                $('.searchFilter-"+propertyCss+" li').each(function(){$(this).removeClass('searchFilterDisabledSelect')});\r\n" +
                "            } \r\n" +
                "            checkbox.attr('checked',false);\r\n" +
                "            " + propertyCss + "Checked--;\r\n" +
                "        } else {\r\n" +
                "            $(this).removeClass('searchFilterDisabledSelect');\r\n" +
                "            checkbox.attr('checked',true);\r\n" +
                "            " + propertyCss + "Checked++;\r\n" +
                "        }\r\n" +
                "        updateSearchFilterResults();\r\n" +
                "    } );" +

                "     $('.searchFilter-" + propertyCss + "Display').click(function() {\r\n "+
                "        var value = $(this).attr('value');\r\n" +
                "        var liObj = $(this).parent(); \r\n" +
                "        var checkbox = liObj.find(':checkbox');\r\n" +
                "        if (" + propertyCss + "Checked == 0) {\r\n" +
                "            $('.searchFilter-" + propertyCss + " li').each(function(){liObj.addClass('searchFilterDisabledSelect')});\r\n" +
                "            liObj.removeClass('searchFilterDisabledSelect');\r\n" +
                "            checkbox.attr('checked',true);\r\n" +
                "            " + propertyCss + "Checked++;\r\n" +
                "        } else if (checkbox.attr('checked') == true) {\r\n" +
                "            liObj.addClass('searchFilterDisabledSelect');\r\n" +
                "            if (" + propertyCss + "Checked == 1) {\r\n" +
                "                // unchecking the only checked category, so reactivate all categories\r\n" +
                "                $('.searchFilter-"+propertyCss+" li').each(function(){liObj.removeClass('searchFilterDisabledSelect')});\r\n" +
                "            } \r\n" +
                "            checkbox.attr('checked',false);\r\n" +
                "            " + propertyCss + "Checked--;\r\n" +
                "        } else {\r\n" +
                "            liObj.removeClass('searchFilterDisabledSelect');\r\n" +
                "            checkbox.attr('checked',true);\r\n" +
                "            " + propertyCss + "Checked++;\r\n" +
                "        }\r\n" +
                "        updateSearchFilterResults();\r\n" +
                "    } );" +
        "</script>");

    }



    protected String getUrl(Category category) {
        PageContext pageContext = (PageContext)getJspContext();
        HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
        StringBuffer sb = new StringBuffer();
        sb.append("<a href=\"");
        sb.append(request.getContextPath());
        sb.append("/");
        sb.append(category.getGeneratedUrl());
        sb.append("\">");
        sb.append(category.getName());
        sb.append("</a>");

        return sb.toString();
    }


    private Map sortAttributes ( HashMap<Object, Object> map ) {

    	HashMap sortedMap = new LinkedHashMap();


    	List yourMapKeys = new ArrayList( map.keySet() );
    	List yourMapValues = new ArrayList( map.values() );
    	TreeSet sortedSet = new TreeSet( yourMapValues );
    	Object[] sortedArray = sortedSet.toArray();
    	int size = sortedArray.length;

    	for (int i=0; i<size; i++) {
    	   sortedMap.put
    	      (yourMapKeys.get(yourMapValues.indexOf(sortedArray[i])),
    	       sortedArray[i]);
    	}

    	return sortedMap;

    }


    public String getProperty() {
        return property;
    }
    public void setProperty(String property) {
        this.property = property;
    }
    public String getDisplayType() {
        return displayType;
    }
    public void setDisplayType(String displayType) {
        this.displayType = displayType;
    }
    public String getDisplayTitle() {
        if (displayTitle==null) return property;
        return displayTitle;
    }
    public void setDisplayTitle(String displayTitle) {
        this.displayTitle = displayTitle;
    }
    public String getPropertyDisplay() {
        return propertyDisplay;
    }
    public void setPropertyDisplay(String propertyDisplay) {
        this.propertyDisplay = propertyDisplay;
    }
}
