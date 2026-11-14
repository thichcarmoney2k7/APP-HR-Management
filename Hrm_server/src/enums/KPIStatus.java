package enums;

public enum KPIStatus {
    PENDING("Chưa đánh giá"),
    EVALUATED("AI đã chấm - Chờ duyệt"),
    CONFIRMED("Đã chốt thưởng");

    private final String displayName;

    KPIStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
