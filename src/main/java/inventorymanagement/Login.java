package inventorymanagement;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;

public class Login extends JFrame {
    private final JTextField username = new JTextField("admin", 20);
    private final JPasswordField password = new JPasswordField(20);
    private final JButton login = new JButton("Sign in");
    public Login() {
        super("Inventory Management | Sign in"); setDefaultCloseOperation(EXIT_ON_CLOSE); setSize(440,330); setLocationRelativeTo(null); setResizable(false);
        JPanel outer = new JPanel(new BorderLayout()); outer.setBorder(new EmptyBorder(28,40,28,40)); outer.setBackground(Color.WHITE);
        JLabel title = new JLabel("Inventory Management", SwingConstants.CENTER); title.setFont(new Font("SansSerif",Font.BOLD,23)); title.setForeground(new Color(30,80,150));
        JLabel subtitle = new JLabel("Sign in to your workspace", SwingConstants.CENTER); subtitle.setForeground(new Color(100,110,125));
        JPanel headings = new JPanel(new GridLayout(2,1,0,6)); headings.setOpaque(false); headings.add(title); headings.add(subtitle); outer.add(headings,BorderLayout.NORTH);
        JPanel form = new JPanel(new GridLayout(5,1,0,7)); form.setOpaque(false); form.setBorder(new EmptyBorder(24,8,8,8)); form.add(new JLabel("Username")); form.add(username); form.add(new JLabel("Password")); form.add(password);
        login.setBackground(new Color(37,99,180)); login.setForeground(Color.WHITE); login.setFocusPainted(false); form.add(login); outer.add(form,BorderLayout.CENTER); setContentPane(outer);
        login.addActionListener(e -> signIn()); password.addActionListener(e -> signIn());
    }
    private void signIn() {
        String user = username.getText().trim(); char[] secret = password.getPassword();
        if (user.isEmpty() || secret.length == 0) { JOptionPane.showMessageDialog(this,"Enter both username and password.","Missing details",JOptionPane.WARNING_MESSAGE); return; }
        login.setEnabled(false); login.setText("Checking…");
        new SwingWorker<Boolean,Void>() {
            private SQLException error;
            @Override protected Boolean doInBackground() { try { return DBConnection.authenticate(user,secret); } catch(SQLException ex) { error=ex; return false; } }
            @Override protected void done() { login.setEnabled(true); login.setText("Sign in"); try { if (error != null) JOptionPane.showMessageDialog(Login.this,"Database error: "+error.getMessage(),"Sign-in failed",JOptionPane.ERROR_MESSAGE); else if (get()) { dispose(); new Dashboard(user).setVisible(true); } else { password.setText(""); JOptionPane.showMessageDialog(Login.this,"Username or password is incorrect.","Sign-in failed",JOptionPane.ERROR_MESSAGE); } } catch(Exception ex) { JOptionPane.showMessageDialog(Login.this,ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE); } }
        }.execute();
    }
}
