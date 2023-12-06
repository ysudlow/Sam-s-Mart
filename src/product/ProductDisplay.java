package product;

import java.time.LocalDate;

public class ProductDisplay {
    private String manufacturer;
    private String brand;
    private String category;

    // Constructor without productID, since it will be generated by the database
    public ProductDisplay(String productName, LocalDate expirationDate, LocalDate markdownDate, int quantity,
            String manufacturer, String brand, String category, double price) {
        this.manufacturer = manufacturer;
        this.brand = brand;
        this.category = category;
    }

    // Additional getters and setters for manufacturer, brand, and category

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    // Method to display product details
    public void displayProduct() {
        // Define the format for the table header
        String headerFormat = "%-10s %-30s %-10s %-10s %-15s %-15s %-15s %-10s %n";
        // Print table header
        System.out.printf(headerFormat, "Product ID", "Product Name", "Expiration Date", "Markdown Date", "Quantity",
                "Manufacturer", "Brand", "Category", "Price");

        // Define the format for the product data
        String dataFormat = "%-10d %-30s %-10s %-10s %-15d %-15s %-15s %-10s $%-9.2f %n";
        // Print product details
        System.out.printf(dataFormat,
                getProductID(),
                getProductName(),
                getExpirationDate().toString(),
                getMarkdownDate().toString(),
                getQuantity(),
                getManufacturer(),
                getBrand(),
                getCategory(),
                getPrice());
    }

    private Object getPrice() {
        return null;
    }

    private Object getQuantity() {
        return null;
    }

    private Object getMarkdownDate() {
        return null;
    }

    private Object getExpirationDate() {
        return null;
    }

    private Object getProductName() {
        return null;
    }

    private Object getProductID() {
        return null;
    }

    // Main method for demonstration purposes
    public static void main(String[] args) {
        // Example data for demonstration
        LocalDate expDate = LocalDate.parse("2023-12-31");
        LocalDate markdownDate = LocalDate.parse("2023-11-01");

        // Create a ProductDisplay instance for demonstration
        ProductDisplay product = new ProductDisplay("Sample Product", expDate, markdownDate, 50, "Sample Manufacturer",
                "Sample Brand", "Sample Category", 19.99);

        // Set a product ID for demonstration
        product.setProductID(101);

        // Display the product in table format
        product.displayProduct();
    }

    private void setProductID(int i) {
    }
}