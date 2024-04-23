package com.probuild.retail.web.catalog.upload;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.probuild.retail.web.catalog.domain.ImportedItem;
import com.probuild.retail.web.catalog.domain.Item;

public class XlsBulkImporter implements BulkImporter {
    
    private static final Logger logger = 
                         LoggerFactory.getLogger( XlsBulkImporter.class );
    
    private Map<Character, Character> replaceMap;
    
    private List<ImportedItem> itemsRead;

    public int row = 0;
    public int column = 0;

    public void setRow (int row) {this.row = row; }
    public int getRow () {return row; }
    
    public void setColumn (int col) {this.column = col; }
    public int getColumn () {return column; }
    
    
    /**
     *	Default constructor
     */
    public XlsBulkImporter() {
        super();
        
        replaceMap = new HashMap<Character,Character>();
        replaceMap.put ( new Character( (char)8220 ), new Character( '"' ) );
        replaceMap.put ( new Character( (char)8221 ), new Character( '"' ) );
        replaceMap.put ( new Character( (char)8216 ), new Character( '\'' ) );
        replaceMap.put ( new Character( (char)8217 ), new Character( '\'' ) );
        replaceMap.put ( new Character( (char)8482 ), new Character( ' ' ) ); // trademark
        
    }


    public List<ImportedItem> readUserInputtedItems(String fileName) {
        
        List<ImportedItem> items = new ArrayList<ImportedItem>();
        
        // attempt to open file
        InputStream in = null;
        Workbook wb = null;
        try {
            in = new FileInputStream ( new File ( fileName ) );
            
            // open spreadsheet
            wb = new HSSFWorkbook( in );
        }
        catch(FileNotFoundException e) {
            logger.error( "Failed to open file", e );
            return items;
        }
        catch(IOException e) {
            logger.error( "Failed to open workbook", e );
            return items;
        }
        
        
        Sheet sheet = wb.getSheetAt( 0 ); // get the first sheet, should not be any others
        
        if ( sheet.getRow( 0 ).getPhysicalNumberOfCells() < 23 ) {
            logger.error( "Not enough columns present to extract data from" );
            return items;
        }
        
        /*
         * Process all the rows in the spreadsheet
         */
        row = 0;
        for ( Row spreadsheetRow : sheet ) {
        	
            if (row++ == 0 ) { // skip first row
                continue;
            }
            
            // assume no data in row since first cell is blank
            if ( StringUtils.isEmpty( 
            		spreadsheetRow.getCell(0) == null ? null :spreadsheetRow.getCell(0).toString() ) ) {
                logger.debug( "Skipping row, no data" );
                continue;
            }
            
            ImportedItem item = transform ( spreadsheetRow );
            items.add( item );
        }
        
        
        // close the stream
        try {
            in.close();
        }
        catch(IOException e) {
        }
        
        itemsRead = items;
        
        return items;
    }

