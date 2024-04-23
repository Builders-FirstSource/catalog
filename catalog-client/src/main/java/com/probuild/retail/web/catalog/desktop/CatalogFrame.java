package com.probuild.retail.web.catalog.desktop;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
/*import java.util.Enumeration;
import java.util.Hashtable;*/
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.apache.commons.lang.StringUtils;
/*import org.broadleafcommerce.catalog.domain.Category;
import org.broadleafcommerce.catalog.domain.Product;*/
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.caucho.hessian.client.HessianProxyFactory;
import com.probuild.retail.web.catalog.desktop.support.CatalogTreeNode;
import com.probuild.retail.web.catalog.desktop.support.Configuration;
import com.probuild.retail.web.catalog.desktop.support.JTreeUtil;
import com.probuild.retail.web.catalog.desktop.util.IconUtil;
import com.probuild.retail.web.catalog.domain.Item;
import com.probuild.retail.web.catalog.domain.ItemGroup;
import com.probuild.retail.web.catalog.domain.ItemImage;
//import com.probuild.retail.web.catalog.domain.ItemInventory;
//import com.probuild.retail.web.catalog.ext.service.CatalogServiceExtImpl;
import com.probuild.retail.web.catalog.ext.service.WebCatalogService;
import com.probuild.retail.web.catalog.ext.util.XTrustProvider;
import com.probuild.retail.web.catalog.repository.AS400ItemRepository;
import com.probuild.retail.web.catalog.repository.ItemRepository;
/*import com.probuild.retail.web.catalog.ext.dao.CategoryDaoExtImpl;
import com.probuild.retail.web.catalog.ext.dao.ProductDaoExtImpl;
import com.probuild.retail.web.catalog.ext.domain.SkuAvailabilityExtImpl;*/

import net.miginfocom.swing.MigLayout;

public class CatalogFrame {

    private static final Logger logger =
                                LoggerFactory.getLogger(CatalogFrame.class);
    private static String CATALOG_SERVICE_URL = "";

    CatalogTreeSelectionListener selectListener = null;
    CatalogTreeWillExpandListener willExpandListener = null;
    private JTree catalogTree;
    private WebCatalogService service;
    //private CategoryDaoExtImpl categoryservice;
    private JPanel rightContent;
    private JScrollPane leftScroller;
    boolean isbreak = false;

    private JPopupMenu catalogTreeGroupMenu;

    private ItemRepository repository;
    //private AS400ItemRepository AS400repository;
    private static Configuration config;


    /**
     *  Default constructor
     */
    public CatalogFrame() {
        super();
    }

    public CatalogFrame( WebCatalogService service ) {
        super();
        this.service = service;

        String url = config.getLegacyConnectionString();
        String user = config.getLegacyUser();
        String pass = config.getLegacyUserPass();

        repository = new AS400ItemRepository();
        repository.setConnection( url, user, pass );
        if ( !repository.connect() )  // TODO: catch window close and close conn
        {
            JOptionPane.showMessageDialog( null,
                    "Could not connect to item repository!\n" +
                       "Make sure user name, password, and url are correct.\n",
                    "Item Repository Unavailable",
                    JOptionPane.ERROR_MESSAGE );
        }
    }

