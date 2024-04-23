package com.probuild.retail.web.catalog.desktop;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.tree.DefaultMutableTreeNode;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.netbeans.validation.api.Problem;
import org.netbeans.validation.api.builtin.Validators;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.netbeans.validation.api.ui.ValidationPanel;

import com.probuild.retail.web.catalog.desktop.support.CatalogPanelFactory;
import com.probuild.retail.web.catalog.desktop.support.CatalogTreeNode;
import com.probuild.retail.web.catalog.desktop.support.Configuration;
import com.probuild.retail.web.catalog.desktop.support.ImageFilter;
import com.probuild.retail.web.catalog.desktop.support.ImagePreview;
import com.probuild.retail.web.catalog.desktop.support.JTreeUtil;
import com.probuild.retail.web.catalog.desktop.util.IconUtil;
import com.probuild.retail.web.catalog.domain.Item;
import com.probuild.retail.web.catalog.domain.ItemFilter;
import com.probuild.retail.web.catalog.domain.ItemGroup;
import com.probuild.retail.web.catalog.domain.ItemImage;
import com.probuild.retail.web.catalog.domain.ItemGroupSelect;
import com.probuild.retail.web.catalog.ext.service.WebCatalogService;

import edu.emory.mathcs.backport.java.util.Collections;