    public boolean writeUserInputtedTemplate(List<Item> items, String fileName) {
        
        Workbook wb = new HSSFWorkbook();

        Sheet sheet = wb.createSheet( "Import Items" );
        
        createBulkImportHeaderRow( wb, sheet );
        
        for ( Item item : items ) {
            createBulkImportRow( sheet, item );
        }
        
        // adjust widths
        for ( int i = 0; i < 25; i++ )
            sheet.autoSizeColumn((short)i);
        
        FileOutputStream fileOut;
        try {
            fileOut = new FileOutputStream( fileName );
            wb.write(fileOut);
            fileOut.close();
        }
        catch(FileNotFoundException e) {
            
        }
        catch(IOException e) {

        }


        
        return false;
    }
    
    
    private Row createBulkImportHeaderRow ( Workbook wb, Sheet sheet ) {
        Font font = wb.createFont();
        font.setBoldweight( Font.BOLDWEIGHT_BOLD );
        CellStyle cellStyle = wb.createCellStyle();
        cellStyle.setFont( font );
        
        Row row = sheet.createRow( 0 );

        Cell skuCell = row.createCell(0);
        skuCell.setCellStyle( cellStyle );
        skuCell.setCellValue( "SKU" );

        Cell upcCell = row.createCell( 1 );
        upcCell.setCellStyle( cellStyle );
        upcCell.setCellValue( "UPC" );
        
        Cell altCell = row.createCell( 2 );
        altCell.setCellStyle( cellStyle );
        altCell.setCellValue( "Alternate Item" );
        
        Cell uomCell = row.createCell( 3 );
        uomCell.setCellStyle( cellStyle );
        uomCell.setCellValue( "UOM" );
        
        Cell deptCell = row.createCell( 4 );
        deptCell.setCellStyle( cellStyle );
        deptCell.setCellValue( "Department" );
        
        Cell modelCell = row.createCell( 5 );
        modelCell.setCellStyle( cellStyle );
        modelCell.setCellValue( "Model Number" );
        
        Cell manufCell = row.createCell( 6 );
        manufCell.setCellStyle( cellStyle );
        manufCell.setCellValue( "Manufacturer Name" );
        
        Cell manufUrlCell = row.createCell( 7 );
        manufUrlCell.setCellStyle( cellStyle );
        manufUrlCell.setCellValue( "Manufacturer URL" );
        
        Cell nameCell = row.createCell( 8 );
        nameCell.setCellStyle( cellStyle );
        nameCell.setCellValue( "Item Name" );
        
        Cell descrCell = row.createCell( 9 );
        descrCell.setCellStyle( cellStyle );
        descrCell.setCellValue( "Item Description" );
        
        Cell regPriceCell = row.createCell( 10 );
        regPriceCell.setCellStyle( cellStyle );
        regPriceCell.setCellValue( "Regular Price" );
        
        Cell salePriceCell = row.createCell( 11 );
        salePriceCell.setCellStyle( cellStyle );
        salePriceCell.setCellValue( "Sale Price" );
        
        Cell relatedItemsCell = row.createCell( 12 );
        relatedItemsCell.setCellStyle( cellStyle );
        relatedItemsCell.setCellValue( "Related SKUs" );
        
        Cell groupPathCell = row.createCell( 13 );
        groupPathCell.setCellStyle( cellStyle );
        groupPathCell.setCellValue( "Group Path" );
        
        Cell groupImgCell = row.createCell( 14 );
        groupImgCell.setCellStyle( cellStyle );
        groupImgCell.setCellValue( "Group Image" );
        
        Cell widthCell = row.createCell( 15 );
        widthCell.setCellStyle( cellStyle );
        widthCell.setCellValue( "Width" );
        
        Cell lengthCell = row.createCell( 16 );
        lengthCell.setCellStyle( cellStyle );
        lengthCell.setCellValue( "Length" );
        
        Cell depthCell = row.createCell( 17 );
        depthCell.setCellStyle( cellStyle );
        depthCell.setCellValue( "Depth" );
        
        Cell attr1Cell = row.createCell( 18 );
        attr1Cell.setCellStyle( cellStyle );
        attr1Cell.setCellValue( "Attribute 1 Name" );
        
        Cell attr1ValCell = row.createCell( 19 );
        attr1ValCell.setCellStyle( cellStyle );
        attr1ValCell.setCellValue( "Attribute 1 Value" );
        
        Cell attr2Cell = row.createCell( 20 );
        attr2Cell.setCellStyle( cellStyle );
        attr2Cell.setCellValue( "Attribute 2 Name" );
        
        Cell attr2ValCell = row.createCell( 21 );
        attr2ValCell.setCellStyle( cellStyle );
        attr2ValCell.setCellValue( "Attribute 2 Value" );
        
        Cell attr3Cell = row.createCell( 22 );
        attr3Cell.setCellStyle( cellStyle );
        attr3Cell.setCellValue( "Attribute 3 Name" );
        
        Cell attr3ValCell = row.createCell( 23 );
        attr3ValCell.setCellStyle( cellStyle );
        attr3ValCell.setCellValue( "Attribute 3 Value" );
        
        Cell attr4Cell = row.createCell( 24 );
        attr4Cell.setCellStyle( cellStyle );
        attr4Cell.setCellValue( "Attribute 4 Name" );
        
        Cell attr4ValCell = row.createCell( 25 );
        attr4ValCell.setCellStyle( cellStyle );
        attr4ValCell.setCellValue( "Attribute 4 Value" );
        return row;
    }
    
