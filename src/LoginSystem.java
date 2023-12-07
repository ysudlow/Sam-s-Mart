import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import databasemanager.DatabaseManager;
import databasemanager.Date;
import databasemanager.PurchaseOrder;
import databasemanager.User;
import databasemanager.UserRole;
import product.java.Product;

public class LoginSystem {
    // Static variable to hold the currently logged in user
    private static User currentUser = null;

    public static void main(String[] args) throws Exception {
        // First, check the database connection before entering the main application
        // loop
        try (DatabaseManager dbManager = new DatabaseManager()) {
            System.out.println("Connected to the database successfully.");
        } catch (SQLException e) {
            System.out.println("Failed to connect to the database: " + e.getMessage());
            e.printStackTrace();
            return; // Exit the application if the database connection fails
        } catch (RuntimeException e) {
            System.out.println(
                    "An unexpected runtime error occurred while trying to connect to the database: " + e.getMessage());
            e.printStackTrace();
            return; // Exit the application for any other unexpected runtime errors
        }

        // Now we start the main loop of the application
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                try {
                    // Check if a user is logged in and display the appropriate menu
                    if (currentUser == null) {
                        displayMainMenu();
                    } else {
                        displayUserMenu(scanner);
                    }

                    int choice = scanner.nextInt();
                    scanner.nextLine(); // consume newline

                    // Handle the main menu or user menu option based on login status
                    if (currentUser == null) {
                        handleMainMenuChoice(scanner, choice);
                    } else {
                        handleUserMenuChoice(scanner, choice);
                    }
                } catch (InputMismatchException ime) {
                    System.out.println("Please enter a valid number.");
                    scanner.nextLine(); // consume the wrong input
                } catch (SQLException sqle) {
                    System.out.println("A database error has occurred: " + sqle.getMessage());
                    sqle.printStackTrace();
                } catch (Exception e) {
                    System.out.println("An unexpected error occurred: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            // Scanner will be closed by try-with-resources
        }
    }

    // Do not close the scanner as it is tied to System.in and will be closed by the
    // JVM upon application exit

    // Displays the main menu options to the user
    private static void displayMainMenu() {
        System.out.println("\nWelcome to Sam's Mart inventory system! Please select a number below.");
        System.out.println("1. Login");
        System.out.println("2. Register");
        System.out.println("3. Sign Out");
        if (currentUser != null && currentUser.getRole() == UserRole.ADMIN) {
            System.out.println("4. Change User Role");
        }
        System.out.println("5. Exit");
        System.out.print("Choose an option: ");
    }

    // Handles the main menu choices
    private static void handleMainMenuChoice(Scanner scanner, int choice) throws Exception {
        switch (choice) {
            case 1:
                loginUser(scanner);
                break;
            case 2:
                registerUser(scanner);
                break;
            case 3:
                signOut();
                break;
            case 4:
                if (currentUser != null && currentUser.getRole() == UserRole.ADMIN) {
                    changeUserRole(scanner);
                } else {
                    System.out.println("Access denied. Only admins can change user roles.");
                }
                break;
            case 5:
                System.out.println("Goodbye!");
                exitApplication(scanner); // Close the scanner and exit the application
            default:
                System.out.println("Invalid option.");
                break;
        }
    }

    // Handles the user menu choices
    // Handle Admin User Menu
    public static void handleAdminMenuChoice(Scanner scanner, int choice) throws SQLException {
        try (DatabaseManager dbManager = new DatabaseManager()) {
            switch (choice) {
                case 1: // Delete a user
                    System.out.print("Enter the email of the user to delete: ");
                    String emailToDelete = scanner.nextLine();
                    dbManager.deleteUserByEmail(emailToDelete);
                    break;
                case 2: // Assign Manager role
                    System.out.print("Enter the email of the user to assign as Manager: ");
                    String emailToPromote = scanner.nextLine();
                    dbManager.assignManagerRole(emailToPromote);
                    break;
                // ... other admin choices ...
            }
        }
    }

    // Method for admins and managers to Add/delete products
    private static void addOrDeleteProducts(Scanner scanner) throws Exception {
        // Check if the user is logged in and has the right role
        if (currentUser == null
                || (currentUser.getRole() != UserRole.ADMIN && currentUser.getRole() != UserRole.MANAGER)) {
            System.out.println("You must be an admin or a manager to modify products.");
            return;
        }

        System.out.println("Select an option:");
        System.out.println("1. Add Product");
        System.out.println("2. Delete Product");
        System.out.print("Your choice: ");

        int choice = scanner.nextInt();
        scanner.nextLine(); // consume newline

        switch (choice) {
            case 1:
                addProduct(scanner);
                break;
            case 2:
                deleteProduct(scanner);
                break;
            case 3:
                return; // This will exit the method and return to the previous menu
            default:
                System.out.println("Invalid choice. Please try again.");
                break;
        }
    }

    // Method to handle user login
    private static void loginUser(Scanner scanner) throws ClassNotFoundException {
        System.out.print("Enter your email: ");
        String email = scanner.nextLine();

        System.out.print("Enter your password (phone number): ");
        String phoneNumber = scanner.nextLine(); // Password is the phone number

        // Log the email and phone number for debugging purposes
        System.out.println("Attempting to log in with email: " + email + " and phone number: " + phoneNumber);

        try (DatabaseManager dbManager = new DatabaseManager()) {
            User user = dbManager.authenticateUser(email, phoneNumber); // Use phoneNumber as the password
            if (user != null) {
                currentUser = user;
                System.out.println("Login successful! Welcome, " + user.getFirstName() + " " + user.getLastName());
                // You may want to display the user menu or perform other actions here
            } else {
                System.out.println("Login failed: Invalid credentials.");
            }
        } catch (SQLException e) {
            // Debugging log
            System.out.println("A database error occurred during login: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            // Debugging log
            System.out.println("An unexpected error occurred during login. Please contact support.");
            e.printStackTrace();
        }
    }

    // Method to handle user registration
    private static void registerUser(Scanner scanner) {
        System.out.print("Enter your first name: ");
        String firstName = scanner.nextLine();

        System.out.print("Enter your last name: ");
        String lastName = scanner.nextLine();

        System.out.print("Enter your phone number: ");
        String phoneNumber = scanner.nextLine();

        // Check if phone number is 10 digits
        if (!isValidPhoneNumber(phoneNumber)) {
            System.out.println("Invalid phone number. Must be 10 digits.");
            return;
        }

        System.out.print("Enter your email: ");
        String email = scanner.nextLine();

        // Check if email contains an '@' symbol
        if (!isValidEmail(email)) {
            System.out.println("Invalid email. Please enter a valid email address.");
            return;
        }
        User newUser = new User(0, firstName, lastName, phoneNumber, email, phoneNumber, UserRole.EMPLOYEE);

        try (DatabaseManager dbManager = new DatabaseManager()) {
            if (!dbManager.userExists(email)) {
                dbManager.addUser(newUser);
                System.out.println("Registration successful!");
            } else {
                System.out.println("An account with this email already exists. Please log in.");
            }
        } catch (SQLException e) {
            System.out.println("A database error occurred during registration.");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("An unexpected error occurred during registration.");
            e.printStackTrace();
        }
    }

    // Helper method to validate phone number
    private static boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber.matches("\\d{10}");
    }

    // Helper method to validate email
    private static boolean isValidEmail(String email) {
        return email.contains("@");
    }

    // Method to handle user sign-out
    private static void signOut() {
        if (currentUser != null) {
            System.out.println(
                    currentUser.getFirstName() + " " + currentUser.getLastName() + " has signed out. Thank you!");
            currentUser = null; // This ensures the user is signed out
        } else {
            System.out.println("No user is currently logged in.");
        }
    }

    // Method to change the role of a user
    private static void changeUserRole(Scanner scanner) throws Exception {
        System.out.print("Enter the email of the user you want to change the role for: ");
        String email = scanner.nextLine();

        System.out.println("Choose a role:");
        System.out.println("1. ADMIN");
        System.out.println("2. MANAGER");
        System.out.println("3. EMPLOYEE");
        System.out.print("Enter your choice: ");

        UserRole newRole;
        try {
            int roleChoice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            newRole = getRoleFromChoice(roleChoice);
        } catch (InputMismatchException ime) {
            System.out.println("Please enter a valid number.");
            scanner.nextLine(); // consume the wrong input
            return;
        }

        if (newRole == null) {
            System.out.println("Invalid role choice.");
            return;
        }

        try (DatabaseManager dbManager = new DatabaseManager()) {
            if (dbManager.userExists(email)) {
                dbManager.updateUserRole(email, newRole);
                System.out.println("Role updated successfully!");
            } else {
                System.out.println("No user found with the given email.");
            }
        } catch (SQLException e) {
            System.out.println("A database error occurred while updating roles.");
            e.printStackTrace();
        }
    }

    // Helper method to convert choice to UserRole
    private static UserRole getRoleFromChoice(int roleChoice) {
        switch (roleChoice) {
            case 1:
                return UserRole.ADMIN;
            case 2:
                return UserRole.MANAGER;
            case 3:
                return UserRole.EMPLOYEE;
            default:
                return null;
        }
    }

    // Method to display the user menu after successful login
    private static void displayUserMenu(Scanner scanner) throws Exception {
        while (currentUser != null) {
            System.out.println("User Menu:");
            System.out.println("1. View Products");
            System.out.println("2. Add/Delete Products");
            System.out.println("3. Update Inventory");
            System.out.println("4. View Stores");
            System.out.println("5. View My Details");
            System.out.println("6. Check Expired Items");
            System.out.println("7. View Markdown Items");
            System.out.println("8. Sign Out");

            boolean isAdmin = (currentUser.getRole() == UserRole.ADMIN);
            boolean isManager = (currentUser.getRole() == UserRole.MANAGER);

            if (isAdmin) {
                System.out.println("9. Manage Purchase Orders");
                System.out.println("10. Role Management");
                System.out.println("11. View All Users");
            } else if (isManager) {
                System.out.println("9. Manage Purchase Orders");
            }

            System.out.println("12. Exit Application");

            System.out.print("Choose an option: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            switch (choice) {
                case 1:
                    viewProducts();
                    break;
                case 2:
                    addOrDeleteProducts(scanner);
                    break;
                case 3:
                    updateInventory(scanner);
                    break;
                case 4:
                    viewStores(scanner);
                    break;
                case 5:
                    viewMyDetails();
                    break;
                case 6:
                    checkExpiredItems();
                    break;
                case 7:
                    displayMarkdownProducts();
                    break;
                case 8:
                    signOut();
                    break;
                case 9:
                    if (isAdmin || isManager) {
                        managePurchaseOrders(scanner);
                    } else {
                        System.out.println("Access denied. Only admins and managers can manage purchase orders.");
                    }
                    break;
                case 10:
                    if (isAdmin) {
                        manageRoles(scanner);
                    } else {
                        System.out.println("Access denied. Only admins can manage roles.");
                    }
                    break;
                case 11:
                    if (isAdmin) {
                        viewAllUsers();
                    } else {
                        System.out.println("Access denied. Only admins can view all users.");
                    }
                    break;
                case 12:
                    System.out.println("Exiting application...");
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
                    break;
            }
        }
    }

    // View all users
    private static void viewAllUsers() throws SQLException {
        if (currentUser == null
                || (currentUser.getRole() != UserRole.ADMIN && currentUser.getRole() != UserRole.MANAGER)) {
            System.out.println("Access denied. This feature is only available to admins and managers.");
            return;
        }

        try (DatabaseManager dbManager = new DatabaseManager()) {
            List<User> users = dbManager.getAllUsers();
            if (users.isEmpty()) {
                System.out.println("No users found.");
            } else {
                System.out.println("Users List:");
                for (User user : users) {
                    System.out.printf("ID: %d, Name: %s %s, Email: %s, Phone: %s, Role: %s%n",
                            user.getUserId(),
                            user.getFirstName(),
                            user.getLastName(),
                            user.getEmail(),
                            user.getPhoneNumber(),
                            user.getRole());

                }
            }
        }
    }

    // View products
    {
    }

    private static void viewProducts() {
        try (DatabaseManager dbManager = new DatabaseManager()) {
            List<Product> products = dbManager.getAllProducts(); // Assuming this method exists and returns all products

            if (products == null || products.isEmpty()) {
                System.out.println("No products available.");
            } else {
                // Print table header with Product ID
                System.out.printf("%-20s %-20s %-20s %-20s %-20s %n", "Product ID", "Name", "Quantity", "Price",
                        "category");
                System.out.println(
                        "------------------------------------------------------------------------------------------------");
                for (Product product : products) {
                    // Assuming Product class has getters for the necessary fields
                    System.out.printf("%-20d %-20s %-20d %-20.2f %-20s %n",
                            product.getProductID(),
                            product.getProductName(),
                            product.getQuantity(),
                            product.getPrice(),
                            product.getCategory().toString()); // Assuming getExpiryDate() returns a LocalDate
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to retrieve products: " + e.getMessage());
            e.printStackTrace();
        }
        // The try-with-resources statement will auto close dbManager, so no need for a
        // finally block to close it.
    }

    private static void manageRoles(Scanner scanner) {
        // Check if the current user is an admin before proceeding
        if (currentUser == null || currentUser.getRole() != UserRole.ADMIN) {
            System.out.println("Access denied. Only admins can manage roles.");
            return;
        }

        System.out.println("Role Management:");
        System.out.println("1. Grant Manager Role");
        // ... any other role management options can be added here
        System.out.println("2. Return to User Menu");

        int choice = scanner.nextInt();
        scanner.nextLine(); // consume newline

        switch (choice) {
            case 1:
                grantManagerRole(scanner);
                break;
            case 2:
                // Simply break out of this switch to return to the user menu
                break;
            default:
                System.out.println("Invalid option. Please try again.");
                break;
        }
    }

    private static void managePurchaseOrders(Scanner scanner) {
        boolean continueManaging = true;

        while (continueManaging) {
            System.out.println("Managing Purchase Orders:");
            System.out.println("1. View Purchase Orders");
            System.out.println("2. Add New Purchase Order");
            System.out.println("3. Update Existing Purchase Order");
            System.out.println("4. Delete Purchase Order");
            System.out.println("5. Return to Main Menu");

            System.out.print("Choose an option: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            switch (choice) {
                case 1:
                    viewPurchaseOrders(scanner);
                    break;
                case 2:
                    addNewPurchaseOrder(scanner);
                    break;
                case 3:
                    updatePurchaseOrder(scanner);
                    break;
                case 4:
                    deletePurchaseOrder(scanner);
                    break;
                case 5:
                    continueManaging = false;
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
                    break;
            }
        }
    }

    private static void viewPurchaseOrders(Scanner scanner) {
        try (DatabaseManager dbManager = new DatabaseManager()) {
            List<PurchaseOrder> orders = dbManager.getAllPurchaseOrders();
            if (orders.isEmpty()) {
                System.out.println("No purchase orders found.");
            } else {
                System.out.println("Purchase Orders List:");
                for (PurchaseOrder order : orders) {
                    // Assuming you have getters like getPoNumber, getProductId, etc.
                    System.out.printf(
                            "PO Number: %d, Product ID: %d, Quantity: %d, Order Date: %s, Tracking Number: %s%n",
                            order.getPoNumber(),
                            order.getProductId(),
                            order.getQuantity(),
                            order.getOrderDate().toString(),
                            order.getTrackingNumber());
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving purchase orders: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void addNewPurchaseOrder(Scanner scanner) {
        // Prompt the user to enter the productID
        System.out.print("Enter productID: ");
        int productID = scanner.nextInt();

        try (DatabaseManager dbManager = new DatabaseManager()) {
            // Check if the productID exists in the Product table
            Product product = dbManager.getProductByID(productID);

            if (product != null) {
                // If the product exists, create a new PurchaseOrder object
                PurchaseOrder newPurchaseOrder = new PurchaseOrder(productID, null, null);

                // Set the productID
                newPurchaseOrder.setProductID(productID);

                // Generate random PO and Tracking numbers
                Random rand = new Random();
                int poNumber = rand.nextInt(90000) + 10000; // Random 5-digit PO number
                String trackingNumber = String.valueOf(rand.nextInt(900000000) + 1000000000); // Random 10-digit
                                                                                              // tracking number

                // Set PO and Tracking numbers
                newPurchaseOrder.setPoNumber(poNumber);
                newPurchaseOrder.setTrackingNumber(trackingNumber);

                // Set the quantity and order date as needed
                int quantity = 1; // You can change this to the desired quantity
                LocalDate orderDate = LocalDate.now(); // Set the order date to the current date

                // Add the purchase order to the database
                boolean isAdded = dbManager.addPurchaseOrder(newPurchaseOrder, quantity, orderDate);

                if (isAdded) {
                    System.out.println("Purchase order added successfully.");
                    // Display the generated PO and Tracking numbers
                    System.out.println("PO Number: " + poNumber);
                    System.out.println("Tracking Number: " + trackingNumber);
                } else {
                    System.out.println("Failed to add purchase order.");
                }
            } else {
                System.out.println("Product not found for the given productID.");
            }
        } catch (SQLException e) {
            System.err.println("Error adding purchase order: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Rest of your methods...

    private static void updatePurchaseOrder(Scanner scanner) {
        System.out.print("Enter order ID to update: ");
        int orderId = scanner.nextInt();
        scanner.nextLine(); // consume newline

        System.out.print("Enter new Product ID (or -1 to skip): ");
        int productId = scanner.nextInt();
        scanner.nextLine(); // consume newline

        System.out.print("Enter new Quantity (or -1 to skip): ");
        int quantity = scanner.nextInt();
        scanner.nextLine(); // consume newline

        System.out.print("Enter new Order Date (YYYY-MM-DD) or press Enter to skip: ");
        String dateInput = scanner.nextLine();
        LocalDate orderDate = dateInput.isEmpty() ? null : LocalDate.parse(dateInput);

        System.out.print("Enter new Tracking Number (or press Enter to skip): ");
        String trackingNumber = scanner.nextLine();

        try (DatabaseManager dbManager = new DatabaseManager()) {
            boolean isUpdated = dbManager.updatePurchaseOrder(orderId, productId, quantity, orderDate, trackingNumber);
            if (isUpdated) {
                System.out.println("Purchase order updated successfully.");
            } else {
                System.out.println("Failed to update purchase order.");
            }
        } catch (SQLException e) {
            System.err.println("Error updating purchase order: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void deletePurchaseOrder(Scanner scanner) {
        System.out.print("Enter order ID to delete: ");
        int orderId = scanner.nextInt();
        scanner.nextLine(); // consume newline

        try (DatabaseManager dbManager = new DatabaseManager()) {
            boolean isDeleted = dbManager.deletePurchaseOrder(orderId);
            if (isDeleted) {
                System.out.println("Purchase order deleted successfully.");
            } else {
                System.out.println("Failed to delete purchase order.");
            }
        } catch (SQLException e) {
            System.err.println("Error deleting purchase order: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void grantManagerRole(Scanner scanner) {
        // Check if the current user is an admin before proceeding
        if (currentUser == null || currentUser.getRole() != UserRole.ADMIN) {
            System.out.println("Access denied. Only admins can grant roles.");
            return;
        }

        System.out.print("Enter the email of the user to grant Manager role to: ");
        String email = scanner.nextLine();
        // Assume dbManager is your database manager instance or however you interact
        // with your database
        try (DatabaseManager dbManager = new DatabaseManager()) {
            User user = dbManager.getUserByEmail(email);
            if (user != null && !user.getRole().equals(UserRole.MANAGER)) {
                dbManager.updateUserRole(email, UserRole.MANAGER); // Corrected line
                System.out.println("Manager role granted to user with email: " + email);
            } else {
                System.out.println("User not found or already a manager.");
            }
        } catch (SQLException e) {
            System.out.println("A database error occurred. Please try again later.");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    private static void viewStores(Scanner scanner) {
        System.out.println("View Stores:");
        System.out.println("1. View All Stores");
        System.out.println("2. Edit Stores");

        System.out.print("Choose an option: ");
        int choice = scanner.nextInt();
        scanner.nextLine(); // consume newline

        switch (choice) {
            case 1:
                viewAllStores(scanner);
                break;
            case 2:
                if (currentUser.getRole() == UserRole.ADMIN) {
                    editStores(scanner);
                } else {
                    System.out.println("Access denied. Only admins can edit stores.");
                }
                break;
            default:
                System.out.println("Invalid option. Please try again.");
                break;
        }
    }

    private static void viewAllStores(Scanner scanner) {
        // DatabaseManager class is used to interact with the database
        try (DatabaseManager dbManager = new DatabaseManager()) {
            dbManager.viewAllStores();
        } catch (Exception e) {
            System.err.println("Error viewing stores: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void editStores(Scanner scanner) {
        boolean keepEditing = true;
        while (keepEditing) {
            System.out.println("Edit Stores:");
            System.out.println("1. Add New Store");
            System.out.println("2. Update Existing Store");
            System.out.println("3. Delete Store");
            System.out.println("4. Return to Store Menu");

            System.out.print("Choose an option: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            switch (choice) {
                case 1:
                    addNewStore(scanner);
                    break;
                case 2:
                    updateExistingStore(scanner);
                    break;
                case 3:
                    deleteStore(scanner);
                    break;
                case 4:
                    keepEditing = false;
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
                    break;
            }
        }
    }

    private static void addNewStore(Scanner scanner) {
        System.out.print("Enter store name: ");
        String storeName = scanner.nextLine();

        System.out.print("Enter address: ");
        String address = scanner.nextLine();

        System.out.print("Enter city: ");
        String city = scanner.nextLine();

        System.out.print("Enter state: ");
        String state = scanner.nextLine();

        System.out.print("Enter zip code: ");
        int zip = scanner.nextInt();
        scanner.nextLine(); // consume newline

        System.out.print("Enter phone number: ");
        String phone = scanner.nextLine();

        System.out.print("Enter store type (retail/warehouse): ");
        String storeType = scanner.nextLine();

        LocalDate openingDate = LocalDate.now(); // Auto-generated current date
        int storeId = new Random().nextInt(90000) + 10000; // Random 5-digit ID

        try (DatabaseManager dbManager = new DatabaseManager()) {
            boolean isAdded = dbManager.addStore(storeId, storeName, address, city, state, zip, phone, storeType,
                    openingDate);

            if (isAdded) {
                System.out.println("Store added successfully.");
            } else {
                System.out.println("Failed to add the store.");
            }
        } catch (Exception e) {
            System.err.println("Error adding store: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void updateExistingStore(Scanner scanner) {
        System.out.print("Enter store ID to update: ");
        int storeId = scanner.nextInt();
        scanner.nextLine(); // consume newline

        System.out.print("Enter new store name or press Enter to skip: ");
        String storeName = scanner.nextLine();

        System.out.print("Enter new address or press Enter to skip: ");
        String address = scanner.nextLine();

        // Assuming the updateStore method also requires city, state, zip, phone, and
        // storeType
        System.out.print("Enter new city or press Enter to skip: ");
        String city = scanner.nextLine();

        System.out.print("Enter new state or press Enter to skip: ");
        String state = scanner.nextLine();

        System.out.print("Enter new zip code or press Enter to skip: ");
        int zip = scanner.nextInt();
        scanner.nextLine(); // consume newline

        System.out.print("Enter new phone number or press Enter to skip: ");
        String phone = scanner.nextLine();

        System.out.print("Enter new store type (retail/warehouse) or press Enter to skip: ");
        String storeType = scanner.nextLine();

        try (DatabaseManager dbManager = new DatabaseManager()) {
            boolean isUpdated = dbManager.updateStore(storeId, storeName, address, city, state, zip, phone, storeType);
            if (isUpdated) {
                System.out.println("Store updated successfully.");
            } else {
                System.out.println("Failed to update the store.");
            }
        } catch (Exception e) {
            System.err.println("Error updating store: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void deleteStore(Scanner scanner) {
        System.out.print("Enter store ID to delete: ");
        int storeId = scanner.nextInt();
        scanner.nextLine(); // consume newline

        try (DatabaseManager dbManager = new DatabaseManager()) {
            boolean isDeleted = dbManager.deleteStore(storeId);
            if (isDeleted) {
                System.out.println("Store deleted successfully.");
            } else {
                System.out.println("Failed to delete the store.");
            }
        } catch (Exception e) {
            System.err.println("Error deleting store: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void viewMyDetails() {
        if (currentUser == null) {
            System.out.println("No user is currently logged in.");
            return;
        }

        // ANSI escape code for blue text
        String ANSI_BLUE = "\u001B[34m";
        // ANSI escape code to reset text color
        String ANSI_RESET = "\u001B[0m";

        System.out.println(ANSI_BLUE + "User Details:" + ANSI_RESET);
        System.out.println(ANSI_BLUE + "First Name: " + currentUser.getFirstName() + ANSI_RESET);
        System.out.println(ANSI_BLUE + "Last Name: " + currentUser.getLastName() + ANSI_RESET);
        System.out.println(ANSI_BLUE + "Email: " + currentUser.getEmail() + ANSI_RESET);
        System.out.println(ANSI_BLUE + "Role: " + currentUser.getRole() + ANSI_RESET);
        // You can add more details if needed
    }

    // Helper method to prompt for a date
    private static LocalDate promptForDate(Scanner scanner, String prompt) {
        System.out.print(prompt);
        String input = scanner.nextLine();
        if (!input.isEmpty()) {
            try {
                return LocalDate.parse(input);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Please use YYYY-MM-DD.");
            }
        }
        return null;
    }

    // Helper method to prompt for an integer
    private static int promptForInt(Scanner scanner, String errorMessage) {
        while (true) {
            try {
                return scanner.nextInt();
            } catch (InputMismatchException e) {
                System.out.println(errorMessage);
                scanner.next(); // Consume the invalid input
            }
        }
    }

    // Helper method to prompt for a double
    private static double promptForDouble(Scanner scanner, String errorMessage) {
        while (true) {
            try {
                return scanner.nextDouble();
            } catch (InputMismatchException e) {
                System.out.println(errorMessage);
                scanner.next(); // Consume the invalid input
            }
        }
    }

    private static void updateInventory(Scanner scanner) throws Exception {
        // Only logged-in users can update inventory
        if (currentUser == null) {
            System.out.println("Please log in to update inventory.");
            return;
        }

        System.out.println("Update Inventory:");
        System.out.println("1. Update Product Quantity");
        System.out.println("2. Add New Product");
        System.out.println("3. Return to User Menu");

        System.out.print("Choose an option: ");
        int choice = scanner.nextInt();
        scanner.nextLine(); // consume newline

        switch (choice) {
            case 1:
                updateProductQuantity(scanner);
                break;
            case 2:
                // Check if the user has admin or manager role
                if (currentUser.hasRole("admin") || currentUser.hasRole("manager")) {
                    addProduct(scanner);
                } else {
                    System.out.println("This function is for Admin and Manager users only.");
                    // Loop back to update inventory
                    updateInventory(scanner);
                }
                break;
            case 3:
                // Return to user menu
                break;
            default:
                System.out.println("Invalid option. Please try again.");
                updateInventory(scanner); // Loop back for correct input
                break;
        }
    }

    private static void updateProductQuantity(Scanner scanner) {
    }

    // Method to add products
    public static void addProduct(Scanner scanner) {
        // Check if the user is an admin or a manager
        if (!currentUser.hasRole("ADMIN") && !currentUser.hasRole("MANAGER")) {
            System.out.println("Access denied: This function is available for Admin and Manager users only.");
            return;
        }
        System.out.println("Adding a new product...");

        // Prompt user for product details
        System.out.print("Enter product name: ");
        String productName = scanner.nextLine();

        LocalDate expirationDate = promptForDate(scanner,
                "Enter expiration date (YYYY-MM-DD) or press Enter if none: ");
        LocalDate markdownDate = promptForDate(scanner, "Enter markdown date (YYYY-MM-DD) or press Enter if none: ");

        System.out.print("Enter quantity: ");
        int quantity = promptForInt(scanner, "Invalid input for quantity.");
        scanner.nextLine(); // Consume the newline after integer input

        System.out.print("Enter manufacturer: ");
        String manufacturer = scanner.nextLine();

        System.out.print("Enter brand: ");
        String brand = scanner.nextLine();

        System.out.print("Enter category: ");
        String category = scanner.nextLine();

        System.out.print("Enter price: ");
        double price = promptForDouble(scanner, "Invalid input for price.");
        scanner.nextLine(); // Consume the newline after double input

        Product product = new Product(productName, expirationDate, markdownDate, quantity, manufacturer, brand, price,
                category);

        // Insert the product into the database
        try (DatabaseManager dbManager = new DatabaseManager()) {
            try (Connection connection = dbManager.getConnection()) {
                connection.setAutoCommit(false);

                String insertSQL = "INSERT INTO product (productName, expirationDate, markdownDate, quantity, manufacturer, brand, price, category) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = connection.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, product.getProductName());
                    stmt.setDate(2, expirationDate != null ? Date.valueOf(expirationDate) : null);
                    stmt.setDate(3, markdownDate != null ? Date.valueOf(markdownDate) : null);
                    stmt.setInt(4, product.getQuantity());
                    stmt.setString(5, product.getManufacturer());
                    stmt.setString(6, product.getBrand());
                    stmt.setDouble(7, product.getPrice());
                    stmt.setString(8, product.getCategory());

                    int affectedRows = stmt.executeUpdate();
                    if (affectedRows > 0) {
                        try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                            if (generatedKeys.next()) {
                                product.setProductID(generatedKeys.getInt(1));
                                System.out.println("Product added successfully with ID: " + product.getProductID());
                            }
                        }
                    }
                    connection.commit(); // Commit the transaction
                } catch (SQLException e) {
                    connection.rollback(); // Rollback the transaction in case of an error
                    System.err.println("SQL error: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Implement the handleUserMenuChoice method to handle the user's menu selection
    private static void handleUserMenuChoice(Scanner scanner, int choice) throws Exception {
        switch (choice) {
            case 1:
                // View products
                break;
            case 2:
                // Add/Delete Products
                addOrDeleteProducts(scanner);
                break;
            case 3:
                // Update Inventory
                updateInventory(scanner);
                break;
            // Add cases for other menu options as needed
            default:
                System.out.println("Invalid option. Please try again.");
                break;
        }
    }

    // ...

    // Method to delete product
    private static void deleteProduct(Scanner scanner) throws Exception {
        System.out.print("Enter Product ID to delete: ");
        int productID = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        try (DatabaseManager dbManager = new DatabaseManager()) {
            dbManager.deleteProduct(productID);
            System.out.println("Product deleted successfully.");
        } catch (SQLException e) {
            System.out.println("A database error occurred while deleting the product.");
            e.printStackTrace();
        }
    }

    private static void checkExpiredItems() throws SQLException {
        DatabaseManager dbManager = null;
        try {
            dbManager = new DatabaseManager();
            List<Product> expiredProducts = dbManager.getExpiredProducts();

            if (expiredProducts.isEmpty()) {
                System.out.println("No expired products.");
            } else {
                System.out.println("Expired Products:");
                for (Product product : expiredProducts) {
                    System.out.printf("\u001B[31m Product ID: %d, Name: %s, Expiry Date: %s \u001B[0m%n",
                            product.getProductID(),
                            product.getProductName(),
                            product.getExpirationDate());
                }
            }
        } finally {
            if (dbManager != null) {
                try {
                    dbManager.close();
                } catch (SQLException e) {
                    System.out.println("Failed to close database connection: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    public static void displayMarkdownProducts() throws SQLException {
        DatabaseManager dbManager = null;
        try {
            dbManager = new DatabaseManager();
            List<Product> markdownProducts = dbManager.getMarkdownProducts(); // Corrected this line

            if (markdownProducts == null || markdownProducts.isEmpty()) {
                System.out.println("There are no markdown products at this time.");
            } else {
                System.out.println("\u001B[33mMarkdown Products:\u001B[0m");
                for (Product product : markdownProducts) {
                    System.out.printf(
                            "\u001B[33mProduct ID: %d, Name: %s, Expiry Date: %s, Markdown Date: %s\u001B[0m\n",
                            product.getProductID(),
                            product.getProductName(),
                            product.getExpirationDate(),
                            product.getMarkdownDate());
                }
            }
        } finally {
            if (dbManager != null) {
                try {
                    dbManager.close();
                } catch (SQLException e) {
                    System.out.println("Failed to close database connection: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    private static void exitApplication(Scanner scanner) {
        System.out.println("Exiting the application...");
        scanner.close(); // Close the scanner
        System.exit(0); // Exit the program
    }

    {
    }
}
