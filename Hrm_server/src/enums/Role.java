package enums;

public enum Role {
    CANDIDATE("Ứng viên"),
    EMPLOYEE("Nhân viên"),
    MANAGER("Quản lý");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}