/*
 * ImgFileUtils.java
 *
 * Created on September 6, 2007, 3:07 PM
 *
 * Joseph Simmons
 * Dixieline Lumber, 2007
 */

package com.probuild.retail.web.catalog.desktop.support;

import java.io.File;
import javax.swing.ImageIcon;

/**
 *
 * @author jsimmons
 */
public class ImgFileUtils {
    
    public final static String jpeg = "jpeg";
    public final static String jpg = "jpg";
    public final static String gif = "gif";
    public final static String tiff = "tiff";
    public final static String tif = "tif";
    public final static String png = "png";
    
    
    /** Creates a new instance of ImgFileUtils */
    public ImgFileUtils() {
    }
    
    
    /*
     * Get the extension of a file.
     */
    public static String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');
        
        if (i > 0 &&  i < s.length() - 1) {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
    }

    
    /*
     * Get the file name no extension.
     */
    public static String getFilename(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');
        
        if (i > 0 &&  i < s.length() ) {
            ext = s.substring(0, i);
        }
        return ext;
    }
    
    
    
    /** Returns an ImageIcon, or null if the path was invalid. */
    protected static ImageIcon createImageIcon(String path) {
        
        java.net.URL imgURL = ImgFileUtils.class.getResource(path);
        
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
        
    }
    
}
