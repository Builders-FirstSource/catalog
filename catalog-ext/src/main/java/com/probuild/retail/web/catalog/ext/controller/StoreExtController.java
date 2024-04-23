package com.probuild.retail.web.catalog.ext.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.profile.domain.Address;
import org.broadleafcommerce.profile.domain.AddressImpl;
import org.broadleafcommerce.profile.service.CountryService;
import org.broadleafcommerce.profile.service.StateService;
import org.broadleafcommerce.store.domain.Store;
import org.broadleafcommerce.store.service.StoreService;
import org.broadleafcommerce.store.web.model.FindAStoreForm;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.probuild.retail.web.catalog.ext.domain.FindAStoreFormExt;

@Controller("storeExtController")
public class StoreExtController {

    protected final Log logger = LogFactory.getLog(getClass());

    @Resource(name="blStoreService")
    private StoreService storeService;
    @Resource(name="blStateService")
    protected StateService stateService;
    @Resource(name="blCountryService")
    protected CountryService countryService;

    @RequestMapping("/findAStore.htm")
    public String showStores( ModelMap model, 
                              @RequestParam("toUrl") String backToUrl, 
                              HttpSession session ) {
        //System.out.println ( "Store locator recieved get request" );
        //System.out.println ( "Back to URL: " + backToUrl );
        
        List<Store> storeList = storeService.readAllStores();
        FindAStoreForm findAStoreForm = new FindAStoreForm();
        findAStoreForm.setDistance("30");
        model.addAttribute("stateList", stateService.findStates());
        model.addAttribute("countryList", countryService.findCountries());
        model.addAttribute("stores", storeList);
        model.addAttribute("findAStoreForm", findAStoreForm);
        
        // if back to url was set, put it in the session for later
        if ( backToUrl != null && backToUrl.length() > 0 ) {
            session.setAttribute( "backToUrl", backToUrl );
        }
        
        return "storeLocator/findAStore";
    }

    
    @RequestMapping("/set.htm")
    public String setStore( @RequestParam("locId")Long storeId, 
                            HttpServletRequest req,
                            HttpServletResponse res,
                            HttpSession session ) {
        //System.out.println ( "Store locator recieved get request locId:" + storeId );

        
        
        
        Cookie[] cookie = req.getCookies();
        Cookie found = null;
        if ( cookie != null ) {
            for( Cookie c : cookie ) {
                if ( c.getName().equals( "catalog" ) ) {
                    found = c;
                }
            }
        }
        
        if ( found == null ) {
            found = new Cookie( "catalog", "|locId=" + storeId );
            found.setPath( "/catalog" );
            found.setMaxAge( 3600 * 24 * 120 );
        }
        else {
            found.setValue( "|locId=" + storeId );
            found.setPath( "/catalog" );
            found.setMaxAge( 3600 * 24 * 120 );
        }
        res.addCookie( found );
        
        String backToUrl = (String)session.getAttribute( "backToUrl" );
        
        if ( backToUrl == null || backToUrl.length() == 0 ) {
            return "redirect:store";
        }
        else {
            //System.out.println ( "Sending user back to url " + backToUrl );
            return "redirect:/" + backToUrl;
        }

    }
    
    
    @RequestMapping(method = RequestMethod.POST)
    public String findStores(ModelMap model, @ModelAttribute FindAStoreFormExt findAStoreForm, BindingResult errors) {
        Address searchAddress = new AddressImpl();
        searchAddress.setAddressLine1(findAStoreForm.getAddressLine1());
        searchAddress.setAddressLine2(findAStoreForm.getAddressLine2());
        searchAddress.setCity(findAStoreForm.getCity());
        searchAddress.setState(findAStoreForm.getState());
        searchAddress.setPostalCode(findAStoreForm.getPostalCode());
        searchAddress.setCountry(findAStoreForm.getCountry());
        
        //System.out.println ( "Looking for stores near " + findAStoreForm.getPostalCode() );
        
        if (findAStoreForm.getPostalCode() == null || "".equals(findAStoreForm.getPostalCode()) ||
                "".equals(findAStoreForm.getDistance()) || findAStoreForm.getPostalCode().length() != 5) {
            model.addAttribute("errorMessage" , "Please enter a valid zip/postal code and distance." );
            return showStores(model, "", null );
        }

        // TreeMap to sort by distance
        Map<Store,Double> storeMap = storeService.findStoresByAddress(searchAddress,
                Double.parseDouble(findAStoreForm.getDistance()));
        TreeMap<Double,Store> sortedStoreMap = new TreeMap<Double,Store>();
        
        for ( Store store : storeMap.keySet() ) {
            sortedStoreMap.put( storeMap.get( store ), store );
        }
        
        findAStoreForm.setSortedStoreMap( sortedStoreMap );
        findAStoreForm.setStoreDistanceMap( storeMap );
        if (findAStoreForm.getStoreDistanceMap().size() == 0) {
            model.addAttribute("errorMessage" , "No stores found in your area." );
        }
        model.addAttribute("stateList", stateService.findStates());
        model.addAttribute("countryList", countryService.findCountries());
        model.addAttribute("findAStoreForm", findAStoreForm);
        
        return "storeLocator/findAStore";
    }

    public StoreService getStoreService() {
        return storeService;
    }
    public void setStoreService(StoreService storeService) {
        this.storeService = storeService;
    }

    public StateService getStateService() {
        return stateService;
    }
    public void setStateService(StateService stateService) {
        this.stateService = stateService;
    }

    public CountryService getCountryService() {
        return countryService;
    }
    public void setCountryService(CountryService countryService) {
        this.countryService = countryService;
    }
    

}