    public void init ( ) {


        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch(ClassNotFoundException e) {
        }
        catch(InstantiationException e) {
        }
        catch(IllegalAccessException e) {
        }
        catch(UnsupportedLookAndFeelException e) {
        }

        catalogTree = new JTree();


        leftScroller = new JScrollPane();
        JScrollPane rightScroller = new JScrollPane();


        JPanel layoutContent = new JPanel ( new MigLayout( "fill" ) );
        rightContent = new JPanel( new BorderLayout() );

        selectListener = new CatalogTreeSelectionListener();
        willExpandListener = new CatalogTreeWillExpandListener();
        catalogTree.addTreeSelectionListener( selectListener );
        catalogTree.addTreeWillExpandListener(willExpandListener);

        setParentGroups ( );

        leftScroller.getViewport().add( catalogTree ); // since not using const.
        leftScroller.setPreferredSize( new Dimension ( 200, 230 ) );



        rightScroller.getViewport().add( rightContent ); // since not using const.
        rightScroller.setVerticalScrollBarPolicy(
                                        JScrollPane.VERTICAL_SCROLLBAR_ALWAYS );
        rightScroller.getVerticalScrollBar().setUnitIncrement( 15 );

        //rightScroller.setPreferredSize( new Dimension ( 400, 170 ) );

        JSplitPane splitPane = new JSplitPane(
                                JSplitPane.HORIZONTAL_SPLIT,
                                leftScroller,
                                rightScroller );


        // put all the pieces together for display
        //layoutContent.add( makeToolBar(), "wrap" );
        layoutContent.add( splitPane, "grow, push, wrap" );
        layoutContent.add( makeVersionPanel(), "grow" );


        // setup the actions
        catalogTree.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                catalogTreeMouseClicked(evt);
            }
        });

        catalogTreeGroupMenu = this.makeCatalogGroupPopupMenu();


        // display a frame with all the widgets
        JFrame frame = new JFrame( "Catalog Manager" );

        frame.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                logger.debug( "App exiting. Closing connections" );
                repository.disconnect();
            }

        });


        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout( new MigLayout( "fill" ) );
        frame.add( layoutContent, "grow" );

        frame.setJMenuBar( makeMenu() );

        frame.setIconImage( IconUtil.makeImageIcon(
                                    IconUtil.BOOK, "Catalog" ).getImage() );

        frame.pack();
        frame.setSize( 800, 550 );
        frame.setVisible( true );


    }

    public static void main( String[] args ) {
        //String url = "http://localhost:8080/catalog/services/CatalogService";
        config = new Configuration();

        XTrustProvider.install(); // accept a self signed cert for SSL


        // check for settings
        // load settings here

        if ( !config.isConfigFilePresent() ) {
            // display settings frame here
            config.saveConfig(); // initialize an empty instance
            SettingsFrame settings = new SettingsFrame ( null, config );
            settings.setVisible( true );

        } else {
            config.loadConfig();
        }


        CATALOG_SERVICE_URL = System.getProperty( "catalog.service" );
        //CATALOG_SERVICE_URL = "http://localhost:8085/catalog/services/CatalogService";

        HessianProxyFactory factory = new HessianProxyFactory();
        final WebCatalogService service;
        try {

            factory.setUser( config.getCatalogUser() );
            factory.setPassword( config.getCatalogUserPass() );
            service = (WebCatalogService) factory.create(
                            WebCatalogService.class, CATALOG_SERVICE_URL );

            // make a quick call to test
            service.imageExists( "", "cat" ); // should return false


            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    CatalogFrame frame = new CatalogFrame( service );
                    frame.init();
                }
              });
        }
        catch(Exception e) {
            logger.error( "Catalog server could not be reached", e );

            JFrame error = new JFrame();
            error.setVisible( true );
            JOptionPane.showMessageDialog( error,
                    "The catalog server is not responding!\n" +
                                "Make sure the service is running?\n" +
                                "Check settings and try again.",
                    "Catalog Service Unavailable",
                    JOptionPane.ERROR_MESSAGE );

            SettingsFrame settings = new SettingsFrame ( null, config );
            settings.setVisible( true );

            logger.debug( "Exit due to error" );

            System.exit( 0 );
        }

    }

    private JPanel makeVersionPanel ( ) {
        JPanel layout = new JPanel ( new MigLayout( "fill" ) );

        JPanel panel = new JPanel( new MigLayout( ) );

        JLabel serverLbl = new JLabel (
                        CATALOG_SERVICE_URL + ", " + getCatalogAdminVersion() );
        serverLbl.setFont(
               serverLbl.getFont().deriveFont( Font.PLAIN ).deriveFont( 10f ) );

        panel.add( serverLbl, "left" );

        layout.add( panel, "right" );

        return layout;
    }

