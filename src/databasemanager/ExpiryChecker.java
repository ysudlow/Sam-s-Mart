package databasemanager;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import product.java.Product;

public class ExpiryChecker {

    private static final Logger LOGGER = Logger.getLogger(ExpiryChecker.class.getName());

    private DatabaseManager dbManager;
    private NotificationService notificationService;

    // Constructor
    public ExpiryChecker(DatabaseManager dbManager, NotificationService notificationService) {
        this.dbManager = dbManager;
        this.notificationService = notificationService;
    }

    // Method to check for expired products
    public List<Product> checkForExpiredProducts() throws SQLException {
        return dbManager.getExpiredProducts();
    }

    // Method to check and notify about expired products
    public void checkAndNotifyExpiredProducts() {
        try {
            List<Product> expiredProducts = checkForExpiredProducts();
            if (expiredProducts != null && !expiredProducts.isEmpty()) {
                for (Product product : expiredProducts) {
                    notificationService.notifyExpiry(product);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL error in checkAndNotifyExpiredProducts", e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in checkAndNotifyExpiredProducts", e);
        }
    }
}
