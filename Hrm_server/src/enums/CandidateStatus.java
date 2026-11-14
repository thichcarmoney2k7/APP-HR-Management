package enums;

public enum CandidateStatus {
    PENDING("Chờ AI duyệt"),
    APPROVED("Đạt vòng CV"),
    REJECTED("Không đạt"),
    HIRED("Đã tuyển dụng");

    private final String displayName;

    CandidateStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}