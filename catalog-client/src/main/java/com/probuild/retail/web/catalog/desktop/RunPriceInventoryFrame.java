package com.probuild.retail.web.catalog.desktop;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.probuild.retail.web.catalog.desktop.support.Configuration;
import com.probuild.retail.web.catalog.desktop.util.IconUtil;
import com.probuild.retail.web.catalog.desktop.util.JTextAreaOutputStream;
import com.probuild.retail.web.catalog.domain.ImportedItem;
import com.probuild.retail.web.catalog.ext.service.WebCatalogService;
import com.probuild.retail.web.catalog.ext.util.RunPriceAndInventory;
import com.probuild.retail.web.catalog.upload.ImportInfo;
import com.probuild.retail.web.catalog.upload.ImportProblem;
import com.probuild.retail.web.catalog.upload.Importer;
import com.probuild.retail.web.catalog.upload.XlsBulkImporter;
import com.probuild.retail.web.catalog.upload.Importer.ImportResult;

public class RunPriceInventoryFrame extends JFrame {

    private static final Logger logger =
        LoggerFactory.getLogger(BulkUploadFrame.class);

    private static final long serialVersionUID = 1L;

    private WebCatalogService service;

    private JPanel layout;

    private JButton invStartBtn;
    private JButton priceStartBtn;

    private JLabel errorLbl;
    private JLabel noteLb;
    private JLabel spaceLb;

    private JTextArea logUpdate;


    private Configuration config;

    /**
     *  Default constructor
     */
    public RunPriceInventoryFrame( WebCatalogService service, Configuration config ) {
        super();

        this.service = service;
        this.config = config;

        init();
    }

    private void init ( ) {

        layout = new JPanel ( new MigLayout( "fill" ) );

        JLabel titleLbl = new JLabel (
                "Price and Inventory Update",
                IconUtil.makeImageIcon( IconUtil.FILE_MANAGER, "" ),
                SwingConstants.RIGHT );
        titleLbl.setFont( titleLbl.getFont().deriveFont( 16f ) );

        layout.add( titleLbl, "wrap" );
        layout.add(makeLogTextPanel(), "grow, push, wrap");
        layout.add( makeActionButtonPanel(), "right, wrap" );



        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setTitle( "Price and Inventory Update" );
        setLayout( new BorderLayout() );
        add( layout, BorderLayout.CENTER );

        setIconImage( IconUtil.makeImageIcon(
                IconUtil.BOOK, "Catalog" ).getImage() );

        // pack the frame
        this.setPreferredSize( new Dimension ( 600, 400 ) );
        pack();

    }

