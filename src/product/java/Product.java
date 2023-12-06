package product.java;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class Product {
    private int productID; // Set by the database
    private String productName;
    private LocalDate expirationDate;
    private LocalDate markdownDate;
    private int quantity;
    private String manufacturer;
    private String brand;
    private double price;
    private String category;
    private double total; // Set by the database
    private LocalDate dateAdded; // Set by the database

    // Constructor without productID, total, and dateAdded since they are set by the
    // database
    public Product(String productName, LocalDate expirationDate, LocalDate markdownDate, int quantity,
            String manufacturer, String brand, double price, String category) {
        this.productName = productName;
        this.expirationDate = expirationDate;
        this.markdownDate = markdownDate;
        this.quantity = quantity;
        this.manufacturer = manufacturer;
        this.brand = brand;
        this.price = price;
        this.category = category;
    }

    // Constructor for productID, productName, and expirationDate (for expired
    // items)
    public Product(int productID, String productName, LocalDate expirationDate) {
        this.productID = productID;
        this.productName = productName;
        this.expirationDate = expirationDate;
    }

    // Constructor for productID, productName, expirationDate, and markdownDate (for
    // markdown items)
    public Product(int productID, String productName, LocalDate expirationDate, LocalDate markdownDate) {
        this.productID = productID;
        this.productName = productName;
        this.expirationDate = expirationDate;
        this.markdownDate = markdownDate;
    }

    // Constructor that takes a ResultSet and extracts the product data
    public Product(ResultSet resultSet) throws SQLException {
        this.productID = resultSet.getInt("productID");
        this.productName = resultSet.getString("productName");
        this.expirationDate = resultSet.getDate("expirationDate").toLocalDate();
        this.markdownDate = resultSet.getDate("markdownDate").toLocalDate();
        this.quantity = resultSet.getInt("quantity");
        this.manufacturer = resultSet.getString("manufacturer");
        this.brand = resultSet.getString("brand");
        this.price = resultSet.getDouble("price");
        this.category = resultSet.getString("category");
        this.total = resultSet.getDouble("total");
        this.dateAdded = resultSet.getDate("date_added").toLocalDate();
    }

    // Getters and setters for all fields
    public int getProductID() {
        return productID;
    }

    public void setProductID(int productID) {
        this.productID = productID;
    }

    // ... rest of the getters and setters ...

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

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public LocalDate getExpiryDate() {
        return expirationDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expirationDate = expiryDate;
    }

    public LocalDate getMarkdownDate() {
        return markdownDate;
    }

    public void setMarkdownDate(LocalDate markdownDate) {
        this.markdownDate = markdownDate;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    public double getTotal() {
        return total;
    }

    public LocalDate getDateAdded() {
        return dateAdded;
    }

    @Override
    public String toString() {
        return "Product [productID=" + productID + ", productName=" + productName + ", expirationDate=" + expirationDate
                + ", markdownDate=" + markdownDate + ", quantity=" + quantity + ", manufacturer=" + manufacturer
                + ", brand=" + brand + ", price=" + price + ", category=" + category + ", total=" + total
                + ", dateAdded=" + dateAdded + "]";
    }
}
