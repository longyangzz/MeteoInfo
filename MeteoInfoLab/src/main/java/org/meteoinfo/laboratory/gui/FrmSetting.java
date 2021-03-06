/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.laboratory.gui;

import java.io.IOException;
import org.fife.ui.rsyntaxtextarea.Theme;

/**
 *
 * @author yaqiang
 */
public class FrmSetting extends javax.swing.JDialog {
    
    private FrmMain parent;

    /**
     * Creates new form FrmSetting
     */
    public FrmSetting(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        this.parent = (FrmMain)parent;
        
        initComponents();
        
        this.jComboBox_Theme.removeAllItems();
        this.jComboBox_Theme.addItem("default");        
        this.jComboBox_Theme.addItem("dark");
        this.jComboBox_Theme.addItem("eclipse");
        this.jComboBox_Theme.addItem("idea");
        this.jComboBox_Theme.addItem("monokai");
        this.jComboBox_Theme.addItem("vs");
        this.jComboBox_Theme.addItem("default-alt");
        String themeName = this.parent.getOptions().getEditorTheme();
        this.jComboBox_Theme.setSelectedItem(themeName);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel_Editor = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jComboBox_Theme = new javax.swing.JComboBox<>();
        jButton_Apply = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jLabel1.setText("Theme");

        jComboBox_Theme.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox_ThemeActionPerformed(evt);
            }
        });

        jButton_Apply.setText("Apply");
        jButton_Apply.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_ApplyActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel_EditorLayout = new javax.swing.GroupLayout(jPanel_Editor);
        jPanel_Editor.setLayout(jPanel_EditorLayout);
        jPanel_EditorLayout.setHorizontalGroup(
            jPanel_EditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel_EditorLayout.createSequentialGroup()
                .addGroup(jPanel_EditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel_EditorLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboBox_Theme, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel_EditorLayout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addComponent(jButton_Apply, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(142, Short.MAX_VALUE))
        );
        jPanel_EditorLayout.setVerticalGroup(
            jPanel_EditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel_EditorLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(jPanel_EditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jComboBox_Theme, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 89, Short.MAX_VALUE)
                .addComponent(jButton_Apply)
                .addGap(17, 17, 17))
        );

        jTabbedPane1.addTab("Editor", jPanel_Editor);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.Alignment.TRAILING)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.Alignment.TRAILING)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jComboBox_ThemeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox_ThemeActionPerformed
        // TODO add your handling code here:
        
    }//GEN-LAST:event_jComboBox_ThemeActionPerformed

    private void jButton_ApplyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_ApplyActionPerformed
        // TODO add your handling code here:
        String theme = (String)this.jComboBox_Theme.getSelectedItem();
        this.changeStyleViaThemeXml(theme);
        this.parent.getOptions().setEditorTheme(theme);
    }//GEN-LAST:event_jButton_ApplyActionPerformed

    /**
    * Changes the styles used by the editor via an XML file specification. This
    * method is preferred because of its ease and modularity.
    */
   private void changeStyleViaThemeXml(String themeName) {
      try {
         Theme theme = Theme.load(getClass().getResourceAsStream(
               "/org/fife/ui/rsyntaxtextarea/themes/" +  themeName + ".xml"));
         for (TextEditor textEditor : this.parent.getEditorDock().getAllTextEditor())
            theme.apply(textEditor.getTextArea());
      } catch (IOException ioe) { // Never happens
         ioe.printStackTrace();
      }
   }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(FrmSetting.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FrmSetting.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FrmSetting.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FrmSetting.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                FrmSetting dialog = new FrmSetting(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton_Apply;
    private javax.swing.JComboBox<String> jComboBox_Theme;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel_Editor;
    private javax.swing.JTabbedPane jTabbedPane1;
    // End of variables declaration//GEN-END:variables
}
