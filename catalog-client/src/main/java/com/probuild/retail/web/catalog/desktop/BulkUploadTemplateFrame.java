package com.probuild.retail.web.catalog.desktop;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.apache.commons.lang.StringUtils;
import org.netbeans.validation.api.Problem;
import org.netbeans.validation.api.builtin.Validators;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.netbeans.validation.api.ui.ValidationPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.probuild.retail.web.catalog.desktop.support.Configuration;
import com.probuild.retail.web.catalog.desktop.util.IconUtil;
import com.probuild.retail.web.catalog.domain.Item;
import com.probuild.retail.web.catalog.repository.ItemRepository;
import com.probuild.retail.web.catalog.upload.BulkImporter;
import com.probuild.retail.web.catalog.upload.XlsBulkImporter;

import net.miginfocom.swing.MigLayout;

public class BulkUploadTemplateFrame extends JFrame {
    private static final Logger logger = 
                    LoggerFactory.getLogger(BulkUploadTemplateFrame.class);
    
    private static final long serialVersionUID = 1L;

    private ItemRepository repository;
    
    //private JLabel errorLbl;
    
    private JProgressBar progressBar;
    
    JTextField startSkuFld;
    JTextField endSkuFld;
    JTextField start1SkuFld;
    JTextField end1SkuFld;
    JTextField start2SkuFld;
    JTextField end2SkuFld;
    JTextField start3SkuFld;
    JTextField end3SkuFld;
    JTextField deptFld;
    JTextField manufFld;
    JTextField vendFld;
    
    private ValidationPanel skuValidatorPanel;
    private ValidationPanel manufValidatorPanel;
    private ValidationPanel vendValidatorPanel;
    private ValidationPanel deptValidatorPanel;
    private JLabel skuErrorLbl;
    private JLabel manufErrorLbl;
    private JLabel vendErrorLbl;
    private JLabel deptErrorLbl;
    
    private File xlsItemFile; // source data speadsheet 
    
    String startSku;
    String endSku; 
    String dept;
    String manuf;
    String vend;
    
    @SuppressWarnings("unused")
    private boolean processing = false;
    
    /**
     *	Default constructor
     */
    public BulkUploadTemplateFrame( ItemRepository repository ) {
        super();
        
        this.repository = repository;
        
        init();
    }

