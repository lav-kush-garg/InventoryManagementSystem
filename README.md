# Inventory & Stock Management System

A simple Java 17, Swing, JDBC and MySQL college project. It provides login, a dashboard, product and supplier management, audited stock-in/out, low-stock highlighting, and purchase-order drafts and decisions.

## Requirements

- JDK 17 or newer
- Apache NetBeans with Maven support
- MySQL Server 8.x running locally
- Maven 3.9+ (or NetBeans' bundled Maven)

## Database setup

The application connects first to the MySQL server, creates `inventory_management` if needed, then creates its tables. It does not clear existing data. Demo suppliers and products are added only if their respective tables are empty. `stock_movements` is an additional audit table required to retain stock-in/out history.

Open `config.properties` in the project folder and edit the settings there before the first launch:

| Setting | Value to enter |
|---|---|
| `MYSQL_HOST` | Usually `localhost` |
| `MYSQL_PORT` | Usually `3306` |
| `MYSQL_USER` | Your MySQL username, often `root` |
| `MYSQL_PASSWORD` | Your MySQL password; leave blank only if MySQL has no password |
| `INVENTORY_ADMIN_USERNAME` | The app login name, default `admin` |
| `INVENTORY_ADMIN_PASSWORD` | Replace `CHANGE_ME_BEFORE_FIRST_RUN` with the password you want for the app login |

The MySQL account must be allowed to create a database and tables. The initial admin password is hashed with BCrypt. On subsequent launches, an existing admin password is never reset.

Keep `config.properties` private. It is excluded by the project's `.gitignore` so it will not be committed to Git. The initial admin password is hashed with BCrypt before it is stored in the database. On subsequent launches, an existing admin password is never reset.

## Open and run in NetBeans

Choose **File → Open Project**, select the `InventoryManagementSystem` folder, and allow Maven to load dependencies. Edit `config.properties` in the project folder, save it, then run `inventorymanagement.Main` from NetBeans. From a terminal you can run `mvn clean test` and `mvn package`; to launch via Maven use `mvn exec:java -Dexec.mainClass=inventorymanagement.Main`.

## Initial login

On the first run, the configured username (default `admin`) is created using the password in `config.properties`. Change the placeholder before first launch. Keep the configured secret safe; the app does not expose password reset functionality.
