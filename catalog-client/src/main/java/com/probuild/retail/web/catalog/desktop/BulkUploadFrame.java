package com.probuild.retail.web.catalog.desktop;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
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
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.probuild.retail.web.catalog.desktop.support.Configuration;
import com.probuild.retail.web.catalog.desktop.util.IconUtil;
import com.probuild.retail.web.catalog.domain.ImportedItem;
import com.probuild.retail.web.catalog.ext.service.WebCatalogService;
import com.probuild.retail.web.catalog.upload.BulkImporter;
import com.probuild.retail.web.catalog.upload.ImportInfo;
import com.probuild.retail.web.catalog.upload.ImportProblem;
import com.probuild.retail.web.catalog.upload.Importer;
import com.probuild.retail.web.catalog.upload.XlsBulkImporter;
import com.probuild.retail.web.catalog.upload.Importer.ImportResult;

import net.miginfocom.swing.MigLayout;

public class BulkUploadFrame extends JFrame {
    private static final Logger logger = 
                    LoggerFactory.getLogger(BulkUploadFrame.class);
    
    private static final long serialVersionUID = 1L;

    private WebCatalogService service;
    
    private JLabel errorLbl;
    
    private JLabel filePathLbl;
    private JProgressBar progressBar;
    private JLabel progressLbl;
    private JCheckBox overwriteChkBox;
    
    private JTable messageTable;
    private JTable errorTable;
    private JTable itemTable;
    
    private File xlsItemFile; // source data speadsheet 
    
    private XlsBulkImporter bulkImporter;
    
    @SuppressWarnings("unused")
    private boolean processing = false;
    
    private Configuration config;

    /**
     *	Default constructor
     */
    public BulkUploadFrame( WebCatalogService service, Configuration config ) {
        super();
        
        this.service = service;
        this.config = config;
        //config.loadConfig(); // refresh if any changes made
        
        init();
    }

    private void init ( ) {
        
        JPanel layout = new JPanel ( new MigLayout( "fill" ) );

        JLabel titleLbl = new JLabel ( 
                "Bulk Item Import", 
                IconUtil.makeImageIcon( IconUtil.FILE_MANAGER, "" ),
                SwingConstants.RIGHT );
        titleLbl.setFont( titleLbl.getFont().deriveFont( 16f ) );
        
        layout.add( titleLbl, "wrap" );
        layout.add( makeBrowsePanel(), "wrap" );
        //layout.add( makeScanPanel(), "wrap" );
        //layout.add( makeTables(), "grow, push, wrap" );
        layout.add( makeItemToInsertTable(), "grow, push, wrap" );
        layout.add( makeActionButtonPanel(), "right, wrap" );
        
        
        
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setTitle( "Upload Items" );
        setLayout( new BorderLayout() );
        add( layout, BorderLayout.CENTER );
        
        setIconImage( IconUtil.makeImageIcon( 
                                IconUtil.BOOK, "Catalog" ).getImage() );
        
        // pack the frame
        this.setPreferredSize( new Dimension ( 600, 400 ) );
        pack();
        
    }

    private JPanel makeActionButtonPanel ( ) {
        
        JButton saveBtn = new JButton ( "Insert Items" );
        JButton deleteBtn = new JButton ( "Delete Items" );
        
        errorLbl = new JLabel ( );
        errorLbl.setIcon( 
                        IconUtil.makeImageIcon( IconUtil.MEDIUM_WARNING, "") );
        errorLbl.setVisible( false );
        
        overwriteChkBox = new JCheckBox( "Replace Exisiting Items?" );
        overwriteChkBox.setSelected( false );
        
        JPanel btnPanel = new JPanel ( new MigLayout() );

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

        
        JPanel actionBtnPanel = new JPanel ( new MigLayout( "fill" ) );
        
        btnPanel.add( overwriteChkBox, "right, wrap" );
        btnPanel.add( saveBtn, "right, wrap" );
        btnPanel.add( deleteBtn, "right, wrap" );
        
        
        
        actionBtnPanel.add( btnPanel, "right, span" );
        actionBtnPanel.add( errorLbl, "span" );
        
        return actionBtnPanel;
    }

