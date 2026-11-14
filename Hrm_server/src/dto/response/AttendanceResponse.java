package dto.response;

import enums.AttendanceStatus;

import java.time.LocalDate;
import java.time.LocalTime;

public class AttendanceResponse {

    private Long attendanceId;
    private Long employeeId;
    private String employeeName;
    private Integer lateMinutes;
    private LocalDate workDate;
    private LocalTime checkInTime;  // Đổi từ String sang LocalTime
    private LocalTime checkOutTime; // Đổi từ String sang LocalTime
    private Double workHours;
    private String status;
    private String message;

    public AttendanceResponse() {
    }

    public AttendanceResponse(Long attendanceId, Long employeeId, String employeeName, LocalDate workDate,
                              LocalTime checkInTime, LocalTime checkOutTime, Integer lateMinutes,
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

    public LocalDate getWorkDate() {
        return workDate;
    }

    public void setWorkDate(LocalDate workDate) {
        this.workDate = workDate;
    }

    public LocalTime getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(LocalTime checkInTime) {
        this.checkInTime = checkInTime;
    }

    public LocalTime getCheckOutTime() {
        return checkOutTime;
    }

    public void setCheckOutTime(LocalTime checkOutTime) {
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