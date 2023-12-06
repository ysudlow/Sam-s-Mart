package databasemanager;

import java.sql.SQLException;

public enum UserRole {
    ADMIN, EMPLOYEE, MANAGER;

    public void createAdminUser() throws SQLException {
        User adminUser = new User(00000, "Admin", "User", "0000000000", "admin@example.com", "securePassword",
                UserRole.ADMIN);
        addUser(adminUser);
    }

    private void addUser(User adminUser) {
    }

}
