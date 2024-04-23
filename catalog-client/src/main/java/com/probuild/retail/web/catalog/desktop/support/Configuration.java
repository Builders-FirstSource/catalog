package com.probuild.retail.web.catalog.desktop.support;

import java.io.File;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Configuration {
    private static final Logger logger = 
                        LoggerFactory.getLogger(Configuration.class);
    
    private XMLConfiguration config;
    public static String defaultFilePath = System.getProperty( "user.home" );
    
    /**
     *	Default constructor
     */
    public Configuration() {
        super();
    }
    
    /**
     * Checks users home directory for previous config file.
     * 
     * @return
     */
    public boolean isConfigFilePresent ( ) {
        
        File configFile = getConfigFile();
        
        if ( configFile.exists() )
            return true;
        
        return false;
    }
    
    public void loadConfig ( ) {
        
        if ( !isConfigFilePresent() ) {
            throw new RuntimeException ( "No config file found to load" );
        }
        
        File configFile = getConfigFile ( );
        
        try {
            config = new XMLConfiguration( configFile );
            config.load();
        }
        catch(ConfigurationException e) {
            logger.error( "Failed to load config", e );
        }
        
    }
    
    public void saveConfig ( ) {
        
        try {
            if ( config == null ) { // no config initialized yet
                File configFile = getConfigFile ( );
                config = new XMLConfiguration( configFile );
            }
            
            config.save();
        }
        catch(ConfigurationException e) {
            logger.error( "Failed to save config", e );
        }
    }
    
    public String getImageFolder ( ) {
        return config.getString( "imageFolder", "" );
    }
    public void setImageFolder ( String imgFolder ) {
        config.setProperty( "imageFolder", imgFolder );
    }
    
    private File getConfigFile ( ) {
        String userHomeDir = System.getProperty( "user.home" );
        File configFile = 
            new File ( userHomeDir + "/.catalogAdminSettings.xml" );
        
        return configFile;
    }
    
    public String getCatalogUser ( ) {
        return config.getString( "catalog.user", "" );
    }
    public void setCatalogUser ( String user ) {
        config.setProperty( "catalog.user", user );
    }

    public String getCatalogUserPass ( ) {
        return config.getString( "catalog.password", "" );
    }
    public void setCatalogUserPass ( String pass ) {
        config.setProperty( "catalog.password", pass );
    }
    
    public String getLegacyConnectionString ( ) {
        return config.getString( "legacy.url", "" );
    }
    public void setLegacyConnectionString ( String url ) {
        config.setProperty( "legacy.url", url );
    }
    
    public String getLegacyUser ( ) {
        return config.getString( "legacy.user", "" );
    }
    public void setLegacyUser ( String user ) {
        config.setProperty( "legacy.user", user );
    }

    public String getLegacyUserPass ( ) {
        return config.getString( "legacy.password", "" );
    }
    public void setLegacyUserPass ( String pass ) {
        config.setProperty( "legacy.password", pass );
    }
    
    public XMLConfiguration getConfig ( ) {
        return config;
    }
}