//    private void showBusyPanel ( String label ) {
//
//        JPanel layout = new JPanel ( new BorderLayout() );
//        JProgressBar progressBar = new JProgressBar();
//
//        progressBar.setIndeterminate( true );
//        progressBar.setString( "Fetching " + label );
//
//        rightContent.setVisible( false );
//        rightContent.removeAll();
//
//        layout.setLayout( new MigLayout ( "fill" ) );
//        layout.add( progressBar, "center, push" );
//
//        rightContent.add( layout, BorderLayout.CENTER );
//        rightContent.revalidate();
//        rightContent.setVisible( true );
//    }


    private JToolBar makeToolBar ( ) {
        JToolBar toolBar = new JToolBar();

        JButton newDocBtn = IconUtil.makeNavigationButton(
                                        IconUtil.NEW_DOCUMENT,
                                        "newDocument",
                                        "Create a new upload spreadsheet",
                                        "New Doc" );
        JButton saveDocBtn = IconUtil.makeNavigationButton(
                                        IconUtil.SAVE_DOCUMENT,
                                        "saveDocument",
                                        "Import a spreadsheet",
                                        "Save Doc" );
        saveDocBtn.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BulkUploadFrame bframe = new BulkUploadFrame( service, config );
                bframe.setVisible( true );
            }
        });
        newDocBtn.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BulkUploadTemplateFrame bframe = new BulkUploadTemplateFrame( repository );
                bframe.setVisible( true );
            }
        });


        toolBar.add( newDocBtn );
        toolBar.add( saveDocBtn );

        toolBar.setFloatable( false );

        return toolBar;
    }

    private JMenuBar makeMenu ( ) {

        JMenuBar menuBar = new JMenuBar ( );

        JMenu fileMenu = new JMenu ( "Options" );
        fileMenu.setMnemonic( KeyEvent.VK_O );
        fileMenu.getAccessibleContext().setAccessibleDescription(
                "Catalog Admin Tool Options");
        menuBar.add( fileMenu);

        //a group of JMenuItems
        JMenuItem menuItem = new JMenuItem("Quit",
                                 KeyEvent.VK_Q );
        menuItem.setIcon(
                IconUtil.makeImageIcon( IconUtil.SMALL_LOGOUT, "" ) );
        menuItem.getAccessibleContext().setAccessibleDescription(
                "Quit the Program");
        menuItem.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                repository.disconnect();
                System.exit( 0 );
            }
        });


        // new upload template
        JMenuItem menuItemTemplate = new JMenuItem("Create Upload Template", KeyEvent.VK_T );
        menuItemTemplate.setIcon(
                  IconUtil.makeImageIcon( IconUtil.SMALL_TEXT_DOCUMENT, "" ) );
        menuItemTemplate.getAccessibleContext().
                           setAccessibleDescription( "Create upload template");
        menuItemTemplate.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BulkUploadTemplateFrame bframe = new BulkUploadTemplateFrame( repository );
                bframe.setVisible( true );
            }
        });

        // new upload template
        JMenuItem menuItemUpload = new JMenuItem("Import Items from Template", KeyEvent.VK_I );
        menuItemUpload.setIcon(
                  IconUtil.makeImageIcon( IconUtil.SMALL_SAVE_DOCUMENT, "" ) );
        menuItemUpload.getAccessibleContext().
                           setAccessibleDescription( "Upload items from template");
        menuItemUpload.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BulkUploadFrame bframe = new BulkUploadFrame( service, config );
                bframe.setVisible( true );
            }
        });


        // Price and Inventory Update
        JMenuItem menuItemUpdate = new JMenuItem("Run Price and Inventory Update", KeyEvent.VK_I );
        menuItemUpdate.setIcon(
                  IconUtil.makeImageIcon( IconUtil.SMALL_TEXT_DOCUMENT, "" ) );
        menuItemUpdate.getAccessibleContext().
                           setAccessibleDescription( "Run Price and Inventory Update");
        menuItemUpdate.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RunPriceInventoryFrame updateframe = new RunPriceInventoryFrame( service, config );
                updateframe.setVisible( true );
            }
        });

        // Html Splash Screen
        JMenuItem menuItemSplash = new JMenuItem("Splash Screen Code", KeyEvent.VK_H );
        menuItemSplash.setIcon(
                  IconUtil.makeImageIcon( IconUtil.SMALL_SPLASH_CONTENT, "" ) );
        menuItemSplash.getAccessibleContext().
                           setAccessibleDescription( "Settings");
        menuItemSplash.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SplashEditorFrame sframe = new SplashEditorFrame(service);
                sframe.setVisible( true );
            }
        });

        // Html Parent Menu Screen
        JMenuItem menuItemParentMenu = new JMenuItem("Parent Menu Code", KeyEvent.VK_P );
        menuItemParentMenu.setIcon(
                  IconUtil.makeImageIcon( IconUtil.SMALL_SPLASH_CONTENT, "" ) );
        menuItemParentMenu.getAccessibleContext().
                           setAccessibleDescription( "Settings");
        menuItemParentMenu.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ParentMenuEditorFrame sframe = new ParentMenuEditorFrame(service);
                sframe.setVisible( true );
            }
        });

        // settings
        JMenuItem menuItemSettings = new JMenuItem("Settings", KeyEvent.VK_S );
        menuItemSettings.setIcon(
                  IconUtil.makeImageIcon( IconUtil.SMALL_SETTINGS, "" ) );
        menuItemSettings.getAccessibleContext().
                           setAccessibleDescription( "Settings");
        menuItemSettings.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SettingsFrame sframe = new SettingsFrame( null, config );
                sframe.setVisible( true );
            }
        });


        fileMenu.add( menuItem );
        fileMenu.add( menuItemTemplate );
        fileMenu.add( menuItemUpload );
        fileMenu.add( menuItemUpdate );
        fileMenu.add( menuItemSplash );
        fileMenu.add( menuItemParentMenu );
        fileMenu.add( menuItemSettings );

        return menuBar;
    }


    private JPopupMenu makeCatalogGroupPopupMenu ( ) {

        JPopupMenu treePopupMenu = new JPopupMenu ( );

        JMenuItem addGroupMenu = new JMenuItem ( "Add Group" );
        addGroupMenu.setIcon(
                        IconUtil.makeImageIcon( IconUtil.MEDIUM_ADD, "" ) );
        JMenuItem editGroupMenu = new JMenuItem ( "Edit Group" );
        editGroupMenu.setIcon(
                        IconUtil.makeImageIcon( IconUtil.MEDIUM_EDIT, "" ) );
        JMenuItem removeGroupMenu = new JMenuItem ( "Delete Group" );
        removeGroupMenu.setIcon(
                        IconUtil.makeImageIcon( IconUtil.MEDIUM_REMOVE, "" ) );
        /*
         * Search an item
         *
         */
        JMenuItem searchItemMenu = new JMenuItem ( "Search Item" );
        searchItemMenu.setIcon(
                IconUtil.makeImageIcon( IconUtil.MEDIUM_COG, "" ) );


        JMenuItem addItemMenu = new JMenuItem ( "Add Item" );
        addItemMenu.setIcon(
                IconUtil.makeImageIcon( IconUtil.MEDIUM_COG, "" ) );

        removeGroupMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteGroupMenuActionPerformed(evt);
            }
        });
        addItemMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addItemMenuItemActionPerformed(evt);
            }
        });

        /*
         * Search an item
         *
         */
        searchItemMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchItemMenuItemActionPerformed(evt);
            }
        });
        addGroupMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addGroupMenuActionPerformed(evt);
            }
        });
        editGroupMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editGroupMenuActionPerformed(evt);
            }
        });


        treePopupMenu.add( addGroupMenu );
        treePopupMenu.add( editGroupMenu );
        treePopupMenu.add( removeGroupMenu );
        treePopupMenu.add( searchItemMenu );
        treePopupMenu.add( addItemMenu );


        return treePopupMenu;
    }



    private void setParentGroups ( ) {

        List <ItemGroup>parent = null;
        DefaultMutableTreeNode parentNode = null;

        // dummy node to make tree think it can expand
        CatalogTreeNode dummyNode = new CatalogTreeNode();
        dummyNode.setLabel( "Nothing" );
        dummyNode.setType( "group" );


        // create the root node
        CatalogTreeNode rootNode = new CatalogTreeNode();
        rootNode.setLabel( "Catalog" );
        rootNode.setType( "catalog" );

        DefaultMutableTreeNode root = new DefaultMutableTreeNode ( rootNode );


        // get the parent groups from the session
        try {
            parent = service.findAllChildCategories( new Long(1) ); // 1 = root id
        } catch ( Exception e ) {
            JOptionPane.showMessageDialog( catalogTree,
                    "An error occurred!\n" + e.getMessage(),
                    "Catalog Error",
                    JOptionPane.ERROR_MESSAGE );

            return;
        }

        // loop through the parents and add them to the tree
        for ( int i = 0; i < parent.size(); i++ ) {

            ItemGroup pGroup = parent.get( i );
            //System.out.println ( "Parent group " + pGroup.getId() );

            CatalogTreeNode newNode = new CatalogTreeNode ( );
            newNode.setType( "group" );
            newNode.setLabel( pGroup.getName() );
            newNode.setObject( pGroup );
            newNode.setKey( pGroup.getId() );

            DefaultMutableTreeNode dummy =
                                new DefaultMutableTreeNode ( dummyNode );

            parentNode = new DefaultMutableTreeNode ( newNode );

            parentNode.add( dummy );
            root.add ( parentNode );

        }

        DefaultTreeModel model = (DefaultTreeModel)catalogTree.getModel();

        model.setRoot( root );

        catalogTree.setCellRenderer( new CatalogTreeCellRenderer() );

    } //setParentGroups



    private void catalogTreeMouseClicked(java.awt.event.MouseEvent evt) {

        // get node clicked
        TreePath selPath = catalogTree.getPathForLocation(
                                                    evt.getX(), evt.getY() );

        if ( selPath == null )
            return;

        catalogTree.setSelectionPath( selPath );


        // get node at path and determine if it is an item node
        DefaultMutableTreeNode node =
                 (DefaultMutableTreeNode)selPath.getLastPathComponent();

        CatalogTreeNode cNode = (CatalogTreeNode)node.getUserObject();


        if ( cNode.getType().equals ( "item" ) ) {

            if (evt.getButton() == MouseEvent.BUTTON3 ) {
                //itemPopupMenu.show(
                //        (Component)evt.getSource(), evt.getX(), evt.getY() );

            }

            else if ( evt.getClickCount() >= 2 ) {
                editItemActionPerformed ( null );
            }


        }


        else if ( cNode.getType().equals ( "group" ) ) {
            if (evt.getButton() == MouseEvent.BUTTON3 ) {

                catalogTreeGroupMenu.show(
                        (Component)evt.getSource(), evt.getX(), evt.getY() );

            }
            if (evt.getClickCount() >= 2 ) {
                editGroupMenuActionPerformed ( null );
            }


        }

        else if ( cNode.getType().equals ( "catalog" ) ) {
            if (evt.getButton() == MouseEvent.BUTTON3 ) {
                catalogTreeGroupMenu.show(
                        (Component)evt.getSource(), evt.getX(), evt.getY() );

            }
            if (evt.getClickCount() >= 2 ) {
                setParentGroups ( ); // rebuild the tree
            }
        }



    }


    private ItemGroup getSelectedTreeGroup ( ) {

        // get the selected group
        TreePath selPath = catalogTree.getSelectionPath();
        DefaultMutableTreeNode node =
                 (DefaultMutableTreeNode)selPath.getLastPathComponent();
        CatalogTreeNode cNode = (CatalogTreeNode)node.getUserObject();

        ItemGroup group = (ItemGroup)cNode.getObject();

        if ( group == null ) { // most likely root was clicked

            group = new ItemGroup.Builder().
                            id( new Long(1) ).
                            name( "Store, do not edit" ).build();
        }

        return group;
    }


    private Item getSelectedTreeItem ( ) {

        // get the selected group
        TreePath selPath = catalogTree.getSelectionPath();
        DefaultMutableTreeNode node =
                 (DefaultMutableTreeNode)selPath.getLastPathComponent();
        CatalogTreeNode cNode = (CatalogTreeNode)node.getUserObject();

        Item item = (Item)cNode.getObject();

        return item;
    }

    private void showGroupManager ( ItemGroup group ) {

        rightContent.removeAll();

        GroupManagerPanel form =
                new GroupManagerPanel ( service, config, group, catalogTree );
        rightContent.add( form, BorderLayout.CENTER );
        form.setVisible( true );
        rightContent.revalidate();

    }

    private void showItemManager ( Item item ) {

        rightContent.removeAll();

        ItemManagerPanel form =
                new ItemManagerPanel ( service, config, item, catalogTree );
        rightContent.add( form, BorderLayout.CENTER );
        form.setVisible( true );
        rightContent.revalidate();

    }

    /************************************************************************
     * Actions
     ***********************************************************************/
    private void editGroupMenuActionPerformed( ActionEvent evt ) {

        catalogTree.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        ItemGroup group = getSelectedTreeGroup();

        showGroupManager ( group );
        catalogTree.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    private void addGroupMenuActionPerformed( ActionEvent evt ) {

        ItemGroup group = getSelectedTreeGroup();

        // can this group accept a new group, no items
        int itemsInGroup =
                    service.findProductsForCategory( group.getId() ).size();

        if ( itemsInGroup == 0 ) {

            ItemGroup newGroup = new ItemGroup.Builder().
                                    id( new Long(0) ).
                                    parent( group ).build();

            showGroupManager ( newGroup );

        } else {
            JOptionPane.showMessageDialog(null,
                "This group has items attached to it and can \n" +
                                        "not accept a group at this time",
                "The group could not be added",
                JOptionPane.ERROR_MESSAGE);
        }

    }

    private void deleteGroupMenuActionPerformed( ActionEvent evt ) {

        ItemGroup group = getSelectedTreeGroup();

        // can this group be deleted, no items, no sub groups
        int itemsInGroup =
                    service.findProductsForCategory( group.getId() ).size();
        int subGroups = service.findAllChildCategories( group.getId() ).size();

        if ( itemsInGroup == 0 && subGroups == 0 ) {

            service.removeCategory( group.getId() );
            DefaultMutableTreeNode node = JTreeUtil.getSelectedNode( catalogTree );
            JTreeUtil.removeNode( catalogTree, node, node.getUserObject() );

        } else {
            if ( itemsInGroup > 0 )
                JOptionPane.showMessageDialog(null,
                        "This group has items (" + itemsInGroup +
                                ") attached to it and can \n" +
                                   "not be deleted at this time",
                        "The group could not be deleted",
                        JOptionPane.ERROR_MESSAGE);
            else
                JOptionPane.showMessageDialog(null,
                        "This group has child groups (" + subGroups +
                                ") attached to it and can \n" +
                                    "not be deleted at this time",
                        "The group could not be deleted",
                    JOptionPane.ERROR_MESSAGE);
        }

    }


    private void editItemActionPerformed( ActionEvent evt ) {
        catalogTree.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        Item item = getSelectedTreeItem();

        showItemManager ( item );
        catalogTree.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }


    private void addItemMenuItemActionPerformed ( ActionEvent evt ) {

        ItemGroup group = getSelectedTreeGroup();

        int subGroups = service.findAllChildCategories( group.getId() ).size();

        if ( subGroups != 0 ) {
            JOptionPane.showMessageDialog( catalogTree,
                    "This group cannot accept items!",
                    "Could Not Add Item",
                    JOptionPane.ERROR_MESSAGE);

            return;
        }

        // open dialog
        String skuNum = (String)JOptionPane.showInputDialog(
                            catalogTree,
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
            JOptionPane.showMessageDialog( catalogTree,
                    "The SKU already exsists!\nLook for it under " + skuItem.getGroup().getName(),
                    "SKU Already in Catalog",
                    JOptionPane.ERROR_MESSAGE);

            return;
        }

        skuItem = repository.findItem( skuNum );

        if ( skuItem == null ) {
            JOptionPane.showMessageDialog( catalogTree,
                    "The SKU could not be found!",
                    "SKU Not Found",
                    JOptionPane.ERROR_MESSAGE);
            return;

        }


        // add setup item and add to tree
        skuItem.setGroup( group );
        skuItem.setRelatedItems( new ArrayList<Item>(0) );
        skuItem.setImages( new ArrayList<ItemImage>(0) );

        long millis = System.currentTimeMillis();
        skuItem.setActiveEndDate( new Date ( millis + (60000l * 60l * 24l * 800l) ) );
        skuItem.setActiveStartDate( new Date() );
        if ( skuItem.getSalePrice() == null )
            skuItem.setSalePrice( BigDecimal.ZERO );
        if ( skuItem.getRegularPrice() == null )
            skuItem.setRegularPrice( BigDecimal.ZERO );

        // set sale and retail the same if no sale price
        if ( skuItem.getSalePrice().doubleValue() == 0 ) {
            skuItem.setSalePrice(
                 new BigDecimal ( skuItem.getRegularPrice().doubleValue() ) );
        }

        showItemManager( skuItem );

    }



    /*
     * Search an item
     *
     */
    private void searchItemMenuItemActionPerformed ( ActionEvent evt ) {

        // open dialog
        String skuNum = (String)JOptionPane.showInputDialog(
                            catalogTree,
                            "Enter the item's ERP SKU:",
                "Enter SKU",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                "");

        Item skuItem = null;

        // If a string was returned, say so.
        if ( skuNum != null &&  skuNum.length() > 0 && StringUtils.isNumeric( skuNum ) ) {
            skuItem = service.findItemBySkuNum( skuNum );
        }

        skuItem = repository.findItem( skuNum );

        if ( skuItem == null ) {
            JOptionPane.showMessageDialog( catalogTree,
                    "The SKU could not be found!",
                    "SKU Not Found",
                    JOptionPane.ERROR_MESSAGE);
            return;

        }


        /*
         * @Comment: Unregisters catalog Tree listeners before starting a search
         */
        catalogTree.removeTreeSelectionListener(selectListener);
        catalogTree.removeTreeWillExpandListener(willExpandListener);

        // make cursor busy
        catalogTree.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        setCatalogTree ( );
        DefaultMutableTreeNode skuNode = traverse(catalogTree,skuNum);
        if(skuNode != null) {
            CatalogTreeNode cNode = (CatalogTreeNode)skuNode.getUserObject();
            Item itemSku = (Item)cNode.getObject();
            showItemManager(itemSku);
        }

        isbreak = false; //To come out of recursive and for loop
        catalogTree.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        /*
         * @Comment: Registers back the catalog Tree listeners after a search
         */
        catalogTree.addTreeSelectionListener( selectListener );
        catalogTree.addTreeWillExpandListener(willExpandListener);

    }



    public void scanForChildren(DefaultTreeModel model, DefaultMutableTreeNode root) {

        int cnt = model.getChildCount(root);
        for(int i = 0 ; i< cnt; i++) {
            DefaultMutableTreeNode c = (DefaultMutableTreeNode) root.getChildAt(i);
            CatalogTreeNode node = (CatalogTreeNode) c.getUserObject();
            removeNodesFromParent(model,c);
            if((node.getType()).equals("group")) {
                ItemGroup group = (ItemGroup)node.getObject();
                 List<ItemGroup> children = service.findAllChildCategories(group.getId());
                 addChildGroups_search(children,c);
            }
        }
    }

    public void removeNodesFromParent (
                DefaultTreeModel model, DefaultMutableTreeNode parent ) {


            while ( model.getChildCount( parent ) > 0 ) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode)model.getChild( parent, 0 );
                model.removeNodeFromParent( node );


            }

        }

    /*
     * @Comment : Create sub groups for the parent categories.
     *            Used by Search implementation.
     */
    private void setCatalogTree ( ) {

        DefaultTreeModel model = (DefaultTreeModel)catalogTree.getModel();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode)model.getRoot();
        scanForChildren(model,root);
    }

    public void highlightNode(DefaultMutableTreeNode node, DefaultTreeModel model) {

        TreeNode[] nodes = model.getPathToRoot(node);
        System.out.println("nodes in the tree are ->"+nodes);
        java.util.Vector vec = new java.util.Vector();
        for(int i=0;i<nodes.length;i++) {
            vec.addElement(nodes[i]);
            TreePath path = new TreePath(convertVectorToArray(vec));
            catalogTree.setSelectionPath(path);
            catalogTree.expandPath(path);
            catalogTree.scrollPathToVisible(path);
        }

    }

    public TreeNode[] convertVectorToArray(java.util.Vector vect) {
        TreeNode[] tn = new TreeNode[vect.size()];
        for(int i=0; i < vect.size(); i++) {
            tn[i] = (TreeNode)vect.elementAt(i);
        }
        return tn;
    }

    public DefaultMutableTreeNode traverse(JTree tree,String skuNum) {
        DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
        DefaultMutableTreeNode node = null;
        if (model != null) {

            DefaultMutableTreeNode root = (DefaultMutableTreeNode)model.getRoot();
            System.out.println(root.toString());
            node = walk(model,root,skuNum);
            if(node != null) {
                return node;
            }
        }
        else {
            System.out.println("Tree is empty.");
        }
        return null;
    }

    public DefaultMutableTreeNode walk(DefaultTreeModel model, DefaultMutableTreeNode o,String skuNum) {
        int cc=0;
        int i=0;
        DefaultMutableTreeNode node = null;
        if(isbreak) {
            return null;
        }
        String skuNumber = skuNum;
        cc = model.getChildCount(o);
        for( i=0; i < cc ; i++) {
            if(isbreak) {
                break;
            }
            DefaultMutableTreeNode child = (DefaultMutableTreeNode)model.getChild(o, i );
            if (model.isLeaf(child)) {
                CatalogTreeNode cNode = (CatalogTreeNode)child.getUserObject();
                String cLabel = cNode.getLabel();
                if(cLabel.equals("Nothing")) {
                    removeNodesFromParent_search ( model, child );
                }
                List<Item> childItems = getItemsForLeafNode(child);
                node =  addChildItems_search(childItems,child,skuNumber);
                if(node != null) {
                    isbreak = true;
                    break;
                }

             } else {
                 walk(model,child,skuNumber);
            }
        }
        return null;
    }

    public List<Item> getItemsForLeafNode(DefaultMutableTreeNode childNode) {

        CatalogTreeNode cTNode = (CatalogTreeNode)childNode.getUserObject();
        ItemGroup iGroup = (ItemGroup)cTNode.getObject();
        List<Item> items = service.findProductsForCategory(iGroup.getId());
        if(items != null) {
            return items;
        }
        return null;
    }

    public void removeNodesFromParent_search (
            DefaultTreeModel model, DefaultMutableTreeNode parent ) {


        while ( model.getChildCount( parent ) > 0 ) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)model.getChild( parent, 0 );
            model.removeNodeFromParent( node );


        }

    }


    private DefaultMutableTreeNode addChildItems_search( List<Item> children, DefaultMutableTreeNode parent, String skuNumber ) {

        DefaultMutableTreeNode child = null;

        DefaultTreeModel model = (DefaultTreeModel)catalogTree.getModel();


        for ( int i = 0; i < children.size(); i++ ) {

            // look for any nodes called "Untitled" and select them
            Item item = (Item)children.get(i);

            CatalogTreeNode newNode = new CatalogTreeNode ( );
            newNode.setLabel( item.getName() + ", " + item.getSku() );
            newNode.setType( "item" );
            newNode.setKey( item.getId() );
            newNode.setObject( item );

            child = new DefaultMutableTreeNode( newNode );
            model.insertNodeInto( child, parent, parent.getChildCount() );

            if((item.getSku()).equals(Integer.valueOf(skuNumber))) {
                System.out.println("Found node");
                highlightNode(child,model);
                CatalogTreeNode cNode = (CatalogTreeNode)child.getUserObject();
                Item skuitem = (Item)cNode.getObject();
                showItemManager(skuitem);
                return child;

            }
       }
        return null;
   }

    //Called from search function
    @SuppressWarnings("unused")
    private void addChildGroups_search( List<ItemGroup> children,DefaultMutableTreeNode parent ) {

        DefaultMutableTreeNode child = null;
        DefaultMutableTreeNode dummy = null;

        DefaultTreeModel model = (DefaultTreeModel)catalogTree.getModel();

        // dummy node to make tree think it can expand
        CatalogTreeNode dummyGroup = new CatalogTreeNode();
        dummyGroup.setLabel( "Nothing" );
        dummyGroup.setType( "group" );

        for ( int i = 0; i < children.size(); i++ ) {

            // look for any nodes called "Untitled" and select them
            ItemGroup group = (ItemGroup)children.get(i);

            CatalogTreeNode newNode = new CatalogTreeNode ( );
            //System.out.println("group name is \t" + group.getName());
            newNode.setLabel( group.getName() );
            newNode.setType( "group" );
            newNode.setKey( group.getId() );
            newNode.setObject( group );


            dummy = new DefaultMutableTreeNode( dummyGroup );
            child = new DefaultMutableTreeNode( newNode );
            model.insertNodeInto( child, parent, parent.getChildCount() );
            // add a dummy node to the child we just added
            //model.insertNodeInto( dummy, child, child.getChildCount() );

            DefaultMutableTreeNode c = null;
            ItemGroup group1 = group;
            if(group1 != null) {
                //System.out.println("group1 is  "+group1);
                List<ItemGroup> children1 = service.findAllChildCategories(group1.getId());
                if(children1 != null) {
                    //System.out.println("children1" + children1 + "\t child \t" + child );
                    addChildGroups_search(children1,child);
                }
            }
        }
    }






    /************************************************************************
     *
     * Catalog Tree Logic Starts Here
     *
     ***********************************************************************/
    private class CatalogTreeCellRenderer extends DefaultTreeCellRenderer {

        private static final long serialVersionUID = 1L;

        private ImageIcon groupIcon;
        private ImageIcon rootIcon;
        private ImageIcon itemIcon;

        public CatalogTreeCellRenderer () {

            // set up the icons
            groupIcon = IconUtil.makeImageIcon( IconUtil.SMALL_TREE, "Group" );
            rootIcon = IconUtil.makeImageIcon( IconUtil.SMALL_TREE, "Listing" );
            itemIcon = IconUtil.makeImageIcon( IconUtil.SMALL_PACKAGE, "Item" );
        }

        public Component getTreeCellRendererComponent(  JTree tree,
                                                        Object value,
                                                        boolean sel,
                                                        boolean expanded,
                                                        boolean leaf,
                                                        int row,
                                                        boolean hasFocus) {

            super.getTreeCellRendererComponent( tree, value, sel,
                                                expanded, leaf, row,
                                                hasFocus );

            // get CatalogNode object
            /*DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
            DefaultMutableTreeNode node1 = null;
            if(node.getUserObject() instanceof CatalogTreeNode) {
                System.out.println("Yes " + node.getUserObject());
                node1 = node;
            } else {
                System.out.println("No " + node.getUserObject());
                node1 = (DefaultMutableTreeNode)node.getUserObject();
            }



            CatalogTreeNode cNode = (CatalogTreeNode) node1.getUserObject();*/
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
          CatalogTreeNode cNode = (CatalogTreeNode) node.getUserObject();
            setFont( new java.awt.Font("Dialog", 0, 10) );

            if ( cNode.getType().equals ( "catalog" ) ) {
                setIcon(rootIcon);

            }
            else if ( cNode.getType().equals( "group" ) ) {

                setIcon(groupIcon);
            }
            else if ( cNode.getType().equals ( "item" ) ) {
                setIcon(itemIcon);
            }


            return this;
        }

    }


    private class CatalogTreeSelectionListener implements TreeSelectionListener {

        /**
         * Respond to user node selections
         **/
        public void valueChanged( TreeSelectionEvent e ) {

        }

    }


    private class CatalogTreeWillExpandListener
                                        implements TreeWillExpandListener {

        public void treeWillExpand( TreeExpansionEvent event )
                                                throws ExpandVetoException {


            List<ItemGroup> childGroups = null;
            List<Item> childItems = null;

            // make cursor busy
            catalogTree.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));


            DefaultTreeModel model = (DefaultTreeModel)catalogTree.getModel();

            // get the node to insert at and the parent node object
            DefaultMutableTreeNode parent =
                (DefaultMutableTreeNode)event.getPath().getLastPathComponent();


            CatalogTreeNode parentNode =
                                    (CatalogTreeNode)parent.getUserObject();


            // clear out the dummy nodes and new real ones
            removeNodesFromParent ( model, parent );

            // so what type is this node and what is under it, item?, group?
            String parentType = parentNode.getType();
            //String childType = "";

            if ( parentType.equals( "group" ) ) {

                // does this group have sub groups or items?
                ItemGroup group = (ItemGroup)parentNode.getObject();


                // try to get children
                try {
                    childGroups =
                            service.findAllChildCategories( group.getId());
                } catch ( Exception e ) {
                    JOptionPane.showMessageDialog( catalogTree,
                            "An error occurred!\n" + e.getMessage(),
                            "Catalog Error",
                            JOptionPane.ERROR_MESSAGE );
                    return;
                }

                if ( childGroups.size() > 0 ) {
                    //childType = "group";

                    // add those children
                    addChildGroups ( childGroups, parent );

                    catalogTree.setCursor(
                            Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    return;

                }



                // try to get items assigned to this group
                try {
                    childItems = service.findProductsForCategory( group.getId() );
                } catch ( Exception e ) {
                    JOptionPane.showMessageDialog( catalogTree,
                            "An error occurred!\n" + e.getMessage(),
                            "Catalog Error",
                            JOptionPane.ERROR_MESSAGE );

                    return;
                }

                if ( childItems.size() > 0 ) {
                    //childType = "item";

                    // add those children
                    addChildItems ( childItems, parent );

                    catalogTree.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    return;

                }


            }

            catalogTree.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

        } //treeWillExpand



        public void removeNodesFromParent (
                DefaultTreeModel model, DefaultMutableTreeNode parent ) {


            while ( model.getChildCount( parent ) > 0 ) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode)model.getChild( parent, 0 );
                model.removeNodeFromParent( node );


            }

        }



        public void treeWillCollapse(TreeExpansionEvent event)
        throws ExpandVetoException {
        }




        /***
         *
         * Help function to add child group nodes to parent
         *
         ***/
        private void addChildGroups( List<ItemGroup> children,
                DefaultMutableTreeNode parent ) {

            DefaultMutableTreeNode child = null;
            DefaultMutableTreeNode dummy = null;

            DefaultTreeModel model = (DefaultTreeModel)catalogTree.getModel();

            // dummy node to make tree think it can expand
            CatalogTreeNode dummyGroup = new CatalogTreeNode();
            dummyGroup.setLabel( "Nothing" );
            dummyGroup.setType( "group" );

            for ( int i = 0; i < children.size(); i++ ) {

                // look for any nodes called "Untitled" and select them
                ItemGroup group = (ItemGroup)children.get(i);

                CatalogTreeNode newNode = new CatalogTreeNode ( );
                newNode.setLabel( group.getName() );
                newNode.setType( "group" );
                newNode.setKey( group.getId() );
                newNode.setObject( group );


                dummy = new DefaultMutableTreeNode( dummyGroup );
                child = new DefaultMutableTreeNode( newNode );


                model.insertNodeInto( child, parent, parent.getChildCount() );

                // add a dummy node to the child we just added
                model.insertNodeInto( dummy, child, child.getChildCount() );



            }

        }



        /***
         *
         * Help function to add child item nodes to parent
         *
         ***/
        private void addChildItems( List<Item> children,
                                        DefaultMutableTreeNode parent ) {

            DefaultMutableTreeNode child = null;

            DefaultTreeModel model = (DefaultTreeModel)catalogTree.getModel();


            for ( int i = 0; i < children.size(); i++ ) {

                // look for any nodes called "Untitled" and select them
                Item item = (Item)children.get(i);

                CatalogTreeNode newNode = new CatalogTreeNode ( );
                newNode.setLabel( item.getName() + ", " + item.getSku() );
                newNode.setType( "item" );
                newNode.setKey( item.getId() );
                newNode.setObject( item );

                child = new DefaultMutableTreeNode( newNode );


                model.insertNodeInto( child, parent, parent.getChildCount() );


            }

        }
    }


    private String getCatalogAdminVersion ( ) {
        String version = "";

        Package pkg = CatalogFrame.class.getPackage();
        version = pkg.getSpecificationVersion();

        if ( version == null )
            version = "unknown";

        return version;
    }

}
