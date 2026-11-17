package inventorymanagement;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(new FlatLightLaf()); }
            catch (Exception ignored) { }
            JFrame loading = new JFrame("Inventory Management");
            JLabel message = new JLabel("Connecting to database…", SwingConstants.CENTER);
            message.setFont(message.getFont().deriveFont(16f)); loading.add(message, BorderLayout.CENTER);
            loading.setSize(380,120); loading.setLocationRelativeTo(null); loading.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE); loading.setVisible(true);
            new SwingWorker<Void, Void>() {
                private Exception error;
                @Override protected Void doInBackground() { try { DBConnection.initialize(); } catch (SQLException ex) { error = ex; } return null; }
                @Override protected void done() {
                    loading.dispose();
                    if (error != null) { JOptionPane.showMessageDialog(null, error.getMessage(), "Database setup failed", JOptionPane.ERROR_MESSAGE); System.exit(1); }
                    new Login().setVisible(true);
                }
            }.execute();
        });
    }
}
