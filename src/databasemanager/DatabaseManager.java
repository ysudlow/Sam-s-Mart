package databasemanager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import product.java.Product;

public class DatabaseManager implements AutoCloseable {
    private Connection connection;

    LocalDate localDate = LocalDate.now(); // Example LocalDate
    java.sql.Date sqlDate = java.sql.Date.valueOf(localDate); // Converting LocalDate to java.sql.Date

    // Constructor that sets up the connection to the MySQL database
    public DatabaseManager() {
        try {
            String url = "jdbc:mysql://localhost:3306/applicationdb";
            String user = "root";
            String password = "password";
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(url, user, password);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("JDBC Driver not found: " + e.getMessage(), e);
        } catch (SQLException e) {
            throw new RuntimeException("Unable to connect to the database: " + e.getMessage(), e);
        }
    }

    // Check if a user exists by email
    public boolean userExists(String email) throws SQLException {
        String query = "SELECT COUNT(*) AS count FROM users WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count") > 0;
                }
            }
        }
        return false;
    }

    public void addUser(User newUser) throws SQLException {
        // Assuming 'connection' is a class member of type java.sql.Connection that has
        // been initialized elsewhere
        // Start a transaction
        connection.setAutoCommit(false);
        try {
            // Prepare the SQL statement for inserting a new user
            String insertQuery = "INSERT INTO users (first_name, last_name, phone_number, email, password, role) VALUES (?, ?, ?, ?, ?, ?)";

            // Create a PreparedStatement for executing the query
            try (PreparedStatement stmt = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) { // ID
                // Set the parameters for the PreparedStatement
                stmt.setString(1, newUser.getFirstName());
                stmt.setString(2, newUser.getLastName());
                stmt.setString(3, newUser.getPhoneNumber());
                stmt.setString(4, newUser.getEmail());
                stmt.setString(5, newUser.getPassword()); // Ensure that you hash the password before storing it
                stmt.setString(6, newUser.getRole().toString());

                // Execute the update
                int affectedRows = stmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Creating user failed, no rows affected.");
                }

                // Retrieve the generated key (user ID)
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        newUser.setUserId(generatedKeys.getInt(1)); // Assuming User class has a setId method to store
                                                                    // the ID
                    } else {
                        throw new SQLException("Creating user failed, no ID obtained.");
                    }
                }

                // Commit the transaction
                connection.commit();
            } catch (SQLException e) {
                // Rollback the transaction in case of an error
                if (connection != null) {
                    connection.rollback();
                }
                // Re-throw the exception to be handled elsewhere
                throw e;
            }
        } finally {
            // Reset auto-commit to its default state
            if (connection != null) {
                connection.setAutoCommit(true);
            }
        }
    }

    // Check if a user ID already exists in the database
    public boolean userIdExists(int userId) throws SQLException {
        String query = "SELECT COUNT(*) AS count FROM users WHERE user_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count") > 0; // If count is greater than 0, the user ID exists
                }
            }
        }
        return false; // If no count is found, the user ID does not exist
    }

    // Get a user by email
    public User getUserByEmail(String email) throws SQLException {
        String query = "SELECT * FROM users WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getInt("user_id"),
                            rs.getString("first_name"),
                            rs.getString("last_name"),
                            rs.getString("phone_number"),
                            rs.getString("email"),
                            rs.getString("password"),
                            UserRole.valueOf(rs.getString("role")));
                }
            }
        }
        return null;
    }

    public User authenticateUser(String email, String phoneNumber) throws SQLException {
        // The query should select a user where the email and password match
        String query = "SELECT * FROM users WHERE email = ? AND password = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, email);
            stmt.setString(2, phoneNumber); // This assumes you are storing the phone number in plain text as a
                                            // password, which is not recommended for real applications

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // If a row is returned, the user is authenticated
                    return new User(
                            rs.getInt("user_id"),
                            rs.getString("first_name"),
                            rs.getString("last_name"),
                            rs.getString("phone_number"),
                            rs.getString("email"),
                            rs.getString("password"),
                            UserRole.valueOf(rs.getString("role")));
                }
            }
        }
        // If no row is returned, then the user is not found or password does not match
        return null;
    }

    // Assign manager role via admin
    public void assignManagerRole(String email) throws SQLException {
        updateUserRole(email, UserRole.MANAGER);
    }

    // Update a user's role
    public void updateUserRole(String email, UserRole newRole) throws SQLException {
        String update = "UPDATE users SET role = ? WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(update)) {
            stmt.setString(1, newRole.toString());
            stmt.setString(2, email);
            stmt.executeUpdate();
        }
    }

    // Admin delete users method
    public void deleteUserByEmail(String email) throws SQLException {
        String sql = "DELETE FROM users WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.executeUpdate();
        }
    }

    // Method to retrieve all users from the database
    public List<User> getAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM users";
        try (Connection connection = this.getConnection();
                Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                User user = new User(
                        rs.getInt("user_id"), // UserID
                        rs.getString("first_name"), // FirstName
                        rs.getString("last_name"), // LastName
                        rs.getString("phone_number"), // PhoneNumber
                        rs.getString("email"), // Email
                        rs.getString("password"), // Password
                        UserRole.valueOf(rs.getString("role")) // Role
                );

                users.add(user);
            }
        }
        return users;
    }

    // Method to retrieve all products from the database
    public List<Product> getAllProducts() throws SQLException {
        List<Product> products = new ArrayList<>();
        String query = "SELECT * FROM Product"; // Adjust the table name and columns as necessary
        try (Connection connection = getConnection(); // Ensure you have a connection here
                Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                LocalDate expirationDate = null;
                if (rs.getDate("expirationDate") != null) {
                    expirationDate = rs.getDate("expirationDate").toLocalDate();
                }

                LocalDate markdownDate = null;
                if (rs.getDate("markdownDate") != null) {
                    markdownDate = rs.getDate("markdownDate").toLocalDate();
                }

                // Assuming Product constructor is properly defined to accept these arguments
                Product product = new Product(
                        rs.getString("productName"),
                        expirationDate,
                        markdownDate,
                        rs.getInt("quantity"),
                        rs.getString("manufacturer"),
                        rs.getString("brand"),
                        rs.getDouble("price"),
                        rs.getString("category"));
                product.setProductID(rs.getInt("productID"));
                products.add(product);
            }
        }
        return products;
    }

    // Method to add products
    public static void addProduct(Scanner scanner) throws SQLException {
        System.out.println("Adding a new product...");
        try (DatabaseManager dbManager = new DatabaseManager()) {
            Connection connection = dbManager.getConnection();

            connection.setAutoCommit(false);

            // Prompt user for product details
            System.out.print("Enter product name: ");
            String productName = scanner.nextLine();

            System.out.print("Enter expiration date (YYYY-MM-DD) or press Enter if none: ");
            String expirationDateInput = scanner.nextLine();
            LocalDate expirationDate = null;
            if (!expirationDateInput.isEmpty()) {
                expirationDate = LocalDate.parse(expirationDateInput);
            }

            System.out.print("Enter markdown date (YYYY-MM-DD) or press Enter if none: ");
            String markdownDateInput = scanner.nextLine();
            LocalDate markdownDate = null;
            if (!markdownDateInput.isEmpty()) {
                markdownDate = LocalDate.parse(markdownDateInput);
            }

            System.out.print("Enter quantity: ");
            int quantity = scanner.nextInt();
            scanner.nextLine(); // consume the newline left behind by nextInt()

            System.out.print("Enter manufacturer: ");
            String manufacturer = scanner.nextLine();

            System.out.print("Enter brand: ");
            String brand = scanner.nextLine();

            System.out.print("Enter category: ");
            String category = scanner.nextLine();

            System.out.print("Enter price: ");
            double price = scanner.nextDouble();
            scanner.nextLine(); // consume the newline

            // Assume the Product constructor takes these parameters in the same order
            Product product = new Product(productName, expirationDate, markdownDate, quantity, manufacturer, brand,
                    price, category);

            // are closed automatically
            connection.setAutoCommit(false); // Ensure auto-commit is off if handling transactions manually

            String insert = "INSERT INTO product (productName, expirationDate, markdownDate, quantity, manufacturer, brand, price, category) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
                // Set the prepared statement parameters with product details
                stmt.setString(1, product.getProductName()); // productName
                if (expirationDate != null) {
                    stmt.setDate(2, java.sql.Date.valueOf(expirationDate)); // expirationDate
                } else {
                    stmt.setNull(2, Types.DATE);
                }
                if (markdownDate != null) {
                    stmt.setDate(3, java.sql.Date.valueOf(markdownDate)); // markdownDate
                } else {
                    stmt.setNull(3, Types.DATE);
                }
                stmt.setInt(4, quantity); // quantity
                stmt.setString(5, manufacturer); // manufacturer
                stmt.setString(6, brand); // brand
                stmt.setDouble(7, price); // price
                stmt.setString(8, category); // category

                // Execute the update
                int affectedRows = stmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Inserting product failed, no rows affected.");
                }

                // If you need to retrieve generated keys, do it here

                connection.commit();
            } catch (SQLException e) {
                // Rollback transaction if exception occurs
                if (connection != null) {
                    connection.rollback();
                }
                System.out.println("SQL Error: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void deleteProduct(int productID) throws SQLException {
        String delete = "DELETE FROM product WHERE productID = ?"; // Ensure 'productID' is the correct column name
        try (PreparedStatement stmt = connection.prepareStatement(delete)) {
            stmt.setInt(1, productID);
            stmt.executeUpdate();
        }
    }

    // Method to update the quantity of a product in the database
    public boolean updateProductQuantity(int productId, int newQuantity) throws SQLException {
        String updateSql = "UPDATE product SET quantity = ? WHERE productID = ?";
        try (PreparedStatement stmt = this.connection.prepareStatement(updateSql)) {
            stmt.setInt(1, newQuantity);
            stmt.setInt(2, productId);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            // You may want to log this exception or handle it as per your application's
            // requirements.
            throw e; // Re-throwing the exception to indicate failure
        }
    }

    public void viewAllStores() throws SQLException {
        String query = "SELECT * FROM stores";
        try (Connection conn = this.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {

            // Print table header
            System.out.println("\033[35m" + // ANSI purple text color
                    String.format("%-10s %-20s %-20s %-15s %-15s %-10s %-15s %-15s %-15s",
                            "Store ID", "Store Name", "Address", "City", "State", "ZIP", "Phone", "Store Type",
                            "Opening Date")
                    +
                    "\033[0m"); // Reset color

            // Print table rows
            while (rs.next()) {
                int storeId = rs.getInt("store_id");
                String storeName = rs.getString("store_name");
                String address = rs.getString("address");
                String city = rs.getString("city");
                String state = rs.getString("state");
                int zip = rs.getInt("zip");
                String phone = rs.getString("phone");
                String storeType = rs.getString("store_type");
                java.sql.Date openingDate = rs.getDate("opening_date");

                System.out.println("\033[35m" + // ANSI purple text color
                        String.format("%-10d %-20s %-20s %-15s %-15s %-10d %-15s %-15s %-15tF",
                                storeId, storeName, address, city, state, zip, phone, storeType, openingDate)
                        +
                        "\033[0m"); // Reset color
            }
        }
    }

    public boolean addStore(int storeId, String storeName, String address, String city, String state, int zip,
            String phone, String storeType, LocalDate openingDate) {
        String query = "INSERT INTO stores (store_id, store_name, address, city, state, zip, phone, store_type, opening_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = this.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, storeId);
            pstmt.setString(2, storeName);
            pstmt.setString(3, address);
            pstmt.setString(4, city);
            pstmt.setString(5, state);
            pstmt.setInt(6, zip);
            pstmt.setString(7, phone);
            pstmt.setString(8, storeType);
            pstmt.setDate(9, Date.valueOf(openingDate));

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateStore(int storeId, String storeName, String address, String city, String state, int zip,
            String phone, String storeType) {
        String query = "UPDATE stores SET store_name = ?, address = ?, city = ?, state = ?, zip = ?, phone = ?, store_type = ? WHERE store_id = ?";
        try (Connection conn = this.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, storeName);
            pstmt.setString(2, address);
            pstmt.setString(3, city);
            pstmt.setString(4, state);
            pstmt.setInt(5, zip);
            pstmt.setString(6, phone);
            pstmt.setString(7, storeType);
            pstmt.setInt(8, storeId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteStore(int storeId) {
        String query = "DELETE FROM stores WHERE store_id = ?";
        try (Connection conn = this.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, storeId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Product> getExpiredProducts() throws SQLException {
        List<Product> expiredProducts = new ArrayList<>();
        String query = "SELECT productID, productName, expirationDate FROM product WHERE expirationDate < CURDATE()";

        try (Connection connection = this.getConnection();
                PreparedStatement pstmt = connection.prepareStatement(query);
                ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                int productId = rs.getInt("productID");
                String productName = rs.getString("productName");
                LocalDate expirationDate = rs.getDate("expirationDate").toLocalDate();

                // Create a Product object with the retrieved values
                Product product = new Product(productId, productName, expirationDate);
                expiredProducts.add(product);
            }

        }
        return expiredProducts;
    }

    public Connection getConnection() {
        return connection;
    }

    // getMarkdownProducts method
    public List<Product> getMarkdownProducts() throws SQLException {
        List<Product> products = new ArrayList<>();

        String sql = "SELECT * FROM product WHERE expirationDate BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 1 MONTH)";

        try (Connection conn = this.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    // Assuming you have a constructor in Product class that takes ResultSet
                    Product product = new Product(rs);
                    products.add(product);
                }
            }
        }
        return products;
    }

    public List<PurchaseOrder> getAllPurchaseOrders() throws SQLException {
        List<PurchaseOrder> purchaseOrders = new ArrayList<>();
        String query = "SELECT purchaseOrderID, orderDate, deliverydate, /* other columns */ FROM purchase_orders";

        try (Connection connection = this.getConnection();
                PreparedStatement pstmt = connection.prepareStatement(query);
                ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                int purchaseOrderID = rs.getInt("purchaseOrderID");

                // Retrieve the orderDate column as java.sql.Date
                java.sql.Date orderDate = rs.getDate("orderDate");
                LocalDate localOrderDate = (orderDate != null) ? orderDate.toLocalDate() : null;

                // Retrieve the deliverydate column as java.sql.Date
                java.sql.Date deliveryDate = rs.getDate("deliverydate");
                LocalDate localDeliveryDate = (deliveryDate != null) ? deliveryDate.toLocalDate() : null;

                // Create a PurchaseOrder object with the retrieved values
                PurchaseOrder purchaseOrder = new PurchaseOrder(purchaseOrderID, localOrderDate, localDeliveryDate /*
                                                                                                                    * other
                                                                                                                    * values
                                                                                                                    */);
                purchaseOrders.add(purchaseOrder);
            }
        }
        return purchaseOrders;
    }

    public boolean addPurchaseOrder(PurchaseOrder newPurchaseOrder, int quantity, LocalDate orderDate)
            throws SQLException {
        int poNumber = generateRandomPoNumber();
        String trackingNumber = generateRandomTrackingNumber();

        String query = "INSERT INTO purchase_orders (po_number, productID, quantity, order_date, tracking_number) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = this.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, poNumber);
            pstmt.setInt(2, newPurchaseOrder.getProductID()); // Extract productID from newPurchaseOrder
            pstmt.setInt(3, quantity);
            pstmt.setDate(4, Date.valueOf(orderDate));
            pstmt.setString(5, trackingNumber);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            handleSQLException(e);
        }
        return false;
    }

    public Product getProductByID(int productID) throws SQLException {
        String query = "SELECT * FROM product WHERE productID = ?";
        try (Connection conn = this.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, productID);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Product product = new Product();
                    product.setProductID(rs.getInt("productID"));
                    product.setDescription(rs.getString("description"));
                    // Set other product attributes as needed

                    // Check for null values and convert to LocalDate if not null
                    java.sql.Date expirationDate = rs.getDate("expirationDate");
                    if (expirationDate != null) {
                        product.setExpirationDate(expirationDate.toLocalDate());
                    }

                    java.sql.Date markdownDate = rs.getDate("markdownDate");
                    if (markdownDate != null) {
                        product.setMarkdownDate(markdownDate.toLocalDate());
                    }

                    // Set other attributes in a similar manner

                    return product;
                }
            }
        } catch (SQLException e) {
            // Handle any exceptions here
            e.printStackTrace();
            throw e; // You might want to handle this exception differently
        }

        // Return null if no product found with the given productID
        return null;
    }

    public String getDescription(int productID) throws SQLException {
        String query = "SELECT description FROM product WHERE productID = ?";
        try (Connection conn = this.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, productID);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("description");
                }
            }
        } catch (SQLException e) {
            handleSQLException(e);
        }

        return null; // No product found
    }

    public boolean updatePurchaseOrder(int poNumber, int productId, int quantity, LocalDate orderDate,
            String trackingNumber) throws SQLException {
        String query = "UPDATE purchase_orders SET productID = ?, quantity = ?, order_date = ?, tracking_number = ? WHERE po_number = ?";
        try (Connection conn = this.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, productId);
            pstmt.setInt(2, quantity);
            pstmt.setDate(3, Date.valueOf(orderDate));
            pstmt.setString(4, trackingNumber);
            pstmt.setInt(5, poNumber);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            handleSQLException(e);
        }
        return false;
    }

    public boolean deletePurchaseOrder(int poNumber) throws SQLException {
        String query = "DELETE FROM purchase_orders WHERE po_number = ?";
        try (Connection conn = this.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, poNumber);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            handleSQLException(e);
        }
        return false;
    }

    private int generateRandomPoNumber() {
        return new Random().nextInt(90000) + 10000; // Random 5-digit number
    }

    private String generateRandomTrackingNumber() {
        return String.format("%010d", new Random().nextLong(9000000000L) + 1000000000L); // Random 10-digit number
    }

    private void handleSQLException(SQLException e) throws SQLException {
        // Handle SQLException here, you can log the error or perform other actions as
        // needed
        e.printStackTrace();
        throw e;
    }

    @Override
    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}
