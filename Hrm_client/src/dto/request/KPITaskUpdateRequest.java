//Nhân viên tích hoàn thành KPI
package dto.request;

public class KPITaskUpdateRequest {

    private Long employeeId;
    private String monthYear; // Dạng "2026-11"
    private Double targetKpi;
    private Double achievedKpi;
    private String managerComments;

    public KPITaskUpdateRequest() {
    }

    public KPITaskUpdateRequest(Long employeeId, String monthYear, Double targetKpi, Double achievedKpi, String managerComments) {
        this.employeeId = employeeId;
        this.monthYear = monthYear;
        this.targetKpi = targetKpi;
        this.achievedKpi = achievedKpi;
        this.managerComments = managerComments;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getMonthYear() {
        return monthYear;
    }

    public void setMonthYear(String monthYear) {
        this.monthYear = monthYear;
    }

    public Double getTargetKpi() {
        return targetKpi;
    }

    public void setTargetKpi(Double targetKpi) {
        this.targetKpi = targetKpi;
    }

    public Double getAchievedKpi() {
        return achievedKpi;
    }

    public void setAchievedKpi(Double achievedKpi) {
        this.achievedKpi = achievedKpi;
    }

    public String getManagerComments() {
        return managerComments;
    }

    public void setManagerComments(String managerComments) {
        this.managerComments = managerComments;
    }
}