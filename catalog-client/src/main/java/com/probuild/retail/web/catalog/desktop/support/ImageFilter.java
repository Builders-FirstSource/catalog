/*
 * ImageFilter.java
 * 
 * A class to select images in the chooser box
 *
 * Created on September 6, 2007, 3:05 PM
 *
 * Joseph Simmons
 * Dixieline Lumber, 2007
 */

package com.probuild.retail.web.catalog.desktop.support;

import java.io.File;
import javax.swing.filechooser.FileFilter;


/**
 *
 * @author jsimmons
 */
public class ImageFilter extends FileFilter {

    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }
        
        String extension = ImgFileUtils.getExtension(f);
        if (extension != null) {
            if (extension.equals(ImgFileUtils.tiff) ||
                    extension.equals(ImgFileUtils.tif) ||
                    extension.equals(ImgFileUtils.gif) ||
                    extension.equals(ImgFileUtils.jpeg) ||
                    extension.equals(ImgFileUtils.jpg) ||
                    extension.equals(ImgFileUtils.png)) {
                return true;
            } else {
                return false;
            }
        }
        
        return false;
    }

    public String getDescription() {
        return ( "All Images" );
    }
    
    
}
