package dto.response;

public class AuthResponse {

    private boolean success;
    private String message;
    private Long userId;
    private String username;
    private String role;
    private Long employeeId;
    private String fullName;

    public AuthResponse() {
    }

    public AuthResponse(boolean success, String message, Long userId, String username, String role, Long employeeId, String fullName) {
        this.success = success;
        this.message = message;
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.employeeId = employeeId;
        this.fullName = fullName;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}