    private JPanel makeBrowsePanel ( ) {
        JPanel layout = new JPanel ( new MigLayout() );
        
        JButton browseBtn = new JButton ( "Browse" );
        browseBtn.addActionListener( new ActionListener( ) {
          public void actionPerformed(java.awt.event.ActionEvent evt) {
              chooseFileAction ( evt );
          } 
      });
        
        filePathLbl = new JLabel ( "" );
        
        layout.add( browseBtn );
        layout.add( filePathLbl );
        
        return layout;
    }

    
    private JPanel makeScanPanel ( ) {
        JPanel layout = new JPanel ( new MigLayout() );
        
        progressBar = new JProgressBar ( );
        progressBar.setEnabled( false );
        progressBar.setStringPainted(true);
        progressBar.setPreferredSize( new Dimension ( 350, 15 ) );

        
        progressLbl = new JLabel ( "" );
        
        layout.add( progressBar );
        layout.add( progressLbl );
        
        return layout;
    }
    
    // not used anymore
    private JPanel makeTables ( ) {

        List<ImportProblem> problems = new ArrayList<ImportProblem>();
        List<ImportInfo> infos = new ArrayList<ImportInfo>();
        
        JPanel layout = new JPanel ( new MigLayout( "fill" ) );
        
        JScrollPane mesgScroller = new JScrollPane( );
        JScrollPane errorScroller = new JScrollPane( );
        
        errorTable = new JTable ( );
        errorTable.setModel( new ErrorDataTableModel ( problems ) );
        //errorTable.setPreferredSize( new Dimension ( 200, 300 ) );
        errorScroller.getViewport().add ( errorTable );
        
        messageTable = new JTable ( );
        messageTable.setModel( new MessageDataTableModel ( infos ) );
        //messageTable.setPreferredSize( new Dimension ( 200, 300 ) );
        mesgScroller.getViewport().add( messageTable );
        
        
        JSplitPane splitPane = new JSplitPane(
                            JSplitPane.VERTICAL_SPLIT,
                                errorScroller, 
                                    mesgScroller );
        splitPane.setResizeWeight(0.5);

        layout.add( new JLabel ( "Errors/Messages" ), "wrap" );
        layout.add( splitPane, "grow" );
        
        return layout;
    }
    
