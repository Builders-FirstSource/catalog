package com.probuild.retail.web.catalog.datasync;

import org.python.util.PythonInterpreter;

public class PyhtonTest {

    /**
     *	Default constructor
     */
    public PyhtonTest() {
        super();
    }


    /**
     * @param args
     */
    public static void main(String[] args) {
        System.out.println ( "starting python" );
        PythonInterpreter interp = new PythonInterpreter();
        System.out.println ( "done" );

    }

}
