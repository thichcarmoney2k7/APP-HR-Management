package dto.request;

import java.time.LocalDate;

public class TaskAssignRequest {

    private String title;
    private String description;
    private Long employeeId;
    private LocalDate deadline;

    public TaskAssignRequest() {
    }

    public TaskAssignRequest(String title, String description, Long employeeId, LocalDate deadline) {
        this.title = title;
        this.description = description;
        this.employeeId = employeeId;
        this.deadline = deadline;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }
}