    private JPanel makeItemToInsertTable ( ) {

        
        JPanel layout = new JPanel ( new MigLayout( "fill" ) );
        
        JScrollPane itemScroller = new JScrollPane( );
        
        itemTable = new JTable ( );
        itemTable.setModel( new ImportItemDataTableModel ( new ArrayList<ImportItem>() ) );
        
        itemScroller.getViewport().add ( itemTable );
        
        layout.add( itemScroller, "grow" );
        
        return layout;
    }    
    
    
    /****************************************************************
     * Actions
     ***************************************************************/
    private void chooseFileAction ( ActionEvent evt ) {
        
        String fileChooserDefaultPath = Configuration.defaultFilePath;
        
        JFileChooser fileChooser = new JFileChooser( fileChooserDefaultPath );
        int returnVal = fileChooser.showOpenDialog( this );
        
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            
            String path = fileChooser.getCurrentDirectory().getPath();
            
            if ( !StringUtils.equals( fileChooserDefaultPath, path ) ) {    
                Configuration.defaultFilePath = path;
            }
            
           xlsItemFile = fileChooser.getSelectedFile();
           
           filePathLbl.setVisible( false );
           filePathLbl.setText( xlsItemFile.getAbsolutePath() );
           filePathLbl.setVisible( true );
           
           
           bulkImporter = new XlsBulkImporter();
           
           
           List<ImportedItem> validItems;
           try {
               validItems = bulkImporter.readUserInputtedItems( 
                                           xlsItemFile.getAbsolutePath() );
           } 
           catch ( Exception e) {
        	   
               JOptionPane.showMessageDialog( null,
                       "Import failed with exception: \n" + e.getMessage() + "\n" +
                       "Problem on row number: " + bulkImporter.getRow() + 
                       ", column number: " +  bulkImporter.getColumn()+1 + "\n",
                       "Loading Error",
                       JOptionPane.ERROR_MESSAGE);
               
               return;
           }
           
           List<ImportItem> importItems = 
                               new ArrayList<ImportItem>( validItems.size() );
           for ( ImportedItem itm : validItems ) {
               ImportItem i = new ImportItem ( );
               i.item = itm;
               i.message = "";
               importItems.add ( i );
           }

           // update table
           itemTable.setModel( new ImportItemDataTableModel( importItems ) );
           
           itemTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
           // Pack the second column of the table
           //int vColIndex = 1;
           //int margin = 2;
           for (int c=0; c<itemTable.getColumnCount(); c++) {
               packColumn(itemTable, c, 4);
           }

           
        }
    }
        
    private void saveItemAction ( ActionEvent evt ) {
        
      Importer importer = new Importer ( service, config );
      ImportWorker work = new ImportWorker ( importer );
      Thread processItemThread = new Thread ( work );
      processItemThread.setDaemon( true );
      processItemThread.start();

    }
    
    private void deleteItemAction ( ActionEvent evt ) {
        
        Importer importer = new Importer ( service, config );
        ImportDeleteWorker work = new ImportDeleteWorker ( importer );
        Thread processItemThread = new Thread ( work );
        processItemThread.setDaemon( true );
        processItemThread.start();

      }
    
    
    
    /****************************************************************
     * Table Model
     ***************************************************************/
    private class MessageDataTableModel extends AbstractTableModel {

        private static final long serialVersionUID = 1L;
        
        private final String[] columnNames = {"", "Item", "Message"};
        private List<ImportInfo> infoList;
        
        public MessageDataTableModel ( List<ImportInfo> infoList ) {
            this.infoList = infoList;
        }
        
        public int getColumnCount() {
            return 3;
        }

        public String getColumnName(int col) {
            return columnNames[col];
        }

        
        public int getRowCount() {
            return infoList.size();
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            
            if ( rowIndex > infoList.size() )
                return null;
            
            ImportInfo info = infoList.get( rowIndex );
            
            switch ( columnIndex ) {
                case 0: // row number
                    return new Integer ( rowIndex + 1 );
                
                case 1 : // item info
                    if ( info.getItem() != null )
                        return info.getItem().toString();
                    else
                        return null;
                    
                case 2: // message
                    return info.getMessage();
                    
                default:
                    return null;
            }

        }
        
        public boolean isCellEditable(int row, int col) { return false; }

        public void setInfoList ( List<ImportInfo> inf) {
            this.infoList = inf;
            fireTableDataChanged();
            
        }
    }
    
    private class ErrorDataTableModel extends AbstractTableModel {

        private static final long serialVersionUID = 1L;
        
        private final String[] columnNames = {"", "Item", "Error"};
        private List<ImportProblem> errorList;
        
        public ErrorDataTableModel ( List<ImportProblem> errorList ) {
            this.errorList = errorList;
        }
        
        public int getColumnCount() {
            return 3;
        }

        public String getColumnName(int col) {
            return columnNames[col];
        }

        
        public int getRowCount() {
            return errorList.size();
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            
            if ( rowIndex > errorList.size() )
                return null;
            
            ImportProblem info = errorList.get( rowIndex );
            
            switch ( columnIndex ) {
                case 0: // row number
                    return new Integer ( rowIndex + 1 );
                
                case 1 : // item info
                    return info.getItem().toString();
                    
                case 2: // message
                    return info.getMessage();
                    
                default:
                    return null;
            }

        }
        
        public boolean isCellEditable(int row, int col) { return false; }
        
        public void setErrorList ( List<ImportProblem> prb) {
            this.errorList = prb;
            fireTableDataChanged();
        }

    }
    


    /****************************************************************
     * Table Model
     ***************************************************************/
    private class ImportItemDataTableModel extends AbstractTableModel {

        private static final long serialVersionUID = 1L;
        
        private final String[] columnNames = {"", "SKU", "Item Name", "Group Path", "Status", "Message/Error"};
        private List<ImportItem> itemList;
        
        public ImportItemDataTableModel ( List<ImportItem> items ) {
            super();
            this.itemList = items;
            
        }
        
        public int getColumnCount() {
            return 6;
        }

        public String getColumnName(int col) {
            return columnNames[col];
        }

        
        public int getRowCount() {
            return itemList.size();
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            
            if ( rowIndex > itemList.size() )
                return null;
            
            ImportItem importItem = itemList.get( rowIndex );
            
            switch ( columnIndex ) {
                case 0: // row number
                    return new Integer ( rowIndex + 1 );
                
                case 1 : // sku
                    return importItem.item.getSku();
                    
                case 2: // name
                    return importItem.item.getName();
                    
                case 3: // group
                    return importItem.item.getDeliminatedGroupPath();

                case 4: // status
                    return importItem.status;
                    
                case 5: // message
                    return importItem.message;
                    
                default:
                    return null;
            }

        }
        
        public boolean isCellEditable(int row, int col) { return false; }

        public void setInfoList ( List<ImportItem> items) {
            this.itemList = items;
            fireTableDataChanged();
            
        }
        
        public void updateItem ( int row, ImportItem updated ) {
            itemList.set(row, updated );
            fireTableRowsUpdated( row, row );
        }
        
        public void addItem ( ImportItem updated ) {
            itemList.add( updated );
            fireTableRowsInserted(itemList.size(), itemList.size() );
        }
        
    }    
    
     // Sets the preferred width of the visible column specified by vColIndex. The column
     // will be just wide enough to show the column head and the widest cell in the column.
     // margin pixels are added to the left and right
     // (resulting in an additional width of 2*margin pixels).
     public void packColumn(JTable table, int vColIndex, int margin) {
         TableModel model = table.getModel();
         DefaultTableColumnModel colModel = (DefaultTableColumnModel)table.getColumnModel();
         TableColumn col = colModel.getColumn(vColIndex);
         int width = 0;
    
         // Get width of column header
         TableCellRenderer renderer = col.getHeaderRenderer();
         if (renderer == null) {
             renderer = table.getTableHeader().getDefaultRenderer();
         }
         Component comp = renderer.getTableCellRendererComponent(
             table, col.getHeaderValue(), false, false, 0, 0);
         width = comp.getPreferredSize().width;
    
         // Get maximum width of column data
         for (int r=0; r<table.getRowCount(); r++) {
             renderer = table.getCellRenderer(r, vColIndex);
             comp = renderer.getTableCellRendererComponent(
                 table, table.getValueAt(r, vColIndex), false, false, r, vColIndex);
             width = Math.max(width, comp.getPreferredSize().width);
         }
    
         // Add margin
         width += 2*margin;
    
         // Set the width
         col.setPreferredWidth(width);
     }    
    
    
    /****************************************************************
     * Import Job
     ***************************************************************/
     
     private class ImportDeleteWorker implements Runnable {

         private Importer importer;
         
         private int deleted = 0;
         
         public ImportDeleteWorker ( Importer importer ) {
             this.importer = importer;
         }
         
         public void run() {
             ImportItemDataTableModel model = 
                 (ImportItemDataTableModel)itemTable.getModel();
             

             for ( int i = 0; i < bulkImporter.getItemsRead().size(); i++) {
                 
                 ImportItem importItem = model.itemList.get( i );
                 importItem.message = "";
                 importItem.status = "";
                 
                 ImportedItem itm = bulkImporter.getItemsRead().get( i );
                 if ( importer.removeItem( itm ) ) {
                     deleted++;
                     importItem.status = "Removed";
                 }
                 
                 importItem.status = "-";
                 
                 
                 model.updateItem( i , importItem );
                 
             }
             
             
             
             
             SwingUtilities.invokeLater( new Runnable ( ) {
                 public void run() {
                    
                    errorLbl.setText( "Deleted: " + deleted );
                    errorLbl.setVisible( true );
                 }
                               
             });
         }
     }
     
    private class ImportWorker implements Runnable {

        private Importer importer;
        private List<ImportedItem> validItems;
        
        private int added = 0;
        private int failed = 0;
        private int skipped = 0;
        
        public ImportWorker ( Importer importer ) {
            this.importer = importer;
        }
        
        public void run() {
            ImportItemDataTableModel model = 
                (ImportItemDataTableModel)itemTable.getModel();
            
            boolean proceedWithErrors = false;
            
            // for each item do a precheck
            for ( int i = 0; i < bulkImporter.getItemsRead().size(); i++) {
                ImportedItem itm = bulkImporter.getItemsRead().get( i );
                ImportResult rslt = importer.precheck( itm );
                
                ImportItem importItem = model.itemList.get( i );
                importItem.message = rslt.message;
                
                if ( rslt.message != null && 
                             rslt.message.equals( Importer.ITEM_EXISTS ) ) {
                    importItem.status = "Warn";
                } else if ( StringUtils.isEmpty( rslt.message ) ) {
                    importItem.status = "Verified";
                } else {
                    importItem.status = "Error";
                    proceedWithErrors = true;
                }
                
                model.updateItem( i , importItem );
                
            }
            
            
            if ( proceedWithErrors ) {
                int choice = JOptionPane.showConfirmDialog( null,
                      "Problems with the Data!\n" +
                          "Do you want to send these items to the catalog?",
                      "Confirm Import",
                          JOptionPane.YES_NO_OPTION);
                if ( choice == JOptionPane.NO_OPTION ) {
                    return;
                }
            }
            
            
            
            for ( int i = 0; i < model.itemList.size(); i++) {
                
                ImportItem importItem = model.itemList.get( i );
                
                if ( importItem.status.equals( "Error" ) ) {
                    failed++;
                    continue;
                }
                
                if ( !StringUtils.isEmpty( importItem.message ) ) {
                    
                    if ( StringUtils.equals( importItem.message, Importer.ITEM_EXISTS ) && 
                                                                overwriteChkBox.isSelected()) {
                        importItem.status = "Replaced";
                        added++;
                    
                        importer.replaceItem( importItem.item );

                    } else {
                        importItem.status = "Skipped";
                        skipped++;
                    }
                    model.updateItem( i , importItem );
                    
                    continue;
                }
                
                try {
                    ImportResult rslt = importer.addToCatalog( importItem.item );
                    
                    if ( rslt.success ) {
                        importItem.status = "Added";
                        added++;
                    } else {
                        importItem.status = "Failed";
                        importItem.message = rslt.message;
                        failed++;
                    }
                    
                    
                } catch ( Exception e ) {
                    importItem.status = "Failed";
                    importItem.message += "\n" + e.getMessage();
                    failed++;
                    e.printStackTrace();
                }
                
                model.updateItem( i , importItem );
                
            }
            
            packColumn( itemTable, 5, 4 );
            
            SwingUtilities.invokeLater( new Runnable ( ) {
                public void run() {
                   
                   errorLbl.setText( "Added: " + added + ", Failed: " + failed + ", Skipped: " + skipped );
                   errorLbl.setVisible( true );
                }
                              
            });
            
            
//            BulkImporter bulkImporter = new XlsBulkImporter();
//            
//            
//            // catch any exceptions and report to user
//            try {
//                validItems = 
//                    bulkImporter.readUserInputtedItems( 
//                                            xlsItemFile.getAbsolutePath() );
//                importer.precheck( validItems );
//            } catch ( Exception e) {
//                
//                JOptionPane.showMessageDialog( null,
//                        "Import failed with exception.\n" +
//                                e.getMessage(),
//                        "Fatal Error",
//                        JOptionPane.ERROR_MESSAGE);
//                
//                processing = false;
//                return;
//            }
//            
//            ((ErrorDataTableModel)errorTable.getModel()).setErrorList( importer.getProblems() );
//            ((MessageDataTableModel)messageTable.getModel( )).setInfoList( importer.getMessages() );
            
            // update tables with results
//            SwingUtilities.invokeLater( new Runnable ( ) {
//
////                public void run() {
////                    //System.out.println ( "Messages " + importer.getMessages().size() );
//////                    ((ErrorDataTableModel)errorTable.getModel()).setErrorList( importer.getProblems() );
//////                    ((MessageDataTableModel)messageTable.getModel( )).setInfoList( importer.getMessages() );
////                    //errorTable.setModel( new ErrorDataTableModel( importer.getProblems() ) );
////                    //messageTable.setModel( new MessageDataTableModel( importer.getMessages() ) );
//////                    Dimension mesgTableSize = messageTable.getSize();
//////                    messageTable.getColumnModel().getColumn( 0 ).setPreferredWidth( 25 );
//////                    messageTable.getColumnModel().getColumn( 1 ).setPreferredWidth( 225 );
//////                    messageTable.getColumnModel().getColumn( 2 ).setPreferredWidth( mesgTableSize.width - 250 );
//////                    errorTable.getColumnModel().getColumn( 0 ).setPreferredWidth( 25 );
//////                    errorTable.getColumnModel().getColumn( 1 ).setPreferredWidth( 225 );
//////                    errorTable.getColumnModel().getColumn( 2 ).setPreferredWidth( mesgTableSize.width - 250 );
//////                    messageTable.getParent().setVisible( true );
//////                    errorTable.getParent().setVisible( true );
////                }
////              
////            });

            
//            // if errors stop, else proceed with inserts
//            if ( importer.getProblems() != null && 
//                                importer.getProblems().size() > 0 ) {
//                
//                JOptionPane.showMessageDialog( null,
//                        "There are errors that need to be corrected.\n" +
//                                "Please review the errors list.",
//                        "Import Stopped Due to Errors",
//                        JOptionPane.ERROR_MESSAGE);
//                
//            } else {
//                
//                int choice = JOptionPane.showConfirmDialog( null,
//                        "Everything seems ok!\n" +
//                            "Do you want to send these items to the catalog?",
//                        "Confirm Import",
//                        JOptionPane.YES_NO_OPTION);
//                if ( choice == JOptionPane.YES_OPTION ) {
//                    logger.debug( "User chooses to continue... " );
//                    
//                    try {
//                        int inserted = importer.addToCatalog( validItems );
//                    } catch ( Exception e ) {
//                        JOptionPane.showMessageDialog( null,
//                                "Import failed with exception.\n" +
//                            "(Some items may have been added successfully)\n\"" +
//                                        e.getMessage() + "\"",
//                                "Fatal Error",
//                                JOptionPane.ERROR_MESSAGE);
//                        
//                        processing = false;
//                        return;
//                    }
//                    
//                    
//                    if ( importer.getProblems().size() > 0 ) {
//                        //errorTable.setModel( 
//                        //            new ErrorDataTableModel( importer.getProblems() ) );
//                        ((ErrorDataTableModel)errorTable.getModel()).setErrorList( importer.getProblems() );
//                        errorTable.getColumnModel().getColumn( 0 ).setPreferredWidth( 25 );
//                        errorTable.getColumnModel().getColumn( 1 ).setPreferredWidth( 225 );
//                        errorTable.getColumnModel().getColumn( 2 ).setPreferredWidth( errorTable.getSize().width - 250 );
//                        //errorTable.setVisible( true );
//                        ((MessageDataTableModel)messageTable.getModel( )).setInfoList( importer.getMessages() );
//                        System.out.println ( "Messages " + importer.getMessages().size() );
//                        
//                        JOptionPane.showMessageDialog( null,
//                                "Errors occured while adding items to catalog.\n" +
//                                        "Please review the errors list.",
//                                "Import Finished with Errors",
//                                JOptionPane.ERROR_MESSAGE);
//                    }
//                }
//                
//            }
            

            
//            
//            processing = false;
//            
//            
//            SwingUtilities.invokeLater( new Runnable ( ) {
//
//                public void run() {
//                    progressBar.setString( "Finished!" );
//                    progressBar.setIndeterminate( false );
//                }
//              
//            });
        }
        
        
        public List<ImportedItem> getValidItems ( ) {
            return this.validItems;
        }
        
    }
    
    private class ImportWorkerMonitor implements Runnable {

        private Importer importer;
        
        public ImportWorkerMonitor ( Importer importer ) {
            this.importer = importer;
        }
        
        public void run() {
            
            while ( processing = true ) {
                SwingUtilities.invokeLater( new Runnable ( ) {

                    public void run() {
                        if ( !"Finished!".equals( progressBar.getString() ) )
                                progressBar.setString( importer.getCurrentStatus() );
                    }
                });
                
                try {
                    Thread.sleep( 100 ); // sleep for 1/3 second
                }
                catch(InterruptedException e) {
                }
            }
        }
    }
    
    private class ImportItem {
        
        public ImportedItem item;
        public String message = "";
        public String status = "";
        public Long id;
        public boolean skipped = false;
        
    }
}
