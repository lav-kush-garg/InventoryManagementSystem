package inventorymanagement;

import java.sql.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mindrot.jbcrypt.BCrypt;

/** Owns database setup and provides short-lived JDBC connections. */
public final class DBConnection {
    private static final Logger LOG = Logger.getLogger(DBConnection.class.getName());
    private static final String DB = "inventory_management";
    private static final Properties CONFIG = loadConfig();
    private DBConnection() { }

    /** Loads editable settings from the project-root config.properties file. */
    private static Properties loadConfig() {
        Properties properties = new Properties();
        Path configPath = Path.of(System.getProperty("user.dir"), "config.properties");
        if (Files.isRegularFile(configPath)) {
            try (var input = Files.newInputStream(configPath)) {
                properties.load(input);
            } catch (IOException ex) {
                throw new ExceptionInInitializerError("Cannot read " + configPath + ": " + ex.getMessage());
            }
        }
        return properties;
    }
    private static String env(String key, String fallback) {
        return CONFIG.getProperty(key, System.getenv().getOrDefault(key, fallback)).trim();
    }
    private static String serverUrl() {
        return "jdbc:mysql://" + env("MYSQL_HOST", "localhost") + ":" + env("MYSQL_PORT", "3306")
                + "/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    }
    private static String databaseUrl() {
        return "jdbc:mysql://" + env("MYSQL_HOST", "localhost") + ":" + env("MYSQL_PORT", "3306")
                + "/" + DB + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    }
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(databaseUrl(), env("MYSQL_USER", "root"), env("MYSQL_PASSWORD", ""));
    }

    public static void initialize() throws SQLException {
        String user = env("MYSQL_USER", "root"), password = env("MYSQL_PASSWORD", "");
        try (Connection c = DriverManager.getConnection(serverUrl(), user, password);
             Statement s = c.createStatement()) {
            s.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB + " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, "Unable to connect to MySQL or create the application database", ex);
            throw new SQLException("Could not connect to MySQL. Check that MySQL Server is running and MYSQL_HOST, MYSQL_PORT, MYSQL_USER and MYSQL_PASSWORD are correct. " + ex.getMessage(), ex);
        }
        try (Connection c = getConnection(); Statement s = c.createStatement()) {
            s.executeUpdate("CREATE TABLE IF NOT EXISTS users (user_id INT AUTO_INCREMENT PRIMARY KEY, username VARCHAR(80) NOT NULL UNIQUE, password_hash VARCHAR(100) NOT NULL, role VARCHAR(20) NOT NULL DEFAULT 'ADMIN')");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS suppliers (supplier_id INT AUTO_INCREMENT PRIMARY KEY, supplier_name VARCHAR(120) NOT NULL, phone VARCHAR(40), email VARCHAR(120), address VARCHAR(255))");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS products (product_id INT AUTO_INCREMENT PRIMARY KEY, product_name VARCHAR(120) NOT NULL, category VARCHAR(80) NOT NULL, supplier_id INT, quantity INT NOT NULL DEFAULT 0, min_stock INT NOT NULL DEFAULT 0, price DECIMAL(12,2) NOT NULL DEFAULT 0, CONSTRAINT fk_product_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(supplier_id) ON DELETE SET NULL)");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS purchase_orders (po_id INT AUTO_INCREMENT PRIMARY KEY, product_id INT NOT NULL, supplier_id INT, order_quantity INT NOT NULL, total_amount DECIMAL(12,2) NOT NULL, status VARCHAR(20) NOT NULL DEFAULT 'DRAFT', created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, CONSTRAINT fk_po_product FOREIGN KEY (product_id) REFERENCES products(product_id), CONSTRAINT fk_po_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(supplier_id))");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS stock_movements (movement_id INT AUTO_INCREMENT PRIMARY KEY, product_id INT NOT NULL, movement_type VARCHAR(10) NOT NULL, quantity INT NOT NULL, note VARCHAR(255), created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, CONSTRAINT fk_movement_product FOREIGN KEY (product_id) REFERENCES products(product_id))");
            createAdminIfNeeded(c);
            seedIfEmpty(c);
        }
    }
    private static void createAdminIfNeeded(Connection c) throws SQLException {
        try (PreparedStatement check = c.prepareStatement("SELECT 1 FROM users WHERE username=?")) {
            String username = env("INVENTORY_ADMIN_USERNAME", "admin"); check.setString(1, username);
            try (ResultSet rs = check.executeQuery()) { if (rs.next()) return; }
            String initial = env("INVENTORY_ADMIN_PASSWORD", "");
            if (initial.isBlank() || initial.equals("CHANGE_ME_BEFORE_FIRST_RUN"))
                throw new SQLException("No admin account exists. Open config.properties and replace CHANGE_ME_BEFORE_FIRST_RUN with a password before launching.");
            try (PreparedStatement insert = c.prepareStatement("INSERT INTO users(username,password_hash,role) VALUES(?,?, 'ADMIN')")) {
                insert.setString(1, username); insert.setString(2, BCrypt.hashpw(initial, BCrypt.gensalt(12))); insert.executeUpdate();
            }
        }
    }
    private static void seedIfEmpty(Connection c) throws SQLException {
        try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM suppliers")) {
            rs.next(); if (rs.getInt(1) == 0) try (PreparedStatement p = c.prepareStatement("INSERT INTO suppliers(supplier_name,phone,email,address) VALUES(?,?,?,?)")) {
                p.setString(1,"Campus Supplies"); p.setString(2,"9000000001"); p.setString(3,"supplies@example.com"); p.setString(4,"Local supplier"); p.executeUpdate();
            }
        }
        try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM products")) {
            rs.next(); if (rs.getInt(1) == 0) try (PreparedStatement p = c.prepareStatement("INSERT INTO products(product_name,category,supplier_id,quantity,min_stock,price) SELECT ?,?,supplier_id,?,?,? FROM suppliers ORDER BY supplier_id LIMIT 1")) {
                p.setString(1,"Notebook"); p.setString(2,"Stationery"); p.setInt(3,40); p.setInt(4,10); p.setBigDecimal(5,new java.math.BigDecimal("45.00")); p.executeUpdate();
            }
        }
    }
    public static boolean authenticate(String username, char[] password) throws SQLException {
        try (Connection c = getConnection(); PreparedStatement p = c.prepareStatement("SELECT password_hash FROM users WHERE username=?")) {
            p.setString(1, username);
            try (ResultSet rs = p.executeQuery()) { return rs.next() && BCrypt.checkpw(new String(password), rs.getString(1)); }
        } finally { java.util.Arrays.fill(password, '\0'); }
    }
}
