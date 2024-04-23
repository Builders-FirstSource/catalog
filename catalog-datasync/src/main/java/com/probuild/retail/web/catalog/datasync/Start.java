package com.probuild.retail.web.catalog.datasync;

import java.io.File;
import java.net.URL;
import java.security.ProtectionDomain;

import org.apache.commons.dbcp.BasicDataSource;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.webapp.WebAppContext;

public class Start {

    public static final String DATASYNC_HOME_KEY = "dashboard.home";
    public static final String DATASYNC_PORT_KEY = "dashboard.port";
    
    public static void main(String[] args) throws Exception {
        
        new Start().startServer();
    }


    public Start ( ) {
        
    }
    
    public void startServer ( ) {
        String port = "8081";
        
        // see if the DATASYNC_HOME JVM property was set
        if ( System.getProperty( DATASYNC_HOME_KEY ) == null ) {
            System.out.println ( "  *Consider passing -Ddashboard.home=<path> to the jvm" );
            System.setProperty( DATASYNC_HOME_KEY, "datasync" );
            setupHome( "" );
        } else {
            File home = new File ( System.getProperty( DATASYNC_HOME_KEY ) );
            
            if ( !home.exists() ) {
                setupHome( home.getPath() );
            }
        }
        
        port = System.getProperty( DATASYNC_PORT_KEY, "8081" );
        
        Server server = new Server();
        SocketConnector connector = new SocketConnector();

        // Set some timeout options to make debugging easier.
        connector.setMaxIdleTime(1000 * 60 * 60);
        connector.setSoLingerTime(-1);
        connector.setPort( Integer.parseInt( port ) );
        server.setConnectors(new Connector[] {connector});

        WebAppContext context = new WebAppContext();
        context.setServer(server);
        context.setContextPath("/");

        ProtectionDomain protectionDomain = Start.class.getProtectionDomain();
        URL location = protectionDomain.getCodeSource().getLocation();
        context.setWar(location.toExternalForm());

        server.addHandler(context);
        
       
        try {
            String home = System.getProperty( DATASYNC_HOME_KEY );
            // Create the DataSource service
            BasicDataSource ds = new BasicDataSource();
            ds.setUrl("jdbc:hsqldb:file:" + home + "/db/dashboard.db" );
            ds.setDriverClassName("org.hsqldb.jdbcDriver");
            ds.setUsername("sa");
            ds.setPassword("");
            
            //org.mortbay.jetty.plus.naming.NamingEntry.setScope(NamingEntry.SCOPE_GLOBAL);
            // This actually registers the resource
            new org.mortbay.jetty.plus.naming.Resource( "jdbc/dashboardDB", ds );
            
            server.start();
            System.in.read();
            server.stop();
            server.join();
        }
        catch(Exception e) {
            e.printStackTrace();
            System.exit(100);
        }
        
        
    }
    
    private void setupHome ( String prefix ) {
        File home = new File ( prefix + "datasync" );
        File db = new File ( prefix + "datasync/db" );
        File scripts = new File ( prefix + "datasync/scripts" );
        File history = new File ( prefix + "datasync/history" );
        
        home.mkdir();
        db.mkdir();
        scripts.mkdir();
        history.mkdir();
        
    }
    
}
