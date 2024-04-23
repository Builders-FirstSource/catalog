package com.probuild.retail.web.catalog.desktop.util;

import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;

public class IconUtil {

    public static final String NEW_DOCUMENT = "/imgs/document-new.png";
    public static final String SAVE_DOCUMENT = "/imgs/document-save.png";
    public static final String BOOK = "/imgs/address-book.png";
    public static final String SMALL_TAG = "/imgs/tag.png";
    public static final String SMALL_PACKAGE = "/imgs/package.png";
    public static final String SMALL_TREE = "/imgs/organize.png";
    public static final String FILE_MANAGER = "/imgs/file-manager.png";
    public static final String MEDIUM_ADD = "/imgs/list-add.png";
    public static final String MEDIUM_REMOVE = "/imgs/list-remove.png";
    public static final String MEDIUM_EDIT = "/imgs/edit-clear.png";
    public static final String MEDIUM_COG = "/imgs/cog.png";
    public static final String MEDIUM_WARNING = "/imgs/dialog-warning.png";
    public static final String LARGE_INFORMATION = "/imgs/dialog-information.png";
    public static final String MEDIUM_CANCEL = "/imgs/edit-undo.png";
    public static final String MEDIUM_DELETE = "/imgs/process-stop.png";
    public static final String MEDIUM_SAVE = "/imgs/save-drawers.png";
    public static final String SMALL_TEXT_DOCUMENT = "/imgs/text-x-generic.png";
    public static final String SMALL_SAVE_DOCUMENT = "/imgs/document-save-small.png";
    public static final String SMALL_LOGOUT = "/imgs/system-log-out.png";
    public static final String SETTINGS = "/imgs/preferences-system.png";
    public static final String SMALL_SETTINGS = "/imgs/preferences-system-small.png";
    public static final String SMALL_SPLASH_CONTENT = "/imgs/internet-news-reader-small.png";
    public static final String SPLASH_CONTENT = "/imgs/internet-news-reader.png";
    
    /**
     *	Default constructor
     */
    public IconUtil() {
        super();
    }

    public static ImageIcon makeImageIcon( String imageName, String altText ) {
        
        ImageIcon imageIcon = null;
        
        // Look for the image.
        URL imageURL = IconUtil.class.getResource( imageName );
        
        
        if(imageURL != null) { // image found
            imageIcon = new ImageIcon(imageURL, altText);
        }

        return imageIcon;
    }
    
    public static JButton makeNavigationButton( String imageName, 
                                                String actionCommand, 
                                                String toolTipText, 
                                                String altText) {
        // Look for the image.
        URL imageURL = IconUtil.class.getResource( imageName );

        // Create and initialize the button.
        JButton button = new JButton();
        button.setActionCommand(actionCommand);
        button.setToolTipText(toolTipText);

        if(imageURL != null) { // image found
            button.setIcon(new ImageIcon(imageURL, altText));
        } else { // no image found
            button.setText(altText);
            System.err.println("Resource not found: " + imageName);
        }

        return button;
    }

    
}
