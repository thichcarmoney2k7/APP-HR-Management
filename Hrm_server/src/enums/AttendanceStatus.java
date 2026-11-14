package enums;

public enum AttendanceStatus {
    ON_TIME("Đúng giờ"),
    LATE("Đi muộn"),
    EARLY_LEAVE("Về sớm"),
    ABSENT("Vắng mặt");

    private final String displayName;

    AttendanceStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}