import java.sql.SQLException;
import java.util.InputMismatchException;
import java.util.Scanner;

import databasemanager.DatabaseManager;

public class ProductService {

    private DatabaseManager dbManager;
    private Scanner scanner;

    public ProductService(DatabaseManager dbManager, Scanner scanner) {
        this.dbManager = dbManager;
        this.scanner = scanner;
    }

    public void updateProductQuantity() {
        try {
            System.out.println("Enter the product ID of the item you wish to update:");
            int productId = scanner.nextInt();
            scanner.nextLine(); // Consume the newline left-over

            System.out.println("Enter the new quantity:");
            int newQuantity = scanner.nextInt();
            scanner.nextLine(); // Consume the newline left-over

            boolean success = dbManager.updateProductQuantity(productId, newQuantity);
            if (success) {
                System.out.println("Quantity updated successfully.");
            } else {
                System.out.println("Failed to update quantity. Please check the product ID.");
            }
        } catch (InputMismatchException e) {
            System.out.println("Invalid input. Please enter numeric values.");
            scanner.nextLine(); // Consume the invalid input
        } catch (SQLException e) {
            System.out.println("A database error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
