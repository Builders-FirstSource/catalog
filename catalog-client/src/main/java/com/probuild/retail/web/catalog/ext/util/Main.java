package com.probuild.retail.web.catalog.ext.util;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import org.netbeans.validation.api.builtin.Validators;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.netbeans.validation.api.ui.ValidationPanel;

public class Main {
    public static void main(String[] args) {
      //This is our actual UI
      JPanel inner = new JPanel();
      JLabel lbl = new JLabel("Enter a URL");
      JTextField f = new JTextField();
      f.setColumns(40);

      //Setting the component name is important - it is used in
      //error messages
      f.setName("URL");

      inner.add(lbl);
      inner.add(f);

      //Create a ValidationPanel - this is a panel that will show
      //any problem with the input at the bottom with an icon
      ValidationPanel panel = new ValidationPanel();
      panel.setInnerComponent(inner);
      ValidationGroup group = panel.getValidationGroup();

      //This is all we do to validate the URL:
      group.add(f, Validators.REQUIRE_NON_EMPTY_STRING,
              Validators.NO_WHITESPACE,
              Validators.URL_MUST_BE_VALID);
      
      panel.setVisible( true );
      
      JFrame frame = new JFrame();
      frame.add( inner );
      frame.setVisible( true );
      frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
      
      //panel.showOkCancelDialog("URL");
      //Convenience method to show a simple dialog
      //if (panel.showOkCancelDialog("URL")) {
      //  System.out.println("User clicked OK.  URL is " + f.getText());
      //  System.exit(0);
      //} else {
      //  System.err.println("User clicked cancel.");
      //  System.exit(1);
      //}
    }
}
