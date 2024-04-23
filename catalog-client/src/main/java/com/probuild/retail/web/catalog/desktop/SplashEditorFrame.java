package com.probuild.retail.web.catalog.desktop;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

import net.miginfocom.swing.MigLayout;

import com.probuild.retail.web.catalog.desktop.util.IconUtil;
import com.probuild.retail.web.catalog.ext.service.WebCatalogService;

public class SplashEditorFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private WebCatalogService service;
    
    private RSyntaxTextArea htmlTextArea;

    private JLabel errorLbl;
    
    
    /**
     *	Default constructor
     */
    public SplashEditorFrame( WebCatalogService service ) {
        super();
        
        this.service = service;

        init();
        
        htmlTextArea.setText( service.getSplashHtml() );
    }

    private void init ( ) {
        
        JPanel layout = new JPanel ( new MigLayout( "fill" ) );
        

        JLabel titleLbl = new JLabel ( 
                "Splash Screen HTML", 
                IconUtil.makeImageIcon( IconUtil.SPLASH_CONTENT, "" ),
                SwingConstants.RIGHT );
        titleLbl.setFont( titleLbl.getFont().deriveFont( 16f ) );
        
        
       
        htmlTextArea = new RSyntaxTextArea();
        htmlTextArea.setName( "HTML Content" );
        htmlTextArea.setText( "" );
        htmlTextArea.setColumns( 100 );
        htmlTextArea.setRows( 30 );
        htmlTextArea.setWrapStyleWord( true );
        htmlTextArea.setLineWrap( false );
        //htmlTextArea.setFont( nameFld.getFont() );
        
        htmlTextArea.setSyntaxEditingStyle( SyntaxConstants.SYNTAX_STYLE_HTML );
        
        JScrollPane valueScroller = new JScrollPane();
        //valueScroller.setPreferredSize( new Dimension ( 400, 400 ) );
        //valueScroller.setMinimumSize( new Dimension ( 100, 100 ) );
        valueScroller.getViewport().setView( htmlTextArea );
        

        // add the two name and value panels to the layout
        layout.add( titleLbl, "wrap, span" );
        layout.add( valueScroller, "push, grow, wrap" );
     
        layout.add ( makeActionButtonPanel(), "right, span" );
        
        
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setTitle( "Edit Splash Screen Content" );
        setLayout( new BorderLayout() );
        add( layout, BorderLayout.CENTER );
        
        setIconImage( IconUtil.makeImageIcon( 
                            IconUtil.BOOK, "Catalog" ).getImage() );
        
        // pack the frame
        pack();
        
    }

    
    private JPanel makeActionButtonPanel ( ) {
        
        JButton cancelBtn = new JButton ( "Cancel" );
        JButton finishBtn = new JButton ( "Save" );
        
        errorLbl = new JLabel ( );
        errorLbl.setIcon( 
                        IconUtil.makeImageIcon( IconUtil.MEDIUM_WARNING, "") );
        errorLbl.setVisible( false );
        
        JPanel btnPanel = new JPanel ( new MigLayout() );
        

        finishBtn.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                finishedAction(evt);
            } 
        });
        cancelBtn.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelAction(evt);
            } 
        });
        
        JPanel actionBtnPanel = new JPanel ( new MigLayout( "fill" ) );
        
        btnPanel.add( cancelBtn );
        btnPanel.add( finishBtn );
        
        actionBtnPanel.add( btnPanel, "right, span" );
        actionBtnPanel.add( errorLbl, "span" );
        
        return actionBtnPanel;
    }
    

     
    
    
    /****************************************************************
     * Actions
     ***************************************************************/
    
    private void finishedAction ( ActionEvent evt ) {
        
        service.saveSplashHtml( htmlTextArea.getText() );
        
        this.dispose();
    }
    
    private void cancelAction ( ActionEvent evt ) {
        
        this.dispose();
    }


    

}
