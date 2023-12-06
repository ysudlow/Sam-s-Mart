import java.sql.SQLException;
import java.util.Scanner;

import databasemanager.DatabaseManager;

public class InventoryService {

    private DatabaseManager dbManager;
    private Scanner scanner;

    public InventoryService(DatabaseManager dbManager, Scanner scanner) {
        this.dbManager = dbManager; // dbManager is initialized here
        this.scanner = scanner;
    }

    public void displayInventoryMenu() {
        while (true) {
            System.out.println("\nInventory Management:");
            System.out.println("1. View Products");
            System.out.println("2. Update Product Quantity");
            System.out.println("3. Add New Product");
            System.out.println("4. Remove Product");
            System.out.println("5. Return to Main Menu");

            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1:
                    viewProducts();
                    break;
                case 2:
                    updateProductQuantity();
                    break;
                case 3:
                    addNewProduct();
                    break;
                case 4:
                    removeProduct();
                    break;
                case 5:
                    System.out.println("Returning to Main Menu...");
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private void viewProducts() {
    }

    private void updateProductQuantity() {
        // Implementation to update product quantity
        // Example:
        System.out.println("Enter Product ID:");
        int id = scanner.nextInt();
        System.out.println("Enter new quantity:");
        int quantity = scanner.nextInt();

        try {
            boolean success = dbManager.updateProductQuantity(id, quantity);
            if (success) {
                System.out.println("Product quantity updated successfully.");
            } else {
                System.out.println("Product update failed. Please check the Product ID.");
            }
        } catch (SQLException e) {
            System.out.println("Error updating product: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void addNewProduct() {
        // Implementation to add a new product
    }

    private void removeProduct() {
        // Implementation to remove a product
    }

    // Other methods as needed...
}
