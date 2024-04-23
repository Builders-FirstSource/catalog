package com.probuild.retail.web.catalog.desktop;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.netbeans.validation.api.ui.ValidationPanel;

import com.probuild.retail.web.catalog.desktop.support.CatalogPanelFactory;
import com.probuild.retail.web.catalog.desktop.util.IconUtil;
import com.probuild.retail.web.catalog.domain.Item;
import com.probuild.retail.web.catalog.domain.ItemFilter;
import com.probuild.retail.web.catalog.ext.service.WebCatalogService;

import edu.emory.mathcs.backport.java.util.Collections;

import net.miginfocom.swing.MigLayout;

public class FilterChooserFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private WebCatalogService service;
    
    private JButton addFilterNameBtn;
    private JButton addFilterValueBtn;
    private JTextField filterNameFld;
    private JTextField filterValueFld;
    private JList filterNameLst;
    private JList filterValueLst;
    private JPanel definedFiltersPanel;
    
    private ValidationPanel filterNameValidatorPanel;
    private JLabel errorLbl;
    
    private JPanel itemFilterPanel;
    
    private List<ItemFilter> itemFilters;
    private List<String> definedFilters;
    private List<ItemFilter> definedValues;
    
    private Item item;
    
    /**
     *	Default constructor
     */
    public FilterChooserFrame( WebCatalogService service,
                               Item item,
                               List<ItemFilter> filterList,
                               JPanel itemFilterPanel ) {
        super();
        
        this.service = service;
        
        itemFilters = filterList;
        
        this.itemFilterPanel = itemFilterPanel;
        
        this.item = item;

        init();
    }

    private void init ( ) {
        
        JPanel layout = new JPanel ( new MigLayout( "fill" ) );
        
        
        JPanel namesPanel = new JPanel ( new MigLayout( "fill" ) );
        JPanel addNamePanel = new JPanel ( new MigLayout() );
        JPanel valuesPanel = new JPanel ( new MigLayout( "fill" ) );
        JPanel addValuePanel = new JPanel ( new MigLayout() );

        TitledBorder title;
        title = BorderFactory.createTitledBorder("New");

        definedFiltersPanel = new JPanel ( new BorderLayout() );
        
        
        JLabel titleLbl = new JLabel ( 
                "Item Filters", 
                IconUtil.makeImageIcon( IconUtil.FILE_MANAGER, "" ),
                SwingConstants.RIGHT );
        titleLbl.setFont( titleLbl.getFont().deriveFont( 16f ) );
        
        
        // add name panel
        addFilterNameBtn = new JButton ( "Add" );
        addFilterNameBtn.addActionListener( new ActionListener ( ) {

            public void actionPerformed( ActionEvent evt ) {
                addNewNameAction( evt );
            } 
            
        } );
        filterNameFld = new JTextField ( );
        filterNameFld.setColumns( 10 );
        addNamePanel.add( new JLabel ( "Name" ) );
        addNamePanel.add( filterNameFld );
        addNamePanel.add( addFilterNameBtn );
        addNamePanel.setBorder( title );
        
        // add value panel
        addFilterValueBtn = new JButton ( "Add" );
        addFilterValueBtn.addActionListener( new ActionListener ( ) {

            public void actionPerformed( ActionEvent evt ) {
                addNewValueAction( evt );
            } 
            
        } );
        filterValueFld = new JTextField ( );
        filterValueFld.setColumns( 10 );
        addValuePanel.add( new JLabel ( "Value" ) );
        addValuePanel.add( filterValueFld );
        addValuePanel.add( addFilterValueBtn );
        addValuePanel.setBorder( title );
        
        // add the name field and select list to the same panel
        filterNameLst = new JList();
        filterNameLst.addListSelectionListener( new ListSelectionListener() {
            public void valueChanged( ListSelectionEvent evt ) {
                filterNameListSelection( evt );
            }
        });
        JScrollPane nameScroller = new JScrollPane();
        nameScroller.setPreferredSize( new Dimension ( 100, 200 ) );
        nameScroller.setMinimumSize( new Dimension ( 100, 100 ) );
        nameScroller.getViewport().setView( filterNameLst );
        
        namesPanel.add( addNamePanel, "wrap" );
        namesPanel.add( nameScroller, "push, grow" );
        
        
        // add the name field and select list to the same panel
        filterValueLst = new JList();
        filterValueLst.addMouseListener( new MouseAdapter() {
            public void mouseClicked( MouseEvent evt ) {
                filterValueListMouseClicked( evt );
            }
        });
        JScrollPane valueScroller = new JScrollPane();
        valueScroller.setPreferredSize( new Dimension ( 100, 200 ) );
        valueScroller.setMinimumSize( new Dimension ( 100, 100 ) );
        valueScroller.getViewport().setView( filterValueLst );
        
        valuesPanel.add( addValuePanel, "wrap" );
        valuesPanel.add( valueScroller, "push, grow" );
        
        
        // add the two name and value panels to the layout
        layout.add( titleLbl, "wrap, span" );
        layout.add( namesPanel, "push, grow" );
        layout.add( valuesPanel, "push, grow, wrap" );
        
        
        
        definedFiltersPanel.add( makeChoosenFilterPanel(), 
                                                BorderLayout.CENTER );
        layout.add ( definedFiltersPanel, "wrap, span" );
        
        layout.add ( makeActionButtonPanel(), "right, span" );
        
        
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setTitle( "Choose Item Filters" );
        setLayout( new BorderLayout() );
        add( layout, BorderLayout.CENTER );
        
        setIconImage( IconUtil.makeImageIcon( 
                            IconUtil.BOOK, "Catalog" ).getImage() );
        
        // pack the frame
        pack();
        
        definedFilters = findExistingFilters();
        Object[] listData = definedFilters.toArray();
        if ( listData != null )
            filterNameLst.setListData( listData );
        
    }

    
    private JPanel makeActionButtonPanel ( ) {
        
        //JButton cancelBtn = new JButton ( "Cancel" );
        JButton finishBtn = new JButton ( "Finished" );
        
        errorLbl = new JLabel ( );
        errorLbl.setIcon( 
                        IconUtil.makeImageIcon( IconUtil.MEDIUM_WARNING, "") );
        errorLbl.setVisible( false );
        
        JPanel btnPanel = new JPanel ( new MigLayout() );
        

        finishBtn.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                finishedAction(evt);
            } 
        });
        //cancelBtn.addActionListener( new java.awt.event.ActionListener() {
        //    public void actionPerformed(java.awt.event.ActionEvent evt) {
        //        cancelAction(evt);
        //    } 
        //});
        
        JPanel actionBtnPanel = new JPanel ( new MigLayout( "fill" ) );
        
        //btnPanel.add( cancelBtn );
        btnPanel.add( finishBtn );
        
        actionBtnPanel.add( btnPanel, "right, span" );
        actionBtnPanel.add( errorLbl, "span" );
        
        return actionBtnPanel;
    }
    
    
    private JPanel makeChoosenFilterPanel ( ) {
        
        JPanel layout = new JPanel ( new MigLayout( "fill" ) );
        JPanel filterPanel = new JPanel ( new MigLayout() );

        
        for ( final ItemFilter filter : itemFilters ) {
            
            JLabel removeLbl = new JLabel ( 
                      IconUtil.makeImageIcon( IconUtil.MEDIUM_EDIT, "Remove") );
            removeLbl.setToolTipText( "Remove" );
            removeLbl.addMouseListener( new MouseAdapter() {
                public void mouseClicked( MouseEvent evt ) {
                    removeFilterMouseClicked( evt, filter );
                }
            });
            JLabel filterLbl = new JLabel ( filter.toString() );
            filterLbl.setFont( filterLbl.getFont().deriveFont( Font.PLAIN ) );
            filterPanel.add( filterLbl );
            filterPanel.add( removeLbl, "wrap" );
            
        }
        
        JScrollPane filterScroller = new JScrollPane();
        filterScroller.getViewport().setView( filterPanel );
        filterScroller.setPreferredSize( new Dimension ( 250, 100 ) );
        
        JLabel title = new JLabel ( "Defined Filters" );
        
        layout.add( title, "wrap" );
        layout.add( filterScroller );
        
        return layout;
    }
    
    // might need to have the database return these directly
    private List<String> findExistingFilters ( ) {
     
        List<ItemFilter> filters = service.findAllItemFilters();
        List<String> definedFilters = new ArrayList<String>();
        
        for ( ItemFilter fltr : filters ) {
            
            boolean found = false;
            for ( String f : definedFilters ) {
                if ( f.equals( fltr.getName() ) ) {
                    found = true;
                    break;
                }
            }
            
            if ( found )
                continue;
            
            definedFilters.add( fltr.getName() );
        }
        
        Collections.sort( definedFilters );
        
        return definedFilters;
    }
    
    // might need to have the database return these directly
    private List<ItemFilter> findExistingValues ( String name ) {
        
        List<ItemFilter> filters = service.findAllItemFilters();
        List<ItemFilter> definedValues = new ArrayList<ItemFilter>();
        
        for ( ItemFilter fltr : filters ) {
            
            if ( fltr.getName().equals( name ) ) {
                boolean found = false;
                for ( ItemFilter f : definedValues ) {
                
                    if ( fltr.getValue().equals( f.getValue() ) ) {
                        found = true;
                        break;
                    }
                }

                if ( !found )
                    definedValues.add( fltr );
            }
            

            
        }
        
        Collections.sort( definedValues, new AttributeNameSort() );
        
        return definedValues;
    }
     
    
    
    /****************************************************************
     * Actions
     ***************************************************************/
    
    private void finishedAction ( ActionEvent evt ) {
        
        // update the filter panel on the client
        itemFilterPanel.setVisible( false );
        itemFilterPanel.removeAll();
        itemFilterPanel.add( 
                CatalogPanelFactory.makeFilterPanel( itemFilters ), 
                BorderLayout.CENTER );
        itemFilterPanel.setVisible( true );
        itemFilterPanel.revalidate();
        
        this.dispose();
    }
    
    private void addNewNameAction ( ActionEvent evt ) {
        errorLbl.setVisible( false );
//        Problem problem = filterNameValidatorPanel.getValidationGroup().validateAll();
//        if ( problem != null ) {
//            errorLbl.setText("<html><font color=\"red\">" + 
//                                    problem.getMessage() + "</font></html>" );
//            errorLbl.setVisible( true );
//            return;
//        }
        
        String newFilterName = filterNameFld.getText();
        
        if ( "".equals( newFilterName ) ) {
            errorLbl.setText( 
                 "<html><font color=\"red\">Enter a name value</font></html>" );
            errorLbl.setVisible( true );
            return;
        }
            
        definedFilters.add( newFilterName );
        filterNameLst.setListData( definedFilters.toArray() );
    }
    
    private void addNewValueAction ( ActionEvent evt ) {
        errorLbl.setVisible( false );
        
//      Problem problem = filterNameValidatorPanel.getValidationGroup().validateAll();
//      if ( problem != null ) {
//          errorLbl.setText("<html><font color=\"red\">" + 
//                                  problem.getMessage() + "</font></html>" );
//          errorLbl.setVisible( true );
//          return;
//      }
      
        String newValueName = filterValueFld.getText();
      
        if ( "".equals( newValueName ) ) {
            errorLbl.setText( 
               "<html><font color=\"red\">Enter a filter value</font></html>" );
            errorLbl.setVisible( true );
            return;
        }
       
        // if there is text in the filter name field we will use it
        String newFilterName = filterNameFld.getText();
        String selectedFilterName = (String)filterNameLst.getSelectedValue();
        String name = "";
        
        // name selected in list no text in name field
        if ( "".equals( newFilterName ) && selectedFilterName != null ) {
            name = selectedFilterName;
        } else if ( newFilterName.length() > 0 ) {
            name = newFilterName;
            definedFilters.add( name );
            filterNameLst.setListData( definedFilters.toArray() );
        } else {
            errorLbl.setText( 
               "<html><font color=\"red\">Select a filter name</font></html>" );
            errorLbl.setVisible( true );
            return;
        }
      
        ItemFilter filter = new ItemFilter.Builder().
                                       itemId( item.getSkuId() ).
                                       name( name ).
                                       value( newValueName ).build();
        if ( definedValues == null )
            definedValues = new ArrayList<ItemFilter>(1);
        
        definedValues.add( filter );
        filterValueLst.setListData( definedValues.toArray() );
       
        // update list
        itemFilters.add( filter );
        rebuildSelectedFilterPanel ( );
       
    }
    
    private void filterNameListSelection( ListSelectionEvent evt ) {
        
        if ( filterNameLst.getSelectedIndex() < 0 )
            return;
        
        String filterName = (String)filterNameLst.getSelectedValue();
        
        Object[] values = findExistingValues( filterName).toArray();
        if ( values != null )
            filterValueLst.setListData( values );
        
        
    }
    
    
    private void filterValueListMouseClicked( MouseEvent evt ) {
        
        if ( evt.getClickCount() < 2 )
            return;
        
        
        // get filter value selected
        if ( filterValueLst.getSelectedIndex() < 0 )
            return;
        
        ItemFilter filterVal = (ItemFilter)filterValueLst.getSelectedValue();

        // only one value per filter can be assigned to an item
        boolean success = isFilterAlreadyChoosen( filterVal );
        
        if ( success ) {
            
            JOptionPane.showMessageDialog(null,
                     "You can only select 1 value from filter " + 
                                                     filterVal.getName(),
                     "Filter could not be selected",
                     JOptionPane.ERROR_MESSAGE);
            
            return;
        }
        
        filterVal.setId( null ); // so it will get persisted for this item
        filterVal.setItemId( item.getSkuId() );
        itemFilters.add( filterVal );
        
        rebuildSelectedFilterPanel ( );
    }
    
    private void removeFilterMouseClicked ( MouseEvent evt, ItemFilter filter ) {
        
        itemFilters.remove( filter );
        
        rebuildSelectedFilterPanel ( );
    }
    
    
    private void rebuildSelectedFilterPanel ( ) {
        definedFiltersPanel.setVisible( false );
        definedFiltersPanel.removeAll();
        definedFiltersPanel.add( makeChoosenFilterPanel(), BorderLayout.CENTER );
        definedFiltersPanel.setVisible( true );
        definedFiltersPanel.revalidate();
    }
    
    
    private boolean isFilterAlreadyChoosen ( ItemFilter filter ) {
        boolean success = false;
        
        for ( ItemFilter fltr : itemFilters ) {
            if ( fltr.getName().equals( filter.getName() ) ) {
                success = true;
                break;
            }
        }
        
        return success;
    }
    
    
    public class AttributeNameSort implements java.util.Comparator {
        public int compare(Object attr1, Object attr2) {
            int sdif = 
               ((ItemFilter)attr1).getValue().compareTo( ((ItemFilter)attr2).getValue() );
            return sdif;
        }
    }
}
