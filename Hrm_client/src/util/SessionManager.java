package util;

public class SessionManager {

    private static Long userId;
    private static Long employeeId;
    private static String username;
    private static String fullName;
    private static String role;

    // Lưu thông tin ngay khi đăng nhập thành công
    public static void setSession(Long userId, Long employeeId, String username, String fullName, String role) {
        SessionManager.userId = userId;
        SessionManager.employeeId = employeeId;
        SessionManager.username = username;
        SessionManager.fullName = fullName;
        SessionManager.role = role;
    }

    // Xóa thông tin khi bấm Đăng xuất
    public static void clearSession() {
        userId = null;
        employeeId = null;
        username = null;
        fullName = null;
        role = null;
    }

    public static boolean isLoggedIn() {
        return userId != null;
    }

    public static boolean isAdminOrManager() {
        return "ADMIN".equalsIgnoreCase(role) || "MANAGER".equalsIgnoreCase(role);
    }

    public static Long getUserId() {
        return userId;
    }

    public static Long getEmployeeId() {
        return employeeId;
    }

    public static void setEmployeeId(Long employeeId) {
        SessionManager.employeeId = employeeId;
    }

    public static String getUsername() {
        return username;
    }

    public static String getFullName() {
        return fullName;
    }

    public static String getRole() {
        return role;
    }

    public static void clear() {
        clearSession();
    }
}

