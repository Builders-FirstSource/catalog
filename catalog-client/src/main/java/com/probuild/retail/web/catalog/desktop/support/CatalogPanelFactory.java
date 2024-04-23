package com.probuild.retail.web.catalog.desktop.support;

import java.awt.Color;
import java.awt.Font;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import com.probuild.retail.web.catalog.domain.ItemFilter;



public class CatalogPanelFactory {

    /**
     *	Default constructor
     */
    public CatalogPanelFactory() {
        super();
    }

    public static JPanel makeFilterPanel ( List<ItemFilter> filters) {
        
        JPanel layout = new JPanel ( new MigLayout() );
        
        // create a panel for each filter
        for ( ItemFilter filter : filters ) {
            JPanel filterPanel = new JPanel ( new MigLayout() );
            
            JLabel filterLbl = new JLabel ( filter.toString() );
            filterLbl.setFont( filterLbl.getFont().deriveFont( Font.PLAIN ) );
            filterLbl.setForeground( Color.BLUE );
            
//            JLabel removeLbl = new JLabel ( 
//                    "<html><font color=red><u>remove</u></font></html>" );
//                    //IconUtil.makeImageIcon( IconUtil.MEDIUM_REMOVE, "" ),
//                    //SwingConstants.LEFT );
//            removeLbl.setFont( 
//                        removeLbl.getFont().deriveFont( Font.PLAIN, 10f ) );
            
            filterPanel.add( filterLbl, "left" );
            //filterPanel.add( removeLbl, "center" );
            
            layout.add( filterPanel, "wrap" );
        }
        
        return layout;
    }
    
}
