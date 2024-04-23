package com.probuild.retail.web.catalog.desktop;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.probuild.retail.web.catalog.desktop.support.Configuration;
import com.probuild.retail.web.catalog.desktop.util.IconUtil;
import com.probuild.retail.web.catalog.domain.Item;
import com.probuild.retail.web.catalog.repository.ItemRepository;
import com.probuild.retail.web.catalog.upload.BulkImporter;
import com.probuild.retail.web.catalog.upload.XlsBulkImporter;

import net.miginfocom.swing.MigLayout;

public class SettingsFrame extends JDialog {
    private static final Logger logger = 
                    LoggerFactory.getLogger(SettingsFrame.class);
    
    private static final long serialVersionUID = 1L;

    private JTextField imgFolderFld;
    private JTextField catalogUserFld;
    private JPasswordField catalogPasswordFld;
    
    private JTextField legacyUserFld;
    private JPasswordField legacyPasswordFld;
    private JTextField legacyUrlFld;
    
    @SuppressWarnings("unused")
    private boolean processing = false;
    
    private Configuration config;
    
    
    /**
     *	Default constructor
     */
    public SettingsFrame( JFrame parent, Configuration config ) {
        super(parent, true);
        
        this.config = config;

        init();
    }

    private void init ( ) {
        
        Container contentPane = getContentPane();
        
        JPanel layout = new JPanel ( new MigLayout( "fill" ) );

        JLabel titleLbl = new JLabel ( 
                "Settings", 
                IconUtil.makeImageIcon( IconUtil.SETTINGS, "" ),
                SwingConstants.RIGHT );
        titleLbl.setFont( titleLbl.getFont().deriveFont( 16f ) );
        
        
        layout.add( titleLbl, "wrap" );
        layout.add( makeSettingsPanel(), "wrap" );
        
        layout.add( makeServiceUserPanel(), "wrap" );
        
        layout.add( makeLegacyUserPanel(), "wrap" );
        
        JButton saveBtn = new JButton ( "Save" );
        saveBtn.addActionListener( new ActionListener( ) {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                config.setImageFolder( imgFolderFld.getText() );
                config.setCatalogUser( catalogUserFld.getText() );
                config.setCatalogUserPass( new String( catalogPasswordFld.getPassword() ) );
                config.setLegacyUser( legacyUserFld.getText() );
                config.setLegacyUserPass( new String ( legacyPasswordFld.getPassword() ) );
                config.setLegacyConnectionString( legacyUrlFld.getText() );
                config.saveConfig();
                closeFrame ( );
            } 
        });
        
        layout.add( saveBtn );
                
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setTitle( "Settings" );
        setLayout( new BorderLayout() );
        
        //add( layout, BorderLayout.CENTER );
        contentPane.add( layout, BorderLayout.CENTER );
        
        //setIconImage( IconUtil.makeImageIcon( 
        //                            IconUtil.BOOK, "Catalog" ).getImage() );
        
        
        pack();
        
    }
    
    

    public String showDialog ( ) {
        setVisible( true );
        
        return "done";
    }
    private JPanel makeSettingsPanel ( ) {

        JPanel layout = new JPanel ( new MigLayout ( ) );
        
        
        imgFolderFld = new JTextField ( );
        imgFolderFld.setColumns( 20 );
        imgFolderFld.setText( config.getImageFolder() );
        
        JPanel imgFolderPanel = new JPanel ( new MigLayout() );
        imgFolderPanel.add( new JLabel ( "Local Image Folder: " ) );
        imgFolderPanel.add( imgFolderFld );
        
        JButton browseBtn = new JButton ( "Locate" );
        browseBtn.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                JFileChooser chooser = new JFileChooser ( );
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int choice = chooser.showOpenDialog( null );
                
                if ( choice == JFileChooser.APPROVE_OPTION ) {
                    File file = chooser.getSelectedFile();

                    imgFolderFld.setText( file.getPath() );
                }
            } 
        });

        layout.add( imgFolderPanel );
        layout.add( browseBtn );
        
        return layout;
    }
    
    
    
    private JPanel makeServiceUserPanel ( ) {

        JPanel layout = new JPanel ( new MigLayout ( ) );
        layout.setBorder( BorderFactory.createTitledBorder( "Catalog Connection" ) );
        
        JLabel userLabel = new JLabel ( "User:" );
        catalogUserFld = new JTextField ( );
        catalogUserFld.setColumns( 10 );
        catalogUserFld.setText( config.getCatalogUser() );
        
        JLabel passwordLabel = new JLabel ( "Password:" );
        catalogPasswordFld = new JPasswordField ( );
        catalogPasswordFld.setColumns( 10 );
        catalogPasswordFld.setText( config.getCatalogUserPass() );

        layout.add( userLabel );
        layout.add( catalogUserFld );
        layout.add( passwordLabel );
        layout.add( catalogPasswordFld );
        
        return layout;
    }

    
    private JPanel makeLegacyUserPanel ( ) {

        JPanel layout = new JPanel ( new MigLayout ( ) );
        layout.setBorder( BorderFactory.createTitledBorder( "Legacy Connection" ) );
        
        JLabel urlLabel = new JLabel ( "JDBC URL:" );
        legacyUrlFld = new JTextField ( );
        legacyUrlFld.setColumns( 35 );
        legacyUrlFld.setText( config.getLegacyConnectionString() );
        
        JLabel userLabel = new JLabel ( "User:" );
        legacyUserFld = new JTextField ( );
        legacyUserFld.setColumns( 10 );
        legacyUserFld.setText( config.getLegacyUser() );
        
        JLabel passwordLabel = new JLabel ( "Password:" );
        legacyPasswordFld = new JPasswordField ( );
        legacyPasswordFld.setColumns( 10 );
        legacyPasswordFld.setText( config.getLegacyUserPass() );

        layout.add( urlLabel );
        layout.add( legacyUrlFld, "wrap, span 3" );
        layout.add( userLabel );
        layout.add( legacyUserFld );
        layout.add( passwordLabel );
        layout.add( legacyPasswordFld );
        
        return layout;
    }
    
    /****************************************************************
     * Actions
     ***************************************************************/
    private void chooseFileActionSkuRange ( ActionEvent evt ) {
        

    }
    
    private void closeFrame (  ) {
        this.setVisible( false );
        this.dispose();
    }

}
