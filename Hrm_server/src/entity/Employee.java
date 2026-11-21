package entity;

import jakarta.persistence.*;

@Entity
@Table(name = "employees")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @OneToOne
    @JoinColumn(name = "candidate_id", unique = true)
    private Candidate candidate;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "years_of_experience")
    private Integer yearsOfExperience;

    @Column(name = "base_salary", nullable = false)
    private Double baseSalary;

    @Column(name = "face_id", columnDefinition = "LONGTEXT")
    private String faceId;

    @Column(name = "department_id")
    private Integer departmentId;

    @Column(length = 100)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(length = 50)
    private String position;

    @ManyToOne
    @JoinColumn(name = "manager_id")
    private Employee manager;

    public Employee() {
    }

    public Employee(Long id, User user, Candidate candidate, String fullName, Integer yearsOfExperience,
                    Double baseSalary, String faceId, Integer departmentId, String email, String phone,
                    String position, Employee manager) {
        this.id = id;
        this.user = user;
        this.candidate = candidate;
        this.fullName = fullName;
        this.yearsOfExperience = yearsOfExperience;
        this.baseSalary = baseSalary;
        this.faceId = faceId;
        this.departmentId = departmentId;
        this.email = email;
        this.phone = phone;
        this.position = position;
        this.manager = manager;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Candidate getCandidate() {
        return candidate;
    }

    public void setCandidate(Candidate candidate) {
        this.candidate = candidate;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Integer getYearsOfExperience() {
        return yearsOfExperience;
    }

    public void setYearsOfExperience(Integer yearsOfExperience) {
        this.yearsOfExperience = yearsOfExperience;
    }

    public Double getBaseSalary() {
        return baseSalary;
    }

    public void setBaseSalary(Double baseSalary) {
        this.baseSalary = baseSalary;
    }

    public String getFaceId() {
        return faceId;
    }

    public void setFaceId(String faceId) {
        this.faceId = faceId;
    }

    public Integer getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Integer departmentId) {
        this.departmentId = departmentId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public Employee getManager() {
        return manager;
    }

    public void setManager(Employee manager) {
        this.manager = manager;
    }
}