    private Row createBulkImportRow ( Sheet sheet, Item item ) {
        Row row = sheet.createRow( sheet.getLastRowNum() + 1 );

        logger.debug( "last row number {}", sheet.getLastRowNum() ); 
        
        Cell skuCell = row.createCell(0);
        skuCell.setCellValue( item.getSku() );

        Cell upcCell = row.createCell( 1 );
        upcCell.setCellValue( new BigDecimal( item.getUpc() ).toString() );
        
        Cell altCell = row.createCell( 2 );
        altCell.setCellValue( item.getAlt() );
        
        Cell uomCell = row.createCell( 3 );
        uomCell.setCellValue( item.getUom() );
        
        Cell deptCell = row.createCell( 4 );
        deptCell.setCellValue( item.getDept() );
        
        Cell modelCell = row.createCell( 5 );
        modelCell.setCellValue( item.getModelNum() );
        
        Cell manufCell = row.createCell( 6 );
        manufCell.setCellValue( WordUtils.capitalizeFully( item.getManufacturer() ) );
        
        Cell manufUrlCell = row.createCell( 7 );
        manufUrlCell.setCellValue( "" );
        
        Cell nameCell = row.createCell( 8 );
        nameCell.setCellValue( WordUtils.capitalizeFully( item.getName() ) );
        
        Cell descrCell = row.createCell( 9 );
        descrCell.setCellValue( item.getDescr() );
        
        Cell regPriceCell = row.createCell( 10 );
        regPriceCell.setCellValue( item.getRegularPrice().doubleValue() );
        
        Cell salePriceCell = row.createCell( 11 );
        if ( item.getSalePrice() != null )
            salePriceCell.setCellValue( item.getSalePrice().doubleValue() );
        else
            salePriceCell.setCellValue( 0 );
        
        Cell relatedItemsCell = row.createCell( 12 );
        relatedItemsCell.setCellValue( "" );
        
        Cell groupPathCell = row.createCell( 13 );
        groupPathCell.setCellValue( "" );
        
        Cell groupImgCell = row.createCell( 14 );
        groupImgCell.setCellValue( "" );
        
        Cell widthCell = row.createCell( 15 );
        widthCell.setCellValue( 0 );
        
        Cell lengthCell = row.createCell( 16 );
        lengthCell.setCellValue( 0 );
        
        Cell depthCell = row.createCell( 17 );
        depthCell.setCellValue( 0 );
        
        
        return row;
    }
    
    
    // transform row to Item
    private ImportedItem transform ( Row row ) {
        
        ImportedItem item = new ImportedItem();
        DecimalFormat df = new DecimalFormat( "######" );

	        for (Cell cell : row ) {
	                        
	            column = cell.getColumnIndex();
	            
	            switch ( column ) {
	                
	                case 0: // sku
	                    item.setSku( BigDecimal.valueOf( 
	                                  cell.getNumericCellValue() ).intValue() );
	                    logger.trace( "SKU read {}", item.getSku() );
	                    break;
	                    
	                case 1: // upc
	                    if ( cell.getCellType() == Cell.CELL_TYPE_STRING )
	                        item.setUpc( cell.getStringCellValue() );
	                    else
	                        item.setUpc( df.format( cell.getNumericCellValue() ) );
	                    logger.trace( "UPC read {}", item.getUpc() );
	                    break;
	                    
	                case 2: // alt
	                    item.setAlt( cell.getStringCellValue() );
	                    logger.trace( "Alt read {}", item.getAlt() );
	                    break;
	                    
	                case 3: // uom
	                    item.setUom( cell.getStringCellValue() );
	                     logger.trace( "UOM read {}", item.getUom() );
	                     break;
	                     
	                case 4: // dept
	                    item.setDept( BigDecimal.valueOf( 
	                                   cell.getNumericCellValue() ).intValue() );
	                    logger.trace( "Dept read {}", item.getDept() );
	                    break;
	                    
	                case 5: // model #
	                    if ( cell.getCellType() == Cell.CELL_TYPE_NUMERIC )
	                        item.setModelNum( df.format( cell.getNumericCellValue() ) );
	                    else
	                        item.setModelNum( cell.getStringCellValue() );
	                    logger.trace( "Model read {}", item.getModelNum() );
	                    break;
	                    
	                case 6: // manufacturer name
	                    item.setManufacturer( cell.getStringCellValue() );
	                    logger.trace( "Manuf read {}", item.getManufacturer() );
	                    break;
	                    
	                case 7: // manufacturer url
	                    break;
	                
	                case 8: // name
	                    item.setName( this.convertNonAscii( cell.getStringCellValue().trim() ) );
	                    logger.trace( "Name read {}", item.getName() );
	                    break;
	                    
	                case 9: // description
	                    item.setDescr( this.convertNonAscii( cell.getStringCellValue() ) );
	                    logger.trace( "Descr read {}", item.getDescr() );
	                    break;
	                    
	                case 10: // regular price
	                    item.setRegularPrice( new BigDecimal( cell.getNumericCellValue()  ) );
	                    logger.trace( "Reg Price read {}", item.getRegularPrice() );
	                    break;
	                
	                case 11: // sale price
	                    item.setSalePrice( new BigDecimal( cell.getNumericCellValue()  ) );
	                    logger.trace( "Sale Price read {}", item.getSalePrice() );
	                    break;
	                    
	                case 12: // related items skus, separated by ','
	                    if ( cell.getCellType() == Cell.CELL_TYPE_NUMERIC )
	                        item.setDeliminatedRelatedItems( df.format( cell.getNumericCellValue() ) );
	                    else
	                        item.setDeliminatedRelatedItems( cell.getStringCellValue() );
	                    logger.trace( "Relateds read {}", item.getDeliminatedRelatedItems() );
	                    break;
	                    
	                case 13: // category path, separated by '|'
	                    item.setDeliminatedGroupPath( 
	                            cell.getStringCellValue() == null ? "" : cell.getStringCellValue().trim() );
	                    logger.trace( "Groups read {}", item.getDeliminatedGroupPath() );
	                    break;
	                    
	                case 14: // category image base name
	                    item.setGroupImage( cell.getStringCellValue() );
	                    logger.trace( "Grp Img {}", item.getGroupImage() );
	                    break;
	                    
	                case 15: // width
	                    if ( cell.getCellType() == Cell.CELL_TYPE_NUMERIC ) {
	                        item.setWidth( BigDecimal.valueOf( cell.getNumericCellValue() ) );
	                    }
	                    else if ( !StringUtils.isEmpty( cell.getStringCellValue().trim() ) ) {
	                        item.setWidth( new BigDecimal( cell.getStringCellValue() ) );
	                    }
	                    else
	                        item.setWidth( BigDecimal.ZERO );
	                    logger.trace( "Width {}", item.getWidth() );
	                    break;
	                    
	                case 16: // length
	                    if ( cell.getCellType() == Cell.CELL_TYPE_NUMERIC )
	                        item.setHeight( BigDecimal.valueOf( cell.getNumericCellValue() ) );
	                    else if ( !StringUtils.isEmpty( cell.getStringCellValue().trim() ) )
	                        item.setHeight( new BigDecimal( cell.getStringCellValue() ) );
	                    else
	                        item.setHeight( BigDecimal.ZERO );
	                    logger.trace( "Length {}", item.getHeight() );
	                    break;
	                    
	                case 17: // depth
	                    if ( cell.getCellType() == Cell.CELL_TYPE_NUMERIC )
	                        item.setDepth( BigDecimal.valueOf( cell.getNumericCellValue() ) );
	                    else if ( !StringUtils.isEmpty( cell.getStringCellValue().trim() ) ) 
	                        item.setDepth( new BigDecimal( cell.getStringCellValue() ) );
	                    else
	                        item.setDepth( BigDecimal.ZERO );
	                    logger.trace( "Depth {}", item.getDepth() );
	                    break;
	                    
	                case 18: // attribute name 1
	                    item.setFilter1Name( cell.getStringCellValue() );
	                    logger.trace( "filter n1 {}", item.getFilter1Name() );
	                    break;
	                    
	                case 19: // attribute value 1
	                    // item.setFilter1Value( cell.getStringCellValue() );
	                    if ( cell.getCellType() == Cell.CELL_TYPE_NUMERIC )
	                        item.setFilter1Value( "" + BigDecimal.valueOf( cell.getNumericCellValue() ) );
	                    else if ( !StringUtils.isEmpty( cell.getStringCellValue().trim() ) ) 
	                        item.setFilter1Value( cell.getStringCellValue() );
	                    logger.trace( "filter v1 {}", item.getFilter2Value() );
	                    break;
	                    
	                case 20: // attribute name 2
	                    item.setFilter2Name( cell.getStringCellValue() );
	                    logger.trace( "filter n2 {}", item.getFilter2Name() );
	                    break;
	                    
	                case 21: // attribute value 2
	                    // item.setFilter2Value( cell.getStringCellValue() );
	                    if ( cell.getCellType() == Cell.CELL_TYPE_NUMERIC )
	                        item.setFilter2Value( "" + BigDecimal.valueOf( cell.getNumericCellValue() ) );
	                    else if ( !StringUtils.isEmpty( cell.getStringCellValue().trim() ) ) 
	                        item.setFilter2Value( cell.getStringCellValue() );
	                    logger.trace( "filter v2 {}", item.getFilter2Value() );
	                    break;
	                    
	                case 22: // attribute name 3
	                    item.setFilter3Name( cell.getStringCellValue() );
	                    logger.trace( "filter n3 {}", item.getFilter3Name() );
	                    break;
	                    
	                case 23: // attribute value 3
	                    //item.setFilter3Value( cell.getStringCellValue() );
	                    if ( cell.getCellType() == Cell.CELL_TYPE_NUMERIC )
	                        item.setFilter3Value( "" + BigDecimal.valueOf( cell.getNumericCellValue() ) );
	                    else if ( !StringUtils.isEmpty( cell.getStringCellValue().trim() ) ) 
	                        item.setFilter3Value( cell.getStringCellValue() );
	                    logger.trace( "filter v3 {}", item.getFilter3Value() );
	                    break;
	                    
	                case 24: // attribute name 4
	                    item.setFilter4Name( cell.getStringCellValue() );
	                    logger.trace( "filter n4 {}", item.getFilter4Name() );
	                    break;
	                    
	                case 25: // attribute value 4
	                    // item.setFilter4Value( cell.getStringCellValue() );
	                    if ( cell.getCellType() == Cell.CELL_TYPE_NUMERIC )
	                        item.setFilter4Value( "" + BigDecimal.valueOf( cell.getNumericCellValue() ) );
	                    else if ( !StringUtils.isEmpty( cell.getStringCellValue().trim() ) ) 
	                        item.setFilter4Value( cell.getStringCellValue() );
	                    logger.trace( "filter v4 {}", item.getFilter4Value() );
	                    break;
	                    
	                default:
	                    break;
	            }
	        }

        //item.setUom( "EA" ); //TODO remove this
        item.setRegularPrice( new BigDecimal( 0) );
        item.setSalePrice( new BigDecimal ( 0 ) );
        return item;
        
    }

    private String convertNonAscii ( String text ) {
        
        if ( text == null ) {
            return text;
        }
        
        char transform[] = new char[text.length()];
        int j = 0;
        for ( int i = 0; i < text.length(); i++ ) {
            if ( replaceMap.containsKey( text.charAt( i ) ) ) {
                
                if ( text.charAt( i) != (char)8482 ) { // check for trademark
                    transform[j] = replaceMap.get( text.charAt( i ) );
                    j++;
                }
                
                //logger.debug( "Transform smart quote - " + text );
            } else {
                transform[j] = text.charAt( i );
                j++;
            }
        }
        
        return new String ( transform, 0, j );
    }


    public List<ImportedItem> getItemsRead() {
        return itemsRead;
    }
    
    
}
