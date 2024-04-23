package com.probuild.retail.web.catalog.datasync.utils;

import java.io.IOException;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400SecurityException;
import com.ibm.as400.access.AS400Text;
import com.ibm.as400.access.IFSFile;
import com.ibm.as400.access.IFSTextFileInputStream;
import com.ibm.as400.access.IFSTextFileOutputStream;
import com.ibm.as400.access.ProgramParameter;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * AS400FileAccess.java
 *
 * Copyright 2013. ProBuild Holdings, Inc. All rights reserved.
 *
 * Description: provided the interface to the iSeries for saving files and
 * calling programs (EJB version).
 *
 *
 * @author
 *
 * Description:
 *
 * @author
 *
 * History:
 *  Date            Name            Description
 *  ----------      -------------   -------------------------------
 *  October 20, 2012                <File created>
 *
 */
public class AS400FileAccess {

    // server
    String server = "as400.dxlbr.com";

    // Username, password
    String uname = "samc9190";
    String pword = "MC#9190S@";

    // directory
    public static String AS400_DIR = "/QDLS/MC9190/";

    // AS400 access class
    private AS400 as400sys;

    /**
     * Establish a JTOpen command and program call connection to the AS400
     *
     * Returns false if connection fails.
     */
    private boolean connectToAS400(  ) {

        boolean retVal = false;


        // AS400 access instance
        as400sys = new AS400( server, uname, pword );

        try {
            as400sys.connectService( AS400.COMMAND );
            retVal = true;  
            System.out.println( "connected to AS400 system with user " + uname );
        }
        catch (AS400SecurityException e1) {
            e1.printStackTrace();
        }
        catch (IOException e1) {
            System.out.println ( "Exception: " + e1.getMessage() );
        }

        return retVal;
    }


    /**
     * Terminates a JTOpen command and program call connection to the AS400
     * Returns false if connection fails.
     */
    private void disconnectFromAS400(  ) {

        as400sys.disconnectService( AS400.COMMAND );
        System.out.println( "disconnect to AS400 system" );
    }


    /**
     * Generate a unique file name on the AS400 with input string a prefix.
     *
     * @param filePrefix
     *
     * @return String containing the generated name
     */
    private String generateIFSName( String filePrefix ){


        java.io.File filename = new java.io.File(AS400_DIR,
                filePrefix.substring(0, 4) + (new java.util.Random()).nextInt( 5000 ) );

        System.out.println( "generateIFSName: AS400_DIR: " + AS400_DIR +
                " filename " + filename );

        if (filename.exists()){
            return generateIFSName( filePrefix );
        }
        else{
            return filename.getName();
        }
    }


    /**
     * Remove IFS file from the AS400 with input string a prefix.
     *
     * @param filePrefix
     *
     * @return String containing the generated name
     */
    private boolean removeIFSFile( String fileName ) throws IOException{

        IFSFile aDirectory = new IFSFile(as400sys, AS400_DIR + fileName);

        if (aDirectory.isFile()){
            return aDirectory.delete();
        }
        return false;
    }


    /**
     * Write the object to a file on iSeries
     *
     * @param parmList - AS400 program parameters
     * @param obj
     *
     * @return integer contain success ( 1 ) or failure (-1)
     */
    private boolean writeFileToAS400( String fileName, String data ) {

        boolean retVal = false;

        IFSTextFileOutputStream file;
        try {
            file = new IFSTextFileOutputStream( as400sys, AS400_DIR + fileName, 37);
            file.write( data );
            file.close();
            retVal = true;  // success

        }
        catch (AS400SecurityException e1) {
            System.out.println("AS400SecurityException " + e1.getMessage());
        }
        catch (IOException e1) {
            System.out.println("IOException " + e1.getMessage());
        }

        return retVal;
    }


    /**
     * Store file on IFS folder on AS/400
     *
     */
    public boolean storeFileToAS400(   ) {

        boolean retVal = false;

        // Establish connection to AS/400
        if (!connectToAS400()) {
            System.out.println("Connect to AS400 system failed" );
            return retVal;
        }

        String fileName = generateIFSName( "INVT" );
        
        // Test data
        String data  = " File data";

        // Write the XML file to the AS400
        if (!writeFileToAS400( fileName, data )) {
            System.out.println("Write file to AS400 Failed" );
            return retVal;
        }

        System.out.println( "Writing " + fileName + "to AS400 complete.");

        disconnectFromAS400();

        return true;
    }


