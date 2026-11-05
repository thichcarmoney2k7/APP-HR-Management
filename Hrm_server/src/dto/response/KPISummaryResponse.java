//# Tổng hợp báo cáo hiệu suất cho Admin
package dto.response;

public class KPISummaryResponse {

    private Long employeeId;
    private String employeeName;
    private String monthYear;
    private Integer totalTasks;
    private Integer completedTasks;
    private Double completionRate;
    private Integer daysWorked;
    private Integer totalLateMinutes;
    private String aiFeedback;
    private Double bonusAmount;

    public KPISummaryResponse() {
    }

    public KPISummaryResponse(Long employeeId, String employeeName, String monthYear,
                              Integer totalTasks, Integer completedTasks, Double completionRate,
                              Integer daysWorked, Integer totalLateMinutes,
                              String aiFeedback, Double bonusAmount) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.monthYear = monthYear;
        this.totalTasks = totalTasks;
        this.completedTasks = completedTasks;
        this.completionRate = completionRate;
        this.daysWorked = daysWorked;
        this.totalLateMinutes = totalLateMinutes;
        this.aiFeedback = aiFeedback;
        this.bonusAmount = bonusAmount;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getMonthYear() {
        return monthYear;
    }

    public void setMonthYear(String monthYear) {
        this.monthYear = monthYear;
    }

    public Integer getTotalTasks() {
        return totalTasks;
    }

    public void setTotalTasks(Integer totalTasks) {
        this.totalTasks = totalTasks;
    }

    public Integer getCompletedTasks() {
        return completedTasks;
    }

    public void setCompletedTasks(Integer completedTasks) {
        this.completedTasks = completedTasks;
    }

    public Double getCompletionRate() {
        return completionRate;
    }

    public void setCompletionRate(Double completionRate) {
        this.completionRate = completionRate;
    }

    public Integer getDaysWorked() {
        return daysWorked;
    }

    public void setDaysWorked(Integer daysWorked) {
        this.daysWorked = daysWorked;
    }

    public Integer getTotalLateMinutes() {
        return totalLateMinutes;
    }

    public void setTotalLateMinutes(Integer totalLateMinutes) {
        this.totalLateMinutes = totalLateMinutes;
    }

    public String getAiFeedback() {
        return aiFeedback;
    }

    public void setAiFeedback(String aiFeedback) {
        this.aiFeedback = aiFeedback;
    }

    public Double getBonusAmount() {
        return bonusAmount;
    }

    public void setBonusAmount(Double bonusAmount) {
        this.bonusAmount = bonusAmount;
    }
}