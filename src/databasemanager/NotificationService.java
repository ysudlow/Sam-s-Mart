package databasemanager;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import product.java.Product;

public class NotificationService {

    private DatabaseManager dbManager;
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_RESET = "\u001B[0m";

    public NotificationService(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public void sendNotification(Product product) {
        // Here we're using the ANSI_RED code to turn the text red, and ANSI_RESET to
        // reset the styling
        System.out.println(ANSI_RED + "WARNING: Product expired: " + product.getProductName() + ", Quantity: "
                + product.getQuantity() + ANSI_RESET);
    }

    public void checkAndNotifyExpiredProducts() {
        LocalDate today = LocalDate.now();
        try {
            List<Product> products = dbManager.getAllProducts();
            Map<String, List<Product>> expiredProductsByCategory = new TreeMap<>();

            for (Product product : products) {
                if (product.getExpirationDate() != null && product.getExpirationDate().isBefore(today)) {
                    expiredProductsByCategory
                            .computeIfAbsent(product.getCategory(), k -> new ArrayList<>())
                            .add(product);
                }
            }

            // Now, let's notify the users about expired products by category
            for (Map.Entry<String, List<Product>> entry : expiredProductsByCategory.entrySet()) {
                System.out.println("Category: " + entry.getKey() + " has expired products:");
                for (Product expiredProduct : entry.getValue()) {
                    sendNotification(expiredProduct);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Object notifyExpiry(Product product) {
        return null;
    }
}
