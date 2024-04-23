package com.probuild.retail.web.catalog.desktop;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.caucho.hessian.client.HessianProxyFactory;
import com.probuild.retail.web.catalog.domain.ItemGroup;
import com.probuild.retail.web.catalog.ext.service.WebCatalogService;

public class CategoryImages {

    private static String CATALOG_SERVICE_URL = "";
    private WebCatalogService service;

    public CategoryImages() {

    }

    public CategoryImages(WebCatalogService service ) {
        super();
        this.service = service;
    }

    public static void main(String[] args) {

        CATALOG_SERVICE_URL = "http://localhost:8085/catalog/services/CatalogService";
        HessianProxyFactory factory = new HessianProxyFactory();
        final WebCatalogService service;
        try {

            factory.setUser( "jdoe" );
            factory.setPassword( "foo" );
            service = (WebCatalogService) factory.create(WebCatalogService.class, CATALOG_SERVICE_URL );
            CategoryImages catImages = new CategoryImages(service);
            catImages.catImgPathtoFile();
        }
        catch(Exception e) {
            e.printStackTrace();
        }

    }

    public void catImgPathtoFile() {
        List<ItemGroup> allGroups = service.findAllCategories();

        List<String> copyStatement = new ArrayList<String>();
        copyStatement.add("echo on\n");
        copyStatement.add("rem\n");
        int cnt = 0;
        if(allGroups != null) {
            for(ItemGroup grp : allGroups) {
                if(grp.getImage() != null) {
                    String image = grp.getImage().substring(Math.max(0, grp.getImage().length() - 10));
                    String copy = "Copy "+image+" \"catalog/"+grp.getName()+".jpg\"\n";
                    copyStatement.add(copy);
                } else {
                    String copy = "Copy \"catalog/"+grp.getName()+".jpg\"\n";
                    copyStatement.add(copy);
                    cnt++;
                }
            }
            System.out.println("No. of entries -> "+allGroups.size()+" cnt = "+cnt);
        }

        try {
            FileWriter writer = new FileWriter("C:/output.bat");
            for(String s : copyStatement) {
                writer.write(s);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
