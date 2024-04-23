package com.probuild.retail.web.catalog.desktop.support;

import java.util.Enumeration;

import javax.swing.JTree;
import javax.swing.text.Position;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

public class JTreeUtil {

    /**
     *	Default constructor
     */
    public JTreeUtil() {
        super();
    }

    public static DefaultMutableTreeNode getSelectedNode ( JTree tree ) {
        
        TreePath selPath = tree.getSelectionPath();
        
        if ( selPath == null )
            return null;
        
        DefaultMutableTreeNode node = 
                 (DefaultMutableTreeNode)selPath.getLastPathComponent();
        
        return node;
        
    } // getSelectedNode

    
    public static void updateNode ( 
                    JTree tree, DefaultMutableTreeNode parent, Object node ) {

        // scroll to child node and make visible on screen
        TreePath path = new TreePath ( parent.getPath() );
        
        tree.getModel().valueForPathChanged( path, node );
        
        //catalogTree.scrollPathToVisible( path );
        
    } // updateNode
    
    
    public static void addNode ( 
                JTree tree, DefaultMutableTreeNode parent, Object node ) {

        
        DefaultMutableTreeNode child = new DefaultMutableTreeNode( node );
        
        
        // insert the node into the model
        DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
        
        model.insertNodeInto( child, parent, parent.getChildCount() );

        
        // scroll to child node and make visible on screen
        TreePath path = new TreePath ( child.getPath() );
   
        tree.scrollPathToVisible( path );
        
        
        
    } // addNode
    
    
    public static void removeNode ( 
                JTree tree, DefaultMutableTreeNode deleteNode, Object node ) {

        // the node to delete was not selected, we need to find it
        if ( deleteNode == null ) {

            // find the node
            TreePath path = tree.getNextMatch( 
                    node.toString(), 0, Position.Bias.Forward );

            if ( path == null )
                return;

            tree.setSelectionPath( path );

            deleteNode = (DefaultMutableTreeNode)path.getLastPathComponent();
        
        } //else {
//
//            // try to get the child
//            TreePath childPath = tree.getNextMatch( 
//                    node.toString(), 0, Position.Bias.Forward );
//
//            if ( childPath == null )
//                return;
//
//            child = (DefaultMutableTreeNode)childPath.getLastPathComponent();
//
//        }


        // remove the node from the model
        DefaultTreeModel model = (DefaultTreeModel)tree.getModel();

        model.removeNodeFromParent( deleteNode );



    } // removeItemNode
    
    
    public static DefaultMutableTreeNode findNodeByName ( JTree tree, Object node ) {
        
        // find the node
        
        TreePath path = tree.getNextMatch( 
                node.toString(), 0, Position.Bias.Forward );

        if ( path == null )
            return null;

        tree.setSelectionPath( path );
 
        return (DefaultMutableTreeNode)path.getLastPathComponent();
        
    }
    
    public static DefaultMutableTreeNode findNode( JTree tree, Object node ) {
        
        DefaultMutableTreeNode nextNode = null;
        DefaultMutableTreeNode root = 
                    (DefaultMutableTreeNode)tree.getModel().getRoot();
        
        Enumeration e = root.breadthFirstEnumeration();
        while(e.hasMoreElements()) {
            nextNode = (DefaultMutableTreeNode)e.nextElement();
            if(node.equals(nextNode.getUserObject() ) ) { 
                return nextNode; 
            }
        }
        return null;
    }
}
