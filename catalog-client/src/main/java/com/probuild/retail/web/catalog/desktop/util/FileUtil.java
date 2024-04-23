package com.probuild.retail.web.catalog.desktop.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

public class FileUtil {

    /**
     *	Default constructor
     */
    public FileUtil() {
        super();
    }

    public static byte[] getFileBytes ( String path ) {

        byte[] bytes = null;

        InputStream in;
        try {
            in = new FileInputStream ( new File ( path ) );
            bytes = IOUtils.toByteArray( in );
            /*
             * @Comment: Closing InputStream (To fix Image replace issue)
             */
            in.close();
        }
        catch(FileNotFoundException e) {
            System.out.println ( "Failed to read file " + e.getMessage() );;
        }
        catch(IOException e) {
            System.out.println ( "Failed to read file " + e.getMessage() );
        }


        return bytes;
    }

}
