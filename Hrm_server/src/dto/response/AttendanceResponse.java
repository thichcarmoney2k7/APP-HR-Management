package dto.response;

public class AttendanceResponse {

    private Long attendanceId;
    private Long employeeId;
    private String employeeName;
    private String workDate;
    private String checkInTime;
    private String checkOutTime;
    private Integer lateMinutes;
    private Double workHours;
    private String status;
    private String message;

    public AttendanceResponse() {
    }

    public AttendanceResponse(Long attendanceId, Long employeeId, String employeeName, String workDate,
                              String checkInTime, String checkOutTime, Integer lateMinutes,
                              Double workHours, String status, String message) {
        this.attendanceId = attendanceId;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.workDate = workDate;
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
        this.lateMinutes = lateMinutes;
        this.workHours = workHours;
        this.status = status;
        this.message = message;
    }

    public Long getAttendanceId() {
        return attendanceId;
    }

    public void setAttendanceId(Long attendanceId) {
        this.attendanceId = attendanceId;
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

    public String getWorkDate() {
        return workDate;
    }

    public void setWorkDate(String workDate) {
        this.workDate = workDate;
    }

    public String getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(String checkInTime) {
        this.checkInTime = checkInTime;
    }

    public String getCheckOutTime() {
        return checkOutTime;
    }

    public void setCheckOutTime(String checkOutTime) {
        this.checkOutTime = checkOutTime;
    }

    public Integer getLateMinutes() {
        return lateMinutes;
    }

    public void setLateMinutes(Integer lateMinutes) {
        this.lateMinutes = lateMinutes;
    }

    public Double getWorkHours() {
        return workHours;
    }

    public void setWorkHours(Double workHours) {
        this.workHours = workHours;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}