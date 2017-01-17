/* @(#)StructPanel.java
 * Copyright © 1999 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */
package ru.sbtqa.monte.media.binary;

/**
 * Panel for structured binary data.
 *
 * @author Werner Randelshofer, Hausmatt 10, CH-6405 Goldau, Switzerland
 * @version 1.0.1 2002-02-05 Class StructModel has been renamed to
 * StructTableModel.
 * <br>1.0 2000-06-12
 */
public class StructPanel extends javax.swing.JPanel {

    private final static long serialVersionUID = 1L;

    /**
     * Initializes the Form
     */
    public StructPanel() {
        initComponents();
    }

    public void setModel(StructTableModel model) {
        table.setModel(model);
        table.sizeColumnsToFit(-1);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the FormEditor.
     */
    private void initComponents () {//GEN-BEGIN:initComponents
        setLayout (new java.awt.BorderLayout ());

        scrollPane = new javax.swing.JScrollPane ();

            table = new javax.swing.JTable ();
            table.setRowSelectionAllowed (false);

        scrollPane.setViewportView (table);
        add (scrollPane, "Center");

    }//GEN-END:initComponents

// Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JTable table;
// End of variables declaration//GEN-END:variables

}