    private JPanel makeActionButtonPanel ( ) {

        invStartBtn = new JButton ( "Start Inventory Update" );
        priceStartBtn = new JButton ( "Start Price Update" );


        noteLb = new JLabel ( );
        //noteLb.setIcon(IconUtil.makeImageIcon( IconUtil.MEDIUM_WARNING, "") );
        noteLb.setText("*Running Price/Inventory update may take several minutes to complete.");
        noteLb.setVisible( true );

        spaceLb = new JLabel ( );
        spaceLb.setText("");
        spaceLb.setVisible(true);

        errorLbl = new JLabel ( );
        errorLbl.setIcon(IconUtil.makeImageIcon( IconUtil.MEDIUM_WARNING, "") );
        errorLbl.setVisible( false );

        JPanel btnPanel = new JPanel ( new MigLayout() );

        invStartBtn.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                invUpdateAction(evt);
            }
        });

        priceStartBtn.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                priceUpdateAction(evt);
            }
        });


        JPanel actionBtnPanel = new JPanel ( new MigLayout( "fill" ) );

        btnPanel.add( noteLb, "right, wrap" );
        btnPanel.add( spaceLb, "right, wrap" );
        btnPanel.add( invStartBtn, "right, wrap" );
        btnPanel.add( priceStartBtn, "right, wrap" );


        actionBtnPanel.add( btnPanel, "right, span" );
        actionBtnPanel.add( errorLbl, "span" );

        return actionBtnPanel;
    }


    private JPanel makeLogTextPanel ( ) {


        JPanel layout = new JPanel ( new MigLayout( "fill" ) );
        layout.setLayout(new BorderLayout());

        logUpdate = new JTextArea(6,20);
        logUpdate.setText("");
        logUpdate.setEditable(false);

        System.setOut(new PrintStream(new JTextAreaOutputStream(logUpdate)));

        JScrollPane scrollingArea = new JScrollPane(logUpdate);
        layout.add(scrollingArea, BorderLayout.CENTER);

        return layout;
    }


    /****************************************************************
     * Actions
     ***************************************************************/

    private void invUpdateAction ( ActionEvent evt ) {


        RunPriceAndInventory runInv = new RunPriceAndInventory(service);
        InvUpdateWorker work = new InvUpdateWorker ( runInv );
        Thread processInvUpdateThread = new Thread ( work );
        processInvUpdateThread.setDaemon( true );
        processInvUpdateThread.start();

    }

    private void priceUpdateAction ( ActionEvent evt ) {

        RunPriceAndInventory runInv = new RunPriceAndInventory(service);
        PriceUpdateWorker work = new PriceUpdateWorker ( runInv );
        Thread processPriceUpdateThread = new Thread ( work );
        processPriceUpdateThread.setDaemon( true );
        processPriceUpdateThread.start();


    }



    /****************************************************************
     * Import Job
     ***************************************************************/


    private class InvUpdateWorker implements Runnable {

        private RunPriceAndInventory prInvUpdate;

        public InvUpdateWorker ( RunPriceAndInventory prInvUpdate ) {
            this.prInvUpdate = prInvUpdate;
        }

        public void run() {

            try {

                invStartBtn.setEnabled(false);
                priceStartBtn.setEnabled(false);

                errorLbl.setText( "Inventory update job is running ..." );
                errorLbl.setVisible( true );

                logUpdate.setText("  ");
                logUpdate.setEditable(false);

                prInvUpdate.updateInventory();

            } catch (Exception e) {
                e.printStackTrace();
                SwingUtilities.invokeLater( new Runnable ( ) {
                    public void run() {

                        invStartBtn.setEnabled(true);
                        priceStartBtn.setEnabled(true);

                        errorLbl.setText( "Inventory Update Failed" );
                        errorLbl.setVisible( true );
                    }

                });
            }

            SwingUtilities.invokeLater( new Runnable ( ) {
                public void run() {

                    invStartBtn.setEnabled(true);
                    priceStartBtn.setEnabled(true);

                    errorLbl.setText( "Inventory Update Completed" );
                    errorLbl.setVisible( true );
                }

            });


        }

    }


    private class PriceUpdateWorker implements Runnable {

        private RunPriceAndInventory prInvUpdate;

        public PriceUpdateWorker ( RunPriceAndInventory prInvUpdate ) {
            this.prInvUpdate = prInvUpdate;
        }

        public void run() {

            try {

                errorLbl.setText( "Price update job is running ... " );
                errorLbl.setVisible( true );

                logUpdate.setText("  ");
                logUpdate.setEditable(false);

                invStartBtn.setEnabled(false);
                priceStartBtn.setEnabled(false);

                prInvUpdate.updatePricing();

            } catch (Exception e) {
                e.printStackTrace();
                SwingUtilities.invokeLater( new Runnable ( ) {
                    public void run() {

                        invStartBtn.setEnabled(true);
                        priceStartBtn.setEnabled(true);

                        errorLbl.setText( "Price Update Failed" );
                        errorLbl.setVisible( true );
                    }

                });
            }

            SwingUtilities.invokeLater( new Runnable ( ) {
                public void run() {

                    invStartBtn.setEnabled(true);
                    priceStartBtn.setEnabled(true);

                    errorLbl.setText( "Price Update Completed" );
                    errorLbl.setVisible( true );
                }

            });


        }

    }

}

