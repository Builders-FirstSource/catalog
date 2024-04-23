package com.probuild.retail.web.catalog.desktop;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.tree.DefaultMutableTreeNode;

import org.netbeans.validation.api.Problem;
import org.netbeans.validation.api.builtin.Validators;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.netbeans.validation.api.ui.ValidationPanel;

import com.probuild.retail.web.catalog.desktop.support.CatalogTreeNode;
import com.probuild.retail.web.catalog.desktop.support.Configuration;
import com.probuild.retail.web.catalog.desktop.support.ImageFilter;
import com.probuild.retail.web.catalog.desktop.support.ImagePreview;
import com.probuild.retail.web.catalog.desktop.support.JTreeUtil;
import com.probuild.retail.web.catalog.desktop.util.FileUtil;
import com.probuild.retail.web.catalog.desktop.util.IconUtil;
import com.probuild.retail.web.catalog.domain.ItemGroup;
import com.probuild.retail.web.catalog.domain.ItemGroupSelect;
import com.probuild.retail.web.catalog.ext.service.WebCatalogService;

import net.miginfocom.swing.MigLayout;

public class GroupManagerPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private String IMG_MIRROR_DIR = "c:/java_dev/catalog/products";

    private ItemGroup group;
    private WebCatalogService service;
    private JTree catalogTree;
    private Configuration config;

    private JFileChooser fc;

    private int itemsInGroup;
    private int subGroups;

    private JLabel groupImgLbl;
    private JPanel groupImgPanel;

    private JTextField nameFld;
    private JComboBox parentGroupCombo;
    private ValidationPanel validatorPanel;
    private JLabel errorLbl;

    private DefaultMutableTreeNode selectedNode;

    /**
     *	Default constructor
     */
    public GroupManagerPanel() {
        super();
    }

    public GroupManagerPanel( WebCatalogService service,
                              Configuration config,
                              ItemGroup group,
                              JTree catalogTree ) {
        super();
        this.group = group;
        this.service = service;
        this.catalogTree = catalogTree;
        this.config = config;

        IMG_MIRROR_DIR = config.getImageFolder();

        // hold onto the selected node in case user changes while editing
        this.selectedNode = JTreeUtil.getSelectedNode( catalogTree );


        if ( group.getId() != null && !group.getId().equals( new Long(0) ) ) {
            itemsInGroup = service.findProductsForCategory( group.getId() ).size();
            subGroups = service.findAllChildCategories( group.getId() ).size();
        } else {
            itemsInGroup = 0;
            subGroups = 0;
        }

        init ( );
    }

    @SuppressWarnings("unchecked")
    public void init ( ) {

        this.setLayout( new BorderLayout() );

        // create an instance of jfilechooser
        fc = new JFileChooser();
        fc.addChoosableFileFilter( new ImageFilter( ) );
        fc.setAcceptAllFileFilterUsed ( false );
        fc.setAccessory ( new ImagePreview( fc ) );
        fc.setMultiSelectionEnabled( true );

        JPanel layoutPanel = new JPanel ( new MigLayout ( "fill" ) );

        JPanel formPanel = new JPanel ( new MigLayout( ) );

        JLabel titleLbl = new JLabel (
                        "Group Management",
                        IconUtil.makeImageIcon( IconUtil.FILE_MANAGER, "" ),
                        SwingConstants.RIGHT );
        titleLbl.setFont( titleLbl.getFont().deriveFont( 16f ) );

        JLabel idValueLbl = new JLabel ( group.getId().toString() );
        idValueLbl.setFont( idValueLbl.getFont().deriveFont(Font.PLAIN) );
        idValueLbl.setForeground( Color.BLUE );

        JLabel imagePathLbl = new JLabel ( group.getImage() );
        imagePathLbl.setFont( imagePathLbl.getFont().deriveFont(Font.PLAIN) );
        imagePathLbl.setForeground( Color.BLUE );

        JLabel itemsCountLbl = new JLabel ( Integer.toString( itemsInGroup ) );
        itemsCountLbl.setFont( itemsCountLbl.getFont().deriveFont(Font.PLAIN) );
        itemsCountLbl.setForeground( Color.BLUE );

        JLabel groupCountLbl = new JLabel ( Integer.toString( subGroups ) );
        groupCountLbl.setFont( groupCountLbl.getFont().deriveFont(Font.PLAIN) );
        groupCountLbl.setForeground( Color.BLUE );

        nameFld = new JTextField ( );
        nameFld.setName( "Category Name" );
        nameFld.setText( group.getName() );
        nameFld.setColumns( 40 );

        parentGroupCombo = makeParentGroupsCombo();

        groupImgPanel = new JPanel ( new BorderLayout() );
        groupImgLbl = createImage();
        groupImgPanel.add( groupImgLbl, BorderLayout.CENTER );

        JButton browseBtn = new JButton ( "Browse" );
        browseBtn.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chooseImageAction( evt );
            }
        });

        formPanel.add( titleLbl, "span, wrap" );
        formPanel.add( new JLabel ( "Internal Id" ), "right" );
        formPanel.add( idValueLbl, "left, span, wrap" );
        formPanel.add( new JLabel ( "Parent" ), "right" );
        formPanel.add( parentGroupCombo, "left, span, wrap" );
        formPanel.add( new JLabel ( "Name" ), "right" );
        formPanel.add( nameFld, "left, span, wrap" );
        formPanel.add( new JLabel ( "Image" ), "right" );
        formPanel.add( groupImgPanel, "left" );
        formPanel.add( browseBtn, "left, wrap" );
        formPanel.add( new JLabel ( "Image Path" ), "right" );
        formPanel.add( imagePathLbl, "left, span, wrap" );
        formPanel.add( new JLabel ( "Items in Group" ), "right" );
        formPanel.add( itemsCountLbl, "left, span, wrap" );
        formPanel.add( new JLabel ( "Child Groups" ), "right" );
        formPanel.add( groupCountLbl, "left, span, wrap" );

        // buttons that appear at lower right corner
        JPanel actionBtnPanel = makeActionButtonPanel();

        layoutPanel.add ( formPanel, "grow, wrap, push" );
        layoutPanel.add( actionBtnPanel, "right" );

        // setup form validation
        validatorPanel = new ValidationPanel ( );
        validatorPanel.setInnerComponent( layoutPanel );
        ValidationGroup validatorGroup = validatorPanel.getValidationGroup();
        validatorGroup.add( nameFld, Validators.REQUIRE_NON_EMPTY_STRING,
                                     Validators.maxLength(50) );

        this.add( layoutPanel, BorderLayout.CENTER );

    }

    private JLabel createImage ( ) {
        byte[] imgBytes = service.readImage( "", group.getImage() );

        if ( imgBytes == null ) {
            JLabel label =  new JLabel ( "...not found" );
            label.setFont( label.getFont().deriveFont(Font.PLAIN) );
            return label;
        }

        ImageIcon icon = new ImageIcon ( imgBytes );
        JLabel pic = new JLabel ( icon );

        return pic;
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
                saveGroupAction(evt);
            }
        });
        deleteBtn.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteGroupAction(evt);
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

        List<ItemGroup> allGroups = service.findAllCategories();

        DefaultComboBoxModel model = new DefaultComboBoxModel();
        int index = 0, count = 0;
        for ( ItemGroup grp : allGroups ) {
            model.addElement( new ItemGroupSelect(grp) );

            if ( group.getParent() != null &&
                    grp.getId().equals( group.getParent().getId() ) ) {
                index = count;
                System.out.println ( "Parent group: " + group.getParent().getId() );
            }

            count++;
        }

        JComboBox combo = new JComboBox ( model );
        combo.setSelectedIndex( index );


        return combo;
    }


    /****************************************************************
     * Actions
     ***************************************************************/
    private void saveGroupAction ( ActionEvent evt ) {

        ItemGroup changedGroup = new ItemGroup ( );
        changedGroup.setId( group.getId() );

        // double check parent to make sure it can accept a group
        ItemGroup parent = (ItemGroup)parentGroupCombo.getSelectedItem();
        int parentItemCount =
                service.findProductsForCategory( parent.getId() ).size();

        if ( parentItemCount > 0 ) {
            JOptionPane.showMessageDialog(null,
                    "The parent has items attached to it and can \n" +
                                            "not accept a group at this time",
                    "The parent group could not be used",
                    JOptionPane.ERROR_MESSAGE);

            return;
        } else {
            changedGroup.setParent( parent );
        }


        Problem problem = validatorPanel.getValidationGroup().validateAll();
        if ( problem != null ) {
            errorLbl.setText("<html><font color=\"red\">" +
                                    problem.getMessage() + "</font></html>" );
            errorLbl.setVisible( true );
            return;
        }

        changedGroup.setName( nameFld.getText() );
        changedGroup.defaultDates();
        changedGroup.setImage( group.getImage() );

        ItemGroup savedGroup = null;
        try {
            savedGroup = service.saveItemGroup( changedGroup );
        } catch ( Exception e ) {
            JOptionPane.showMessageDialog(
                    this,
                    "Error saving group!\n" + e.getMessage(),
                    "Error Saving Group",
                    JOptionPane.ERROR_MESSAGE );
            return;
        }

        //System.out.println ( "Saved group " + savedGroup.getId() );

        // update catalog tree
        CatalogTreeNode node = new CatalogTreeNode.Builder().
                                    label( savedGroup.getName() ).
                                    object( savedGroup ).
                                    key( savedGroup.getId() ).
                                    type( "group" ).
                                    build();

        if ( changedGroup.getId().longValue() == 0 ) { // this was a new node

            JTreeUtil.addNode( catalogTree, selectedNode, node );

        } else if ( group.getParent().getId().longValue() ==
                        changedGroup.getParent().getId().longValue() ) {

            JTreeUtil.updateNode( catalogTree, selectedNode, node );

        } else { // parent was changed during update

            CatalogTreeNode parentDataNode = new CatalogTreeNode.Builder()
                                        .label( parent.getName() )
                                        .object( parent )
                                        .key( parent.getId() )
                                        .type( "group" )
                                        .build();

            DefaultMutableTreeNode parentNode =
                        JTreeUtil.findNode( catalogTree, parentDataNode );
            JTreeUtil.removeNode( catalogTree, selectedNode, node );

            if ( parentNode != null ) // parent node is displayed currently
                JTreeUtil.addNode( catalogTree, parentNode, node );

        }
        // display message, remove form
        clearPanel ( "Group '" + savedGroup.getName() + "' has been saved" );


    }

    private void deleteGroupAction ( ActionEvent evt ) {

        int choice = JOptionPane.showConfirmDialog(
                        this, "Delete " + group.toString() + "?" );
        if ( choice != JOptionPane.OK_OPTION )
            return;

        // from early check, if delete enabled ok to proceed here
        try {
            service.removeCategory( group.getId() );
        } catch ( Exception e ) {
            JOptionPane.showMessageDialog(
                    this,
                    "Error deleting group!\n" + e.getMessage(),
                    "Error Deleting Group",
                    JOptionPane.ERROR_MESSAGE );
            return;
        }

        // update catalog tree
        CatalogTreeNode node = new CatalogTreeNode.Builder().
                                    label( group.getName() ).
                                    object( group ).
                                    key( group.getId() ).
                                    type( "group" ).
                                    build();

        JTreeUtil.removeNode( catalogTree, selectedNode, node );

        // display message, remove form
        clearPanel ( "Group '" + group.getName() + "' has been removed" );


    }

    private void cancelAction ( ActionEvent evt ) {
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


    /**
     * User picks an image from either small or large folder. Both
     * small and large are copied to server if they do not already
     * exist there.
     * @param evt
     */
    private void chooseImageAction ( ActionEvent evt ) {

        fc.setCurrentDirectory( new File ( IMG_MIRROR_DIR ) );

        int returnVal = fc.showDialog( this, "Add Image" );

        // if the user approved, process the files
        if ( returnVal == JFileChooser.APPROVE_OPTION ) {

            // get the selected files
            File[] fileList = fc.getSelectedFiles();

            for ( File imgFile : fileList ) {
                // need to send both a high and low resolution image
                String smallImgPath = "/images/products/small/" + imgFile.getName();
                String largeImgPath = "/images/products/large/" + imgFile.getName();


                if ( !service.imageExists( "", smallImgPath ) ) {

                    byte[] smallBytes = FileUtil.getFileBytes (
                              IMG_MIRROR_DIR + "/small/" + imgFile.getName() );
                    byte[] largeBytes = FileUtil.getFileBytes (
                              IMG_MIRROR_DIR + "/large/" + imgFile.getName() );

                    if ( smallBytes == null || largeBytes == null )
                        continue; // need an error message here

                    //long smallByteCount = service.sendImage( group.getId(), "small", 'P',
                    //        smallImgPath, smallBytes, 'Y' );

                    //long largeByteCount = service.sendImage( group.getId(), "large", 'P',
                    //        largeImgPath, largeBytes, 'Y' );

                    //if ( smallByteCount == 0 || largeByteCount == 0 )
                    if ( smallBytes.length == 0 || largeBytes.length == 0 )
                        continue; // need and error


                }

                group.setImage( smallImgPath );

            }


        }

        groupImgPanel.setVisible( false );
        groupImgPanel.removeAll();
        groupImgPanel.add( createImage(), BorderLayout.CENTER );
        groupImgPanel.revalidate();
        groupImgPanel.setVisible( true );

    }
}
