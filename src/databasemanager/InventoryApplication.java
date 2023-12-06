package databasemanager;

public class InventoryApplication {
    public static void main(String[] args) {
        try {
            // Initialize the DatabaseManager and NotificationService
            DatabaseManager dbManager = new DatabaseManager();
            NotificationService notificationService = new NotificationService(dbManager);

            // Create ExpiryChecker with the dbManager and notificationService
            ExpiryChecker expiryChecker = new ExpiryChecker(dbManager, notificationService);

            // Perform the expiry check
            expiryChecker.checkAndNotifyExpiredProducts();
        } catch (Exception e) {
            // Handle other exceptions
            System.err.println("An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
}