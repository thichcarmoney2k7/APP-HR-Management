package enums;

public enum Role {
    CANDIDATE("Ứng viên"),
    EMPLOYEE("Nhân viên"),
    MANAGER("Quản lý"),
    HR("Nhân sự");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}