    private void init ( ) {
        
        JPanel layout = new JPanel ( new MigLayout( "fill" ) );

        JLabel titleLbl = new JLabel ( 
                "Bulk Item Import Template Creator", 
                IconUtil.makeImageIcon( IconUtil.FILE_MANAGER, "" ),
                SwingConstants.RIGHT );
        titleLbl.setFont( titleLbl.getFont().deriveFont( 16f ) );
        
        progressBar = new JProgressBar();
        progressBar.setString( "Waiting for input" );
        
        layout.add( titleLbl, "wrap" );
        layout.add( makeSkuRangePanel(), "" );
        layout.add( makeDeptSkuRangePanel(), "wrap" );
        layout.add( makeManufSkuRangePanel(), "" );
        layout.add( makeVendSkuRangePanel(), "wrap" );
        
        layout.add( progressBar, "wrap" );
                
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setTitle( "Create Item Import Template" );
        setLayout( new BorderLayout() );
        add( layout, BorderLayout.CENTER );
        
        setIconImage( IconUtil.makeImageIcon( 
                                    IconUtil.BOOK, "Catalog" ).getImage() );
        
        // pack the frame
        //this.setPreferredSize( new Dimension ( 600, 400 ) );
        pack();
        
    }

    
    private JPanel makeSkuRangePanel ( ) {

        JPanel layout = new JPanel ( new MigLayout ( ) );
        layout.setBorder( BorderFactory.createTitledBorder( "SKU Range" ) );

        skuErrorLbl = new JLabel ( );
        skuErrorLbl.setIcon( 
                IconUtil.makeImageIcon( IconUtil.MEDIUM_WARNING, "") );
        skuErrorLbl.setVisible( false );
        
        JLabel title = new JLabel ( "Import Template by SKU Range" );
        
        startSkuFld = new JTextField ( );
        startSkuFld.setName( "Starting SKU" );
        startSkuFld.setColumns( 8 );
        endSkuFld = new JTextField ( );
        endSkuFld.setName( "Ending SKU" );
        endSkuFld.setColumns( 8 );
        
        JPanel rangePanel = new JPanel ( new MigLayout() );
        rangePanel.add( new JLabel ( "Starting SKU:" ) );
        rangePanel.add( startSkuFld );
        rangePanel.add( new JLabel ( "Ending SKU: " ) );
        rangePanel.add( endSkuFld );
        
        JButton createBtn = new JButton ( "Create Template" );
        createBtn.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chooseFileActionSkuRange(evt);
            } 
        });
        
        layout.add( title, "wrap" );
        layout.add( rangePanel, "wrap" );
        layout.add( createBtn, "wrap" );
        layout.add( skuErrorLbl );
        
        // setup form validation
        skuValidatorPanel = new ValidationPanel ( );
        skuValidatorPanel.setInnerComponent( layout );
        ValidationGroup validatorGroup = skuValidatorPanel.getValidationGroup();
        validatorGroup.add( startSkuFld, Validators.REQUIRE_NON_NEGATIVE_NUMBER, 
                                         Validators.REQUIRE_NON_EMPTY_STRING );
        validatorGroup.add( endSkuFld, Validators.REQUIRE_NON_NEGATIVE_NUMBER, 
                                       Validators.REQUIRE_NON_EMPTY_STRING );
        
        return layout;
    }
        
    private JPanel makeDeptSkuRangePanel ( ) {

        JPanel layout = new JPanel ( new MigLayout ( ) );
        layout.setBorder( BorderFactory.createTitledBorder( "Department SKU Range" ) );

        deptErrorLbl = new JLabel ( );
        deptErrorLbl.setIcon( 
                IconUtil.makeImageIcon( IconUtil.MEDIUM_WARNING, "") );
        deptErrorLbl.setVisible( false );
        
        JLabel title = new JLabel ( "Import Template by Department/SKU Range" );
        
        start1SkuFld = new JTextField ( );
        start1SkuFld.setName( "Starting SKU" );
        start1SkuFld.setColumns( 8 );
        end1SkuFld = new JTextField ( );
        end1SkuFld.setName( "Ending SKU" );
        end1SkuFld.setColumns( 8 );
        deptFld = new JTextField ( );
        deptFld.setName( "Department Number" );
        deptFld.setColumns( 10 );
        
        JPanel rangePanel = new JPanel ( new MigLayout() );
        rangePanel.add( new JLabel ( "Department: " ) );
        rangePanel.add( deptFld, "span, wrap" );
        rangePanel.add( new JLabel ( "Starting SKU:" ) );
        rangePanel.add( start1SkuFld );
        rangePanel.add( new JLabel ( "Ending SKU: " ) );
        rangePanel.add( end1SkuFld );
        
        JButton createBtn = new JButton ( "Create Template" );
        createBtn.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chooseFileActionDeptSkuRange(evt);
            } 
        });
        
        layout.add( title, "wrap" );
        layout.add( rangePanel, "wrap" );
        layout.add( createBtn, "wrap" );
        layout.add( deptErrorLbl );
        
        // setup form validation
        deptValidatorPanel = new ValidationPanel ( );
        deptValidatorPanel.setInnerComponent( layout );
        ValidationGroup validatorGroup = deptValidatorPanel.getValidationGroup();
        validatorGroup.add( start1SkuFld, Validators.REQUIRE_NON_NEGATIVE_NUMBER, 
                                          Validators.REQUIRE_NON_EMPTY_STRING );
        validatorGroup.add( end1SkuFld, Validators.REQUIRE_NON_NEGATIVE_NUMBER,
                                        Validators.REQUIRE_NON_EMPTY_STRING );
        validatorGroup.add( deptFld, Validators.REQUIRE_NON_NEGATIVE_NUMBER,
                                     Validators.REQUIRE_NON_EMPTY_STRING );
        
        return layout;
    }
    
    
    private JPanel makeManufSkuRangePanel ( ) {

        JPanel layout = new JPanel ( new MigLayout ( ) );
        layout.setBorder( BorderFactory.createTitledBorder( "Manufacturer SKU Range" ) );
        
        manufErrorLbl = new JLabel ( );
        manufErrorLbl.setIcon( 
                IconUtil.makeImageIcon( IconUtil.MEDIUM_WARNING, "") );
        manufErrorLbl.setVisible( false );
        
        JLabel title = new JLabel ( "Import Template by Manufacturer/SKU Range" );
        
        start3SkuFld = new JTextField ( );
        start3SkuFld.setName( "Starting SKU" );
        start3SkuFld.setColumns( 8 );
        end3SkuFld = new JTextField ( );
        end3SkuFld.setName( "Ending SKU" );
        end3SkuFld.setColumns( 8 );
        manufFld = new JTextField ( );
        manufFld.setName( "Manufacturer Number" );
        manufFld.setColumns( 10 );
        
        JPanel rangePanel = new JPanel ( new MigLayout() );
        rangePanel.add( new JLabel ( "Manufacturer: " ) );
        rangePanel.add( manufFld, "span, wrap" );
        rangePanel.add( new JLabel ( "Starting SKU:" ) );
        rangePanel.add( start3SkuFld );
        rangePanel.add( new JLabel ( "Ending SKU: " ) );
        rangePanel.add( end3SkuFld );
        
        JButton createBtn = new JButton ( "Create Template" );
        createBtn.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chooseFileActionManufSkuRange(evt);
            } 
        });
        
        layout.add( title, "wrap" );
        layout.add( rangePanel, "wrap" );
        layout.add( createBtn, "wrap" );
        layout.add( manufErrorLbl );
        
        // setup form validation
        manufValidatorPanel = new ValidationPanel ( );
        manufValidatorPanel.setInnerComponent( layout );
        ValidationGroup validatorGroup = manufValidatorPanel.getValidationGroup();
        validatorGroup.add( start3SkuFld, Validators.REQUIRE_NON_NEGATIVE_NUMBER,
                                          Validators.REQUIRE_NON_EMPTY_STRING );
        validatorGroup.add( end3SkuFld, Validators.REQUIRE_NON_NEGATIVE_NUMBER, 
                                        Validators.REQUIRE_NON_EMPTY_STRING );
        validatorGroup.add( manufFld, Validators.REQUIRE_NON_NEGATIVE_NUMBER,
                                        Validators.REQUIRE_NON_EMPTY_STRING );
        
        return layout;
    }
    
    
    private JPanel makeVendSkuRangePanel ( ) {

        JPanel layout = new JPanel ( new MigLayout ( ) );
        layout.setBorder( BorderFactory.createTitledBorder( "Vendor SKU Range" ) );
        
        vendErrorLbl = new JLabel ( );
        vendErrorLbl.setIcon( 
                IconUtil.makeImageIcon( IconUtil.MEDIUM_WARNING, "") );
        vendErrorLbl.setVisible( false );
        
        JLabel title = new JLabel ( "Import Template by Vendor/SKU Range" );
        
        start2SkuFld = new JTextField ( );
        start2SkuFld.setName( "Starting SKU" );
        start2SkuFld.setColumns( 8 );
        end2SkuFld = new JTextField ( );
        end2SkuFld.setName( "Ending SKU" );
        end2SkuFld.setColumns( 8 );
        vendFld = new JTextField ( );
        vendFld.setName( "Vendor Number" );
        vendFld.setColumns( 10 );
        
        JPanel rangePanel = new JPanel ( new MigLayout() );
        rangePanel.add( new JLabel ( "Vendor: " ) );
        rangePanel.add( vendFld, "span, wrap" );
        rangePanel.add( new JLabel ( "Starting SKU:" ) );
        rangePanel.add( start2SkuFld );
        rangePanel.add( new JLabel ( "Ending SKU: " ) );
        rangePanel.add( end2SkuFld );
        
        JButton createBtn = new JButton ( "Create Template" );
        createBtn.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chooseFileActionVendSkuRange(evt);
            } 
        });
        
        layout.add( title, "wrap" );
        layout.add( rangePanel, "wrap" );
        layout.add( createBtn, "wrap" );
        layout.add( vendErrorLbl );
        
        // setup form validation
        vendValidatorPanel = new ValidationPanel ( );
        vendValidatorPanel.setInnerComponent( layout );
        ValidationGroup validatorGroup = vendValidatorPanel.getValidationGroup();
        validatorGroup.add( start2SkuFld, Validators.REQUIRE_NON_NEGATIVE_NUMBER,
                                          Validators.REQUIRE_NON_EMPTY_STRING );
        validatorGroup.add( end2SkuFld, Validators.REQUIRE_NON_NEGATIVE_NUMBER, 
                                        Validators.REQUIRE_NON_EMPTY_STRING );
        validatorGroup.add( vendFld, Validators.REQUIRE_NON_NEGATIVE_NUMBER,
                                        Validators.REQUIRE_NON_EMPTY_STRING );
        
        return layout;
    }    
    
    
    
    /****************************************************************
     * Actions
     ***************************************************************/
    private void chooseFileActionSkuRange ( ActionEvent evt ) {
        
        // validate fields first
        Problem problem = skuValidatorPanel.getValidationGroup().validateAll();
        if ( problem != null ) {
            skuErrorLbl.setText("<html><font color=\"red\">" + 
                                    problem.getMessage() + "</font></html>" );
            skuErrorLbl.setVisible( true );
            return;
        }
        
        startSku = startSkuFld.getText();
        endSku = endSkuFld.getText();
        
        logger.debug( "SKUs entered {} / {}", startSku, endSku );
        
        String fileChooserDefaultPath = Configuration.defaultFilePath;
        JFileChooser fileChooser = new JFileChooser( fileChooserDefaultPath );
        fileChooser.setSelectedFile( new File ( "skus" + startSku + "_" + endSku + ".xls" ) );
        int returnVal = fileChooser.showOpenDialog( this );
        
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            String path = fileChooser.getCurrentDirectory().getPath();
            System.out.println ( "Choosen path: " + path + " | Default path: " + fileChooserDefaultPath );
            if ( !StringUtils.equals( fileChooserDefaultPath, path ) ) {    
                Configuration.defaultFilePath = path;
            }
            
            xlsItemFile = fileChooser.getSelectedFile();
           
           Thread work = new Thread ( new Runnable ( ) {
               
                    public void run() {
                        
                        SwingUtilities.invokeLater( new Runnable ( ) {
                           public void run ( ) {
                               progressBar.setString( "Processing" );
                               progressBar.setIndeterminate( true );
                           }
                        });
                        
                        List<Item> items = repository.findItems( startSku, endSku );
                        
                        BulkImporter importer = new XlsBulkImporter();
                        importer.writeUserInputtedTemplate( items, xlsItemFile.getPath() );
                        
                        SwingUtilities.invokeLater( new Runnable ( ) {
                            public void run ( ) {
                                progressBar.setString( "Finished!" );
                                progressBar.setIndeterminate( false );
                            }
                         });
                    } 
           });
           work.setDaemon( true );
           work.start();

        }
    }
    
    private void chooseFileActionDeptSkuRange ( ActionEvent evt ) {
        
        // validate fields first
        Problem problem = deptValidatorPanel.getValidationGroup().validateAll();
        if ( problem != null ) {
            deptErrorLbl.setText("<html><font color=\"red\">" + 
                                    problem.getMessage() + "</font></html>" );
            deptErrorLbl.setVisible( true );
            return;
        }
        
        dept = deptFld.getText();
        startSku = start1SkuFld.getText();
        endSku = end1SkuFld.getText();
        
        logger.debug( "SKUs entered {} / {}", startSku, endSku );
        
        String fileChooserDefaultPath = Configuration.defaultFilePath;
        JFileChooser fileChooser = new JFileChooser( fileChooserDefaultPath );
        fileChooser.setSelectedFile( new File ( "skus" + startSku + "_" + endSku + "in" + dept + ".xls" ) );
        int returnVal = fileChooser.showOpenDialog( this );
        
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            
            String path = fileChooser.getCurrentDirectory().getPath();
            System.out.println ( "Choosen path: " + path + " | Default path: " + fileChooserDefaultPath );
            if ( !StringUtils.equals( fileChooserDefaultPath, path ) ) {    
                Configuration.defaultFilePath = path;
            }
            
            xlsItemFile = fileChooser.getSelectedFile();
           
            Thread work = new Thread ( new Runnable ( ) {
               
               public void run() {
                   
                   SwingUtilities.invokeLater( new Runnable ( ) {
                      public void run ( ) {
                          progressBar.setString( "Processing" );
                          progressBar.setIndeterminate( true );
                      }
                   });
                   
                   List<Item> items = repository.findItemsByDept( new Long(dept), startSku, endSku );
                   
                   BulkImporter importer = new XlsBulkImporter();
                   importer.writeUserInputtedTemplate( items, xlsItemFile.getPath() );
                   
                   SwingUtilities.invokeLater( new Runnable ( ) {
                       public void run ( ) {
                           progressBar.setString( "Finished!" );
                           progressBar.setIndeterminate( false );
                       }
                    });
                   } 
               });
            work.setDaemon( true );
            work.start();
           
        }
        
    }
    
    
    private void chooseFileActionManufSkuRange ( ActionEvent evt ) {
        
        // validate fields first
        Problem problem = manufValidatorPanel.getValidationGroup().validateAll();
        if ( problem != null ) {
            manufErrorLbl.setText("<html><font color=\"red\">" + 
                                    problem.getMessage() + "</font></html>" );
            manufErrorLbl.setVisible( true );
            return;
        }
        
        manuf = manufFld.getText();
        startSku = start3SkuFld.getText();
        endSku = end3SkuFld.getText();
        
        logger.debug( "SKUs entered {} / {}", startSku, endSku );
        
        String fileChooserDefaultPath = Configuration.defaultFilePath;
        JFileChooser fileChooser = new JFileChooser( fileChooserDefaultPath );
        fileChooser.setSelectedFile( new File ( "skus" + startSku + "_" + endSku + "by" + manuf + ".xls" ) );
        int returnVal = fileChooser.showOpenDialog( this );
        
        if(returnVal == JFileChooser.APPROVE_OPTION) {
           
            String path = fileChooser.getCurrentDirectory().getPath();
            System.out.println ( "Choosen path: " + path + " | Default path: " + fileChooserDefaultPath );
            if ( !StringUtils.equals( fileChooserDefaultPath, path ) ) {    
                Configuration.defaultFilePath = path;
            }
            
            xlsItemFile = fileChooser.getSelectedFile();
            Thread work = new Thread ( new Runnable ( ) {
               
               public void run() {
                   
                   SwingUtilities.invokeLater( new Runnable ( ) {
                      public void run ( ) {
                          progressBar.setString( "Processing" );
                          progressBar.setIndeterminate( true );
                      }
                   });
                   
                   List<Item> items = repository.findItemsByManuf( new Long(manuf), startSku, endSku );
                   
                   BulkImporter importer = new XlsBulkImporter();
                   importer.writeUserInputtedTemplate( items, xlsItemFile.getPath() );
                   
                   SwingUtilities.invokeLater( new Runnable ( ) {
                       public void run ( ) {
                           progressBar.setString( "Finished!" );
                           progressBar.setIndeterminate( false );
                       }
                    });
                   } 
               });
            work.setDaemon( true );
            work.start();           
           
        }
    }

    
    
    private void chooseFileActionVendSkuRange ( ActionEvent evt ) {
        
        // validate fields first
        Problem problem = vendValidatorPanel.getValidationGroup().validateAll();
        if ( problem != null ) {
            vendErrorLbl.setText("<html><font color=\"red\">" + 
                                    problem.getMessage() + "</font></html>" );
            vendErrorLbl.setVisible( true );
            return;
        }
        
        vend = vendFld.getText();
        startSku = start2SkuFld.getText();
        endSku = end2SkuFld.getText();
        
        logger.debug( "SKUs entered {} / {}", startSku, endSku );
        
        String fileChooserDefaultPath = Configuration.defaultFilePath;
        JFileChooser fileChooser = new JFileChooser( fileChooserDefaultPath );
        fileChooser.setSelectedFile( new File ( "skus" + startSku + "_" + endSku + "by_vend_" + vend + ".xls" ) );
        int returnVal = fileChooser.showOpenDialog( this );
        
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            
            String path = fileChooser.getCurrentDirectory().getPath();
            System.out.println ( "Choosen path: " + path + " | Default path: " + fileChooserDefaultPath );
            if ( !StringUtils.equals( fileChooserDefaultPath, path ) ) {    
                Configuration.defaultFilePath = path;
                //config.getConfig().setProperty( "fileChooserPath", path );
                //config.saveConfig();
            }
            
            xlsItemFile = fileChooser.getSelectedFile();
            Thread work = new Thread ( new Runnable ( ) {
               
               public void run() {
                   
                   SwingUtilities.invokeLater( new Runnable ( ) {
                      public void run ( ) {
                          progressBar.setString( "Processing" );
                          progressBar.setIndeterminate( true );
                      }
                   });
                   
                   List<Item> items = repository.findItemsByVendor( new Long(vend), startSku, endSku );
                   
                   BulkImporter importer = new XlsBulkImporter();
                   importer.writeUserInputtedTemplate( items, xlsItemFile.getPath() );
                   
                   SwingUtilities.invokeLater( new Runnable ( ) {
                       public void run ( ) {
                           progressBar.setString( "Finished!" );
                           progressBar.setIndeterminate( false );
                       }
                    });
                   } 
               });
            work.setDaemon( true );
            work.start();           
           
        }
    }    
    
}