public class ItemManagerPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private String IMG_MIRROR_DIR = "c:/java_dev/catalog/products";
    //private String IMG_MIRROR_DIR = "c:/trans/image";

    private ItemGroup group;
    private Item item;
    private WebCatalogService service;
    private JTree catalogTree;
    private Configuration config;

    private int itemsInGroup; // not needed
    private int subGroups; // not needed

    private JTextField nameFld;
    private JTextArea descrFld;
    private JTextField depthFld;
    private JTextField widthFld;
    private JTextField heightFld;
    private JTextField weightFld;
    //private JTextField weightUnitFld;
    private JCheckBox hideItemChk;
    private JComboBox parentGroupCombo;
    private ValidationPanel validatorPanel;
    private JLabel errorLbl;
    private JFileChooser fc;

    private JPanel filterPanel;
    private JPanel imagePanel;
    private JPanel relatedItemsPanel;
    private ButtonGroup imgButtonGroup;

    private DefaultMutableTreeNode selectedNode;

    private List<ItemFilter> filters;

    /**
     *	Default constructor
     */
    public ItemManagerPanel() {
        super();
    }

    public ItemManagerPanel( WebCatalogService service,
                             Configuration config,
                              Item item,
                              JTree catalogTree ) {
        super();
        this.group = item.getGroup();
        this.service = service;
        this.catalogTree = catalogTree;
        this.item = item;
        this.config = config;

        if ( item.getAlt() == null )
            item.setAlt ( "" );
        if ( item.getUpc() == null )
            item.setUpc( "" );
        if ( item.getModelNum() == null )
            item.setModelNum( "" );

        IMG_MIRROR_DIR = config.getImageFolder();

        // hold onto the selected node in case user changes while editing
        this.selectedNode = JTreeUtil.getSelectedNode( catalogTree );

        // load the item filters now
        if ( item.getId() != null && item.getId().longValue() != 0 )
            filters = service.findItemFilters ( item.getId() );
        else
            filters = new ArrayList<ItemFilter>(0);

        // want to reorder the image list so that small,large, small1, large1,...
        Collections.sort( item.getImages(), new ImageKeySort () );

        init ( );
    }

    @SuppressWarnings("unchecked")
    public void init ( ) {

        this.setLayout( new BorderLayout() );

        JPanel layoutPanel = new JPanel ( new MigLayout ( "fill" ) );

        JPanel formPanel = new JPanel ( new MigLayout( ) );

        JLabel titleLbl = new JLabel (
                        "Item Management",
                        IconUtil.makeImageIcon( IconUtil.FILE_MANAGER, "" ),
                        SwingConstants.RIGHT );
        titleLbl.setFont( titleLbl.getFont().deriveFont( 16f ) );

        JLabel idValueLbl = new JLabel ( item.getId().toString() );
        idValueLbl.setFont( idValueLbl.getFont().deriveFont(Font.PLAIN) );
        idValueLbl.setForeground( Color.BLUE );

        JLabel skuValueLbl = new JLabel ( item.getSku().toString() );
        skuValueLbl.setFont( skuValueLbl.getFont().deriveFont(Font.PLAIN) );
        skuValueLbl.setForeground( Color.BLUE );

        JLabel upcValueLbl = new JLabel ( item.getUpc().toString() );
        upcValueLbl.setFont( upcValueLbl.getFont().deriveFont(Font.PLAIN) );
        upcValueLbl.setForeground( Color.BLUE );

        JLabel altValueLbl = new JLabel ( item.getAlt().toString() );
        altValueLbl.setFont( skuValueLbl.getFont().deriveFont(Font.PLAIN) );
        altValueLbl.setForeground( Color.BLUE );

        JLabel modelValueLbl = new JLabel ( item.getModelNum() );
        modelValueLbl.setFont( modelValueLbl.getFont().deriveFont(Font.PLAIN) );
        modelValueLbl.setForeground( Color.BLUE );

        JLabel manufValueLbl = new JLabel ( item.getManufacturer() );
        manufValueLbl.setFont( manufValueLbl.getFont().deriveFont(Font.PLAIN) );
        manufValueLbl.setForeground( Color.BLUE );


        // create an instance of jfilechooser
        fc = new JFileChooser();
        fc.addChoosableFileFilter( new ImageFilter( ) );
        fc.setAcceptAllFileFilterUsed ( false );
        fc.setAccessory ( new ImagePreview( fc ) );
        fc.setMultiSelectionEnabled( true );

        nameFld = new JTextField ( );
        nameFld.setName( "Item Name" );
        nameFld.setText( item.getName() );
        nameFld.setColumns( 25 );

        JScrollPane textScroller = new JScrollPane ();
        descrFld = new JTextArea ( );
        descrFld.setName( "Item Description" );
        descrFld.setText( item.getDescr() );
        descrFld.setColumns( 35 );
        descrFld.setRows( 8 );
        descrFld.setWrapStyleWord( true );
        descrFld.setLineWrap( true );
        descrFld.setFont( nameFld.getFont() );
        //descrFld.setSyntaxEditingStyle( SyntaxConstants.SYNTAX_STYLE_NONE );
        //descrFld.setHighlightCurrentLine( false );
        //initDictionary();

        textScroller.setViewportView( descrFld );

        hideItemChk = new JCheckBox ( );
        hideItemChk.setSelected( false );

        if ( item.getActiveEndDate().getTime() < System.currentTimeMillis() )
            hideItemChk.setSelected( true );

        parentGroupCombo = makeParentGroupsCombo();

        formPanel.add( titleLbl, "span, wrap" );
        formPanel.add( new JLabel ( "Internal Id" ), "right" );
        formPanel.add( idValueLbl, "left, span, wrap" );
        formPanel.add( new JLabel ( "SKU" ), "right" );
        formPanel.add( skuValueLbl, "left, span, wrap" );
        formPanel.add( new JLabel ( "UPC" ), "right" );
        formPanel.add( upcValueLbl, "left, span, wrap" );
        formPanel.add( new JLabel ( "Alternate" ), "right" );
        formPanel.add( altValueLbl, "left, span, wrap" );
        formPanel.add( new JLabel ( "Model #" ), "right" );
        formPanel.add( modelValueLbl, "left, span, wrap" );
        formPanel.add( new JLabel ( "Manufacturer" ), "right" );
        formPanel.add( manufValueLbl, "left, span, wrap" );
        formPanel.add( new JLabel ( "Hide Item" ), "right" );
        formPanel.add( hideItemChk, "left, span, wrap" );
        formPanel.add( new JLabel ( "Parent" ), "right" );
        formPanel.add( parentGroupCombo, "left, span, wrap" );
        formPanel.add( new JLabel ( "Name" ), "right" );
        formPanel.add( nameFld, "left, span, wrap" );
        formPanel.add( new JLabel ( "Description" ), "top, right" );
        formPanel.add( textScroller, "left, span, wrap" );
        formPanel.add( new JLabel ( "Measurements" ), "right" );
        formPanel.add( makeMeasurementPanel(), "left, span, wrap" );
        formPanel.add( new JLabel ( "Weight" ), "right" );
        formPanel.add( makeWeightPanel(), "left, span, wrap" );

        JButton addImageBtn = new JButton ( "Browse" );
        addImageBtn.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chooseImageAction( evt );
            }
        });
        imagePanel = new JPanel ( new BorderLayout() );
        imagePanel.add( makeImagePanel(), BorderLayout.CENTER );
        formPanel.add( new JLabel ( "Images" ), "right" );
        formPanel.add( imagePanel, "left" );
        formPanel.add( addImageBtn, "left, wrap" );


        JButton addFilterBtn = new JButton ( "Manage" );
        addFilterBtn.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chooseFiltersAction( evt );
            }
        });
        filterPanel = new JPanel ( new BorderLayout() );
        filterPanel.add( makeFilterPanel(), BorderLayout.CENTER );
        formPanel.add( new JLabel ( "Filters" ), "right" );
        formPanel.add( filterPanel, "left" );
        formPanel.add( addFilterBtn, "left, wrap" );

        JButton attachRelatedBtn = new JButton ( "Add" );
        attachRelatedBtn.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                attachRelatedItem( evt );
            }
        });
        relatedItemsPanel = new JPanel ( new BorderLayout( )  );
        relatedItemsPanel.add( makeRelatedItemsPanel(), BorderLayout.CENTER );
        formPanel.add( new JLabel ( "Related" ), "right" );
        formPanel.add( relatedItemsPanel, "left" );
        formPanel.add( attachRelatedBtn, "left, wrap" );

        // buttons that appear at lower right corner
        JPanel actionBtnPanel = makeActionButtonPanel();

        layoutPanel.add ( formPanel, "grow, wrap, push" );
        layoutPanel.add( actionBtnPanel, "right" );

        // setup form validation
        validatorPanel = new ValidationPanel ( );
        validatorPanel.setInnerComponent( layoutPanel );
        ValidationGroup validatorGroup = validatorPanel.getValidationGroup();
        validatorGroup.add( nameFld, Validators.REQUIRE_NON_EMPTY_STRING,
                                     Validators.maxLength(100) );
        validatorGroup.add( descrFld, Validators.maxLength( 5000 ) );
        validatorGroup.add( depthFld, Validators.REQUIRE_VALID_NUMBER );
        validatorGroup.add( widthFld, Validators.REQUIRE_VALID_NUMBER );
        validatorGroup.add( heightFld, Validators.REQUIRE_VALID_NUMBER );
        validatorGroup.add( weightFld, Validators.REQUIRE_VALID_NUMBER );
        //validatorGroup.add( weightUnitFld,
        //                                Validators.REQUIRE_NON_EMPTY_STRING );

        this.add( layoutPanel, BorderLayout.CENTER );

    }


    private JPanel makeActionButtonPanel ( ) {

        JButton deleteBtn = new JButton ( "Delete" );
        deleteBtn.setIcon(
                    IconUtil.makeImageIcon( IconUtil.MEDIUM_DELETE, "" ) );
        JButton cancelBtn = new JButton ( "Cancel" );
        cancelBtn.setIcon(
                    IconUtil.makeImageIcon( IconUtil.MEDIUM_CANCEL, "" ) );
        JButton saveBtn = new JButton ( "Save" );
        saveBtn.setIcon(
                    IconUtil.makeImageIcon( IconUtil.MEDIUM_SAVE, "" ) );

        errorLbl = new JLabel ( );
        errorLbl.setIcon(
                        IconUtil.makeImageIcon( IconUtil.MEDIUM_WARNING, "") );
        errorLbl.setVisible( false );

        JPanel btnPanel = new JPanel ( new MigLayout() );


        if ( subGroups > 0 || itemsInGroup > 0 ||
                                        group.getId().equals( new Long(0) ) )
            deleteBtn.setEnabled( false );

        if ( group.getId().equals ( new Long(1) ) ) {
            saveBtn.setEnabled( false );
        }

        saveBtn.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveItemAction(evt);
            }
        });
        deleteBtn.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteItemAction(evt);
            }
        });
        cancelBtn.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelAction(evt);
            }
        });

        JPanel actionBtnPanel = new JPanel ( new MigLayout( "fill" ) );

        btnPanel.add( deleteBtn);
        btnPanel.add( cancelBtn );
        btnPanel.add( saveBtn );

        actionBtnPanel.add( btnPanel, "right, span" );
        actionBtnPanel.add( errorLbl, "span" );

        return actionBtnPanel;
    }

    private JComboBox makeParentGroupsCombo ( ) {

        System.out.println ( "Item group " + item.getGroup().getId() );
        List<ItemGroup> allGroups = service.findAllCategories();

        DefaultComboBoxModel model = new DefaultComboBoxModel();
        int index = 0, count = 0;
        for ( ItemGroup grp : allGroups ) {
            model.addElement( new ItemGroupSelect(grp) );

            if ( group.getId() != null &&
                    grp.getId().equals( group.getId() ) ) {
                index = count;
            }

            count++;
        }

        JComboBox combo = new JComboBox ( model );
        combo.setSelectedIndex( index );


        return combo;
    }

    private JPanel makeMeasurementPanel ( ) {
         JPanel layout = new JPanel ( new MigLayout ( ) );

         depthFld = new JTextField ( "0" );
         depthFld.setName( "Depth" );
         depthFld.setColumns( 6 );

         widthFld = new JTextField ( "0" );
         widthFld.setName( "Width" );
         widthFld.setColumns( 6 );

         heightFld = new JTextField ( "0" );
         heightFld.setName ( "Height" );
         heightFld.setColumns( 6 );

         if ( item.getWidth() != null ) {
             widthFld.setText( item.getWidth().toString() );
         }
         if ( item.getHeight() != null ) {
             heightFld.setText( item.getHeight().toString() );
         }
         if ( item.getDepth() != null ) {
             depthFld.setText( item.getDepth().toString() );
         }

         layout.add( new JLabel ( "w" ) );
         layout.add( widthFld );
         layout.add( new JLabel ( "X h" ) );
         layout.add( heightFld );
         layout.add( new JLabel ( "X d" ) );
         layout.add( depthFld );

         return layout;
    }

    private JPanel makeWeightPanel ( ) {
        JPanel layout = new JPanel ( new MigLayout ( ) );

        weightFld = new JTextField ( "0" );
        weightFld.setName( "Weight" );
        weightFld.setColumns( 6 );

        if ( item.getWeight() != null ) {
            weightFld.setText( item.getWeight().toString() );
        }

        //weightUnitFld = new JTextField ( "Lb" );
        //weightUnitFld.setName( "Weight Units" );
        //weightUnitFld.setColumns( 6 );

        layout.add( weightFld );
        layout.add( new JLabel ( "Pounds" ) );
        //layout.add( weightUnitFld );

        return layout;
   }

    private JPanel makeFilterPanel ( ) {

        return CatalogPanelFactory.makeFilterPanel( filters );

    }


    private JPanel makeRelatedItemsPanel ( ) {

        JPanel layout = new JPanel ( new MigLayout() );
        List<Item> items = item.getRelatedItems();

        if ( items == null )
            return layout;

        // create a panel for each filter
        for ( final Item itm : items ) {
            JPanel relatedItemPanel = new JPanel ( new MigLayout() );

            JLabel relatedLbl = new JLabel ( itm.toString() );
            relatedLbl.setFont( relatedLbl.getFont().deriveFont( 10f ).deriveFont( Font.PLAIN ) );
            relatedLbl.setForeground( Color.BLUE );

            JLabel removeLbl = new JLabel(
                    "Remove",
                    IconUtil.makeImageIcon(IconUtil.MEDIUM_EDIT, "Remove"),
                    SwingConstants.LEFT);
            removeLbl.setFont(
                        removeLbl.getFont().deriveFont( Font.PLAIN, 10f ) );
            removeLbl.addMouseListener( new MouseAdapter() {
                public void mouseClicked( MouseEvent evt ) {
                    removeRelatedMouseClicked( evt, itm );
                }
            });

            relatedItemPanel.add( relatedLbl, "left" );
            relatedItemPanel.add( removeLbl );

            layout.add( relatedItemPanel, "wrap" );
        }

        return layout;
    }

    private JPanel makeImagePanel( ) {

        boolean primaryFound = false;

        JPanel layout = new JPanel(new MigLayout());
        List<ItemImage> images = item.getImages();

        imgButtonGroup = new ButtonGroup ( );

        int index = 0;
        for(final ItemImage img : images) {

            System.out.println ( "Image in list is " + img.getKey() + " - " +img.getImagePath() );

            // don't want the large images
            if(!img.getKey().startsWith("small")) continue;

            byte[] imgBytes = service.readImage("", img.getImagePath());

            if(imgBytes == null) {
                continue;
            }

            JPanel filterPanel = new JPanel(new MigLayout());

            JRadioButton primaryRadio = new JRadioButton ( );
            primaryRadio.setName( "Primary" );
            primaryRadio.setActionCommand( Integer.toString( index ) );
            if ( img.getKey().length() == 5 ) {
                primaryRadio.setSelected( true );
                primaryFound = true;
            }
            imgButtonGroup.add( primaryRadio );
            index = index + 2;

            ImageIcon icon = new ImageIcon(imgBytes);
            JLabel pic = new JLabel(icon);

            JLabel removeLbl = new JLabel(
                        "Remove",
                        IconUtil.makeImageIcon(IconUtil.MEDIUM_EDIT, "Remove"),
                        SwingConstants.LEFT);
            removeLbl.setToolTipText("Remove");
            removeLbl.addMouseListener( new MouseAdapter() {
                public void mouseClicked( MouseEvent evt ) {
                    removeImageMouseClicked( evt, img );
                }
            });
            // JLabel removeLbl = new JLabel (
            // "<html><font color=red><u>remove</u></font></html>" );
            removeLbl.setFont(removeLbl.getFont().deriveFont(Font.PLAIN, 10f));

            filterPanel.add(pic, "left, wrap");
            filterPanel.add( primaryRadio, "center, wrap" );
            filterPanel.add(removeLbl, "center");

            layout.add(filterPanel, "bottom" );

        }

        // set the first radio to primary if none set above
        if ( !primaryFound && images.size() > 1 &&
                imgButtonGroup.getElements().hasMoreElements() ) { // need to be a large & small
            ((JRadioButton)imgButtonGroup.
                    getElements().nextElement()).setSelected( true );
        }

        return layout;
    }

    private void reRenderImagePanel ( ) {

        // get the primary image selected
        ItemImage img = getPrimaryImage();

        if ( img == null && item.getImages().size() == 0 ) {
            System.out.println ( "No primary image yet" );
        } else if ( img == null ) {
            img = item.getImages().get( 0 );
        }

        reorderImageList( img );

        // rebuild the image preview
        imagePanel.setVisible( false );
        imagePanel.removeAll();
        imagePanel.add( makeImagePanel( ), BorderLayout.CENTER );
        imagePanel.setVisible( true );
        imagePanel.revalidate();
    }


    private void reRenderRelatedItemsPanel ( ) {
        relatedItemsPanel.setVisible( false );
        relatedItemsPanel.removeAll();
        relatedItemsPanel.add( makeRelatedItemsPanel(), BorderLayout.CENTER );
        relatedItemsPanel.revalidate();
        relatedItemsPanel.setVisible( true );

    }

    /****************************************************************
     * Actions
     ***************************************************************/
    private void saveItemAction ( ActionEvent evt ) {

        Item changedItem = new Item.Builder().
                            id( item.getId() ).
                            name( item.getName() ).
                            descr( item.getDescr() ).
                            sku( item.getSku() ).
                            upc( item.getUpc() ).
                            alt( item.getAlt() ).
                            uom( item.getUom() ).
                            group( item.getGroup() ).
                            manufacturer( item.getManufacturer() ).
                            modelNum( item.getModelNum() ).
                            images( item.getImages() ).
                            regularPrice( item.getRegularPrice() ).
                            salePrice( item.getSalePrice() ).
                            relatedItems( item.getRelatedItems() ).
                            skuId( item.getSkuId() ).
                            build();

        // double check parent to make sure it can accept a group
        ItemGroup parent = (ItemGroup)parentGroupCombo.getSelectedItem();
        int parentGroupCount =
                service.findAllChildCategories( parent.getId() ).size();

        if ( parentGroupCount > 0 ) {
            JOptionPane.showMessageDialog(null,
                    "The parent has groups attached to it and can \n" +
                                            "not accept an item at this time",
                    "The parent group could not be used",
                    JOptionPane.ERROR_MESSAGE);

            return;
        } else {
            changedItem.setGroup(
                    new ItemGroup.Builder().
                        id( parent.getId() ).
                        name( parent.getName() ).build() );
        }


        Problem problem = validatorPanel.getValidationGroup().validateAll();
        if ( problem != null ) {
            errorLbl.setText("<html><font color=\"red\">" +
                                    problem.getMessage() + "</font></html>" );
            errorLbl.setVisible( true );
            return;
        }

        // get primary image if it changed
        ItemImage primaryImg = getPrimaryImage();
        reorderImageList( primaryImg );

        changedItem.setName( nameFld.getText() );
        changedItem.setDescr( descrFld.getText() );
        changedItem.setDepth( new BigDecimal ( depthFld.getText() ) );
        changedItem.setWidth( new BigDecimal ( widthFld.getText() ) );
        changedItem.setHeight( new BigDecimal ( heightFld.getText() ) );
        changedItem.setWeight( new BigDecimal ( weightFld.getText() ) );
        //changedItem.setWeightUnits( weightUnitFld.getText() );

        if ( hideItemChk.isSelected() )
            changedItem.postDate();
        else
            changedItem.defaultDates();

        if ( changedItem.getId() != null && changedItem.getId().longValue() == 0 ) {
            changedItem.setId( null ); // so hibernate persists as new
            changedItem.setSkuId( null );
        }

        Item savedItem = null;
        try {
            savedItem = service.saveItem( changedItem );
        } catch ( Exception e ) {
            JOptionPane.showMessageDialog(
                    this,
                    "Error saving item!\n" + e.getMessage(),
                    "Error Saving Item",
                    JOptionPane.ERROR_MESSAGE );
            return;
        }

        // ensure filter has right ID
        if ( filters.size() > 0 ) {
            for ( ItemFilter fltr: filters )
                fltr.setItemId( savedItem.getSkuId() );
        }

        try {
            service.saveItemFilters( filters );
        } catch ( Exception e ) {
            JOptionPane.showMessageDialog(
                    this,
                    "Error saving item attributes!\n" + e.getMessage(),
                    "Error Saving Item Attributes",
                    JOptionPane.ERROR_MESSAGE );
            return;
        }


        //for ( ItemFilter fltr : filters )
        //    System.out.println ( "Save: " + fltr.toString() );

        //System.out.println ( "Saved group " + savedGroup.getId() );

        // update catalog tree
        CatalogTreeNode node = new CatalogTreeNode.Builder().
                                    label( savedItem.toString() ).
                                    object( savedItem ).
                                    key( savedItem.getId() ).
                                    type( "item" ).
                                    build();

        if ( changedItem.getId() == null ||
                changedItem.getId().longValue() == 0 ) { // this was a new node

            JTreeUtil.addNode( catalogTree, selectedNode, node );

        } else {

            // was group changed?
            if ( item.getGroup().getId().longValue()
                                        != savedItem.getId().longValue() ) {

                CatalogTreeNode parentDataNode = new CatalogTreeNode.Builder()
                                                    .label( parent.getName() )
                                                    .object( parent )
                                                    .key( parent.getId() )
                                                    .type( "group" )
                                                    .build();

                // new parent
                DefaultMutableTreeNode parentNode =
                JTreeUtil.findNode( catalogTree, parentDataNode );

                JTreeUtil.removeNode( catalogTree, selectedNode, node );

                if ( parentNode != null ) // parent node is displayed currently
                JTreeUtil.addNode( catalogTree, parentNode, node );

            } else { // just update node where it is
                JTreeUtil.updateNode( catalogTree, selectedNode, node );
            }

        }

        // display message, remove form
        clearPanel ( "Item '" + savedItem.toString() + "' has been saved" );


    }


    private void deleteItemAction ( ActionEvent evt ) {

        int choice = JOptionPane.showConfirmDialog(
                        this, "Delete " + item.toString() + "?" );
        if ( choice != JOptionPane.OK_OPTION )
            return;

        // from early check, if delete enabled ok to proceed here
        try {
            service.removeItem( item );
        } catch ( Exception e ) {
            JOptionPane.showMessageDialog(
                    this,
                    "Error deleting item!\n" + e.getMessage(),
                    "Error Deleting Item",
                    JOptionPane.ERROR_MESSAGE );
            return;
        }

        // update catalog tree
        JTreeUtil.removeNode( catalogTree, selectedNode, null );

        // display message, remove form
        clearPanel ( "Item '" + item + "' has been removed" );


    }

    private void cancelAction ( ActionEvent evt ) {

        // we should reload the item from the database here to
        // wipe changes made, if any.
        if ( item.getId() != null ) {
            Item refreshedItem = service.findItemByItemId( item.getId() );
            item.setImages( refreshedItem.getImages() );
            item.setRelatedItems( refreshedItem.getRelatedItems() );
            filters = service.findItemFilters( item.getId() );
        }

        clearPanel ( "Operation canceled" );
    }


    private void clearPanel ( String message ) {
        this.setVisible( false );
        this.removeAll();
        this.setLayout( new MigLayout ( "fill" ) );

        JLabel mesgLbl = new JLabel (
                    message,
                    IconUtil.makeImageIcon( IconUtil.LARGE_INFORMATION, ""),
                    SwingConstants.LEFT );
        mesgLbl.setFont( mesgLbl.getFont().deriveFont( 14f ) );

        this.add( mesgLbl, "center, push" );

        this.revalidate();
        this.setVisible( true );
    }


    private void chooseFiltersAction ( ActionEvent evt ) {
        //List<ItemFilter> filters = service.findItemFilters ( item.getId() );
        if ( filters == null )
            filters = new ArrayList<ItemFilter>();

        FilterChooserFrame chooser = new FilterChooserFrame(
                                         service, item, filters, filterPanel );
        chooser.setLocationRelativeTo( this );
        chooser.setVisible( true );
    }


    /**
     * User picks an image from either small or large folder. Both
     * small and large are copied to server if they do not already
     * exist there.
     * @param evt
     */
    private void chooseImageAction ( ActionEvent evt ) {

        System.out.println ( "chooseImageAction invoked");

    	fc.setCurrentDirectory( new File ( IMG_MIRROR_DIR ) );


        int returnVal = fc.showDialog( this, "Add Image" );

        // if the user approved, process the files
        if ( returnVal == JFileChooser.APPROVE_OPTION ) {

            // get the selected files
            File[] fileList = fc.getSelectedFiles();

            System.out.println ( "fileList " + fileList);

            for ( File imgFile : fileList ) {
            	
                // need to send both a high and low resolution image
                String smallImgPath = "/images/products/small/" + imgFile.getName();
                String largeImgPath = "/images/products/large/" + imgFile.getName();


                if ( !service.imageExists( "", smallImgPath ) ) {

                    System.out.println ( "!service.imageExists");

                	byte[] smallBytes =
                            getFileBytes ( IMG_MIRROR_DIR + "/small/" + imgFile.getName() );
                    byte[] largeBytes =
                            getFileBytes ( IMG_MIRROR_DIR + "/large/" + imgFile.getName() );

                    if ( smallBytes == null || largeBytes == null )
                        continue; // need an error message here


                    long smallByteCount =
                            //service.sendImage( "", smallImgPath, smallBytes );
                        service.sendImage( item.getId(), "small", 'P',
                                smallImgPath, smallBytes, 'Y' );

                    long largeByteCount =
                        // service.sendImage( "", largeImgPath, largeBytes );
                    service.sendImage( item.getId(), "large", 'P',
                            largeImgPath, largeBytes, 'Y' );

                    if ( smallByteCount == 0 || largeByteCount == 0 )
                        continue; // need and error

                }
                
                System.out.println ( "after service.imageExists");

                int index = item.getImages().size();
                String key = "";
                if ( index > 1 )
                    key = Integer.toString( index/2 );

                ItemImage imgSmall = new ItemImage.Builder().
                                            itemId( item.getId() ).
                                            key( "small" + key ).
                                            imagePath( smallImgPath ).
                                            build();
                ItemImage imgLarge = new ItemImage.Builder().
                                            itemId( item.getId() ).
                                            key( "large" + key ).
                                            imagePath( largeImgPath ).
                                            build();

                item.getImages().add( imgSmall );
                item.getImages().add( imgLarge );

            }


        }

        reRenderImagePanel ( );

    }

    private void removeImageMouseClicked ( MouseEvent evt, ItemImage image ) {

        String key = image.getKey();

        item.getImages().remove( image );

        // need to remove large image too
        if ( key.length() == 5 ) {
            item.getImages().remove( getImageByKey( "large" ) );
        }
        else {
            String index = key.substring( 5 );
            item.getImages().remove( getImageByKey( "large" + index ) );
        }

        reRenderImagePanel ( );
    }

    private void attachRelatedItem ( ActionEvent evt ) {

        // open dialog
        String skuNum = (String)JOptionPane.showInputDialog(
                            this,
                            "Enter the item's ERP SKU:",
                "Enter SKU",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                "");



        Item skuItem = null;

        // If a string was returned, say so.
        if ( skuNum != null &&
                skuNum.length() > 0 &&
                    StringUtils.isNumeric( skuNum ) ) {

            skuItem = service.findItemBySkuNum( skuNum );
        }

        if ( skuItem != null ) {
            item.getRelatedItems().add( skuItem );

            reRenderRelatedItemsPanel ( );

            return;
        }

        JOptionPane.showMessageDialog( this,
                "The sku was not found!\nIs it in the catalog?",
                "SKU Not Found",
                JOptionPane.ERROR_MESSAGE);

    }

    private void removeRelatedMouseClicked ( MouseEvent evt, Item itm ) {

        item.getRelatedItems().remove( itm );

        reRenderRelatedItemsPanel();
    }


    private byte[] getFileBytes ( String path ) {

        byte[] bytes = null;

        InputStream in;
        try {
            in = new FileInputStream ( new File ( path ) );
            bytes = IOUtils.toByteArray( in );
        }
        catch(FileNotFoundException e) {
            System.out.println ( "Failed to read file " + e.getMessage() );;
        }
        catch(IOException e) {
            System.out.println ( "Failed to read file " + e.getMessage() );
        }


        return bytes;
    }


    private void reorderImageList ( ItemImage primary ) {

        if (  primary == null ) {
            primary = getImageByKey( "small" );

            if ( primary == null ) {
                System.out.println ( "Could not reorder the list, primary null " );
                return;
            }
        }

        Object[] images = item.getImages().toArray();
        item.getImages().clear();


        int start = primary.getImagePath().lastIndexOf( "/" );
        String primaryImageName = primary.getImagePath().substring( start );

        int keyIndex = 1;
        for ( int i = 0; i < images.length; i++ ) {
            ItemImage img = (ItemImage)images[i];

            if ( img.getImagePath().endsWith( primaryImageName ) ) {
                if ( img.getKey().length() > 5 ) {
                    img.setKey( img.getKey().substring( 0, 5 ) );
                }
            } else {
                if ( img.getKey().startsWith( "small" ) ) {
                    img.setKey( "small" + keyIndex );
                } else {
                    img.setKey( "large" + keyIndex );
                    keyIndex++; // assume that large always in list after small
                }


            }

            item.getImages().add( img );

        }

    }


    private ItemImage getPrimaryImage ( ) {

        if ( item.getImages().size() == 0 )
            return null;

        Enumeration<?> elements = imgButtonGroup.getElements();
        while ( elements.hasMoreElements() ) {
            JRadioButton radio = (JRadioButton)elements.nextElement();

            if ( radio.isSelected() ) {
                System.out.println ( "Found selected radio button " + radio.getActionCommand() );
                String itemIndex = radio.getActionCommand();
                int index = Integer.valueOf( itemIndex).intValue();
                if ( index < item.getImages().size() )
                    return item.getImages().get( index );
                else
                    System.out.println ( "Strange, primary must have changed on screen" );
            }

        }

        return null;

    }

    private ItemImage getImageByKey ( String key ) {

        System.out.println ( "Item image count " + item.getImages().size() );

        for ( ItemImage img : item.getImages() ) {
            if ( img.getKey().equals( key ) ) {
                System.out.println ( "Found img " + img.getImagePath() );
                return img;
            }
        }

        return null;

    }


    public class ImageKeySort implements java.util.Comparator {
        public int compare(Object attr1, Object attr2) {

            ItemImage img1 = (ItemImage)attr1;
            ItemImage img2 = (ItemImage)attr2;


            int img1Index = img1.getKey().length() > 5 ?
                Integer.valueOf( img1.getKey().substring( 5 ) ).intValue() : 99;
            int img2Index = img2.getKey().length() > 5 ?
                Integer.valueOf( img2.getKey().substring( 5 ) ).intValue() : 99;

            if ( img1.getKey().startsWith( "small" ) &&
                    img2.getKey().startsWith( "small" ) ) {

                    return img1Index > img2Index ? -1 : 1;

                } else if ( img1.getKey().startsWith( "small" ) &&
                    img2.getKey().startsWith( "large" ) ) {

                // if the small's index is larger than or equal to
                // large's index return 1 else
                if ( img1Index == img2Index )
                    return -1;

                return img1Index > img2Index ? -1 : 1;

            } else if ( img1.getKey().startsWith( "large" ) &&
                    img2.getKey().startsWith( "small" ) ) {

                if ( img1Index == img2Index )
                    return 1;

                return img1Index > img2Index ? -1 : 1;

            } else if ( img1.getKey().startsWith( "large" ) &&
                    img2.getKey().startsWith( "large" ) ) {

                return img1Index > img2Index ? -1 : 1;

            }


            return 0;
        }
    }


//    private void initDictionary ( ) {
//
//        URL url = ItemManagerPanel.class.getResource(
//                                                   "/dictionary/english.zip" );
//
//
//        boolean american = true; // "false" will use British English
//        SpellingParser parser;
//        try {
//            File zip = new File( url.toURI() );
//            parser = SpellingParser.createEnglishSpellingParser(zip, american);
//            //descrFld.addParser(parser);
//        }
//        catch(IOException e) {
//            System.out.println ( "failed to set dictionary" );
//        }
//        catch(URISyntaxException e) {
//            System.out.println ( "failed to set dictionary " + e.getMessage() );
//        }
//
//    }
    
    /*
     * Test method
     * 
     */
    public static void main( String[] args ) {
    	
        String path = "c:/202341.jpg";
        byte[] imageByte1 = (new ItemManagerPanel()).getFileBytes ( path );
        String imageStr1 = imageByte1.toString();
        System.out.println(imageStr1);

        path = "c:/202033.jpg";
        byte[] imageByte2 = (new ItemManagerPanel()).getFileBytes ( path );
        String imageStr2 = imageByte2.toString();
        System.out.println(imageStr2);
        
        System.exit(0);
    }    
}