    /**
     * Create an AS400Text Program parameter
     *
     * @param length - length of AS400Text in parameter
     * @param value - String value of AS400Text parameter
     *
     * @return ProgramParameter of with length and value
     */
    private ProgramParameter createAS400Text( int length, String value ) {

        AS400Text doctype = new AS400Text( length, as400sys);

        return new ProgramParameter( doctype.toBytes( value ) );
    }


    /**
     * Convert output from EBCDIC to ASCII
     *
     * @param result
     * @return
     */
    private String convertOutput ( byte[] result ){

        AS400Text textConverter =
            new AS400Text( result.length, 37, as400sys );

        return (String) textConverter.toObject(result);
    }

    public static final int NUM_BYTE_READ = 65535;


    /*
     * Copy file from the AS400 source to MC9190 destination
     *
     * @param file to download
     * @param file to create
     */
    public boolean downloadFile(String source, String destination) {

        boolean retVal = false;
        System.out.println("downloadFile - source: " + source +
                           ", destination:  " + destination );

        // IO array
        byte[] data = new byte[NUM_BYTE_READ];
        int fileLoc = 0;

        if (!connectToAS400()) {
            System.out.println("downloadFile -Connect to AS400 system failed" );
            return retVal;
        }

        try {

            IFSTextFileInputStream fileIn = new IFSTextFileInputStream( as400sys, source);
            FileOutputStream fileOut = new FileOutputStream(destination);

            boolean done = false;
            while (!done) {
                if (fileIn.read( data, fileLoc, NUM_BYTE_READ ) == -1)
                    done=true;
                fileOut.write( data );
            }

            fileIn.close();
            fileOut.close();

        }
        catch (AS400SecurityException e1) {
                System.out.println("AS400SecurityException " + e1.getMessage());
        }
        catch (IOException e1) {
                System.out.println("IOException " + e1.getMessage());
        }

        disconnectFromAS400();

        return retVal;
    }


    /*
     * Copy file from the AS400 source to MC9190 destination
     *
     * @param sourceDir - File source directory (with correct terminator)
     * @param sourceFile - file to upload
     * @param destinationDir - File target directory (with correct terminator)
     * @param destinationFile - Target file name
     *
     * @return boolean indicating success or failure
     */
    public boolean uploadFile(String sourceDir, String sourceFile,
            String destinationDir, String destinationFile) {

        boolean retVal = false;
        System.out.println("uploadFile - source: " + sourceDir + sourceFile +
                           ", destination:  " + destinationDir + destinationFile);

        String source =  sourceDir + sourceFile;
        String destination =  destinationDir + destinationFile;

        // IO array
        byte[] bucket = new byte[32 * 1024];
        int fileLoc = 0;

        if (!connectToAS400()) {
            System.out.println("uploadFile - Connect to AS400 system failed" );
            return retVal;
        }

        InputStream inStream = null;
        OutputStream outStream = null;

        try {
            try {

                //FileInputStream fileIn = new FileInputStream( source);
                inStream = new BufferedInputStream(new FileInputStream( source));

                //IFSTextFileOutputStream fileOut = new IFSTextFileOutputStream(as400sys, destination, 37);
                outStream = new BufferedOutputStream(new IFSTextFileOutputStream(as400sys, destination, 37));

                int bytesRead = 0;
                while (bytesRead != -1) {
                    bytesRead = inStream.read( bucket );
                    if (bytesRead > 0) {
                        outStream.write( bucket, 0, bytesRead );
                    }
                }
            }
            catch (AS400SecurityException e1) {
                System.out.println("AS400SecurityException " + e1.getMessage());
            }
            finally {
                inStream.close();
                outStream.close();
            }
        }
        catch (IOException e1) {
            System.out.println("IOException " + e1.getMessage());
        }


        disconnectFromAS400();

        return retVal;
    }


    /*
     * Get the PDA.DB file from the AS400 to local drive
     *
     * @param - Target location code for store DBfile
     */
    public boolean getDBFile(String location) {

        boolean retVal = false;

        String source = AS400_DIR + "LOC" + location + "/" + "PDA.DB";
        String destination = "c:\\PDA.DB";
        System.out.println(" GetDBFile - source : " + source + " Destination " + destination);

        return downloadFile(source, destination);
    }

}
