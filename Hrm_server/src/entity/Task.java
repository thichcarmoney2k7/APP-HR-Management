package entity;

import enums.TaskStatus;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Boolean completed = false; // false: Chưa xong, true: Đã hoàn thành

    @Column(name = "assigned_date")
    private LocalDate assignedDate;

    @Column(name = "due_date")
    private LocalDate deadline;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private TaskStatus status = TaskStatus.PENDING;

    // Liên kết: Nối Task này với Employee được giao
    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;


    public Task() {
    }

    public Task(Long id, String title, String description, Boolean completed, LocalDate assignedDate, LocalDate deadline, Employee employee) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.completed = completed;
        this.assignedDate = assignedDate;
        this.deadline = deadline;
        this.employee = employee;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }

    public LocalDate getAssignedDate() {
        return assignedDate;
    }

    public void setAssignedDate(LocalDate assignedDate) {
        this.assignedDate = assignedDate;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setdeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }
    public Long getEmployeeId() {
        return id;
    }

    public void setEmployeeId(Long employee) {
        this.id= id;
    }
}