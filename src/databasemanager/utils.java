package databasemanager;

public class utils {
    public static boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber.matches("\\d{10}");
    }

    public static boolean isValidEmail(String email) {
        return email.contains("@");
    }
}
